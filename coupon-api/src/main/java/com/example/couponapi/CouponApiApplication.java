package com.example.couponapi;

import com.example.couponcore.CouponCoreConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(CouponCoreConfiguration.class)
@SpringBootApplication
public class CouponApiApplication {

	public static void main(String[] args) {
		// coupon-api 와 coupon-core의 yml 설정값을 같이 반영
		System.setProperty("spring.config.name", "application-core, application-api");
		SpringApplication.run(CouponApiApplication.class, args);
	}

}
