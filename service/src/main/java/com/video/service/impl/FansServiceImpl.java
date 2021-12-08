package com.video.service.impl;

import com.github.pagehelper.PageHelper;
import com.video.base.BaseInfoProperties;
import com.video.base.RabbitMQConfig;
import com.video.enums.MessageEnum;
import com.video.enums.YesOrNo;
import com.video.mapper.FansMapper;
import com.video.mapper.FansMapperCustom;
import com.video.mo.MessageMO;
import com.video.pojo.Fans;
import com.video.service.FansService;
import com.video.service.MsgService;
import com.video.utils.JsonUtils;
import com.video.utils.PagedGridResult;
import com.video.vo.FansVO;
import com.video.vo.VlogerVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FansServiceImpl extends BaseInfoProperties implements FansService {
    @Autowired
    private FansMapper fansMapper;

    @Autowired
    private FansMapperCustom fansMapperCustom;
    
    @Autowired
    private Sid sid;

    @Autowired
    private MsgService msgService;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Transactional
    @Override
    public void doFollow(String myId, String vlogerId) {
        String fid = sid.nextShort();
        
        Fans fans = new Fans();
        fans.setId(fid);
        fans.setFanId(myId);
        fans.setVlogerId(vlogerId);
        
        // check whether follow
        // if yes, friend each other
        
        Fans vloger = queryFansRelationship(vlogerId, myId);
        if (vloger != null) {
            fans.setIsFanFriendOfMine(YesOrNo.YES.type);
            vloger.setIsFanFriendOfMine(YesOrNo.YES.type);

            Example example = new Example(Fans.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("vlogerId", vloger.getVlogerId());
            criteria.andEqualTo("fanId", vloger.getFanId());
            fansMapper.updateByExampleSelective(vloger, example);
        } else {
            fans.setIsFanFriendOfMine(YesOrNo.NO.type);
        }
        
        fansMapper.insert(fans);

        // to mongo
//        msgService.createMsg(myId, vlogerId, MessageEnum.FOLLOW_YOU.type, null);
        
        // to mq
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(myId);
        messageMO.setToUserId(vlogerId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + MessageEnum.FOLLOW_YOU.enValue,
                JsonUtils.objectToJson(messageMO));
    }
    
    public Fans queryFansRelationship(String fanId, String vlogerId) {
        Example example = new Example(Fans.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", vlogerId);
        criteria.andEqualTo("fanId", fanId);
        
        List list = fansMapper.selectByExample(example);
        
        Fans fan = null;
        if (list != null && list.size() > 0) {
            fan = (Fans) list.get(0);
        }
        
        return fan;
    }

    @Transactional
    @Override
    public void doCancel(String myId, String vlogerId) {
        // check if friend each other
        Fans fan = queryFansRelationship(myId, vlogerId);
        
        if (fan != null && fan.getIsFanFriendOfMine() == YesOrNo.YES.type) {
            // erase friend
            Fans pendingFan = queryFansRelationship(vlogerId, myId);
            pendingFan.setIsFanFriendOfMine(YesOrNo.NO.type);

            Example example = new Example(Fans.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("vlogerId", pendingFan.getVlogerId());
            criteria.andEqualTo("fanId", pendingFan.getFanId());
            fansMapper.updateByExampleSelective(pendingFan, example);
        }
        
        // delete follow record
        fansMapper.delete(fan);
    }

    @Override
    public boolean queryDoIFollowVloger(String myId, String vlogerId) {
        Fans vloger = queryFansRelationship(myId, vlogerId);
        
        return vloger != null;
    }

    @Override
    public PagedGridResult queryMyFollows(String myId, 
                                          Integer page, 
                                          Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        PageHelper.startPage(page, pageSize);

        List<VlogerVO> list = fansMapperCustom.queryMyFollows(map);
        
        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult queryMyFans(String myId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        PageHelper.startPage(page, pageSize);

        List<FansVO> list = fansMapperCustom.queryMyFans(map);
        
        for (FansVO f : list) {
            String isFan = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + f.getFanId());
            if (StringUtils.isNotBlank(isFan) && isFan.equalsIgnoreCase("1")) {
                f.setFriend(true);
            }
        }

        return setterPagedGrid(list, page);
    }
}
