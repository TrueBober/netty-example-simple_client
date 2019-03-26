package ru.kip.example.netty.simple_client.inbound;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;

import java.nio.charset.Charset;

import static org.slf4j.LoggerFactory.getLogger;

public class ClientInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = getLogger(ClientInboundHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.writeAndFlush(Unpooled.copiedBuffer("Test", Charset.forName("UTF-8"))).sync();
        log.info("Write");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buffer = (ByteBuf) msg;
        try {
            //кол-во байтов, которые можно прочитать из буфера
            int readableBytes = buffer.readableBytes();
            //подготовка буфера для чтения
            byte[] data = new byte[readableBytes];
            //чтение данных в буфер
            buffer.readBytes(data);
            //преобразование данных в строку
            String inboundMessage = new String(data, Charset.forName("UTF-8"));
            log.info("Inbound message: {}", inboundMessage);
        } finally {
            //т.к. буфер находится вне хипа, то для очистки неспользуемой памяти нужно уменьшить кол-во
            //ссылок на выделенный кусок памяти. Когда ссылок будет 0 - память очистится
            buffer.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //закрыть контекст
        ctx.close();
        //логирование ошибки
        log.info("Error", cause);
    }
}
