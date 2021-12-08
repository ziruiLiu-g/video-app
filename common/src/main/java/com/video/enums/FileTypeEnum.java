package com.video.enums;

/**
 * @Desc: 文件类型 枚举
 */
public enum FileTypeEnum {
    BGIMG(1, "user bg image"),
    FACE(2, "user profile");

    public final Integer type;
    public final String value;

    FileTypeEnum(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
