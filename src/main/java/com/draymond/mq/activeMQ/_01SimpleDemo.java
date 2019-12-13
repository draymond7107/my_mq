package com.draymond.mq.activeMQ;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;

import javax.jms.*;
import java.io.IOException;

/**
 * 最简易的MQ执行
 *
 * @Auther: ZhangSuchao
 * @Date: 2019/11/5 18:37
 */
public class _01SimpleDemo {

    private String userName = "admin";
    private String password = "admin";
    private String url = "tcp://192.168.8.65:61616";
    private String queueName = "Qtest_queue";
    private String topicName = "Ttest_topic";


    //--------------------- QUEUE模式  start ---------------------------
    /**
     * 队列模式：生产者
     */
    @Test
    public void queueProduce() throws JMSException {
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
        //  #6  会话session创建生产者Produce
        MessageProducer producer = session.createProducer(queue);
        //  #7  宕机保存消息
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);

        //  #8  生产消息，发送到队列
        for (int i = 1; i <= 10; i++) {
            String msg = "第" + i + "个msg";
            TextMessage message = session.createTextMessage(msg);
            //  #9  发送消息到MQ
            producer.send(message);
        }

        //  #10 关闭资源
        producer.close();
        session.close();
        connection.close();
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
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
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
                message.acknowledge();
            } else {
                break;
            }
        }

        //  #10 关闭资源
        consumer.close();
        session.close();
        connection.close();
    }


    /**
     * 队列模式：消费者
     * Listen方式
     */
    @Test
    public void queueListenConsumer() throws JMSException {
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
        //  监听方式获得message
        consumer.setMessageListener(message -> {
            if (message != null && message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                try {
                    System.out.println("消费了：： " + textMessage.getText());
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
        try {

            System.in.read();//保证控制台不灭：避免没有监听到消息的时候关闭控制台
        } catch (IOException e) {
            e.printStackTrace();
        }

        //  #10 关闭资源
        consumer.close();
        session.close();
        connection.close();
    }

    //--------------------- queue模式  end---------------------------

    //--------------------- topic模式  start-------------------------
    @Test
    public void topicProducer() throws JMSException {

        //  #1  创建连接工厂
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(userName, password, url);
        //  #2  从工厂获得连接connectino
        Connection connection = factory.createConnection();
        //  #3  启动访问
        connection.start();
        //  #4  创建会话session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //  #5  由会话session创建目的地distinct(Queue/Topic)
        Topic topic = session.createTopic(topicName);
        //  #6  会话session创建生产者Produce
        MessageProducer producer = session.createProducer(topic);
        //  #7  宕机保存消息
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);

        //  #8  生产消息，发送到队列
        for (int i = 1; i <= 10; i++) {
            String msg = "第" + i + "个msg";
            TextMessage message = session.createTextMessage(msg);
            message.setStringProperty("top","vip"); //设置消息属性，加强消息的识别度（comsumer可以进行筛选某些消息，着重处理）
            //  #9  发送消息到MQ
            producer.send(message);
        }

        //  #10 关闭资源
        producer.close();
        session.close();
        connection.close();
    }


    @Test
    public void topicReceiveConsumer1() throws JMSException {

        //  #1  创建连接工厂
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(userName, password, url);
        //  #2  从工厂获得连接connectino
        Connection connection = factory.createConnection();
        //  #3  启动访问
        connection.start();
        //  #4  创建会话session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //  #5  由会话session创建目的地distinct(Queue/Topic)
        Topic topic = session.createTopic(topicName);
        //  #6  会话session创建生产者Produce
        MessageConsumer consumer = session.createConsumer(topic);

        while (true) {
            Message receive = consumer.receive();
            if (receive != null && receive instanceof TextMessage) {
                TextMessage message = (TextMessage) receive;
                System.out.println("topicConsumer1收到topic消息" + message.getText());
            }
        }


        //  #10 关闭资源
//        consumer.close();
//        session.close();
//        connection.close();
    }

    @Test
    public void topicListenConsumer2() throws JMSException, IOException {

        //  #1  创建连接工厂
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(userName, password, url);
        //  #2  从工厂获得连接connectino
        Connection connection = factory.createConnection();
        //  #3  启动访问
        connection.start();
        //  #4  创建会话session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //  #5  由会话session创建目的地distinct(Queue/Topic)
        Topic topic = session.createTopic(topicName);
        //  #6  会话session创建生产者Produce
        MessageConsumer consumer = session.createConsumer(topic);

        consumer.setMessageListener(message -> {
            if (message != null && message instanceof TextMessage) {
                TextMessage msg = (TextMessage) message;
                try {
                    System.out.println("topicListenConsumer2收到topic消息" + msg.getText());
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }

        });
        System.in.read();
        //  #10 关闭资源
//        consumer.close();
//        session.close();
//        connection.close();
    }

}