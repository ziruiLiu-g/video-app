package com.video.controller;

import com.video.MinIOConfig;
import com.video.grace.result.GraceJSONResult;
import com.video.model.Stu;
import com.video.utils.MinIOUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@Api(tags = "file test")
public class FileController {

//    @Autowired
//    private MinIOConfig minIOConfig;
    
//    @ApiOperation(value = "file test route")
//    @PostMapping("upload")
//    public GraceJSONResult upload(MultipartFile file) throws Exception {
//        String fileName = file.getOriginalFilename();
//        
//        MinIOUtils.uploadFile(minIOConfig.getBucketName(), fileName, file.getInputStream());
//        
//        String imgUrl = minIOConfig.getFileHost() + "/" + minIOConfig.getBucketName() + "/" + fileName;
//        
//        return GraceJSONResult.ok(imgUrl);
//    }
}
