package pland;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.apache.log4j.Logger;
import pland.gamecore.Messenger;
import pland.gamecore.PLand;
import pland.gamecore.models.Player;

import java.util.Map;

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
        PLandServer.userChannels.remove(userId);
        PLandServer.gameContext.removeUser(userId);
        broadcast(Messenger.leaveGameMessage(userId));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;

        while (in.isReadable()) {
            byte b = in.readByte();

//            System.out.print(b + "("+(char) b+")");

            if ( b != Messenger.END) {
                // Message not end
                buf.writeByte(b);
            } else {
//                System.out.println("\nMessage receive complete!");

                decodeMessage(ctx.channel());
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
                // Join Fail
                channel.writeAndFlush(Messenger.FAIL);
            } else {
                // Join Success
                this.userId = player.getId();
                PLandServer.userChannels.put(userId, channel);
                PLandServer.gameContext.getPlayerMap().put(this.userId, player);
                channel.writeAndFlush(Unpooled.copiedBuffer(Messenger
                        .joinGameMessage(this.userId).getBytes()));
                broadcast(Messenger.allUsersInfo());
            }

        } else if ( msgType >= Messenger.W_PRESSED && msgType <= Messenger.D_RELEASED ) {
            // User moving

            if (userId == -1) return;
            PLandServer.gameContext.getPlayerMap().get(userId).refreshLocation();
            PLandServer.gameContext.userMovementChange(userId, msgType);
            logger.debug("User "+userId+" moves!");

            broadcast(Messenger.userMovingMessage(userId, msgType));
            broadcastToMyViewer(Messenger.userLocationAngleUpdate(userId));

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
    private void broadcast(String message) {
        for (Channel ch : PLandServer.userChannels.values()) {
            final ChannelFuture f = ch.writeAndFlush(Unpooled.copiedBuffer(message.getBytes()));
            f.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (f.isSuccess()) {
//                        System.out.println("Write successful");
                    } else {
                        System.out.println("Error writing message to Abaca host");
                    }
                }
            });
            logger.debug(message);
        }
    }

    private void broadcastToMyViewer(String message) {
        Player current_player = PLandServer.gameContext.getPlayerMap().get(userId);
        int my_x = current_player.getLocX();
        int my_y = current_player.getLocY();

        Map<Integer, Player> users = PLandServer.gameContext.getPlayerMap();
        for (int uid : PLandServer.userChannels.keySet()) {
            if ( !users.containsKey(uid) ) continue;
            Player p = users.get(uid);
            int[] cameraRange = getCameraRange(p.getLocX(), p.getLocY());
            if ( my_x > cameraRange[0] && my_x < cameraRange[1] &&
                 my_y > cameraRange[2] && my_y < cameraRange[3]) {

                Channel ch = PLandServer.userChannels.get(uid);
                final ChannelFuture f = ch.writeAndFlush(Unpooled.copiedBuffer(message.getBytes()));
                f.addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (f.isSuccess()) {
//                            System.out.println("Write successful");
                        } else {
                            System.out.println("Error writing message to Abaca host");
                        }
                    }
                });
                logger.debug(message);

            }
        }
    }

    /**
     * Broadcast message to all the players near by (in camera)
     * @param message
     */
    private void broadcastToNeighbors(String message) {
        Player current_player = PLandServer.gameContext.getPlayerMap().get(userId);
        int x = current_player.getLocX();
        int y = current_player.getLocY();
        int[] range = getCameraRange(x, y);

        broadcastToUserInRange(message, range[0], range[1], range[2], range[3]);
    }

    /**
     * return the camera range of a player in the given location
     * @param player_x
     * @param player_y
     * @return int[]{ x_low, x_high, y_low, y_high }
     */
    private int[] getCameraRange(int player_x, int player_y) {
        int map_weight = PLandServer.gameContext.getMapInfo().getWeight();
        int map_height = PLandServer.gameContext.getMapInfo().getHeight();

        int camera_x = Math.min( Math.max(0, player_x-PLand.CAMERA_WEIGHT/2 ), map_weight - PLand.CAMERA_WEIGHT );
        int camera_y = Math.min( Math.max(0, player_y-PLand.CAMERA_HEIGHT/2 ), map_height - PLand.CAMERA_HEIGHT );
        if ( PLand.CAMERA_WEIGHT >= map_weight ) {
            camera_x = (map_weight-PLand.CAMERA_WEIGHT) / 2;
        } else if ( PLand.CAMERA_HEIGHT >= map_height ) {
            camera_y = (map_height-PLand.CAMERA_HEIGHT) / 2;
        }

        return new int[]{camera_x, PLand.CAMERA_WEIGHT, camera_y, PLand.CAMERA_HEIGHT};
    }

    /**
     * Broadcast the message to every player in a given range
     * @param message
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     */
    private void broadcastToUserInRange(String message, int x1, int x2, int y1, int y2) {
        Map<Integer, Player> users = PLandServer.gameContext.getPlayerMap();
        for (int uid : PLandServer.userChannels.keySet()) {
            if ( !users.containsKey(uid) ) continue;
            Player p = users.get(uid);
            if ( p.getLocX() > x1 && p.getLocX() < x2 && p.getLocY() > y1 && p.getLocY() < y2 ) {

                Channel ch = PLandServer.userChannels.get(uid);
                final ChannelFuture f = ch.writeAndFlush(Unpooled.copiedBuffer(message.getBytes()));
                f.addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (f.isSuccess()) {
//                            System.out.println("Write successful");
                        } else {
                            System.out.println("Error writing message to Abaca host");
                        }
                    }
                });
                logger.debug(message);

            }
        }
    }



}
