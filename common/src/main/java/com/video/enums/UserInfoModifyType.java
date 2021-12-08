package com.video.enums;

import com.video.exceptions.GraceException;
import com.video.grace.result.ResponseStatusEnum;

/**
 * @Desc: 修改用户信息类型 枚举
 */
public enum UserInfoModifyType {
    NICKNAME(1, "nickname"),
    IMOOCNUM(2, "m"),
    SEX(3, "sex"),
    BIRTHDAY(4, "birthday"),
    LOCATION(5, "location"),
    DESC(6, "desc");

    public final Integer type;
    public final String value;

    UserInfoModifyType(Integer type, String value) {
        this.type = type;
        this.value = value;
    }

    public static void checkUserInfoTypeIsRight(Integer type) {
        if (type != UserInfoModifyType.NICKNAME.type &&
                type != UserInfoModifyType.IMOOCNUM.type &&
                type != UserInfoModifyType.SEX.type &&
                type != UserInfoModifyType.BIRTHDAY.type &&
                type != UserInfoModifyType.LOCATION.type &&
                type != UserInfoModifyType.DESC.type) {
            GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_ERROR);
        }
    }
}
