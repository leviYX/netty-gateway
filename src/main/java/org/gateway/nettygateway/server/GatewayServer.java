package org.gateway.nettygateway.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.gateway.nettygateway.config.GatewayServerConfig;
import org.gateway.nettygateway.route.RouteRegistry;

/**
 * Netty 服务端封装。
 *
 * 这个类负责管理 EventLoopGroup、绑定端口、注册 ChannelInitializer，
 * 对外暴露 start / stop 这类比较稳定的生命周期方法。
 */
public final class GatewayServer {

    private final GatewayServerConfig config;
    private final RouteRegistry routeRegistry;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private Channel channel;

    public GatewayServer(GatewayServerConfig config, RouteRegistry routeRegistry) {
        this.config = config;
        this.routeRegistry = routeRegistry;
        // bossGroup 负责接收连接，workerGroup 负责处理读写事件。
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
    }

    public void start() throws InterruptedException {
        // ServerBootstrap 是 Netty 服务端启动的入口对象。
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new GatewayServerInitializer(routeRegistry));

        // bind(...).sync() 会等待端口绑定完成。
        channel = bootstrap.bind(config.port()).sync().channel();
        System.out.println("Netty gateway started on http://127.0.0.1:" + config.port());
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (channel != null) {
            // closeFuture 会在服务端 Channel 关闭时完成。
            channel.closeFuture().sync();
        }
    }

    public void stop() {
        if (channel != null) {
            channel.close();
        }
        // 优雅关闭事件循环，避免强杀线程。
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
