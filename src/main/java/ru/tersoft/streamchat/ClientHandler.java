package ru.tersoft.streamchat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Project streamchat.
 * Created by ivyanni on 28.01.2017.
 */
public class ClientHandler extends SimpleChannelInboundHandler<String> {
    private final ByteBuf firstMessage;

    public ClientHandler() {
        firstMessage = Unpooled.buffer(256);
        for (int i = 0; i < firstMessage.capacity(); i++) {
            firstMessage.writeByte((byte) i);
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String message) {

        String[] strArray = message.split("\n");
        for(String str : strArray) {
            Logger.printLine("> " + str);
        }
        if (message.startsWith("PING")) {
            ctx.channel().writeAndFlush(message.replace("PING", "PONG"));
            Logger.printLine("< " + message.replace("PING", "PONG"));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
