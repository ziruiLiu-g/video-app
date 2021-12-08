package com.video.base;

import com.github.pagehelper.PageInfo;
import com.video.utils.PagedGridResult;
import com.video.utils.RedisOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RefreshScope
public class BaseInfoProperties {
    @Value("${nacos.counts}")
    public Integer nacosCounts;

    @Value("${nacos.whiteList}")
    public String whiteList;
    
    @Autowired
    public RedisOperator redis;

    public static final Integer COMMON_START_PAGE = 1;
    public static final Integer COMMON_PAGE_SIZE = 10;

    public static final Integer COMMON_START_PAGE_ZERO = 0;

    public static final String MOBILE_SMSCODE = "mobile:smscode";
    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_INFO = "redis_user_info";

    // 我的关注总数
    public static final String REDIS_MY_FOLLOWS_COUNTS = "redis_my_follows_counts";
    // 我的粉丝总数
    public static final String REDIS_MY_FANS_COUNTS = "redis_my_fans_counts";

    public static final String REDIS_FANS_AND_VLOGGER_RELATIONSHIP = "redis_fans_and_vlogger_relationship";

    // 视频和发布者获赞数
    public static final String REDIS_VLOG_BE_LIKED_COUNTS = "redis_vlog_be_liked_counts";
    public static final String REDIS_VLOGER_BE_LIKED_COUNTS = "redis_vloger_be_liked_counts";

    public static final String REDIS_VLOG_COMMENT_COUNTS = "redis_vlog_comment_counts";
    public static final String REDIS_VLOG_COMMENT_LIKED_COUNTS = "redis_vlog_comment_liked_counts";
    
    public static final String REDIS_USER_LIKE_VLOG = "redis_user_like_vlog";
    public static final String REDIS_USER_LIKE_COMMENT = "redis_user_like_comment";
    
    public PagedGridResult setterPagedGrid(List<?> list,
                                           Integer page) {
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(list);
        gridResult.setPage(page);
        gridResult.setRecords(pageList.getTotal());
        gridResult.setTotal(pageList.getPages());
        return gridResult;
    }

    public boolean checkInWhiteList(String mobile) {
        String[] whiteNum = whiteList.split(",");
        Set<String> set = new HashSet<>(Arrays.asList(whiteNum));

        if (set.contains(mobile)) return true;

        return false;
    }
}
