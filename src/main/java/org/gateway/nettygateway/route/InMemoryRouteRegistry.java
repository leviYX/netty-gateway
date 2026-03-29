package org.gateway.nettygateway.route;

import io.netty.handler.codec.http.HttpMethod;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于内存列表的路由注册表。
 *
 * 当前实现非常简单，只做“方法 + 路径”的精确匹配，
 * 后续可以扩展成前缀匹配、Host 匹配、参数匹配等。
 */
public final class InMemoryRouteRegistry implements RouteRegistry {

    private final List<RouteDefinition> routes;

    public InMemoryRouteRegistry(List<RouteDefinition> routes) {
        this.routes = List.copyOf(routes);
    }

    @Override
    public Optional<RouteDefinition> find(HttpMethod method, String path) {
        // 顺序遍历查找，当前路由量很小，这种实现最直观。
        return routes.stream()
                .filter(route -> Objects.equals(route.method(), method))
                .filter(route -> Objects.equals(route.path(), path))
                .findFirst();
    }
}
