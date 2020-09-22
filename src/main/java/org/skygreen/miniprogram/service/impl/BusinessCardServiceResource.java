package org.skygreen.miniprogram.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.skygreen.miniprogram.config.MiniprogramProperties;
import org.skygreen.miniprogram.dao.UserDao;
import org.skygreen.miniprogram.dto.LoginCodeDto;
import org.skygreen.miniprogram.entity.User;
import org.skygreen.miniprogram.exception.ForbiddenException;
import org.skygreen.miniprogram.service.IBusinessCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;

@Service
public class BusinessCardServiceResource implements IBusinessCardService {

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private UserDao userRepository;

  @Autowired
  private MiniprogramProperties miniprogramProperties;

  private Map<String, LoginCodeDto> sessionMap = new HashMap<>();

  private boolean isSessionValid(String session) {
    return sessionMap.containsKey(session);
  }

  /*
   * Precondition: session is valid
   */
  private String sessionToOpenid(String session) {
    var loginCode = sessionMap.get(session);
    return loginCode.getOpenid();
  }

  /*
   * Precondition: session is valid
   */
  private Integer sessionToUserId(String session) {
    var openid = sessionToOpenid(session);
    User user = userRepository.findUserByOpenid(openid);
    var id = (user != null) ? user.getId() : -1; // Return -1 when user is not recorded
    return id;
  }

  @Override
  public Integer getUserId(String session) {
    if (!isSessionValid(session)) {
      return null;
    }

    var id = sessionToUserId(session);
    return id;
  }

  @Override
  public User getUser(Integer id) {
    var user = userRepository.findUserById(id);
    return user;
  }

  @Override
  public User setUser(String session, Map<String, String[]> parameterMap) {
    if (!isSessionValid(session)) {
      return null;
    }

    var id = sessionToUserId(session); // Could be unavailable
    var openid = sessionToOpenid(session); // Always available

    User user = null;
    if (!id.equals(-1)) {
      user = userRepository.findUserById(id);
    } else {
      user = new User();
      user.setOpenid(openid);
    }
    configureUserValues(user, parameterMap);
    userRepository.save(user);

    return userRepository.findUserByOpenid(openid);
  }

  private void configureUserValues(User user, Map<String, String[]> parameterMap) {
    var nameArr = parameterMap.get("name");
    if (nameArr != null) {
      user.setName(nameArr[0]);
    }
    var titleArr = parameterMap.get("title");
    if (titleArr != null) {
      user.setTitle(titleArr[0]);
    }
    var organizationArr = parameterMap.get("organization");
    if (organizationArr != null) {
      user.setOrganization(organizationArr[0]);
    }
    var addressArr = parameterMap.get("address");
    if (addressArr != null) {
      user.setAddress(addressArr[0]);
    }
    var postcodeArr = parameterMap.get("postcode");
    if (postcodeArr != null) {
      user.setPostcode(postcodeArr[0]);
    }
    var telephoneArr = parameterMap.get("telephone");
    if (telephoneArr != null) {
      user.setTelephone(telephoneArr[0]);
    }
    var mobileArr = parameterMap.get("mobile");
    if (mobileArr != null) {
      user.setMobile(mobileArr[0]);
    }
    var faxArr = parameterMap.get("fax");
    if (faxArr != null) {
      user.setFax(faxArr[0]);
    }
    var emailArr = parameterMap.get("email");
    if (emailArr != null) {
      user.setEmail(emailArr[0]);
    }
    var websiteArr = parameterMap.get("website");
    if (websiteArr != null) {
      user.setWebsite(websiteArr[0]);
    }
  }

  @Override
  public String loginUser(String jsCode) throws Exception {
    var url = "https://api.weixin.qq.com/sns/jscode2session?grant_type=authorization_code&appid="
        + miniprogramProperties.getAppid() + "&secret=" + miniprogramProperties.getSecret()
        + "&js_code=" + jsCode;
    var loginCodeStr = restTemplate.getForObject(url, String.class);

    ObjectMapper mapper = new ObjectMapper();
    var loginCode = mapper.readValue(loginCodeStr, LoginCodeDto.class);

    if (loginCode.getOpenid() == null) {
      throw new ForbiddenException();
    }

    var session = UUID.randomUUID().toString();
    sessionMap.put(session, loginCode);
    return session;
  }

  @Override
  public byte[] getQRCode(Integer id) {
    if (!userRepository.existsUserById(id)) {
      throw new ForbiddenException();
    }

    var url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token="
        + miniprogramProperties.getAccessToken();

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    var personJsonObject = new JSONObject();
    personJsonObject.put("path", "pages/card/card");
    personJsonObject.put("scene", "id=" + id);

    var request = new HttpEntity<String>(personJsonObject.toString(), headers);

    var responseEntity = restTemplate.postForEntity(url, request, byte[].class);

    byte[] binaryImage = responseEntity.getBody();

    return binaryImage;
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    RestTemplate restTemplate = builder.build();
    return restTemplate;
  }

}
