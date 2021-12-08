package com.video.service.impl;

import com.video.base.BaseInfoProperties;
import com.video.enums.MessageEnum;
import com.video.mo.MessageMO;
import com.video.pojo.Users;
import com.video.repository.MessageRepository;
import com.video.service.MsgService;
import com.video.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MsgServiceImpl extends BaseInfoProperties implements MsgService {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;
    
    @Override
    public void createMsg(String fromUserId, 
                          String toUserId, 
                          Integer msgType, 
                          Map msgContent) {
        Users fromUser = userService.getUser(fromUserId);
        
        MessageMO messageMO = new MessageMO();
        
        messageMO.setFromUserId(fromUserId);
        messageMO.setFromNickname(fromUser.getNickname());
        messageMO.setFromFace(fromUser.getFace());
        
        messageMO.setToUserId(toUserId);
        
        messageMO.setMsgContent(msgContent);
        messageMO.setMsgType(msgType);
        
        messageMO.setCreateTime(new Date());
        
        messageRepository.save(messageMO);
    }

    @Override
    public List<MessageMO> queryList(String toUserId, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, 
                                            pageSize, 
                                            Sort.Direction.DESC, 
                                            "createTime");
        
        List<MessageMO> list = messageRepository.findAllByToUserIdOrderByCreateTimeDesc(toUserId, pageable);
        
        for (MessageMO msg : list) {
            if (msg.getMsgType() != null && msg.getMsgType() == MessageEnum.FOLLOW_YOU.type) {
                Map map = msg.getMsgContent();
                if (map == null) {
                    map = new HashMap();
                }
                
                String relationship = 
                        redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + msg.getToUserId() + ":" + msg.getFromUserId());
                if (StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
                    map.put("isFriend", true);
                } else {
                    map.put("isFriend", false);
                }
                
                msg.setMsgContent(map);
            }
        }
        
        return list;
    }
}
