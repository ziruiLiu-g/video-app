package com.video.controller;

import com.video.base.BaseInfoProperties;
import com.video.grace.result.GraceJSONResult;
import com.video.mo.MessageMO;
import com.video.model.Stu;
import com.video.service.MsgService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@Api(tags = "msg controller")
@RequestMapping("msg")
public class MsgController extends BaseInfoProperties {
    
    @Autowired
    private MsgService msgService;

    @ApiOperation(value = "msg list route")
    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String userId,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE_ZERO;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        
        List<MessageMO> list = msgService.queryList(userId, page, pageSize);
        
        return GraceJSONResult.ok(list);
    }
}
