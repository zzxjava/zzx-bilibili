package com.imooc.bilibili.service.util;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RocketMQ工具类，
 * syncSendMsg()方法是指同步发送消息，
 * asyncSendMsg()方法是指异步发送消息。
 */
public class RocketMQUtil {

    /**
     * 同步发送消息
     *
     * @param producer:消息生产者，消息发布者
     * @param msg：要发送的消息
     * @throws Exception 发送者将消息发送出去后，会阻塞等待接收者的响应，
     *                   只有接收者收到消息并处理完成后，发送者才会继续执行，
     *                   这种发送方式速度较慢，但是可以保证消息发送成功。
     */
    public static void syncSendMsg(DefaultMQProducer producer, Message msg) throws Exception {
        SendResult result = producer.send(msg);
        System.out.println(result);
    }

    /**
     * 异步发送消息
     *
     * @param producer
     * @param msg
     * @throws Exception 发送者将消息发送出去后，会立即返回，不会等待接收者的响应，
     *                   接收者收到消息后会异步处理，这种方式发送速度较快，但是不能保证消息发送成功。
     */
    public static void asyncSendMsg(DefaultMQProducer producer, Message msg) throws Exception {
         producer.send(msg, new SendCallback() {
            //发送成功与失败的回调
            @Override
            public void onSuccess(SendResult sendResult) {
                Logger logger = LoggerFactory.getLogger(RocketMQUtil.class);
                logger.info("异步发送消息成功，消息id：" + sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        });
    }
}
