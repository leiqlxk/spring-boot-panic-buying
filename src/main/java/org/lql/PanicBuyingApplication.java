package org.lql;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Title: PanicBuyingApplication <br>
 * ProjectName: spring-boot-panic-buying <br>
 * description: TODO <br>
 *
 * @author: leiql <br>
 * @version: 1.0 <br>
 * @since: 2021/6/15 15:36 <br>
 */
@SpringBootApplication
@MapperScan(annotationClass = Mapper.class)
@EnableScheduling
public class PanicBuyingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PanicBuyingApplication.class);
    }
}
