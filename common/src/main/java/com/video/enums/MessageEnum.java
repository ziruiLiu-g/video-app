package com.video.enums;

public enum MessageEnum {
    FOLLOW_YOU(1, "follow", "follow"),
    LIKE_VLOG(2, "videoLike", "likeVideo"),
    COMMENT_VLOG(3, "videoComment", "comment"),
    REPLY_YOU(4, "reply", "replay"),
    LIKE_COMMENT(5, "commentLike", "likeComment");

    public final Integer type;
    public final String value;
    public final String enValue;

    MessageEnum(Integer type, String value, String enValue) {
        this.type = type;
        this.value = value;
        this.enValue = enValue;
    }
}
