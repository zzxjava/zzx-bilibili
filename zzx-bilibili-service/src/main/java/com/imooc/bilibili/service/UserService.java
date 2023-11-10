package com.imooc.bilibili.service;

import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.dao.UserDao;
import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.domain.PageResult;
import com.imooc.bilibili.domain.User;
import com.imooc.bilibili.domain.UserInfo;
import com.imooc.bilibili.domain.constant.UserConstant;
import com.imooc.bilibili.domain.exception.ConditionException;
import com.imooc.bilibili.service.util.MD5Util;
import com.imooc.bilibili.service.util.RSAUtil;
import com.imooc.bilibili.service.util.TokenUtil;
import com.mysql.cj.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    @Autowired
    private UserDao userDao;


    public void addUser(User user) {
        /*******************************判断用户的输入是否合理***************************************/
        String phone = user.getPhone();
        if (StringUtils.isNullOrEmpty(phone)) {
            throw new ConditionException("手机号不能为空！");
        }
        //getUserByPhone去数据库查询手机号是否存在
        User dbUser = this.getUserByPhone(phone);
        if (dbUser != null) {
            throw new ConditionException("该手机号已经注册！");
        }
        /************************************正式的注册逻辑**************************************/
        Date now = new Date();//生成一个时间戳，给MD5进行加密
        String salt = String.valueOf(now.getTime());//用当前时间，生成盐值
        String password = user.getPassword();//获取前端传来的密码，是被前端进行RC加密的代码
        String rawPassword;//原始密码
        try {
            rawPassword = RSAUtil.decrypt(password);//RSAUtil下的解密的方法
        } catch (Exception e) {
            throw new ConditionException("密码解密失败！");
        }
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");//使用MD5加密
        user.setSalt(salt);//盐值赋值
        user.setPassword(md5Password);//密码进行了重新加密
        user.setCreateTime(now);//当前时间
        /*添加到数据库*/
        userDao.addUser(user);
        /************************************根据创建的用户id，构建用户信息****************************/
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());//将用户id传入用户信息id
        userInfo.setNick(UserConstant.DEFAULT_NICK);//因为传入的用户中没有传入用户昵称，所以这里随机分配一个用户昵称
        userInfo.setBirth(UserConstant.DEFAULT_BIRTH);
        userInfo.setGender(UserConstant.GENDER_MALE);
        userInfo.setCreateTime(now);
        userDao.addUserInfo(userInfo);

    }

    public User getUserByPhone(String phone) {
        return userDao.getUserByPhone(phone);
    }

    public String login(User user) throws Exception {
        /*************************判断用户是否合法***************************************/
        String phone = user.getPhone() == null ? "" : user.getPhone();
        String email = user.getEmail() == null ? "" : user.getEmail();
        if (StringUtils.isNullOrEmpty(phone) && StringUtils.isNullOrEmpty(email)) {
            throw new ConditionException("参数异常！");
        }


        User dbUser = userDao.getUserByPhoneOrEmail(phone, email);
        if (dbUser == null) {
            throw new ConditionException("当前用户不存在！");
        }

        /*************************判断密码是否正确***************************************/
        /*加密解密*/
        String password = user.getPassword();
        String rawPassword;
        try {
            rawPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new ConditionException("密码解密失败！");
        }
        String salt = dbUser.getSalt();//盐值从数据库中获取
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");

        if (!md5Password.equals(dbUser.getPassword())) {//与数据库中的密码进行比较
            throw new ConditionException("密码错误！");
        }

        /**************************生成Token令牌*************************************/
        return TokenUtil.generateToken(dbUser.getId());
    }

    public String getRefreshTokenByUserId(Long userId) {
        return userDao.getRefreshTokenByUserId(userId);
    }


    public User getUserInfo(Long userId) {
        User user = userDao.getUserById(userId);//查用户信息
        UserInfo userInfo = userDao.getUserInfoByUserId(userId);//查用户基本信息
        //将user和userinfo统一放在一起
        user.setUserInfo(userInfo);
        return user;
    }

    public void updateUsers(User user) throws Exception {
        //先查询下用户是否存在
        Long id = user.getId();
        User dbUser = userDao.getUserById(id);
        if (dbUser == null) {
            throw new ConditionException("用户不存在！");
        }
        //因为在更新用户的时候，可能会更新用户的密码，所以要先判断用户的密码是否为空，如果不为空，则需要将密码进行解密，然后使用MD5算法进行加密，并将加密结果存入数据库中。
        if (!StringUtils.isNullOrEmpty(user.getPassword())) {
            String rawPassword = RSAUtil.decrypt(user.getPassword());
            String md5Password = MD5Util.sign(rawPassword, dbUser.getSalt(), "UTF-8");
            user.setPassword(md5Password);//将解密后的密码设置进去
        }
        user.setUpdateTime(new Date());
        userDao.updateUsers(user);
    }

    public void updateUserInfos(UserInfo userInfo) {
        userInfo.setUpdateTime(new Date());//设置更新时间
        userDao.updateUserInfos(userInfo);
    }


    public User getUserById(Long followingId) {
        return userDao.getUserById(followingId);
    }

    public List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList) {
        return userDao.getUserInfoByUserIds(userIdList);
    }


    /*分页查询*/
    public PageResult<UserInfo> pageListUserInfos(JSONObject params) {
        Integer no = params.getInteger("no");
        Integer size = params.getInteger("size");
        params.put("start", (no - 1) * size);
        params.put("limit", size);
        Integer total = userDao.pageCountUserInfos(params);
        List<UserInfo> list = new ArrayList<>();
        if (total > 0) {
            list = userDao.pageListUserInfos(params);
        }
        return new PageResult<>(total, list);
    }

    public List<UserInfo> batchGetUserInfoByUserIds(Set<Long> userIdList) {
        return userDao.batchGetUserInfoByUserIds(userIdList);
    }
}
