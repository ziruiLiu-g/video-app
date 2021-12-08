package com.video.controller;

import com.video.MinIOConfig;
import com.video.base.BaseInfoProperties;
import com.video.bo.UpdatedUserBO;
import com.video.enums.FileTypeEnum;
import com.video.enums.UserInfoModifyType;
import com.video.grace.result.GraceJSONResult;
import com.video.grace.result.ResponseStatusEnum;
import com.video.model.Stu;
import com.video.pojo.Users;
import com.video.service.UserService;
import com.video.utils.MinIOUtils;
import com.video.utils.SMSUtils;
import com.video.vo.UsersVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@Api(tags = "User info controller")
@RequestMapping("userInfo")
public class UserController extends BaseInfoProperties {
    @Autowired
    private UserService userService;

    @Autowired
    private MinIOConfig minIOConfig;

    @ApiOperation(value = "user query route")
    @GetMapping("query")
    public GraceJSONResult query(@RequestParam String userId) {
        Users user = userService.getUser(userId);
        
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);
        
        // my followers count
        String myFollowersCountStr = redis.get(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);
        
        // my fans count
        String myFansCountStr = redis.get(REDIS_MY_FANS_COUNTS + ":" + userId);
        
        // likes
//        String mylikeVlogCountStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + userId);
        String mylikeVlogGERCountStr = redis.get(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + userId);
        
        Integer myFollowers = 0;
        Integer myFans = 0;
        Integer totalLikes = 0;
        
        if (StringUtils.isNotBlank(myFollowersCountStr)) {
            myFollowers = Integer.valueOf(myFollowersCountStr);
        }
        if (StringUtils.isNotBlank(myFansCountStr)) {
            myFans = Integer.valueOf(myFansCountStr);
        }
//        if (StringUtils.isNotBlank(mylikeVlogCountStr)) {
//            totalLikes += Integer.parseInt(mylikeVlogCountStr);
//        }
        if (StringUtils.isNotBlank(mylikeVlogGERCountStr)) {
            totalLikes += Integer.parseInt(mylikeVlogGERCountStr);
        }
        
        
        usersVO.setMyFollowsCounts(myFollowers);
        usersVO.setMyFansCounts(myFans);
        usersVO.setTotalLikeMeCounts(totalLikes);
        
        return GraceJSONResult.ok(usersVO);
    }

    @ApiOperation(value = "user modify route")
    @PostMapping("modifyUserInfo")
    public GraceJSONResult modifyUserInfo(@RequestBody UpdatedUserBO updatedUserBO, @RequestParam Integer type) throws Exception {
        UserInfoModifyType.checkUserInfoTypeIsRight(type);
        
        Users newUserInfo = userService.updataUserInfo(updatedUserBO, type);
        
        return GraceJSONResult.ok(newUserInfo);
    }

    @ApiOperation(value = "modify image route")
    @PostMapping("modifyImage")
    public GraceJSONResult modifyImage(@RequestParam String userId, @RequestParam Integer type,  MultipartFile file) throws Exception {
        
        if (type != FileTypeEnum.BGIMG.type && type != FileTypeEnum.FACE.type) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }
        
        String fileName = file.getOriginalFilename();

        MinIOUtils.uploadFile(minIOConfig.getBucketName(), fileName, file.getInputStream());

        String imgUrl = minIOConfig.getFileHost() + "/" + minIOConfig.getBucketName() + "/" + fileName;
        
        // img url to db
        UpdatedUserBO updatedUserBO = new UpdatedUserBO();
        updatedUserBO.setId(userId);
        
        if (type == FileTypeEnum.BGIMG.type) {
            updatedUserBO.setBgImg(imgUrl);
        } else {
            updatedUserBO.setFace(imgUrl);
        }
        Users users = userService.updataUserInfo(updatedUserBO);

        return GraceJSONResult.ok(users);
    }
}
