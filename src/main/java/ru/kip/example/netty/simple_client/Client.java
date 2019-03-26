package ru.kip.example.netty.simple_client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import ru.kip.example.netty.simple_client.inbound.ClientInboundHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Client {

    private final String host;

    private final int port;

    private final ClientInboundHandler handler = new ClientInboundHandler();

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        new Client(host, port).runServer();
    }

    private void runServer() throws Exception {
        //"рабочий" бесконечный цикл
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            //вспомогательный класс для построения клиента
            Bootstrap clientBootstrap = new Bootstrap();

            clientBootstrap.group(workerGroup)          //задание рабочей группы
                    .channel(NioSocketChannel.class)    //используется реализация каналов NIO
                    .handler(new SimpleServerChannelInitializer()) //инициализатор каналов
                    .option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = clientBootstrap.connect(host, port).sync();

            // Wait until the connection is closed.
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    private class SimpleServerChannelInitializer extends ChannelInitializer {

        private Channel channel = null;

        @Override
        protected void initChannel(Channel channel) {
            //добавление нового обработчика в цепочку обработчиков канала
            channel.pipeline().addLast(handler);
            this.channel = channel;
        }

    }
}
