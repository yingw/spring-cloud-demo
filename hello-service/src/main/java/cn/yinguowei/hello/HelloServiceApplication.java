package cn.yinguowei.hello;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

@RestController
@SpringBootApplication
public class HelloServiceApplication {

	@Value("${server.port}")
	String port;

	@RequestMapping(value = "/hi",method = RequestMethod.GET)
	public String helloService(@RequestParam(value = "name",defaultValue = "") String name) {
		return "Hello, Im " + name + ", and I come from " + port;
	}

	@GetMapping("/hello")
	public String hello() {
		return "Hello from hello-service";
	}

	public static void main(String[] args) {
		SpringApplication.run(HelloServiceApplication.class, args);
	}
}