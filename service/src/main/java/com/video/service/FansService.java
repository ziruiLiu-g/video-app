package com.video.service;

import com.video.utils.PagedGridResult;

public interface FansService {

    /**
     * follow
     * @param myId
     * @param vlogerId
     */
    public void doFollow(String myId, String vlogerId);

    /**
     * follow
     * @param myId
     * @param vlogerId
     */
    public void doCancel(String myId, String vlogerId);

    /**
     * query follow
     * @param myId
     * @param vlogerId
     */
    public boolean queryDoIFollowVloger(String myId, String vlogerId);

    /**
     * query my follow list
     *
     * @param myId
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult queryMyFollows(String myId,
                                           Integer page,
                                           Integer pageSize);

    /**
     * query my fans list
     *
     * @param myId
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult queryMyFans(String myId,
                                          Integer page,
                                          Integer pageSize);
}
