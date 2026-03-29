package org.gateway.nettygateway.route;

import io.netty.handler.codec.http.HttpMethod;

import java.util.Optional;

/**
 * 路由注册表抽象。
 *
 * 有了这层接口，后续无论路由来自内存、配置文件还是远端配置中心，
 * 请求处理器都不需要跟着改。
 */
public interface RouteRegistry {

    /**
     * 根据请求方法和路径查找匹配路由。
     */
    Optional<RouteDefinition> find(HttpMethod method, String path);
}
