package org.gateway.nettygateway.bootstrap;

import org.gateway.nettygateway.config.GatewayServerConfig;
import org.gateway.nettygateway.route.DefaultRouteRegistryFactory;
import org.gateway.nettygateway.server.GatewayServer;

/**
 * 项目启动入口。
 *
 * 这里先手动组装配置、路由注册表和服务端，后续如果项目变复杂，
 * 可以再把这部分替换为配置加载或依赖注入。
 */
public final class NettyGatewayApplication {

    private NettyGatewayApplication() {
    }

    public static void main(String[] args) throws InterruptedException {
        // 当前先写死监听端口，后续再替换成配置文件。
        GatewayServerConfig config = new GatewayServerConfig(8080);
        // 创建最小静态路由表，方便先把服务跑起来。
        GatewayServer server = new GatewayServer(config, DefaultRouteRegistryFactory.create());
        server.start();
        // 阻塞主线程，避免进程启动后立即退出。
        server.blockUntilShutdown();
    }
}
