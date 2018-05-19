package cn.yinguowei.client;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@EnableFeignClients
@EnableRetry
//@EnableZuulProxy
@EnableDiscoveryClient
@SpringBootApplication
@RestController
@EnableHystrix
public class HelloClientApplication {

	@Autowired HelloService helloService;

	@GetMapping("/")
	@HystrixCommand(fallbackMethod = "fallback")
	public String hello(String name) {
//		return restTemplate.getForObject("http://HELLO-SERVICE/?name="+name, String.class);
		return helloService.hello(name);
	}

	private String fallback(String name) {
		return "What!?";
	}

	@GetMapping("/hello")
	public String hello() {
		return "Hello from hello-client";
	}

//	@Autowired RestTemplate restTemplate;

	public static void main(String[] args) {
		SpringApplication.run(HelloClientApplication.class, args);
	}

//	@Bean
//	@LoadBalanced
//	RestTemplate restTemplate() {
//		return new RestTemplate();
//	}
}


@FeignClient("hello-service")
interface HelloService {
	@RequestMapping(value = "/hi", method = RequestMethod.GET)
	String hello(@RequestParam(value = "name") String name);
}
