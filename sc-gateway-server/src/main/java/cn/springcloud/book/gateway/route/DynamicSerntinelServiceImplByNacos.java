package cn.springcloud.book.gateway.route;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.Executor;

@Component
public class DynamicSerntinelServiceImplByNacos {

    @Autowired
    private DynamicRouteServiceImpl dynamicRouteService;

    public DynamicSerntinelServiceImplByNacos() {

        dynamicRouteByNacosListener("service-dev-aks","aks-g");
        dynamicRouteByNacosListener("com.alibaba.csp.sentinel.demo.flow.rule","Sentinel:Demo");
    }

    /**
     * 监听Nacos Server下发的动态路由配置
     * @param dataId
     * @param group
     */

    public void dynamicRouteByNacosListener (String dataId, String group){
        try {
            String serverAddr = "127.0.0.1:8848";
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
            ConfigService configService=NacosFactory.createConfigService(properties);
            String content = configService.getConfig(dataId, group, 5000);
            System.out.println("content:"+content);

            configService.addListener(dataId, group, new Listener()  {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    System.out.println("configInfo:{}"+configInfo);
                    RouteDefinition definition= JSON.parseObject(configInfo,RouteDefinition.class);
                    dynamicRouteService.update(definition);
                }
                @Override
                public Executor getExecutor() {
                    return null;
                }
            });

            /*boolean isPublishOk = configService.publishConfig(dataId, group, content);
            System.out.println("isPublishOk:"+isPublishOk);*/



     /*   boolean isRemoveOk = configService.removeConfig(dataId, group);
        System.out.println(isRemoveOk);
        Thread.sleep(3000);*/

           /* content = configService.getConfig(dataId, group, 5000);
            System.out.println("configService.getConfig："+content);*/

        } catch (NacosException e) {
            //todo 提醒:异常自行处理此处省略
            System.out.println(e);
        }
    }

}
