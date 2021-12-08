package com.video.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.video.base.BaseInfoProperties;
import com.video.base.RabbitMQConfig;
import com.video.bo.VlogBO;
import com.video.enums.MessageEnum;
import com.video.enums.YesOrNo;
import com.video.mapper.MyLikedVlogMapper;
import com.video.mapper.VlogMapper;
import com.video.mapper.VlogMapperCustom;
import com.video.mo.MessageMO;
import com.video.pojo.MyLikedVlog;
import com.video.pojo.Vlog;
import com.video.service.FansService;
import com.video.service.MsgService;
import com.video.service.VlogService;
import com.video.utils.JsonUtils;
import com.video.utils.PagedGridResult;
import com.video.vo.IndexVlogVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class VlogServiceImpl extends BaseInfoProperties implements VlogService {
    @Autowired
    private VlogMapper vlogMapper;
    
    @Autowired
    private VlogMapperCustom vlogMapperCustom;
    
    @Autowired
    private MyLikedVlogMapper myLikedVlogMapper;
    
    @Autowired
    private FansService fansService;

    @Autowired
    private MsgService msgService;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private Sid sid;

    @Transactional
    @Override
    public void createVlog(VlogBO vlogBO) {
        String vid = sid.nextShort();
        
        Vlog vlog = new Vlog();
        BeanUtils.copyProperties(vlogBO, vlog);
        
        vlog.setId(vid);
        
        vlog.setLikeCounts(0);
        vlog.setCommentsCounts(0);
        vlog.setIsPrivate(YesOrNo.NO.type);
        
        vlog.setCreatedTime(new Date());
        vlog.setUpdatedTime(new Date());
        
        vlogMapper.insert(vlog);
    }

    @Override
    public PagedGridResult getIndexVlogList(String userId,
                                            String search,
                                            Integer page,
                                            Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(search)) {
            map.put("search", search);
        }
        
        List<IndexVlogVO> list = vlogMapperCustom.getIndexVlogList(map);
        
        for (IndexVlogVO v : list) {
            String vlogerId = v.getVlogerId();
            String vlogId = v.getVlogId();
            
            if (StringUtils.isNotBlank(userId)) {
                boolean doIFollowVloger = fansService.queryDoIFollowVloger(userId, vlogerId);
                v.setDoIFollowVloger(doIFollowVloger);
                
                v.setDoILikeThisVlog(doILikeVlog(userId, vlogId));
            }
            
            // like num
            v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        }
        
        return setterPagedGrid(list, page);
    }

    @Override
    public IndexVlogVO getVlogDetailById(String userId, String vlogId) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(vlogId)) {
            map.put("vlogId", vlogId);
        }
        
        List<IndexVlogVO> list = vlogMapperCustom.getVlogDetailById(map);
        
        if (list != null && list.size() > 0) {
            IndexVlogVO vlogVO = list.get(0);

            return setterVO(vlogVO, userId);
        }
        
        return null;
    }

    @Transactional
    @Override
    public void changeToPrivateOrPublic(String userId, String vlogId, Integer yesOrNo) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", vlogId);
        criteria.andEqualTo("vlogerId", userId);
        
        Vlog pendingVlog = new Vlog();
        pendingVlog.setIsPrivate(yesOrNo);
        
        vlogMapper.updateByExampleSelective(pendingVlog, example);
    }

    @Override
    public PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", userId);
        criteria.andEqualTo("isPrivate", yesOrNo);

        PageHelper.startPage(page, pageSize);
        List<Vlog> list = vlogMapper.selectByExample(example);
        
        return setterPagedGrid(list, page);
    }

    @Transactional
    @Override
    public void userLikeVlog(String userId, String vlogId, String vlogerId) {
        String rid = sid.nextShort();

        MyLikedVlog likedVlog = new MyLikedVlog();
        likedVlog.setId(rid);
        likedVlog.setVlogId(vlogId);
        likedVlog.setUserId(userId);
        
        myLikedVlogMapper.insert(likedVlog);

        // after pressing like, vlog and vloger will add up 1 like
        redis.increment(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
        redis.increment(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);

        // after user like vlog, should save the like relationship in redis
        redis.set(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId, "1");
        
        // TODO can change to logic
        String countsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        log.info(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId + " -----> write counts to db");
        Integer counts = 0;
        if (StringUtils.isNotBlank(countsStr)) {
            counts = Integer.valueOf(countsStr);
            if (counts >= nacosCounts) {
                flushCounts(vlogId, counts);
            }
        }
        
        // system msg
        Vlog vlog = this.getVlog(vlogId);
        Map msgContent = new HashMap();
        msgContent.put("vlogId", vlogId);
        msgContent.put("vlogCover", vlog.getCover());
        
        // to mongo
//        msgService.createMsg(userId, 
//                vlog.getVlogerId(), 
//                MessageEnum.LIKE_VLOG.type,
//                msgContent);
        
        // mq
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(userId);
        messageMO.setToUserId(vlog.getVlogerId());
        messageMO.setMsgContent(msgContent);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + MessageEnum.LIKE_VLOG.enValue,
                JsonUtils.objectToJson(messageMO));
    }

    @Transactional
    @Override
    public void userUnLikeVlog(String userId, String vlogId, String vlogerId) {
        MyLikedVlog likedVlog = new MyLikedVlog();
        likedVlog.setVlogId(vlogId);
        likedVlog.setUserId(userId);

        myLikedVlogMapper.delete(likedVlog);

        redis.decrement(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
        redis.decrement(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);
        redis.del(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId);
    }
    
    private boolean doILikeVlog(String myId, String vlogId) {
        String doILike = redis.get(REDIS_USER_LIKE_VLOG + ":" + myId + ":" + vlogId);
        boolean isLike = false;
        
        if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
            isLike = true;
        }
        return isLike;
    }
    
    @Override
    public Integer getVlogBeLikedCounts(String vlogId) {
        String countsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);

        if (countsStr == null || StringUtils.isBlank(countsStr)) {
            countsStr = "0";
        }
        return Integer.valueOf(countsStr);
    }

    @Override
    public PagedGridResult getMyLikedVlogList(String userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        List<IndexVlogVO> list = vlogMapperCustom.getMyLikedVlogList(map);

        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult getMyFollowVlogList(String myId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);
        List<IndexVlogVO> list = vlogMapperCustom.getMyFollowVlogList(map);

        for (IndexVlogVO v : list) {
            String vlogerId = v.getVlogerId();
            String vlogId = v.getVlogId();

            if (StringUtils.isNotBlank(myId)) {
                v.setDoIFollowVloger(true);
                
                v.setDoILikeThisVlog(doILikeVlog(myId, vlogId));
            }

            // like num
            v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        }
        
        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult getMyFriendVlogList(String myId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);
        List<IndexVlogVO> list = vlogMapperCustom.getMyFriendVlogList(map);

        for (IndexVlogVO v : list) {
            String vlogerId = v.getVlogerId();
            String vlogId = v.getVlogId();

            if (StringUtils.isNotBlank(myId)) {
                v.setDoIFollowVloger(true);

                v.setDoILikeThisVlog(doILikeVlog(myId, vlogId));
            }

            // like num
            v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        }

        return setterPagedGrid(list, page);
    }

    @Override
    public Vlog getVlog(String id) {
        return vlogMapper.selectByPrimaryKey(id);
    }

    @Transactional
    @Override
    public void flushCounts(String vlogId, Integer counts) {
        Vlog vlog = new Vlog();
        vlog.setId(vlogId);
        vlog.setLikeCounts(counts);
        
        vlogMapper.updateByPrimaryKeySelective(vlog);
    }

    @Transactional
    @Override
    public void flushCommentsCounts(String vlogId, Integer counts) {
        Vlog vlog = new Vlog();
        vlog.setId(vlogId);
        vlog.setCommentsCounts(counts);

        vlogMapper.updateByPrimaryKeySelective(vlog);
    }

    private IndexVlogVO setterVO(IndexVlogVO v, String userId) {
        String vlogerId = v.getVlogerId();
        String vlogId = v.getVlogId();

        if (StringUtils.isNotBlank(userId)) {
            boolean doIFollowVloger = fansService.queryDoIFollowVloger(userId, vlogerId);
            v.setDoIFollowVloger(doIFollowVloger);

            v.setDoILikeThisVlog(doILikeVlog(userId, vlogId));
        }

        // like num
        v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        
        return v;
    }
}
