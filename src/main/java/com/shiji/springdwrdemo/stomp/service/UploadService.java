package com.shiji.springdwrdemo.stomp.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 上传文件
 *
 * @author yanpanyi
 * @date 2019/3/27
 */
public interface UploadService {

    /**
     * 上传图片
     *
     * @param multipartFile
     * @return 上传的图片路径
     * @throws Exception
     */
    String uploadImage(MultipartFile multipartFile) throws Exception;

    /**
     * 获取上传的图片
     * @return
     */
    Map<String, Object> getImages(int pageSize, int currentPage);
}
