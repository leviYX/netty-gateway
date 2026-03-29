package org.gateway.nettygateway.route;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.List;

/**
 * 默认路由工厂。
 *
 * 这里集中放“系统启动时就存在的静态路由”，便于后续替换成
 * 配置文件加载、数据库加载或管理后台动态下发。
 */
public final class DefaultRouteRegistryFactory {

    private DefaultRouteRegistryFactory() {
    }

    public static RouteRegistry create() {
        // 当前返回内存路由表，适合最小可运行版本。
        return new InMemoryRouteRegistry(List.of(
                new RouteDefinition(
                        HttpMethod.GET,
                        "/",
                        HttpResponseStatus.OK,
                        "text/plain; charset=UTF-8",
                        "Netty Gateway is running."),
                new RouteDefinition(
                        HttpMethod.GET,
                        "/health",
                        HttpResponseStatus.OK,
                        "application/json; charset=UTF-8",
                        "{\"status\":\"UP\"}"),
                new RouteDefinition(
                        HttpMethod.GET,
                        "/gateway/routes",
                        HttpResponseStatus.OK,
                        "application/json; charset=UTF-8",
                        """
                        {"routes":["GET /","GET /health","GET /gateway/routes"]}
                        """.trim())
        ));
    }
}
