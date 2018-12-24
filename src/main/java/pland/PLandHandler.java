package pland;


import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.log4j.Logger;
import pland.gamecore.Messager;
import pland.gamecore.models.Player;

public class PLandHandler extends ChannelInboundHandlerAdapter {
    final static Logger logger = Logger.getLogger(PLandHandler.class);

    private ByteBuf buf;
    private byte userId = -1;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        buf = ctx.alloc().buffer(64);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        buf.release();
        buf = null;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;

        while (in.isReadable()) {
            byte b = in.readByte();

            System.out.println((char) b + " received!");

            if ( b != Messager.END) {
                // Message not end
                buf.writeByte(b);
            } else {
                System.out.println("Message receive complete!");

//                decodeMessage(ctx.channel());
                ctx.channel().writeAndFlush("get!").addListener(new GenericFutureListener<Future<Object>>() {
                    public void operationComplete(Future<Object> future) {
                        // TODO: Use proper logger in production here
                        if (future.isSuccess()) {
                            System.out.println("Data written succesfully");
                        } else {
                            System.out.println("Data failed to write:");
                            future.cause().printStackTrace();
                        }
                    }
                });
                buf.release();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    /**
     * Helper to decode incoming message
     * @param channel
     */
    private void decodeMessage(Channel channel) {
        char msgType = (char) buf.readByte();

        if ( msgType == Messager.JOIN_GAME ) {
            if (userId != -1) return;

            System.out.println("<JOIN_GAME> get!");

            Player player = PLandServer.gameContext.newUserJoin(Messager.getNextString(buf));
            if ( player == null ) {
                channel.writeAndFlush(Messager.FAIL);
            } else {
                this.userId = player.getId();
                PLandServer.userChannels.put(userId, channel);
                channel.writeAndFlush(Messager.joinGameResponse(this.userId)).awaitUninterruptibly();
                broadcast(Messager.userLocationResponse());
            }
        } else if ( msgType >= Messager.W_PRESSED && msgType <= Messager.D_RELEASED ) {
            if (userId == -1) return;

            System.out.println("User "+userId+" moves!");

            broadcast(Messager.userMovingMessage(userId, msgType));
        }

    }


    /**
     * Helper to broadcast message to all users
     * @param message
     */
    private void broadcast(Object message) {
        for (Channel ch : PLandServer.userChannels.values()) {
            ch.writeAndFlush(message);
        }
    }



}
