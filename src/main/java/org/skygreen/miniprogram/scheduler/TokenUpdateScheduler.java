package org.skygreen.miniprogram.scheduler;

import org.skygreen.miniprogram.config.MiniprogramProperties;
import org.skygreen.miniprogram.data.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TokenUpdateScheduler {
  public static final Logger log = LoggerFactory.getLogger(TokenUpdateScheduler.class);

  @Autowired
  private MiniprogramProperties miniprogramProperties;
  @Autowired
  private RestTemplate restTemplate;

  private static final int TIME_RATE = 3600000;

  @Scheduled(fixedRate = TIME_RATE)
  public void updateAccessToken() {
    var url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
        + miniprogramProperties.getAppid() + "&secret=" + miniprogramProperties.getSecret();
    var accessToken = restTemplate.getForObject(url, AccessToken.class).getAccessToken();
    miniprogramProperties.setAccessToken(accessToken);
    log.info("WeChat access token updated");
  }

}
