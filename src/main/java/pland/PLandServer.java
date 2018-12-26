package pland;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import pland.gamecore.GameContext;

import java.util.HashMap;
import java.util.Map;

public class PLandServer {

    public static GameContext gameContext;
    public static Map<Integer, Channel> userChannels = new HashMap<Integer, Channel>();

    final static int DEFAULT_PORT = 8080;
    private int port;

    public PLandServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        gameContext = new GameContext();
        gameContext.loadMap();

        try {

            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new PLandHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(this.port).sync();
            System.out.println("Server listening on port "+this.port);

            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            try {
                for (int i = 0; i < args.length; i++) {
                    if ( args[i].equals("-p") ) {
                        port = Integer.parseInt(args[i+1]);
                    }
                }
            } catch (Exception e) {
                System.out.println("USAGE: PlandServer <-p> [PORT]");
            }
        }
        new PLandServer(port).run();

    }
}
