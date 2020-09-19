package org.skygreen.miniprogram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MiniprogramApplication {
  public static final Logger log = LoggerFactory.getLogger(MiniprogramApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(MiniprogramApplication.class, args);
  }

}
