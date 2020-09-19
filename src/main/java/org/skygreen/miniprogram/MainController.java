package org.skygreen.miniprogram;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;

@Controller
@RequestMapping(path = "/miniprogram")
@Component
public class MainController {

  public static final Logger log = LoggerFactory.getLogger(MainController.class);

  @Autowired // This means to get the bean called userRepository
  // Which is auto-generated by Spring, we will use it to handle the data
  private UserRepository userRepository;

  @Autowired
  private MiniprogramProperties miniprogramProperties;
  private String accessToken = "";

  private static final int TIME_RATE = 3600000;

  private Map<String, LoginCode> sessionMap = new HashMap<>();

  @GetMapping(path = "/getid")
  public @ResponseBody Integer getUserId(@RequestParam(value = "session") String session) {
    var id = sessionToUserId(session);
    return id;
  }

  private String sessionToOpenid(String session) {
    var loginCode = sessionMap.get(session);
    if (loginCode != null) {
      return loginCode.getOpenid();
    } else {
      return null;
    }
  }

  private Integer sessionToUserId(String session) {
    var openid = sessionToOpenid(session);
    User user = (openid != null) ? userRepository.findUserByOpenid(openid) : null;
    var id = (user != null) ? user.getId() : -1;
    return id;
  }

  @GetMapping(path = "/check")
  public @ResponseBody ResponseEntity<User> checkUser(@RequestParam(value = "id") Integer id) {
    var user = userRepository.findUserById(id);
    if (user == null) {
      throw new ForbiddenException();
    }
    return new ResponseEntity<>(user, HttpStatus.OK);
  }

  @PostMapping(path = "/update")
  public @ResponseBody ResponseEntity<User> updateUser(@RequestParam String session,
      @RequestParam Integer id, WebRequest webRequest) {
    if (!isUserIdOwnedBySession(session, id)) {
      // User has no permission over the given id
      throw new ForbiddenException();
    }

    User user = userRepository.findUserById(id);
    if (user == null) {
      // User with the given id doesn't exist
      throw new ForbiddenException();
    }
    setUserValues(user, webRequest);
    userRepository.save(user);
    return new ResponseEntity<>(user, HttpStatus.OK);
  }

  private boolean isUserIdOwnedBySession(String session, Integer id) {
    return sessionToUserId(session).equals(id);
  }

  @PostMapping(path = "/add")
  public @ResponseBody ResponseEntity<User> addUser(@RequestParam String session,
      WebRequest webRequest) {
    var openid = sessionToOpenid(session);
    if (openid == null || userRepository.existsUserByOpenid(openid)) {
      // Invalid session or user already exists
      throw new ForbiddenException();
    }

    User user = new User();
    user.setOpenid(openid);
    setUserValues(user, webRequest);
    userRepository.save(user);
    return new ResponseEntity<>(user, HttpStatus.OK);
  }

  private static void setUserValues(User user, WebRequest webRequest) {
    String name = webRequest.getParameter("name");
    if (name != null) {
      user.setName(name);
    }
    String title = webRequest.getParameter("title");
    if (title != null) {
      user.setTitle(title);
    }
    String organization = webRequest.getParameter("organization");
    if (organization != null) {
      user.setOrganization(organization);
    }
    String address = webRequest.getParameter("address");
    if (address != null) {
      user.setAddress(address);
    }
    String postcode = webRequest.getParameter("postcode");
    if (postcode != null) {
      user.setPostcode(postcode);
    }
    String telephone = webRequest.getParameter("telephone");
    if (telephone != null) {
      user.setTelephone(telephone);
    }
    String mobile = webRequest.getParameter("mobile");
    if (mobile != null) {
      user.setMobile(mobile);
    }
    String fax = webRequest.getParameter("fax");
    if (fax != null) {
      user.setFax(fax);
    }
    String email = webRequest.getParameter("email");
    if (email != null) {
      user.setEmail(email);
    }
    String website = webRequest.getParameter("website");
    if (website != null) {
      user.setWebsite(website);
    }
  }

  @GetMapping(path = "/login")
  public @ResponseBody ResponseEntity<String> loginUser(
      @RequestParam(value = "js_code") String jsCode, RestTemplate restTemplate) throws Exception {
    var url = "https://api.weixin.qq.com/sns/jscode2session?grant_type=authorization_code&appid="
        + miniprogramProperties.getAppid() + "&secret=" + miniprogramProperties.getSecret()
        + "&js_code=" + jsCode;
    var loginCodeStr = restTemplate.getForObject(url, String.class);

    ObjectMapper mapper = new ObjectMapper();
    var loginCode = mapper.readValue(loginCodeStr, LoginCode.class);

    if (loginCode.getOpenid() == null) {
      throw new ForbiddenException();
    }

    var session = UUID.randomUUID().toString();
    sessionMap.put(session, loginCode);
    return new ResponseEntity<>(session, HttpStatus.OK);
  }

  @GetMapping(path = "/getcode", produces = MediaType.IMAGE_JPEG_VALUE)
  public @ResponseBody ResponseEntity<byte[]> getQRCode(@RequestParam(value = "id") Integer id,
      RestTemplate restTemplate) throws Exception {
    if (!userRepository.existsUserById(id)) {
      throw new ForbiddenException();
    }

    var url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + accessToken;

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    var personJsonObject = new JSONObject();
    personJsonObject.put("path", "pages/card/card");
    personJsonObject.put("scene", "id=" + id);

    var request = new HttpEntity<String>(personJsonObject.toString(), headers);

    var responseEntity = restTemplate.postForEntity(url, request, byte[].class);

    byte[] binaryImage = responseEntity.getBody();

    return new ResponseEntity<>(binaryImage, HttpStatus.OK);
  }


  @Scheduled(fixedRate = TIME_RATE)
  public void updateAccessToken() {
    var url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
        + miniprogramProperties.getAppid() + "&secret=" + miniprogramProperties.getSecret();
    accessToken = (new RestTemplate()).getForObject(url, AccessToken.class).getAccessToken();
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    var restTemplate = builder.build();
    return restTemplate;
  }

}
