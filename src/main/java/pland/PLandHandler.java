package pland;


import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.apache.log4j.Logger;
import pland.gamecore.Messenger;
import pland.gamecore.models.Player;

public class PLandHandler extends ChannelInboundHandlerAdapter {
    final static Logger logger = Logger.getLogger(PLandHandler.class);

    private ByteBuf buf;
    private int userId = -1;

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

            System.out.print(b + "("+(char) b+")");

            if ( b != Messenger.END) {
                // Message not end
                buf.writeByte(b);
            } else {
                System.out.println("\nMessage receive complete!");

                decodeMessage(ctx.channel());
//                ctx.channel().writeAndFlush(Unpooled.copiedBuffer("get!".getBytes()));
//                ctx.channel().writeAndFlush(buf);
                buf.clear();
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

        if ( msgType == Messenger.JOIN_GAME ) {
            // User join game
            if (userId != -1) return;
            logger.debug("<JOIN_GAME> get!");

            Player player = PLandServer.gameContext.newUserJoin(Messenger.getNextString(buf));
            if ( player == null ) {
                // Success
                channel.writeAndFlush(Messenger.FAIL);
            } else {
                // Fail
                this.userId = player.getId();
                PLandServer.userChannels.put(userId, channel);
                PLandServer.gameContext.getPlayerMap().put(this.userId, player);
                channel.writeAndFlush(Messenger.joinGameResponse(this.userId));
                broadcast(Messenger.allUsersInfo());
            }
        } else if ( msgType >= Messenger.W_PRESSED && msgType <= Messenger.D_RELEASED ) {
            // User moving
            if (userId == -1) return;
            logger.debug("User "+userId+" moves!");

            broadcast(Messenger.userMovingMessage(userId, msgType));
        } else if (msgType == Messenger.ANGLE_UPDATE) {
            // User turn to another angle
            if (userId == -1) return;
            logger.debug("User "+userId+" turns!");

            char angle_chr = (char) buf.readByte();
            int angle = (angle_chr - 10) * 2;
            PLandServer.gameContext.getPlayerMap().get(userId).setAngle(angle);

            broadcast(Messenger.userTurningMessage(userId, angle_chr));
        }

    }


    /**
     * Helper to broadcast message to all users
     * @param message
     */
    private void broadcast(Object message) {
        for (Channel ch : PLandServer.userChannels.values()) {
            final ChannelFuture f = ch.writeAndFlush(message);
            f.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (f.isSuccess()) {
                        System.out.println("Write successful");
                    } else {
                        System.out.println("Error writing message to Abaca host");
                    }
                }
            });
            logger.info(message);
        }
    }



}
