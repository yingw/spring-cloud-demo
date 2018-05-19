package cn.yinguowei.config;

import org.apache.catalina.loader.ResourceEntry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConfigServerApplicationTests {

    @Value("${server.port}")
    private int port = 0;

    @Test
    public void contextLoads() {
        System.out.println("ConfigServerApplicationTests.contextLoads");
        System.out.println("port = " + port);
        ResponseEntity<Map> entity = new TestRestTemplate().getForEntity(
                "http://localhost:" + 8888 + "/config-server/master", Map.class);
        Assert.assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

}
