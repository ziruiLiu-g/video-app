package com.video.mapper;

import com.video.vo.IndexVlogVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface VlogMapperCustom {
    List<IndexVlogVO> getIndexVlogList(@Param("paramMap") Map<String, Object> map);

    List<IndexVlogVO> getVlogDetailById(@Param("paramMap") Map<String, Object> map);

    List<IndexVlogVO> getMyLikedVlogList(@Param("paramMap") Map<String, Object> map);

    List<IndexVlogVO> getMyFollowVlogList(@Param("paramMap") Map<String, Object> map);

    List<IndexVlogVO> getMyFriendVlogList(@Param("paramMap") Map<String, Object> map);
}