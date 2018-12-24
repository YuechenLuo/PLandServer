package pland.gamecore;

import io.netty.buffer.ByteBuf;
import pland.PLandServer;
import pland.gamecore.models.Player;

import java.util.Map;

public class Messager {
    final public static char SPLIT          =   0x03;
    final public static char END            =   0x04;

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
    final public static char ANGLE          =   0x29;

    final public static char FAIL           =   0xFE;


    final public static int UP              =   0x0100;
    final public static int DOWN            =   0x0101;
    final public static int LEFT            =   0x0102;
    final public static int RIGHT           =   0x0103;


    /**
     * respond message to user who join the game
     * @param uid
     * @return
     */
    public static String joinGameResponse(byte uid) {
        GameContext gt = PLandServer.gameContext;
        Map<Byte, Player> pm = gt.getPlayerMap();
        Player p = pm.get(uid);
        if ( !pm.keySet().contains(uid) ) throw new IllegalArgumentException();

        return new StringBuilder()
                .append(JOIN_GAME)
                .append((char) uid)
                .append((char) gt.getMapInfo().getMapId())
                .append((char) pm.keySet().size())
                .append(p.getUsername()).append(SPLIT)
                .append(p.getLocX()).append(SPLIT)
                .append(p.getLocY()).append(SPLIT)
                .append((char) p.getAngle())
                .append(END)
                .toString();
    }


    /**
     * response message for user location(s)
     * @return
     */
    public static String userLocationResponse() {
        StringBuilder sb = new StringBuilder();
        for (Player p : PLandServer.gameContext.getPlayerMap().values()) {
            sb.append(LOCATION)
                    .append((char) p.getId())
                    .append(p.getUsername()).append(SPLIT)
                    .append(p.getLocX()).append(SPLIT)
                    .append(p.getLocY()).append(SPLIT)
                    .append((char) p.getAngle())
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
    public static String userMovingMessage(byte uid, char movingState) {
        return new StringBuilder()
                .append(movingState)
                .append((char) uid)
                .append(END)
                .toString();
    }


    public static int getNextInteger(ByteBuf buf) {
        int res = 0;
        byte b;
        while ( buf.isReadable() && (b = buf.readByte()) != Messager.SPLIT ) {
            res = res * 10 + b - 48;
        }
        return res;
    }

    public static String getNextString(ByteBuf buf) {
        StringBuilder sb = new StringBuilder();
        byte b;
        while ( buf.isReadable() && (b = buf.readByte()) != Messager.SPLIT ) {
            sb.append((char) b);
        }
        return sb.toString();
    }
}
