package com.video.mapper;

import com.video.my.MyMapper;
import com.video.pojo.Users;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersMapper extends MyMapper<Users> {
}