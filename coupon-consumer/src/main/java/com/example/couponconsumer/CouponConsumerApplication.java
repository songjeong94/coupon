package com.example.couponconsumer;

import com.example.couponcore.CouponCoreConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(CouponCoreConfiguration.class)
@SpringBootApplication
public class CouponConsumerApplication {

	public static void main(String[] args) {
		// coupon-consumer 과 coupon-core의 yml 설정값을 같이 반영
		System.setProperty("spring.config.name", "application-core, application-consumer");
		SpringApplication.run(CouponConsumerApplication.class, args);
	}
}
