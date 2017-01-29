package ru.tersoft.streamchat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * Project streamchat.
 * Created by ivyanni on 28.01.2017.
 */
public class TwitchClient implements Runnable {
    private String HOST = "irc.chat.twitch.tv";
    private int PORT = 6667;
    private ChannelPipeline pipeline;
    private Thread thread;

    public TwitchClient(String host, int port) {
        this.HOST = host;
        this.PORT = port;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap boot = new Bootstrap();
            boot.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            pipeline = ch.pipeline();
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new ClientHandler());
                        }
                    });
            ChannelFuture f = boot.connect(HOST, PORT).sync();
            f.addListener((ChannelFutureListener) future -> {
                if(f.isSuccess()) {
                    sendCommand("PASS oauth:" + DataStorage.getToken(),
                                "NICK " + DataStorage.getUsername(),
                                "JOIN #" + DataStorage.getUsername(),
                                "CAP REQ :twitch.tv/commands",
                                "CAP REQ :twitch.tv/membership",
                                "CAP REQ :twitch.tv/tags");
                }
            });
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void sendCommand(String... msg) {
        for(String str : msg) {
            ChannelFuture f = pipeline.write(str + "\r\n");
            f.addListener((ChannelFutureListener) future -> {
                if(f.isSuccess()) {
                    Logger.printLine("< " + str);
                }
            });
        }
        pipeline.flush();
    }

    public void stop() {
        pipeline = null;
        thread = null;
    }
}
