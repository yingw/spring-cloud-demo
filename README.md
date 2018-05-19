# Spring Cloud Demo

演示 Spring Cloud，Netflix 微服务架构的相关组件

- Spring Cloud 版本：Edgware.SR3
- Spring Boot 版本：1.5.13.RELEASE

各模块说明

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

## TODO 高可用

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

## Turbine


## Spring Retry
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

## Hystrix Dashboard
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-hystrix-dashboard</artifactId>
</dependency>
```

`@EnableHystrixDashboard`

加入地址 xxx/hystrix.stream

> 注意：被测试服务需要依赖：actuator

## Turbine

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
      path: /api-a/**
      serviceId: service-ribbon
    api-b:
      path: /api-b/**
      serviceId: service-feign
```

### 服务过滤做安全验证

```java
@Component
public class MyFilter extends ZuulFilter{

    private static Logger log = LoggerFactory.getLogger(MyFilter.class);
    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        log.info(String.format("%s >>> %s", request.getMethod(), request.getRequestURL().toString()));
        Object accessToken = request.getParameter("token");
        if(accessToken == null) {
            log.warn("token is empty");
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            try {
                ctx.getResponse().getWriter().write("token is empty");
            }catch (Exception e){}

            return null;
        }
        log.info("ok");
        return null;
    }
}
```
- filterType：返回一个字符串代表过滤器的类型，在zuul中定义了四种不同生命周期的过滤器类型，具体如下： 
    - pre：路由之前
    - routing：路由之时
    - post： 路由之后
    - error：发送错误调用
    - filterOrder：过滤的顺序
    - shouldFilter：这里可以写逻辑判断，是否要过滤，本文true,永远过滤。
    - run：过滤器的具体逻辑。可用很复杂，包括查sql，nosql去判断该请求到底有没有权限访问。
    
## Zipkin 链路跟踪

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

然后跨服务调用就会在控制台查到跟踪记录

## Sleuth

TODO

