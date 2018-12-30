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
    public static String joinGameMessage(int uid) {
        GameContext gt = PLandServer.gameContext;
        Map<Integer, Player> pm = gt.getPlayerMap();
        if ( !pm.keySet().contains(uid) ) throw new IllegalArgumentException();
        Player p = pm.get(uid);

        return new StringBuilder()
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
                .toString();
    }

    public static String leaveGameMessage(int uid) {
        Map<Integer, Player> pm = PLandServer.gameContext.getPlayerMap();
        if ( !pm.containsKey(uid) ) throw new IllegalArgumentException();
        return new StringBuilder()
                .append(LEAVE_GAME)
                .append((char) (uid+10))
                .append(END).toString();
    }

    /**
     * Message to report a user's location and angle
     * @param uid
     * @return
     */
    public static String userLocationAngleUpdate(int uid) {
        Player p = PLandServer.gameContext.getPlayerMap().get(uid);
        if ( p == null ) throw new IllegalArgumentException();

        return new StringBuilder()
                .append(LOCATION)
                .append((char) (uid+10))
                .append(p.getLocX()).append(SPLIT)
                .append(p.getLocY()).append(SPLIT)
                .append((char) (p.getAngle()+10))
                .append(END).toString();
    }

    /**
     * response message with all users' information
     * @return
     */
    public static String allUsersInfo() {
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

        return sb.toString();
    }


    /**
     * Message for user's change of miving state
     * @param uid
     * @param movingState
     * @return
     */
    public static String userMovingMessage(int uid, char movingState) {
        return new StringBuilder()
                .append(movingState)
                .append((char) (uid+NUMBER_ENCODING_BIAS))
                .append(END)
                .toString();
    }

    /**
     * Message for user turn to a new angle
     * @param uid
     * @param angle
     * @return
     */
    public static String userTurningMessage(int uid, char angle) {
        return new StringBuilder()
                .append(ANGLE_UPDATE)
                .append((char) (uid+NUMBER_ENCODING_BIAS))
                .append(angle)
                .append(END)
                .toString();
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
