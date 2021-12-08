package com.video.controller;

import com.video.base.RabbitMQConfig;
import com.video.grace.result.GraceJSONResult;
import com.video.model.Stu;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Api(tags = "hello test")
@RefreshScope
public class HelloController {
    
    @Autowired
    public RabbitTemplate rabbitTemplate;
    
    @Value("${nacos.counts}")
    private Integer nacosCounts;

    @ApiOperation(value = "hello test route")
    @GetMapping("hello")
    public Object hello() {
        Stu stu = new Stu("video", 18);
        log.info(stu.toString());
        
        return GraceJSONResult.ok(stu);
    }

    @ApiOperation(value = "nacosCounts test route")
    @GetMapping("nacosCounts")
    public Object nacosCounts() {
        return GraceJSONResult.ok("nacosCounts :" + nacosCounts);
    }

    @ApiOperation(value = "mq test route")
    @GetMapping("produce")
    public Object produce() {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG, "sys.msg.send", "消息测试");

        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "mq test route")
    @GetMapping("produce2")
    public Object produce2() {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG, "sys.msg.delete", "消息测试2");

        return GraceJSONResult.ok();
    }
}
