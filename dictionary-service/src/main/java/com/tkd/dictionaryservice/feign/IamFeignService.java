package com.tkd.dictionaryservice.feign;

import com.tkd.dictionaryservice.dto.IamUserDetails;
import com.tkd.dictionaryservice.dto.UserAccount;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "iam-service", url="${feign.iam-service.url}")
public interface IamFeignService {

    @GetMapping(path = "/internal/user/details?loginId={loginId}")
    IamUserDetails getIamUserDetails(@RequestHeader("X-internal-call") String headerSecret, @PathVariable(value = "loginId") String loginId);

    @GetMapping(path = "/user/details?includeId=true")
    UserAccount getUserDetails(@RequestHeader("Cookie") String cookie);
}
