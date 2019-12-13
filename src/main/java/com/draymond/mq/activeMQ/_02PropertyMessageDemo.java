package com.draymond.mq.activeMQ;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import javax.jms.*;

/**
 * 消息属性的设置： 4.4.3：消息属性
 *
 * @Auther: ZhangSuchao
 * @Date: 2019/11/6 21:45
 */
public class _02PropertyMessageDemo {

    private String userName = "admin";
    private String password = "admin";
    private String url = "tcp://192.168.8.65:61616";
    private String queueName = "Qtest_queue";
    private String topicName = "Ttest_topic";

    /**
     * 队列模式：生产者
     */
    @Test
    public void queueProduce() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(userName, password, url);
        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination queue = session.createQueue(queueName);
        MessageProducer producer = session.createProducer(queue);
        // 消息持久化队列
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);  //消息头 持久化   PERSISTENT持久化 NON_PERSISTENT非持久化（mq挂了后消息会丢失）

        for (int i = 1; i <= 10; i++) {
            String msg = "第" + i + "个msg";
            TextMessage message = session.createTextMessage(msg);
            message.setStringProperty("source", "dingding");
            producer.send(message);
        }
    }


    @Test
    public void queueProduce1() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(userName, password, url);
        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Destination queue = session.createQueue(queueName); //消息头 目的地
        MessageProducer producer = session.createProducer(queue);

        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        producer.setPriority(9);

        for (int i = 1; i <= 10; i++) {
            String msg = "第" + i + "个msg";
            TextMessage message = session.createTextMessage(msg);
            message.setStringProperty("source", "dingding");
            message.setJMSExpiration(1000 * 60 * 60 * 24L);
            message.setStringProperty("isVip","y");
            producer.send(message);
        }
    }


    /**
     * 队列模式：消费者
     * Receive方式：每次只消费一次(需要代码while循环查询)
     */
    @Test
    public void queueReceiveConsumer() throws JMSException {
        //  #1  创建连接工厂
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(userName, password, url);
        //  #2  从工厂获得连接connectino
        Connection connection = factory.createConnection();
        //  #3  启动访问
        connection.start();
        //  #4  创建会话session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //  #5  由会话session创建目的地distinct(Queue/Topic)
        Queue queue = session.createQueue(queueName);
        //  #6  会话session创建消费者Produce
        MessageConsumer consumer = session.createConsumer(queue);
        // receive同步阻塞方式：没有收到消息一直等
        while (true) {
            Message message = consumer.receive();   //没有参数会一直等
            if (message != null && message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                System.out.println("消费了：： " + textMessage);
                //消息属性
                boolean isVip = message.getBooleanProperty("isVip");    //问题：某些消息没有设置这些属性为false
                if (isVip) {
                    //优先处理，或做不同的业务
                } else {
                }

                String source = message.getStringProperty("source");
                // 做一些处理


            }
        }


    }

}