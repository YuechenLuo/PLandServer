package pland.gamecore.models;

import lombok.Getter;
import lombok.Setter;
import pland.gamecore.PLand;


public class Player {

    public Player(byte id, String username) {
        this.id = id;
        this.username = username;
        this.speed = PLand.USER_INITIAL_MOVING_SPEED;
    }

    @Getter @Setter
    private byte id;

    @Getter @Setter
    private String username;

    @Getter @Setter
    private int locX;

    @Getter @Setter
    private int locY;

    @Getter @Setter
    private byte angle;

    @Setter @Getter
    private byte speed;

    @Getter @Setter
    private boolean movingUp;

    @Getter @Setter
    private boolean movingDown;

    @Getter @Setter
    private boolean movingLeft;

    @Getter @Setter
    private boolean movingRight;



}
