package org.skygreen.miniprogram.controller;

import org.skygreen.miniprogram.entity.User;
import org.skygreen.miniprogram.exception.ForbiddenException;
import org.skygreen.miniprogram.service.IBusinessCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping(path = "/miniprogram")
public class MiniprogramController {

  public static final Logger log = LoggerFactory.getLogger(MiniprogramController.class);

  @Autowired
  private IBusinessCardService businessCardService;

  private <T> ResponseEntity<T> createResponseEntity(T object) {
    if (object == null) {
      throw new ForbiddenException();
    }
    return new ResponseEntity<>(object, HttpStatus.OK);
  }

  @GetMapping(path = "/getid")
  public @ResponseBody ResponseEntity<Integer> getUserId(
      @RequestParam(value = "session") String session) {
    Integer id = businessCardService.getUserId(session);
    return createResponseEntity(id);
  }

  @GetMapping(path = "/get")
  public @ResponseBody ResponseEntity<User> getUser(@RequestParam(value = "id") Integer id) {
    User user = businessCardService.getUser(id);
    return createResponseEntity(user);
  }

  @PostMapping(path = "/set")
  public @ResponseBody ResponseEntity<User> setUser(@RequestParam String session,
      WebRequest webRequest) {
    User user = businessCardService.setUser(session, webRequest.getParameterMap());
    return createResponseEntity(user);
  }

  @GetMapping(path = "/login")
  public @ResponseBody ResponseEntity<String> loginUser(
      @RequestParam(value = "js_code") String jsCode) throws Exception {
    String session = businessCardService.loginUser(jsCode);
    return createResponseEntity(session);
  }

  @GetMapping(path = "/getcode", produces = MediaType.IMAGE_JPEG_VALUE)
  public @ResponseBody ResponseEntity<byte[]> getQRCode(@RequestParam(value = "id") Integer id)
      throws Exception {
    byte[] binaryImage = businessCardService.getQRCode(id);
    return createResponseEntity(binaryImage);
  }

}
