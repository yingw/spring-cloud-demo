package cn.yinguowei.consul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableDiscoveryClient
@SpringBootApplication
@RestController
public class ConsulService1Application {

    public static void main(String[] args) {
        SpringApplication.run(ConsulService1Application.class, args);
    }

    @GetMapping("/")
    public String hi(String name) {
        return "Hi, " + name;
    }
}
