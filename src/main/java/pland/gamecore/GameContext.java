package pland.gamecore;

import lombok.Getter;
import pland.PLandServer;
import pland.gamecore.models.MapInfo;
import pland.gamecore.models.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static pland.gamecore.PLand.MAX_NUMBER_OF_LOCATE;

public class GameContext {

    @Getter
    private Map<Integer, Player> playerMap;
    @Getter
    private MapInfo mapInfo;

    public GameContext() {
        playerMap = new HashMap<Integer, Player>();
    }


    /**
     * Load map to the game
     */
    public void loadMap() {
        // TODO: load from file
        mapInfo = new MapInfo();
        mapInfo.setMapId((byte) 0);
        mapInfo.setHeight(200);
        mapInfo.setWeight(200);
    }

    /**
     * New user join into game
     * @param username
     * @return the player added or null for fail
     */
    public Player newUserJoin(String username) {
        Set<Integer> idSet = PLandServer.userChannels.keySet();
        if (idSet.size() > PLand.MAX_USER_NUM) {
            return null;
        }
        int id = 0;
        for (;idSet.contains(id);id++);
        // TODO: unique username
        Player player = new Player(id, username, this.mapInfo);

        if (setRandomUserLocation(player)) {
            this.getPlayerMap().put(id, player);
            return player;
        }
        return null;
    }


    /**
     * Remove a user
     * @param uid
     */
    public void removeUser(int uid) {
        this.playerMap.remove(uid);
    }


    /**
     * Assign a random location to given player without colliding with other players
     * @param player
     * @return
     */
    private boolean setRandomUserLocation(Player player) {
        Random random = new Random();
        int trial = 0;

        while (true) {
            int x = random.nextInt(mapInfo.getWeight() - PLand.USER_SIZE/2) + PLand.USER_SIZE/2;
            int y = random.nextInt(mapInfo.getHeight() - PLand.USER_SIZE/2) + PLand.USER_SIZE/2;
            boolean canUse = true;
            for (Player p : playerMap.values()) {
                if (p != player
                         && Math.sqrt((p.getLocX()-x)*(p.getLocX()-x)
                                    + (p.getLocY()-y)*(p.getLocY()-y)) < PLand.USER_SIZE) {
                    canUse = false;
                }
            }

            if (canUse) {
                player.setLocX(x);
                player.setLocY(y);
                player.setAngle((byte) 0);
                return true;
            }
            if ( trial++ >= MAX_NUMBER_OF_LOCATE ) {
                return false;
            }
        }
    }

    /**
     * change a user's movement state
     * @param uid
     * @param direction
     */
    public void userMovementChange(int uid, char direction) {
        Player player = this.playerMap.get(uid);
        if ( player == null ) throw new IllegalArgumentException();

        switch (direction) {
            case Messenger.W_PRESSED:
                player.moveUp();
                break;
            case Messenger.W_RELEASED:
                player.stopMoveUp();
                break;
            case Messenger.S_PRESSED:
                player.moveDown();
                break;
            case Messenger.S_RELEASED:
                player.stopMoveDown();
                break;
            case Messenger.A_PRESSED:
                player.moveLeft();
                break;
            case Messenger.A_RELEASED:
                player.stopMoveLeft();
                break;
            case Messenger.D_PRESSED:
                player.moveRight();
                break;
            case Messenger.D_RELEASED:
                player.stopMoveRight();
                break;
        }
    }

}
