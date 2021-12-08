package com.video.controller;

import com.video.base.BaseInfoProperties;
import com.video.bo.RegistLoginBO;
import com.video.grace.result.GraceJSONResult;
import com.video.grace.result.ResponseStatusEnum;
import com.video.pojo.Users;
import com.video.service.UserService;
import com.video.utils.IPUtil;
import com.video.utils.SMSUtils;
import com.video.vo.UsersVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RestController
@Slf4j
@Api(tags = "Passport Controller api")
@RequestMapping("passport")
@RefreshScope
public class PassportController extends BaseInfoProperties {
    @Autowired
    private SMSUtils smsUtils;

    @Autowired
    private UserService userService;

    @ApiOperation(value = "sms route")
    @PostMapping("getSMSCode")
    public GraceJSONResult getSMSCode(@RequestParam String mobile, HttpServletRequest request) throws Exception {
        if (StringUtils.isBlank(mobile)) {
            return GraceJSONResult.ok();
        }

        // get user ip
        String userIp = IPUtil.getRequestIp(request);
        // user ip 60s limitation
        redis.setnx60s(MOBILE_SMSCODE + ":" + userIp, userIp);

        String code = (int) ((Math.random() * 9 + 1) * 100000) + "";
        smsUtils.sendSMS(mobile, code);
        log.info(code);

        // validation code to redis
        redis.set(MOBILE_SMSCODE + ":" + mobile, code, 30 * 60);

        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "login route")
    @PostMapping("login")
    public GraceJSONResult login(@Valid @RequestBody RegistLoginBO registLoginBO, HttpServletRequest request) throws Exception {
        String mobile = registLoginBO.getMobile();
        String code = registLoginBO.getSmsCode();
        
        if (!checkInWhiteList(mobile)) {
            // check redis
            String redisCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
            if (StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(code)) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
            }
        }
        
        // check db
        Users user = userService.queryMobileIsExist(mobile);
        if (user == null) {
            // need to reigist
            user = userService.createUser(mobile);
        }
        
        // not null, continue, save session
        String uToken = UUID.randomUUID().toString();
        redis.set(REDIS_USER_TOKEN + ":" + user.getId(), uToken);
        
        // remove verifycode
        redis.del(MOBILE_SMSCODE + ":" + mobile);
        
        // return user info and token
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);
        usersVO.setUserToken(uToken);
        
        return GraceJSONResult.ok(usersVO);
    }

    @ApiOperation(value = "logout route")
    @PostMapping("logout")
    public GraceJSONResult logout(@RequestParam String userId, HttpServletRequest request) throws Exception {
        // clear user token
        redis.del(REDIS_USER_TOKEN + ":" + userId);
        
        return GraceJSONResult.ok();
    }
}
