package com.imooc.bilibili.dao;


import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.domain.User;
import com.imooc.bilibili.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface UserDao {

    /*通过手机号查询返回用户信息*/
    User getUserByPhone(String phone);

    void addUser(User user);

    void addUserInfo(UserInfo userInfo);

    User getUserByPhoneOrEmail(@Param("phone") String phone, @Param("email") String email);

    String getRefreshTokenByUserId(Long userId);

    User getUserById(Long userId);

    UserInfo getUserInfoByUserId(Long userId);

    void updateUserInfos(UserInfo userInfo);

    Integer updateUsers(User user);

    List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList);

    List<UserInfo> pageListUserInfos(JSONObject params);

    Integer pageCountUserInfos(JSONObject params);

    List<UserInfo> batchGetUserInfoByUserIds(Set<Long> userIdList);
}
