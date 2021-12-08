package com.video.service.impl;

import com.video.bo.UpdatedUserBO;
import com.video.enums.Sex;
import com.video.enums.UserInfoModifyType;
import com.video.enums.YesOrNo;
import com.video.exceptions.GraceException;
import com.video.grace.result.ResponseStatusEnum;
import com.video.mapper.UsersMapper;
import com.video.pojo.Users;
import com.video.service.UserService;
import com.video.utils.DateUtil;
import com.video.utils.DesensitizationUtil;
import com.video.vo.UsersVO;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private Sid sid;

    @Override
    public Users queryMobileIsExist(String mobile) {
        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("mobile", mobile);

        Users user = usersMapper.selectOneByExample(userExample);
        return user;
    }

    @Override
    @Transactional
    public Users createUser(String mobile) {
        // global unique key
        String userId = sid.nextShort();

        Users user = new Users();
        user.setId(userId);

        user.setMobile(mobile);
        user.setNickname("User：" + DesensitizationUtil.commonDisplay(mobile));
        user.setUsername("User：" + DesensitizationUtil.commonDisplay(mobile));
        user.setFace("http://8.209.98.139:9000/video-app/268219d8bc3eb135734b3927a81ea8d3fc1f4422.jpg");

        user.setBirthday(DateUtil.stringToDate("1990-01-01"));
        user.setSex(Sex.secret.type);

        user.setCountry("中国");
        user.setProvince("");
        user.setCity("");
        user.setDistrict("");
        user.setDescription("not description");
        user.setCanUsernameBeUpdated(YesOrNo.YES.type);

        user.setCreatedTime(new Date());
        user.setUpdatedTime(new Date());

        usersMapper.insert(user);

        return user;
    }

    @Override
    public Users getUser(String userId) {
        return usersMapper.selectByPrimaryKey(userId);
    }

    @Transactional
    @Override
    public Users updataUserInfo(UpdatedUserBO updatedUserBO) {
        Users pendingUser = new Users();
        BeanUtils.copyProperties(updatedUserBO, pendingUser);

        int result = usersMapper.updateByPrimaryKeySelective(pendingUser);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
        }

        return getUser(updatedUserBO.getId());
    }

    @Override
    public Users updataUserInfo(UpdatedUserBO updatedUserBO, Integer type) {
        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();
        
        if (type == UserInfoModifyType.NICKNAME.type) {
            criteria.andEqualTo("nickname", updatedUserBO.getNickname());
            
            Users user = usersMapper.selectOneByExample(example);
            if (user != null) {
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_NICKNAME_EXIST_ERROR);
            }
        }
        
        if (type == UserInfoModifyType.IMOOCNUM.type) {
            criteria.andEqualTo("username", updatedUserBO.getUsername());

            Users user = usersMapper.selectOneByExample(example);
            if (user != null) {
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_USERNAME_EXIST_ERROR);
            }
            
            Users tempUser = getUser(updatedUserBO.getId());
            if (tempUser.getCanUsernameBeUpdated() == YesOrNo.NO.type) {
                GraceException.display(ResponseStatusEnum.USER_INFO_CANT_UPDATED_USERNAME_ERROR);
            }
            
            updatedUserBO.setCanImoocNumBeUpdated(YesOrNo.NO.type);
        }
        
        return updataUserInfo(updatedUserBO);
    }


}
