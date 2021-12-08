package com.video.controller;

import com.video.base.BaseInfoProperties;
import com.video.bo.CommentBO;
import com.video.enums.YesOrNo;
import com.video.grace.result.GraceJSONResult;
import com.video.model.Stu;
import com.video.service.CommentService;
import com.video.vo.CommentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@Slf4j
@Api(tags = "Comment controller")
@RequestMapping("comment")
public class CommentController extends BaseInfoProperties {
    @Autowired
    private CommentService commentService;

    @ApiOperation(value = "create comment route")
    @PostMapping("create")
    public GraceJSONResult create(@RequestBody @Valid CommentBO commentBO) throws Exception {
        CommentVO commentVO = commentService.createComment(commentBO);
        
        return GraceJSONResult.ok(commentVO);
    }

    @ApiOperation(value = "count comment route")
    @GetMapping("counts")
    public GraceJSONResult count(@RequestParam String vlogId) {
        return GraceJSONResult.ok(commentService.commentCount(vlogId));
    }

    @ApiOperation(value = "query comment route")
    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String vlogId,
                                @RequestParam(defaultValue = "") String userId,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize) {
        return GraceJSONResult.ok(
                commentService.queryVlogComments(vlogId, userId, page, pageSize));
    }

    @ApiOperation(value = "delete comment route")
    @DeleteMapping("delete")
    public GraceJSONResult delete(@RequestParam String commentUserId,
                                @RequestParam String commentId,
                                @RequestParam String vlogId) {
        commentService.deleteComment(commentUserId, commentId, vlogId);
        
        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "like comment route")
    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String userId,
                                  @RequestParam String commentId) {
        commentService.likeOrUnlikeComment(userId, commentId, YesOrNo.YES.type);
        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "unlike comment route")
    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String userId,
                                @RequestParam String commentId) {
        commentService.likeOrUnlikeComment(userId, commentId, YesOrNo.NO.type);
        return GraceJSONResult.ok();
    }
}
