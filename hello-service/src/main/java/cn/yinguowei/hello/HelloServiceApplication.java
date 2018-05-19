package cn.yinguowei.hello;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.web.bind.annotation.*;

@RestController
@SpringBootApplication
@EnableHystrix
public class HelloServiceApplication {

	@Value("${server.port}")
	String port;

	@RequestMapping(value = "/hi",method = RequestMethod.GET)
	public String helloService(@RequestParam(value = "name",defaultValue = "") String name) {
		return "Hello, Im " + name + ", and I come from " + port;
	}

	@HystrixCommand(fallbackMethod = "what")
	@GetMapping("/hello")
	public String hello() {
		return "Hello from hello-service";
	}

	public String what() {
		return "WHAT!?";
	}

	public static void main(String[] args) {
		SpringApplication.run(HelloServiceApplication.class, args);
	}
}