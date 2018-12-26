package pland.gamecore;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import pland.PLandServer;
import pland.gamecore.models.Player;

import java.util.Map;

public class Messenger {
    final public static char SPLIT          =   0x03;
    final public static char END            =   0x04;
    final public static int NUMBER_ENCODING_BIAS = 10;

    final public static char JOIN_GAME      =   0x10;
    final public static char LEAVE_GAME     =   0x11;

    final public static char W_PRESSED      =   0x20;
    final public static char W_RELEASED     =   0x21;
    final public static char S_PRESSED      =   0x22;
    final public static char S_RELEASED     =   0x23;
    final public static char A_PRESSED      =   0x24;
    final public static char A_RELEASED     =   0x25;
    final public static char D_PRESSED      =   0x26;
    final public static char D_RELEASED     =   0x27;
    final public static char LOCATION       =   0x28;
    final public static char ANGLE_UPDATE   =   0x29;
    final public static char SPEED_UPDATE   =   0x2A;


    final public static char FAIL           =   0xFE;


    /**
     * respond message to user who join the game
     * @param uid
     * @return
     */
    public static ByteBuf joinGameResponse(int uid) {
        GameContext gt = PLandServer.gameContext;
        Map<Integer, Player> pm = gt.getPlayerMap();
        if ( !pm.keySet().contains(uid) ) throw new IllegalArgumentException();
        Player p = pm.get(uid);

        return Unpooled.copiedBuffer(
                new StringBuilder()
                .append(JOIN_GAME)
                .append((char) (uid+NUMBER_ENCODING_BIAS))
                .append((char) (gt.getMapInfo().getMapId()+NUMBER_ENCODING_BIAS))
                .append((char) (pm.keySet().size()+NUMBER_ENCODING_BIAS))
                .append(p.getUsername()).append(SPLIT)
                .append(p.getLocX()).append(SPLIT)
                .append(p.getLocY()).append(SPLIT)
                .append((char) (p.getAngle()+NUMBER_ENCODING_BIAS))
                .append((char) (p.getSpeed()+NUMBER_ENCODING_BIAS))
                .append(END)
                .toString().getBytes());
    }


    /**
     * response message with all users' information
     * @return
     */
    public static ByteBuf allUsersInfo() {
        StringBuilder sb = new StringBuilder();
        for (Player p : PLandServer.gameContext.getPlayerMap().values()) {
            sb.append(LOCATION)
                    .append((char) (p.getId()+NUMBER_ENCODING_BIAS))
                    .append(p.getLocX()).append(SPLIT)
                    .append(p.getLocY()).append(SPLIT)
                    .append((char) (p.getAngle()+NUMBER_ENCODING_BIAS))
                    .append(p.getUsername()).append(SPLIT)
                    .append((char) (p.getSpeed()+NUMBER_ENCODING_BIAS))
                    .append(END);
        }

        return Unpooled.copiedBuffer(sb.toString().getBytes());
    }


    /**
     * Message for user's change of miving state
     * @param uid
     * @param movingState
     * @return
     */
    public static ByteBuf userMovingMessage(int uid, char movingState) {
        return Unpooled.copiedBuffer(
                new StringBuilder()
                .append(movingState)
                .append((char) (uid+NUMBER_ENCODING_BIAS))
                .append(END)
                .toString().getBytes());
    }


    public static ByteBuf userTurningMessage(int uid, char angle) {
        return Unpooled.copiedBuffer(
                new StringBuilder()
                .append(ANGLE_UPDATE)
                .append((char) (uid+NUMBER_ENCODING_BIAS))
                .append(angle)
                .append(END)
                .toString().getBytes()
        );
    }


    public static int getNextInteger(ByteBuf buf) {
        int res = 0;
        byte b;
        while ( buf.isReadable() && (b = buf.readByte()) != Messenger.SPLIT ) {
            res = res * 10 + b - 48;
        }
        return res;
    }

    public static String getNextString(ByteBuf buf) {
        StringBuilder sb = new StringBuilder();
        byte b;
        while ( buf.isReadable() && (b = buf.readByte()) != Messenger.SPLIT ) {
            sb.append((char) b);
        }
        return sb.toString();
    }

}
