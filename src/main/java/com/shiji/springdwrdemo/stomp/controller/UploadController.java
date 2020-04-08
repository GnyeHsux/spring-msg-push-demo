package com.shiji.springdwrdemo.stomp.controller;

import com.alibaba.fastjson.JSONObject;
import com.shiji.springdwrdemo.stomp.annotation.ChatRecord;
import com.shiji.springdwrdemo.stomp.domain.mo.ChatFile;
import com.shiji.springdwrdemo.stomp.domain.vo.ResponseVO;
import com.shiji.springdwrdemo.stomp.service.UploadService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 上传文件
 *
 * @author yanpanyi
 * @date 2019/03/27
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Resource
    private UploadService uploadService;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @return
     * @throws Exception
     */
    @PostMapping("/image")
    public ResponseVO uploadImage(@RequestParam("file") MultipartFile multipartFile) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("path", uploadService.uploadImage(multipartFile));

        return new ResponseVO(jsonObject);
    }

    @RequestMapping("/images")
    public ResponseVO getImages(@RequestParam(defaultValue = "1") int pageSize, @RequestParam(defaultValue = "10") int currentPage) {
        Map<String, Object> rstMap = uploadService.getImages(pageSize, currentPage);
        return new ResponseVO(rstMap);
    }
}
