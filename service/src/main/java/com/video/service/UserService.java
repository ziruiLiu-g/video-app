package com.video.service;

import com.video.bo.UpdatedUserBO;
import com.video.pojo.Users;

public interface UserService {

    /**
     * check user existed
     * 
     * @param mobile
     * @return
     */
    public Users queryMobileIsExist(String mobile);

    /**
     * create user and return user obj
     *
     * @param mobile
     * @return
     */
    public Users createUser(String mobile);

    /**
     * query user info by user key
     *
     * @param userId
     * @return
     */
    public Users getUser(String userId);

    /**
     * upoate userInfo
     * 
     * @param updatedUserBO
     * @return
     */
    public Users updataUserInfo(UpdatedUserBO updatedUserBO);

    /**
     * upoate userInfo
     *
     * @param updatedUserBO
     * @param type
     * @return
     */
    public Users updataUserInfo(UpdatedUserBO updatedUserBO, Integer type);
}
