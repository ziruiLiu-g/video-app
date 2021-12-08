package com.video.controller;

import com.video.base.BaseInfoProperties;
import com.video.bo.VlogBO;
import com.video.enums.YesOrNo;
import com.video.grace.result.GraceJSONResult;
import com.video.grace.result.ResponseStatusEnum;
import com.video.pojo.Users;
import com.video.service.FansService;
import com.video.service.UserService;
import com.video.service.VlogService;
import com.video.utils.PagedGridResult;
import com.video.vo.IndexVlogVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Api(tags = "fans controller")
@RequestMapping("fans")
public class FansController extends BaseInfoProperties {
    @Autowired
    private FansService fansService;
    
    @Autowired
    private UserService userService;

    @ApiOperation(value = "follow route")
    @PostMapping("follow")
    public GraceJSONResult follow(@RequestParam String myId,
                                  @RequestParam String vlogerId) {
        if (StringUtils.isBlank(myId) || StringUtils.isBlank(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }
        
        if (myId.equalsIgnoreCase(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }
        
        Users vloger = userService.getUser(vlogerId);
        Users myInfo = userService.getUser(myId);
        
        if (myInfo == null || vloger == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }
        
        // save
        fansService.doFollow(myId, vlogerId);
        
        // plus 1 fan to vlogger , plus one to follow count
        redis.increment(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        redis.increment(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);
        
        redis.set(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId, "1");
        
        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "cancel follow route")
    @PostMapping("cancel")
    public GraceJSONResult cancel(@RequestParam String myId,
                                  @RequestParam String vlogerId) {
       // save
        fansService.doCancel(myId, vlogerId);

        redis.decrement(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        redis.decrement(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);

        redis.del(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId);


        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "query if follow route")
    @GetMapping("queryDoIFollowVloger")
    public GraceJSONResult queryDoIFollowVloger(@RequestParam String myId,
                                  @RequestParam String vlogerId) {
        if (redis.keyIsExist(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId)) {
            return GraceJSONResult.ok(true);
        } else {
            boolean isFan = fansService.queryDoIFollowVloger(myId, vlogerId);
            if (isFan) {
                redis.set(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId, "1");
                return GraceJSONResult.ok(true);
            } else {
                return GraceJSONResult.ok(false);
            }
        }
    }

    @ApiOperation(value = "get follow list route")
    @GetMapping("queryMyFollows")
    public GraceJSONResult queryMyFollows(@RequestParam String myId,
                                          @RequestParam Integer page,
                                          @RequestParam Integer pageSize) {
        return GraceJSONResult.ok(fansService.queryMyFollows(myId, page, pageSize));
    }

    @ApiOperation(value = "get fans list route")
    @GetMapping("queryMyFans")
    public GraceJSONResult queryMyFans(@RequestParam String myId,
                                          @RequestParam Integer page,
                                          @RequestParam Integer pageSize) {
        return GraceJSONResult.ok(fansService.queryMyFans(myId, page, pageSize));
    }
}
