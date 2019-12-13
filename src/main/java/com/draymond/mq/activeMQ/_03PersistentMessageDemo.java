package com.draymond.mq.activeMQ;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import javax.jms.*;
import java.io.IOException;

/**
 * 持久化演示
 *
 * @Auther: ZhangSuchao
 * @Date: 2019/11/8 08:52
 */
public class _03PersistentMessageDemo {


    private String userName = "admin";
    private String password = "admin";
    private String url = "tcp://192.168.8.65:61616";
    private String queueName = "Qtest_queue";
    private String topicName = "Ttest_topic";

    /**
     * topic 生产者
     */
    @Test
    public void topicProduce() throws JMSException {

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(userName, password, url);
        Connection connection = factory.createConnection();
        Session session = connection.createSession(true, Session.CLIENT_ACKNOWLEDGE);
        Topic topic = session.createTopic(topicName);
        MessageProducer producer = session.createProducer(topic);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);  //设置持久化
        // 与queue不同，需要先设置持久化的topic然后再启动
        connection.start();
        int i = 1;
        while (i <= 3) {
            TextMessage textMessage = session.createTextMessage("topic");
            producer.send(textMessage);
            i++;
        }
        // 做一些原子性的业务逻辑
        session.commit();  //提交
        //  session.rollback();     //回滚
        //  #10 关闭资源
        producer.close();
        session.close();
        connection.close();
    }


    @Test
    public void topicConsume1() throws JMSException, IOException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(userName, password, url);
        Connection connection = factory.createConnection();
        // 设置 ClientID  标记（必须有）
        connection.setClientID("ZSC");
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);  //手动签收
        Topic topic = session.createTopic(topicName);
        TopicSubscriber topicSubscriber = session.createDurableSubscriber(topic, "topicConsume1...");  //topicConsume1为topic的名称
        connection.start();
        topicSubscriber.setMessageListener(message -> {
            TextMessage textMessage = (TextMessage) message;
            //   message.acknowledge();  //提交签收（如果不签，则会一直收到）
            try {
                System.out.println(textMessage.getText());
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
   System.in.read();

    }

}
