package com.video.service.impl;

import com.github.pagehelper.PageHelper;
import com.video.base.BaseInfoProperties;
import com.video.base.RabbitMQConfig;
import com.video.bo.CommentBO;
import com.video.enums.MessageEnum;
import com.video.enums.YesOrNo;
import com.video.mapper.CommentMapper;
import com.video.mapper.CommentMapperCustom;
import com.video.mapper.FansMapper;
import com.video.mapper.FansMapperCustom;
import com.video.mo.MessageMO;
import com.video.pojo.Comment;
import com.video.pojo.Fans;
import com.video.pojo.Vlog;
import com.video.service.CommentService;
import com.video.service.FansService;
import com.video.service.MsgService;
import com.video.service.VlogService;
import com.video.utils.JsonUtils;
import com.video.utils.PagedGridResult;
import com.video.vo.CommentVO;
import com.video.vo.FansVO;
import com.video.vo.VlogerVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentMapperCustom commentMapperCustom;
    
    @Autowired
    private Sid sid;
    
    @Autowired
    private MsgService msgService;
    
    @Autowired
    private VlogService vlogService;

    @Autowired
    public RabbitTemplate rabbitTemplate;
    
    @Transactional
    @Override
    public CommentVO createComment(CommentBO commentBO) {
        String commentId = sid.nextShort();

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setVlogId(commentBO.getVlogId());
        comment.setVlogerId(commentBO.getVlogerId());
        
        comment.setCommentUserId(commentBO.getCommentUserId());
        comment.setFatherCommentId(commentBO.getFatherCommentId());
        comment.setContent(commentBO.getContent());
        
        comment.setLikeCounts(0);
        comment.setCreateTime(new Date());
        
        commentMapper.insert(comment);
        
        // comment count
        redis.increment(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId(), 1);

        // return latest comment to frontend
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);
        
        // to mongo
        Vlog vlog = vlogService.getVlog(commentBO.getVlogId());
        Map msgContent = new HashMap();
        msgContent.put("vlogId", commentBO.getVlogId());
        msgContent.put("vlogCover", vlog.getCover());
        msgContent.put("commentId", commentId);
        msgContent.put("commentContent", commentBO.getContent());
        
        Integer type = MessageEnum.COMMENT_VLOG.type;
        String routeType = MessageEnum.COMMENT_VLOG.enValue;
        if (StringUtils.isNotBlank(commentBO.getFatherCommentId()) && 
                !commentBO.getFatherCommentId().equalsIgnoreCase("0")) {
            type = MessageEnum.REPLY_YOU.type;
            routeType = MessageEnum.REPLY_YOU.enValue;
        }


        // TODO can change to logic
        String countsStr = redis.get(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId());
        log.info(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId() + " -----> write vlog comment counts to db");
        Integer counts = 0;
        if (StringUtils.isNotBlank(countsStr)) {
            counts = Integer.valueOf(countsStr);
            if (counts >= nacosCounts) {
                vlogService.flushCommentsCounts(commentBO.getVlogId(), counts);
            }
        }
        
//        msgService.createMsg(commentBO.getCommentUserId(), 
//                            commentBO.getVlogerId(),
//                            type,
//                            msgContent);
        
//         use mq
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(commentBO.getCommentUserId());
        messageMO.setToUserId(commentBO.getVlogerId());
        messageMO.setMsgContent(msgContent);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + routeType,
                JsonUtils.objectToJson(messageMO));
        
        return commentVO;
    }

    @Transactional
    @Override
    public Integer commentCount(String vlogId) {
        String countStr = redis.get(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId);
        
        if (StringUtils.isBlank(countStr)) {
            countStr = "0";
        }
        
        return Integer.valueOf(countStr);
    }

    @Override
    public PagedGridResult queryVlogComments(String vlogId, String userId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);
        
        PageHelper.startPage(page, pageSize);
        
        List<CommentVO> list = commentMapperCustom.getCommentList(map);
        
        for (CommentVO cv : list) {
            String commentId = cv.getCommentId();
            
            // current video one comment likes count
            String countStr = redis.getHashValue(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId);
            Integer counts = 0;
            if (StringUtils.isNotBlank(countStr)) {
                counts = Integer.valueOf(countStr);
            }
            
            cv.setLikeCounts(counts);

            // check user had liked the comment
            String doILike = redis.hget(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId);
            if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
                cv.setIsLike(YesOrNo.YES.type);
            }
        }
        
        return setterPagedGrid(list, page);
    }

    @Transactional
    @Override
    public void deleteComment(String commentUserId, String commentId, String vlogId) {
        Comment pendingDelete = new Comment();
        pendingDelete.setId(commentId);
        pendingDelete.setCommentUserId(commentUserId);
        
        commentMapper.delete(pendingDelete);
        
        redis.decrement(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId, 1);
    }

    @Transactional
    @Override
    public void likeOrUnlikeComment(String userId, String commentId, Integer isLike) {
        if (isLike.equals(YesOrNo.YES.type)) {
            redis.incrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);
            redis.setHashValue(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId, "1");
        } else {
            redis.decrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);
            redis.hdel(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId);
        }
        
        
        
        Comment comment = getComment(commentId);
        Vlog vlog = vlogService.getVlog(comment.getVlogId());
        Map msgContent = new HashMap();
        msgContent.put("vlogId", vlog.getId());
        msgContent.put("vlogCover", vlog.getCover());
        msgContent.put("commentId", commentId);

        // to mongo
//        msgService.createMsg(userId, 
//                            comment.getCommentUserId(),
//                            MessageEnum.LIKE_COMMENT.type, 
//                            msgContent);


        // TODO can change to logic
        String countsStr = redis.hget(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId);
        log.info(REDIS_VLOG_COMMENT_LIKED_COUNTS + ":" + commentId + " -----> write comment like counts to db");
        Integer counts = 0;
        if (StringUtils.isNotBlank(countsStr)) {
            counts = Integer.valueOf(countsStr);
            if (counts >= nacosCounts) {
                flushCounts(commentId, counts);
            }
        }
        

        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(userId);
        messageMO.setToUserId(comment.getCommentUserId());
        messageMO.setMsgContent(msgContent);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + MessageEnum.LIKE_COMMENT.enValue,
                JsonUtils.objectToJson(messageMO));
    }

    @Override
    public Comment getComment(String id) {
        return commentMapper.selectByPrimaryKey(id);
    }

    @Transactional
    @Override
    public void flushCounts(String vlogId, Integer counts) {
        Comment comment = new Comment();
        comment.setId(vlogId);
        comment.setLikeCounts(counts);

        commentMapper.updateByPrimaryKeySelective(comment);
    }
}
