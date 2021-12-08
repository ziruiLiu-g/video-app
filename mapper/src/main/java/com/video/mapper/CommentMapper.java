package com.video.mapper;


import com.video.my.MyMapper;
import com.video.pojo.Comment;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentMapper extends MyMapper<Comment> {
}