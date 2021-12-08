package com.video.service;

import com.video.bo.UpdatedUserBO;
import com.video.bo.VlogBO;
import com.video.pojo.Users;
import com.video.pojo.Vlog;
import com.video.utils.PagedGridResult;
import com.video.vo.IndexVlogVO;

import java.util.List;

public interface VlogService {
    /**
     * create vlog
     *
     * @param vlogBO
     * @return
     */
    public void createVlog(VlogBO vlogBO);

    /**
     * search vlog
     *
     * @param search
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult getIndexVlogList(String userId,
                                            String search,
                                            Integer page,
                                            Integer pageSize);

    /**
     * search by vlog key
     *
     * @param userId
     * @param vlogId
     * @return
     */
    public IndexVlogVO getVlogDetailById(String userId, String vlogId);

    /**
     * change public vlog to private
     *
     * @param vlogId
     * @return
     */
    public void changeToPrivateOrPublic(String userId,
                                               String vlogId,
                                               Integer yesOrNo);

    /**
     * query my vlog
     *
     * @param userId
     * @param page
     * @param pageSize
     * @param yesOrNo
     * @return
     */
    public PagedGridResult queryMyVlogList(String userId,
                                Integer page,
                                Integer pageSize,
                                Integer yesOrNo);

    /**
     * press like
     *
     * @param userId
     * @param vlogId
     * @return
     */
    public void userLikeVlog(String userId,
                             String vlogId,
                             String vlogerId);

    /**
     * press like
     *
     * @param userId
     * @param vlogId
     * @return
     */
    public void userUnLikeVlog(String userId,
                             String vlogId,
                             String vlogerId);


    /**
     * get Vlog Liked Counts
     * @param vlogId
     * @return
     */
    public Integer getVlogBeLikedCounts(String vlogId);

    /**
     * query my like vlog
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult getMyLikedVlogList(String userId,
                                           Integer page,
                                           Integer pageSize);

    /**
     * query my follow vlog
     *
     * @param myId
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult getMyFollowVlogList(String myId,
                                              Integer page,
                                              Integer pageSize);

    /**
     * query my friends vlog
     *
     * @param myId
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult getMyFriendVlogList(String myId,
                                               Integer page,
                                               Integer pageSize);

    /**
     * get vlog by id
     * 
     * @param id
     * @return
     */
    public Vlog getVlog(String id);

    /**
     * counts to db
     */
    public void flushCounts(String vlogId, Integer counts);

    /**
     * counts to db
     */
    public void flushCommentsCounts(String vlogId, Integer counts);
}
