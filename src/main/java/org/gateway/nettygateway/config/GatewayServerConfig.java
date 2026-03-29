package org.gateway.nettygateway.config;

/**
 * 服务端配置对象。
 *
 * 当前只有端口，后续可以逐步扩展为线程数、超时、连接限制等配置。
 */
public record GatewayServerConfig(int port) {
}
