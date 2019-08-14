package cn.springcloud.book.gateway.config;

import cn.springcloud.book.gateway.exception.JsonSentinelGatewayBlockExceptionHandler;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @description:
 * @author: bobqiu
 * @create: 2019-08-13
 **/
@Configuration
public class GatewayConfiguration {
    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public GatewayConfiguration(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }


    /**
     * 配置SentinelGatewayBlockExceptionHandler，限流后异常处理
     *
     * @return
     */
 /*   @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }*/
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public JsonSentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new JsonSentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    /**
     * 配置SentinelGatewayFilter
     *
     * @return
     */
    @Bean
    @Order(-1)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    @PostConstruct
    public void doInit() throws Exception {
        //initGatewayRules();
        initSentinelRulesNacos("127.0.0.1:8848","DEFAULT_GROUP","sentinelnacos");
    }

    /**
     * 配置限流规则
     */
    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        /*rules.add(new GatewayFlowRule("test_route")
                .setCount(1) // 限流阈值
                .setIntervalSec(10) // 统计时间窗口，单位是秒，默认是 1 秒
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_CLIENT_IP).setFieldName("name")
                )
        );*/
        rules.add(new GatewayFlowRule("hello_route")
                .setCount(1));
        GatewayRuleManager.loadRules(rules);
    }

    private void initSentinelRulesNacos(String remoteAddress,String groupId,String dataId) throws Exception {
        /*ReadableDataSource<String, List<NacosSentinelRule>> flowRuleDataSource = new NacosDataSource<>(remoteAddress, groupId, dataId,
                source -> JSON.parseObject(source, new TypeReference<List<NacosSentinelRule>>() {
                }));
        List<NacosSentinelRule> flowRules = flowRuleDataSource.loadConfig();
        Set<GatewayFlowRule> rules = new HashSet<>();
        for (NacosSentinelRule flowRule : flowRules) {
            GatewayFlowRule gatewayFlowRule = new GatewayFlowRule();
            System.out.println(flowRule.toString());
            gatewayFlowRule.setResource(flowRule.getResource());
            gatewayFlowRule.setCount(flowRule.getCount());
            gatewayFlowRule.setGrade(flowRule.getGrade());

            rules.add(gatewayFlowRule);
        }*/
        ReadableDataSource<String, Set<GatewayFlowRule>> gatewayFlowRuleDatasource = new NacosDataSource<>(remoteAddress, groupId, dataId,
                source -> JSON.parseObject(source, new TypeReference<Set<GatewayFlowRule>>() {
                }));

        System.out.println("######gatewayFowRule:"+gatewayFlowRuleDatasource.getProperty().toString());

        GatewayRuleManager.register2Property(gatewayFlowRuleDatasource.getProperty());
        //FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    };
}
