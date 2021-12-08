package com.video.controller;

import com.video.base.BaseInfoProperties;
import com.video.bo.VlogBO;
import com.video.enums.FileTypeEnum;
import com.video.enums.YesOrNo;
import com.video.grace.result.GraceJSONResult;
import com.video.grace.result.ResponseStatusEnum;
import com.video.model.Stu;
import com.video.pojo.Users;
import com.video.service.UserService;
import com.video.service.VlogService;
import com.video.utils.PagedGridResult;
import com.video.vo.IndexVlogVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@Api(tags = "vlog controller")
@RequestMapping("vlog")
public class VlogController extends BaseInfoProperties {
    @Autowired
    private VlogService vlogService;

    @Autowired
    private UserService userService;

    @ApiOperation(value = "vlog publish route")
    @PostMapping("publish")
    public GraceJSONResult publish(@RequestBody VlogBO vlogBO) {
        String vlogerId = vlogBO.getVlogerId();
        
        if (StringUtils.isBlank(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }
        
        vlogService.createVlog(vlogBO);
        
        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "vlog search route")
    @GetMapping("indexList")
    public GraceJSONResult indexList(@RequestParam(defaultValue = "") String userId,
                                     @RequestParam(defaultValue = "") String search,
                                     @RequestParam Integer page,
                                     @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = vlogService.getIndexVlogList(userId, search, page, pageSize);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @ApiOperation(value = "vlog search with id route")
    @GetMapping("detail")
    public GraceJSONResult detail(@RequestParam(defaultValue = "") String userId,
                                     @RequestParam String vlogId) {
        IndexVlogVO indexVlogVO = vlogService.getVlogDetailById(userId, vlogId);

        return GraceJSONResult.ok(indexVlogVO);
    }

    @ApiOperation(value = "change public vlog to private route")
    @PostMapping("changeToPrivate")
    public GraceJSONResult changeToPrivate(@RequestParam String userId,
                                  @RequestParam String vlogId) {
        vlogService.changeToPrivateOrPublic(userId, vlogId, YesOrNo.YES.type);
        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "change private vlog to public route")
    @PostMapping("changeToPublic")
    public GraceJSONResult changeToPublic(@RequestParam String userId,
                                           @RequestParam String vlogId) {
        vlogService.changeToPrivateOrPublic(userId, vlogId, YesOrNo.NO.type);
        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "my Public vlog route")
    @GetMapping("myPublicList")
    public GraceJSONResult myPublicList(@RequestParam String userId,
                                     @RequestParam Integer page,
                                     @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = vlogService.queryMyVlogList(userId, 
                page, 
                pageSize, 
                YesOrNo.NO.type);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @ApiOperation(value = "my Private vlog route")
    @GetMapping("myPrivateList")
    public GraceJSONResult myPrivateList(@RequestParam String userId,
                                        @RequestParam Integer page,
                                        @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = vlogService.queryMyVlogList(userId,
                page,
                pageSize,
                YesOrNo.YES.type);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @ApiOperation(value = "my like vlog route")
    @GetMapping("myLikedList")
    public GraceJSONResult myLikedList(@RequestParam String userId,
                                         @RequestParam Integer page,
                                         @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = vlogService.getMyLikedVlogList(userId,
                                                                        page,
                                                                        pageSize);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @ApiOperation(value = "my follow vlog route")
    @GetMapping("followList")
    public GraceJSONResult followList(@RequestParam String myId,
                                      @RequestParam Integer page,
                                      @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = vlogService.getMyFollowVlogList(myId,
                                                                        page,
                                                                        pageSize);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @ApiOperation(value = "my friend vlog route")
    @GetMapping("friendList")
    public GraceJSONResult friendList(@RequestParam String myId,
                                      @RequestParam Integer page,
                                      @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = vlogService.getMyFriendVlogList(myId,
                page,
                pageSize);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @ApiOperation(value = "like vlog route")
    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String userId,
                                @RequestParam String vlogerId,
                                         @RequestParam String vlogId) {
        Users user = userService.getUser(userId);
        Users vloger = userService.getUser(vlogerId);
        IndexVlogVO indexVlogVO = vlogService.getVlogDetailById(userId, vlogId);
        
        if (user == null) {
            return GraceJSONResult.ok(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        if (vloger == null) {
            return GraceJSONResult.ok(ResponseStatusEnum.USER_INFO_UPDATED_ERROR);
        }
        
        if (indexVlogVO == null) {
            return GraceJSONResult.ok(ResponseStatusEnum.FILE_NOT_EXIST_ERROR);
        }
        
        vlogService.userLikeVlog(userId, vlogId, vlogerId);

        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "unlike vlog route")
    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String userId,
                                @RequestParam String vlogerId,
                                @RequestParam String vlogId) {
        Users user = userService.getUser(userId);
        Users vloger = userService.getUser(vlogerId);
        IndexVlogVO indexVlogVO = vlogService.getVlogDetailById(userId, vlogId);

        if (user == null) {
            return GraceJSONResult.ok(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        if (vloger == null) {
            return GraceJSONResult.ok(ResponseStatusEnum.USER_INFO_UPDATED_ERROR);
        }

        if (indexVlogVO == null) {
            return GraceJSONResult.ok(ResponseStatusEnum.FILE_NOT_EXIST_ERROR);
        }

        vlogService.userUnLikeVlog(userId, vlogId, vlogerId);

        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "total likes count route")
    @PostMapping("totalLikedCounts")
    public GraceJSONResult totalLikedCounts(@RequestParam String vlogId) {
        return GraceJSONResult.ok(vlogService.getVlogBeLikedCounts(vlogId));
    }
}
