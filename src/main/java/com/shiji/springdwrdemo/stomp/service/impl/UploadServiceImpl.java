package com.shiji.springdwrdemo.stomp.service.impl;

import com.shiji.springdwrdemo.dao.ChatFileRepository;
import com.shiji.springdwrdemo.stomp.config.FileConfig;
import com.shiji.springdwrdemo.stomp.constant.DateConstant;
import com.shiji.springdwrdemo.stomp.domain.mo.ChatFile;
import com.shiji.springdwrdemo.stomp.enums.CodeEnum;
import com.shiji.springdwrdemo.stomp.exception.ErrorCodeException;
import com.shiji.springdwrdemo.stomp.service.UploadService;
import com.shiji.springdwrdemo.stomp.utils.CheckUtils;
import com.shiji.springdwrdemo.stomp.utils.DateUtils;
import com.shiji.springdwrdemo.stomp.utils.Md5Utils;
import com.shiji.springdwrdemo.stomp.utils.UUIDUtils;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnailator;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @author yanpanyi
 * @date 2019/3/27
 */
@Service
@Slf4j
public class UploadServiceImpl implements UploadService {

    @Resource
    private FileConfig fileConfig;

    @Autowired
    private ChatFileRepository chatFileRepository;
    static BufferedImage waterMarkImg = null;
    static {
        try {
            ClassPathResource image = new ClassPathResource("static/images/watermark.png");
            waterMarkImg = ImageIO.read(image.getURL());
        } catch (IOException e) {
            log.error("读取水印图片异常！！！", e);
        }
    }

    @Override
    public String uploadImage(MultipartFile multipartFile) throws Exception {
        if (multipartFile.isEmpty()) {
            throw new ErrorCodeException(CodeEnum.FAILED);
        }

        return execute(multipartFile);
    }

    @Override
    public List<ChatFile> getImages() {
        return chatFileRepository.findAll();
    }

    private String execute(MultipartFile multipartFile) throws Exception {
        String originalFilename = multipartFile.getOriginalFilename();
        if (StringUtils.isEmpty(originalFilename)) {
            throw new ErrorCodeException(CodeEnum.INVALID_PARAMETERS);
        }
        log.info("file type: {}", multipartFile.getContentType());

        String type = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        if (!CheckUtils.isImage(multipartFile.getContentType())) {
            throw new ErrorCodeException(CodeEnum.UPLOADED_FILE_IS_NOT_AN_IMAGE);
        }
        String md5 = Md5Utils.getMD5(multipartFile.getInputStream());
        Optional<ChatFile> chatFile = chatFileRepository.findOne(Example.of(ChatFile.builder().md5(md5).build()));
        if (chatFile.isPresent()) {
            return chatFile.get().getUrl();
        }

        String fileName = UUIDUtils.create() + "." + type;
        String respPath = fileConfig.getAccessAddress() + fileName;

        File file = new File(fileConfig.getDirectoryMapping() + fileConfig.getUploadPath() + fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        multipartFile.transferTo(file);

        chatFileRepository.insert(ChatFile.builder().fileName(multipartFile.getOriginalFilename()).size(multipartFile.getSize()).md5(md5).fileType(type).url(respPath).createTime(DateUtils.getDate(DateConstant.SEND_TIME_FORMAT)).build());

        //压缩图片+水印

        String parentPath = fileConfig.getDirectoryMapping() + fileConfig.getUploadPath();
        Thumbnails.of(file).scale(0.25f).watermark(Positions.BOTTOM_CENTER, waterMarkImg, 0.5f).toFile(parentPath + "25/" + fileName);
        Thumbnails.of(file).scale(0.5f).watermark(Positions.BOTTOM_CENTER, waterMarkImg, 1f).toFile(parentPath + "50/" + fileName);
        Thumbnails.of(file).scale(0.75f).watermark(Positions.BOTTOM_CENTER, waterMarkImg, 1f).toFile(parentPath + "75/" + fileName);

        return respPath;
    }
}
