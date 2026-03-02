package com.log.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Initial example pom took from - https://github.com/spring-cloud/spring-cloud-function/blob/3.2.x/spring-cloud-function-samples/function-sample-aws-routing/pom.xml
 */
@SpringBootApplication
public class LogProcessorApplication {
    public static void main(String[] args) {
		SpringApplication.run(LogProcessorApplication.class, args);
	}
}
