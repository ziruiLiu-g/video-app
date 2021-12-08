package com.video.mapper;


import com.video.my.MyMapper;
import com.video.pojo.Comment;
import com.video.vo.CommentVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CommentMapperCustom extends MyMapper<Comment> {
    public List<CommentVO> getCommentList(@Param("paramMap") Map<String, Object> map);
}