package ru.tersoft.streamchat.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import ru.tersoft.streamchat.util.DataStorage;
import ru.tersoft.streamchat.util.Logger;

/**
 * Project streamchat.
 * Created by ivyanni on 28.01.2017.
 */
public class TwitchClient implements Runnable {
    private ChannelPipeline pipeline;
    private volatile Thread thread;
    private EventLoopGroup group;

    public TwitchClient() {
    }

    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        DataStorage dataStorage = DataStorage.getDataStorage();
        group = new NioEventLoopGroup();
        try {
            Bootstrap boot = new Bootstrap();
            boot.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            pipeline = ch.pipeline();
                            pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(5000));
                            pipeline.addLast("stringDecoder", new StringDecoder());
                            pipeline.addLast("stringEncoder", new StringEncoder());
                            pipeline.addLast(new ClientHandler());
                        }
                    });
            ChannelFuture f = boot.connect("35.160.173.203", 6667).sync();
            f.addListener((ChannelFutureListener) future -> {
                if(f.isSuccess()) {
                    sendCommand("PASS oauth:" + dataStorage.getToken(),
                                "NICK " + dataStorage.getUsername(),
                                "JOIN #" + dataStorage.getUsername(),
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

    private void sendCommand(String... msg) {
        for(String str : msg) {
            ChannelFuture f = pipeline.write(str + "\r\n");
            f.addListener((ChannelFutureListener) future -> {
                if(f.isSuccess()) {
                    Logger.getLogger().printLine("< " + str);
                }
            });
        }
        pipeline.flush();
    }

    public void stop() {
        if(group != null) {
            group.shutdownGracefully();
        }
        thread = null;
    }
}
