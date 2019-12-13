package com.draymond.mq;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    static Log logger = LogFactory.getLog(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        logger.error("项目启动完成");
    }

}
