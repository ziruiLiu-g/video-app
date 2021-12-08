package com.video.service;

import com.video.bo.VlogBO;
import com.video.mo.MessageMO;
import com.video.utils.PagedGridResult;
import com.video.vo.IndexVlogVO;

import java.util.List;
import java.util.Map;

public interface MsgService {
    /**
     * create msg
     *
     * @param fromUserId
     * @return
     */
    public void createMsg(String fromUserId,
                          String toUserId,
                          Integer msgType,
                          Map msgContent);

    /**
     * query message
     * 
     * @param toUserId
     * @param page
     * @param pageSize
     * @return
     */
    public List<MessageMO> queryList(String toUserId, Integer page, Integer pageSize);
}
