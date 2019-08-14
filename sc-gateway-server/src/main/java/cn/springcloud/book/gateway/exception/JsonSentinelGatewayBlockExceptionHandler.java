package cn.springcloud.book.gateway.exception;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.alibaba.csp.sentinel.util.function.Supplier;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @description:
 * @author: bobqiu
 * @create: 2019-08-13
 **/
public class JsonSentinelGatewayBlockExceptionHandler implements WebExceptionHandler {

    private List<ViewResolver> viewResolvers;
    private List<HttpMessageWriter<?>> messageWriters;

    public JsonSentinelGatewayBlockExceptionHandler(List<ViewResolver> viewResolvers, ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolvers;
        this.messageWriters = serverCodecConfigurer.getWriters();
    }

    private Mono<Void> writeResponse(ServerResponse response, ServerWebExchange exchange,Throwable ex) {
       // return response.writeTo(exchange, contextSupplier.get());
        SentinelResponse errorResponse = null;
        // 不同的异常返回不同的提示语
        if (ex instanceof FlowException) {
            errorResponse = SentinelResponse.builder()
                    .status(100).msg("接口限流了")
                    .build();
        } else if (ex instanceof DegradeException) {
            errorResponse = SentinelResponse.builder()
                    .status(101).msg("服务降级了")
                    .build();
        } else if (ex instanceof ParamFlowException) {
            errorResponse = SentinelResponse.builder()
                    .status(102).msg("热点参数限流了")
                    .build();
        } else if (ex instanceof SystemBlockException) {
            errorResponse = SentinelResponse.builder()
                    .status(103).msg("触发系统保护规则")
                    .build();
        } else if (ex instanceof AuthorityException) {
            errorResponse = SentinelResponse.builder()
                    .status(104).msg("授权规则不通过")
                    .build();
        }
        ServerHttpResponse serverHttpResponse = exchange.getResponse();
        StringBuilder sb = new StringBuilder(128);
        sb.append("{").append("\"code\":").append(errorResponse.getStatus()).append(",").append("\"message\":\"").append(errorResponse.getMsg()).append("\"}");
        serverHttpResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        byte[] datas = sb.toString().getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = serverHttpResponse.bufferFactory().wrap(datas);
        return serverHttpResponse.writeWith(Mono.just(buffer));
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        // This exception handler only handles rejection by Sentinel.
        if (!BlockException.isBlockException(ex)) {
            return Mono.error(ex);
        }

        //System.out.println("&&&&&&&&&&"+ex.toString());
        return handleBlockedRequest(exchange, ex)
                .flatMap(response -> writeResponse(response, exchange,ex));
    }

    private Mono<ServerResponse> handleBlockedRequest(ServerWebExchange exchange, Throwable throwable) {
        return GatewayCallbackManager.getBlockHandler().handleRequest(exchange, throwable);
    }

    private final Supplier<ServerResponse.Context> contextSupplier = () -> new ServerResponse.Context() {
        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            return JsonSentinelGatewayBlockExceptionHandler.this.messageWriters;
        }

        @Override
        public List<ViewResolver> viewResolvers() {
            return JsonSentinelGatewayBlockExceptionHandler.this.viewResolvers;
        }
    };
}
