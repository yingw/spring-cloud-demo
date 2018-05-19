# Spring Cloud Demo

演示 Spring Cloud，Netflix 微服务架构的相关组件

- Spring Cloud 版本：Edgware.SR3
- Spring Boot 版本：1.5.13.RELEASE

## 目录
- [Spring Cloud Demo](#spring-cloud-demo)
  * [各模块说明](#%E5%90%84%E6%A8%A1%E5%9D%97%E8%AF%B4%E6%98%8E)
  * [配置中心 Config Server](#%E9%85%8D%E7%BD%AE%E4%B8%AD%E5%BF%83-config-server)
    + [使用文件配置库](#%E4%BD%BF%E7%94%A8%E6%96%87%E4%BB%B6%E9%85%8D%E7%BD%AE%E5%BA%93)
    + [客户端配置](#%E5%AE%A2%E6%88%B7%E7%AB%AF%E9%85%8D%E7%BD%AE)
    + [使用 git 的配置](#%E4%BD%BF%E7%94%A8-git-%E7%9A%84%E9%85%8D%E7%BD%AE)
    + [高可用](#%E9%AB%98%E5%8F%AF%E7%94%A8)
  * [注册中心 Eureka Service](#%E6%B3%A8%E5%86%8C%E4%B8%AD%E5%BF%83-eureka-service)
    + [客户端](#%E5%AE%A2%E6%88%B7%E7%AB%AF)
  * [Ribbon 负载均衡](#ribbon-%E8%B4%9F%E8%BD%BD%E5%9D%87%E8%A1%A1)
  * [Feign 声明式 HTTP 客户端](#feign-%E5%A3%B0%E6%98%8E%E5%BC%8F-http-%E5%AE%A2%E6%88%B7%E7%AB%AF)
  * [Hystrix 断路器（Circuit Breaker）](#hystrix-%E6%96%AD%E8%B7%AF%E5%99%A8circuit-breaker)
  * [Spring Retry 重试](#spring-retry-%E9%87%8D%E8%AF%95)
  * [Hystrix Dashboard 断路器监控](#hystrix-dashboard-%E6%96%AD%E8%B7%AF%E5%99%A8%E7%9B%91%E6%8E%A7)
  * [Turbine 断路器聚合](#turbine-%E6%96%AD%E8%B7%AF%E5%99%A8%E8%81%9A%E5%90%88)
  * [Zuul 路由网关](#zuul-%E8%B7%AF%E7%94%B1%E7%BD%91%E5%85%B3)
  * [Zipkin 链路追踪](#zipkin-%E9%93%BE%E8%B7%AF%E8%BF%BD%E8%B8%AA)
    + [客户端](#%E5%AE%A2%E6%88%B7%E7%AB%AF-1)
  * [Sleuth 分布式追踪](#sleuth-%E5%88%86%E5%B8%83%E5%BC%8F%E8%BF%BD%E8%B8%AA)
  * [Spring Cloud Bus](#spring-cloud-bus)

## 各模块说明

服务 | 说明 | URL
---|---|---
Config Server | 配置中心 |http://localhost:8888/
Eureka Service | 注册中心、服务发现 | http://localhost:8761/, http://localhost:8762/
Hello Service | 测试服务 | http://localhost:9000/
Hello Client | 测试客户端 | http://localhost:9999/
Hystrix | 断路器 |
Hystrix Dashboard | 断路器面板 | http://localhost:8010/hystrix.html
Turbine | 断路器聚合 | http://localhost:8769/turbine.stream
Zipkin | 链路追踪 |  http://localhost:9411/
Zuul | 动态路由 |
Ribbon | 客户端负载均衡 |
Feign | 声明式 HTTP 客户端 |
Spring Retry | 重试 |

>建议不要使用 spring.io 的临时库。


## 配置中心 Config Server

创建 `config-server` 项目

依赖
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

### 使用文件配置库
先使用文件配置仓库方式，创建另一个存放配置的项目目录 **cloud-config-repo**，新建 **config-server.properties**：
```properties
foo=bar
```

**application.properties**
```properties
server.port=8888
spring.profiles.active=native
spring.cloud.config.server.native.search-locations=file:///D:/MyArchitecture/spring-cloud-demo/cloud-config-repo
```
> 原地址：
spring.cloud.config.server.native.search-locations=file:///D:/SpringCloud/workspace-idea/wilmar-cloud/0.config-repo

> 注意：使用文件库需要设置 profile 为 native，使用本地文件，官方是推荐使用远程 git 仓库

> 注意：使用 windows 的本地配置文件目录需要三个 ///

启动后，访问：
- http://localhost:8888/config-server-master.yml
- http://localhost:8888/config-server/master

TODO：刷新配置
scope
curl -x POST ..../refresh

### 客户端配置
客户端配置 Config Server 地址
```properties
spring.cloud.config.label=master //分支
spring.cloud.config.profile=dev //环境
spring.cloud.config.uri= http://localhost:8888/
```

### 使用 git 的配置
如果要使用 git 仓库作为配置文件仓库
```properties
spring.cloud.config.server.git.uri=https://github.com/forezp/SpringcloudConfig/
spring.cloud.config.server.git.searchPaths=respo
spring.cloud.config.label=master
spring.cloud.config.server.git.username=your username
spring.cloud.config.server.git.password=your password
```

### 高可用
TODO

Config Server 的高可用需要将 COnfig Server 注册到 Eureka Service 上去，但是会存在互相依赖的问题。


## 注册中心 Eureka Service

新建项目，依赖于 **Eureka Server**, **Config Client**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka-server</artifactId>
</dependency>
```
改名 `application.properties` 为 `bootstrap.properties`

```
spring.application.name=eureka-service
spring.cloud.config.uri=http://localhost:8888
```

在`cloud-config-repo`中新建 **eureka-service.properties**

> 除外部配置其实也可以放到项目自己的 application.properties 里
```
server.port=${PORT:8761}
# 禁止自己注册
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
# eureka.client.enabled=false

#set this to avoid: "EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP WHEN THEY'RE NOT. RENEWALS ARE LESSER THAN THRESHOLD AND HENCE THE INSTANCES ARE NOT BEING EXPIRED JUST TO BE SAFE."
eureka.server.enable-self-preservation=false

# 但是加上禁止自我保护又会提示：THE SELF PRESERVATION MODE IS TURNED OFF.THIS MAY NOT PROTECT INSTANCE EXPIRY IN CASE OF NETWORK/OTHER PROBLEMS.

# 地址？（如果把自己也注册为客户端，设置：eureka.instance.hostname=localhost）
eureka.client.service-url.defaultZone=http://${eureka.instance.hostname}:${server.port}/eureka/
```

机器名，建议设置机器名为域名或者改成用ip （用IP也会有多个IP的问题）
```properties
eureka.instance.prefer-ip-address=true
```

声明
```
@EnableEurekaServer
```

启动，访问：http://localhost:8761/

### 客户端
依赖 eureka

`@EnableEurekaClient`

```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
spring.application.name=demo-client
```
> 注意：应用名称注册到 Eureka Service 上后 会自动全大写：DEMO-CLIENT


## Ribbon 负载均衡

`@EnableDiscoveryClient`

实例一个 RestTemplate
```java
@Bean
@LoadBalanced
RestTemplate restTemplate() {
    return new RestTemplate();
}
```
调用
```java
public String hiService(String name) {
    return restTemplate.getForObject("http://SERVICE-HI/hi?name="+name,String.class);
}
```

## Feign 声明式 HTTP 客户端

声明
`@EnableFeignClients`

调用
```java
@FeignClient(value = "service-hi")
public interface SchedualServiceHi {
    @RequestMapping(value = "/hi",method = RequestMethod.GET) // 这里是服务方提供地址
    String sayHiFromClientOne(@RequestParam(value = "name") String name);
}
```

> 注意这里有个小问题，就是 Feign 老版本不理解 GetMapping 等缩写，一定要用 RequestMapping；另外 @RequestMapping 一定要显式声明。可能新版本解决了这些问题

## Hystrix 断路器（Circuit Breaker）

TODO: 断路器的开关阈值（目前：Hystrix 是5秒20次）

依赖
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-hystrix</artifactId>
</dependency>
```

声明 `@EnableHystrix`

方法上添加断路器声明 
`@HystrixCommand(fallbackMethod = "fallback")`

再定义一个 `fallback` 方法

如果是在 Feign 里面的写法：SchedualServiceHiHystric 是接口的实现
```
@FeignClient(value = "service-hi",fallback = SchedualServiceHiHystric.class)
```

Josh 觉得这样写不优雅，建议使用 spring-retry

## Spring Retry 重试
Spring Retry是从spring batch独立出来的一个能功能，主要实现了重试和熔断。

依赖
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-hystrix</artifactId>
</dependency>
```

1.2.0

`@EnableRetry`

声明
`@Retryable`

```java
@Recover
public int fallback(RuntimeException ex)
```

@Retryable(include = XXExcption.claass)

方法签名要一致，一个 Component 里一对

可以加上 @Async 来异步

改成断路器 `@CircuitBreaker`

## Hystrix Dashboard 断路器监控
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-hystrix-dashboard</artifactId>
</dependency>
```

`@EnableHystrixDashboard`

加入地址 xxx/hystrix.stream

> 注意：被测试服务需要依赖：actuator

## Turbine 断路器聚合

新建工程 **turbine-service**

依赖
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-turbine</artifactId>
</dependency>
```
启用 `@EnableTurbine`

端口开在 8769

配置
```properties
security.basic.enabled=false
turbine.aggregator.cluster-config=default
turbine.app-config=hello-service,hello-client
turbine.cluster-name-expression=new String("default")
```
http://localhost:8769/turbine.stream

到 HystrixDashboard 监控 turbine，就可以看到多个断路器状态

http://localhost:8010/hystrix/monitor?stream=http%3A%2F%2Flocalhost%3A8769%2Fturbine.stream

## Zuul 路由网关
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zuul</artifactId>
</dependency>
```

`@EnableZuulProxy`

Zuul 的功能很多：路由、安全、转发

zuul 的配置
```yaml
zuul:
  routes:
    api-a:
      path: /api-hi/**
      serviceId: hi-service
    api-b:
      path: /api-hey/**
      serviceId: hey-service
```

## Zipkin 链路追踪

新建工程 **zipkin-server**

依赖
```xml
<dependency>
    <groupId>io.zipkin.java</groupId>
    <artifactId>zipkin-server</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.java</groupId>
    <artifactId>zipkin-autoconfigure-ui</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
```

启用 `@EnableZipkinServer`

设置端口 9411


### 客户端

给客户端增加 zipkin 依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```

Zipkin 服务端地址配置
`spring.zipkin.base-url=http://localhost:9411`

然后跨服务调用就会在控制台查到跟踪记录 http://localhost:9411

## Sleuth 分布式追踪

TODO

## Spring Cloud Bus

