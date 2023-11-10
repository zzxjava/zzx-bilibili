package com.imooc.bilibili.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.dao.UserMomentsDao;
import com.imooc.bilibili.domain.UserMoment;
import com.imooc.bilibili.domain.constant.UserMomentsConstant;
import com.imooc.bilibili.service.util.RocketMQUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * 用户动态提醒服务层
 */
@Service
public class UserMomentsService {

    @Autowired
    private UserMomentsDao userMomentsDao;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 新建用户动态
     *
     * @param userMoment
     * @throws Exception
     */
    public void addUserMoments(UserMoment userMoment) throws Exception {
        //设置创建时间
        userMoment.setCreateTime(new Date());
        /*添加UserMoment*/
        userMomentsDao.addUserMoments(userMoment);

        //向MQ中发布一条消息告诉订阅者，我发送了一条动态
        DefaultMQProducer producer = (DefaultMQProducer) applicationContext.getBean("momentsProducer");
        Message msg = new Message(UserMomentsConstant.TOPIC_MOMENTS, JSONObject.toJSONString(userMoment).getBytes(StandardCharsets.UTF_8));
        //调用同步发送消息的方法
        RocketMQUtil.syncSendMsg(producer, msg);
    }

    /**
     * 获取关注者的动态列表
     *
     * @param userId
     * @return
     */
    public List<UserMoment> getUserSubscribedMoments(Long userId) {
        /*从redis里面取*/
        String key = "subscribed-" + userId;
        String listStr = redisTemplate.opsForValue().get(key);
        /*将String转换成Java对象数组的形式*/
        return JSONArray.parseArray(listStr, UserMoment.class);
    }
}
