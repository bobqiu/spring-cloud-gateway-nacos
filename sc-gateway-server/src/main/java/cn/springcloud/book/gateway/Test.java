package cn.springcloud.book.gateway;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;

import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @description:
 * @author: bobqiu
 * @create: 2019-08-12
 **/

public class Test {
        public static void main(String[] args) throws NacosException, InterruptedException {
            String serverAddr = "127.0.0.1:8848";
            String dataId = "appA";
            String group = "DEFAULT_GROUP";
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
            ConfigService configService = NacosFactory.createConfigService(properties);
            String content = configService.getConfig(dataId, group, 5000);
            System.out.println("11:::::"+content);
            configService.addListener(dataId, group,new Listener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    System.out.println("recieve111111111:" + configInfo);
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }
            });

            boolean isPublishOk = configService.publishConfig(dataId, group, "content");
            System.out.println(isPublishOk);

            Thread.sleep(3000);
            content = configService.getConfig(dataId, group, 5000);
            System.out.println(content);

            boolean isRemoveOk = configService.removeConfig(dataId, group);
            System.out.println(isRemoveOk);
            Thread.sleep(3000);

            content = configService.getConfig(dataId, group, 5000);
            System.out.println(content);
            Thread.sleep(300000);

        }
}

