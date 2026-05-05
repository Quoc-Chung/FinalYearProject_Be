package com.quocchung.cntt1.techcycle_system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.quocchung.cntt1.techcycle_system.mapper")
public class TechcycleSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(TechcycleSystemApplication.class, args);
	}

}
