package ru.tersoft.streamchat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Project streamchat.
 * Created by ivyanni on 28.01.2017.
 */
public class ClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        DataStorage.setActiveStatus(true);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        DataStorage.setActiveStatus(false);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String message) {
        String[] strArray = message.split("\n");
        for(String str : strArray) {
            Logger.getLogger().printLine("> " + str);
            if (str.startsWith("PING")) {
                ctx.channel().writeAndFlush(str.replace("PING", "PONG") + "\r\n");
                Logger.getLogger().printLine("< " + str.replace("PING", "PONG"));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
