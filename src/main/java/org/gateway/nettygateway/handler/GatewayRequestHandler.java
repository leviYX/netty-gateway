package org.gateway.nettygateway.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;
import org.gateway.nettygateway.route.RouteDefinition;
import org.gateway.nettygateway.route.RouteRegistry;

import java.util.Optional;

/**
 * 最核心的请求处理器。
 *
 * 这个类负责把 HTTP 请求映射到路由定义，再根据路由定义构造响应。
 * 当前还是“静态响应”模式，后续演进成反向代理时，这里会变成：
 * 查路由 -> 转发到上游 -> 接收上游响应 -> 回写给客户端。
 */
public final class GatewayRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final RouteRegistry routeRegistry;

    public GatewayRequestHandler(RouteRegistry routeRegistry) {
        this.routeRegistry = routeRegistry;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        // 如果 HTTP 解码失败，直接返回 400。
        DecoderResult decoderResult = request.decoderResult();
        if (!decoderResult.isSuccess()) {
            writeResponse(ctx, request, HttpResponseStatus.BAD_REQUEST, "text/plain; charset=UTF-8",
                    "Bad request");
            return;
        }

        // QueryStringDecoder.path() 只取路径部分，不包含查询参数。
        String path = new QueryStringDecoder(request.uri()).path();
        Optional<RouteDefinition> route = routeRegistry.find(request.method(), path);
        if (route.isEmpty()) {
            writeResponse(ctx, request, HttpResponseStatus.NOT_FOUND, "text/plain; charset=UTF-8",
                    "No route matched: " + request.method().name() + " " + path);
            return;
        }

        RouteDefinition definition = route.get();
        writeResponse(ctx, request, definition.status(), definition.contentType(), definition.body());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 学习阶段先直接打印异常，后续再替换成统一日志记录。
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 统一构造 FullHttpResponse，并根据是否 Keep-Alive 决定是否关闭连接。
     */
    private static void writeResponse(
            ChannelHandlerContext ctx,
            FullHttpRequest request,
            HttpResponseStatus status,
            String contentType,
            String body) {
        byte[] content = body.getBytes(CharsetUtil.UTF_8);
        // 这里直接把字符串包装成完整 HTTP 响应体，适合当前最小示例。
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                Unpooled.wrappedBuffer(content));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, content.length);

        // HTTP/1.1 默认支持长连接，这里按客户端请求头决定是否复用连接。
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderNames.KEEP_ALIVE);
            ctx.writeAndFlush(response);
        } else {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
