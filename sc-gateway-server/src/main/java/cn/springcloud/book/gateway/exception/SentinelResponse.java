package cn.springcloud.book.gateway.exception;

import lombok.Builder;
import lombok.Data;

/**
 * @description:
 * @author: bobqiu
 * @create: 2019-08-13
 **/
@Data
@Builder
public class SentinelResponse {
    private Integer status;
    private String msg;
}
