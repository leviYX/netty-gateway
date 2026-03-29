package org.gateway.nettygateway.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.gateway.nettygateway.handler.GatewayRequestHandler;
import org.gateway.nettygateway.route.RouteRegistry;

/**
 * 负责给每个新连接初始化 pipeline。
 *
 * 可以把它理解成“新连接的装配工厂”：每来一个 SocketChannel，
 * 就往里面按顺序塞入编解码器、聚合器和业务处理器。
 */
public final class GatewayServerInitializer extends ChannelInitializer<SocketChannel> {

    private final RouteRegistry routeRegistry;

    public GatewayServerInitializer(RouteRegistry routeRegistry) {
        this.routeRegistry = routeRegistry;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        channel.pipeline()
                // 负责 HTTP 请求解码和 HTTP 响应编码。
                .addLast(new HttpServerCodec())
                // 把分片的 HTTP 消息聚合成 FullHttpRequest，简化后续处理。
                .addLast(new HttpObjectAggregator(1024 * 1024))
                // 真正的业务处理器。
                .addLast(new GatewayRequestHandler(routeRegistry));
    }
}
