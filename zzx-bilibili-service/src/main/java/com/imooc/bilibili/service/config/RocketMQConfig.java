package com.imooc.bilibili.service.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.domain.UserFollowing;
import com.imooc.bilibili.domain.UserMoment;
import com.imooc.bilibili.domain.constant.UserMomentsConstant;
import com.imooc.bilibili.service.UserFollowingService;
import com.imooc.bilibili.service.websocket.WebSocketService;
import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * RocketMQ是一个分布式消息中间件，它提供了生产者和消费者两种模式，
 * 可以用来实现分布式消息发布/订阅、延迟消息、消息过滤等功能
 */
@Configuration
public class RocketMQConfig {

    /**
     * @Value注解用于从配置文件中读取指定的参数值，这里nameServerAddr表示 从配置文件中读取rocketmq.name.server.address参数的值，
     * 并将其赋值给nameServerAddr变量。
     */
    @Value("${rocketmq.consumer.nameServer}")
    private String nameServerAddr;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserFollowingService userFollowingService;

    /**
     * 生产者：发送消息的
     *
     * @return
     * @throws Exception 在配置生产者模式时，一般需要配置的内容有：
     *                   <p>
     *                   1、生产者组名称：用于标识生产者的组名，一般来说可以使用应用的应用名作为组名；
     *                   <p>
     *                   2、NameServer地址：RocketMQ服务端提供的NameServer地址，用于生产者连接服务端；
     *                   <p>
     *                   3、以及一些可选配置，比如生产者重试次数、等待时间等。
     */
    @Bean("momentsProducer")
    public DefaultMQProducer momentsProducer() throws Exception {
        //配置生产者组名称,配置生产者组名称的作用是用来标识生产者的唯一性，一个生产者组只能有一个生产者，并且不能与其他生产者组的生产者同名，以防止同一个topic的消息被重复消费。
        DefaultMQProducer producer = new DefaultMQProducer(UserMomentsConstant.GROUP_MOMENTS);
        //NameServer地址(名称服务器地址)
        producer.setNamesrvAddr(nameServerAddr);
        producer.start();
        return producer;
    }

    /**
     * 消费者：接收消息的
     * 配置订阅者--三个关键，消费者组名称、NameServer地址、订阅主题等配置正确就可以正常运行
     *
     * @return
     * @throws Exception
     */
    @Bean("momentsConsumer")
    public DefaultMQPushConsumer momentsConsumer() throws Exception {
        //消费者组名称
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UserMomentsConstant.GROUP_MOMENTS);
        //NameServer地址
        consumer.setNamesrvAddr(nameServerAddr);
        //订阅主题,订阅主题UserMomentsConstant.TOPIC_MOMENTS
        consumer.subscribe(UserMomentsConstant.TOPIC_MOMENTS, "*");


        //监听器，消费者收到消息时，会执行下面逻辑方法
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            /*在监听器里面对传入的消息进行处理*/
            /*主要是获取用户ID，然后根据用户ID获取粉丝ID，最后将消息发送给所有的粉丝*/
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                /*取出列表中的元素*/
                MessageExt msg = msgs.get(0);
                if (msg == null) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }

                String bodyStr = new String(msg.getBody());
                UserMoment userMoment = JSONObject.toJavaObject(JSONObject.parseObject(bodyStr), UserMoment.class);
                Long userId = userMoment.getUserId();
                /*获取粉丝的ID*/
                List<UserFollowing> fanList = userFollowingService.getUserFans(userId);
                /*给每一个用户都发送*/
                /*这段代码的意思是，遍历粉丝列表，为每一个粉丝构造一个key，
                然后从redis中获取以这个key为标识的用户订阅列表，如果订阅列表为空，
                就创建一个新的订阅列表，将当前的消息添加到订阅列表中，然后将订阅列表更新到缓存中。*/
                for (UserFollowing fan : fanList) {
                    /*构建key*/
                    String key = "subscribed-" + fan.getUserId();
                    /*去redis中获取以该key为标识的用户订阅列表*/
                    String subscribedListStr = redisTemplate.opsForValue().get(key);
                    List<UserMoment> subscribedList;
                    if (StringUtil.isNullOrEmpty(subscribedListStr)) {
                        subscribedList = new ArrayList<>();
                    } else {
                        //将subscribedListStr类型转换成列表
                        subscribedList = JSONArray.parseArray(subscribedListStr, UserMoment.class);
                    }
                    /*添加列表，存到redis*/
                    subscribedList.add(userMoment);
                    redisTemplate.opsForValue().set(key, JSONObject.toJSONString(subscribedList));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        return consumer;
    }

    @Bean("danmusProducer")
    public DefaultMQProducer danmusProducer() throws Exception {
        // 实例化消息生产者Producer
        DefaultMQProducer producer = new DefaultMQProducer(UserMomentsConstant.GROUP_DANMUS);
        // 设置NameServer的地址
        producer.setNamesrvAddr(nameServerAddr);
        // 启动Producer实例
        producer.start();
        return producer;
    }

    @Bean("danmusConsumer")
    public DefaultMQPushConsumer danmusConsumer() throws Exception {
        // 实例化消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UserMomentsConstant.GROUP_DANMUS);
        // 设置NameServer的地址
        consumer.setNamesrvAddr(nameServerAddr);
        // 订阅一个或者多个Topic，以及Tag来过滤需要消费的消息
        consumer.subscribe(UserMomentsConstant.TOPIC_DANMUS, "*");
        // 注册回调实现类来处理从broker拉取回来的消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                MessageExt msg = msgs.get(0);
                byte[] msgByte = msg.getBody();
                String bodyStr = new String(msgByte);
                JSONObject jsonObject = JSONObject.parseObject(bodyStr);
                String sessionId = jsonObject.getString("sessionId");
                String message = jsonObject.getString("message");
                WebSocketService webSocketService = WebSocketService.WEBSOCKET_MAP.get(sessionId);
                if (webSocketService.getSession().isOpen()) {//判断session是否为打开的状态
                    try {
                        webSocketService.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // 标记该消息已经被成功消费
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        // 启动消费者实例
        consumer.start();
        return consumer;
    }

}
