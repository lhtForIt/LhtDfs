package com.lht.lhtfs;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
// 高版本有个bug,自动配置类无法生效,需要手动导入
@Import(RocketMQAutoConfiguration.class)
public class LhtFsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LhtFsApplication.class, args);
    }
    @Value("${lhtfs.path}")
    private String uploadPath;
    @Bean
    ApplicationRunner applicationRunner(){
        return args -> {
            FileUtils.init(uploadPath);
            System.out.println("=======> lht fs start.......");
        };
    }

}
