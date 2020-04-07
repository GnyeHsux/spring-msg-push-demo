package com.shiji.springdwrdemo.stomp.domain.mo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@Builder
public class ChatFile implements Serializable {

    private static final long serialVersionUID = 3732097478657750405L;

    private String fileName;

    private String fileType;

    private String md5;

    private String url;

    private Long size;

    private String createTime;
}
