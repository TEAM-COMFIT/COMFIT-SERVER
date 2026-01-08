package sopt.comfit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ComfitApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComfitApplication.class, args);
	}

}
