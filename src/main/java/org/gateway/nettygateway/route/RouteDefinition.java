package org.gateway.nettygateway.route;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * 路由定义。
 *
 * 当前字段同时描述“匹配条件”和“响应结果”，是为了尽快跑通最小案例。
 * 以后做反向代理时，响应部分通常会被“上游地址、超时、重试策略”等信息替代。
 */
public record RouteDefinition(
        HttpMethod method,
        String path,
        HttpResponseStatus status,
        String contentType,
        String body) {
}
