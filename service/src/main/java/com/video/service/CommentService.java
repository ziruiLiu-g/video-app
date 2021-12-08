package com.video.service;

import com.video.bo.CommentBO;
import com.video.enums.YesOrNo;
import com.video.pojo.Comment;
import com.video.pojo.Vlog;
import com.video.utils.PagedGridResult;
import com.video.vo.CommentVO;

public interface CommentService {
    /**
     * publish comment
     */
    public CommentVO createComment(CommentBO commentBO);

    /**
     * vlog comments counts
     * 
     * @param vlogId
     * @return
     */
    public Integer commentCount(String vlogId);

    /**
     * query vlog comments
     *
     * @param vlogId
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult queryVlogComments(String vlogId,
                                             String userId,
                                             Integer page,
                                             Integer pageSize);

    /**
     * delete vlog comment
     *
     * @param commentUserId
     * @param commentId
     * @param vlogId
     * @return
     */
    public void deleteComment(String commentUserId,
                              String commentId,
                              String vlogId);

    /**
     * like or unlike comment
     * 
     * @param userId
     * @param commentId
     * @param isLike
     * @param commentId
     */
    public void likeOrUnlikeComment(String userId, String commentId, Integer isLike);

    /**
     * comment
     * 
     * @param id
     */
    public Comment getComment(String id);

    /**
     * counts to db
     */
    public void flushCounts(String vlogId, Integer counts);
}
