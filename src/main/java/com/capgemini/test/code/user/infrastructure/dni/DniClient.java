package com.capgemini.test.code.user.infrastructure.dni;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
 
import feign.Logger;
import feign.okhttp.OkHttpClient;
 
@FeignClient(name = "checkDniClient", url = "${external.service.url}", configuration = DniClient.FeignConfig.class)
interface DniClient {
  // Not a @Configuration: component scanning would apply these beans to every Feign client.
  class FeignConfig {
 
    @Bean(name = "customOkHttpClient")
    public OkHttpClient okHttpClient() {
      return new OkHttpClient();
    }
 
    @Bean(name = "customLogger")
    public Logger.Level feignLoggerLevel() {
      return Logger.Level.FULL;
    }
  }
 
  @PatchMapping(value = "/check-dni")
  ResponseEntity<CheckDniResponse> check(@RequestBody CheckDniRequest request);
}
 
