package pland.gamecore.models;

import lombok.Getter;
import lombok.Setter;
import pland.gamecore.PLand;


public class Player {

    public Player(int id, String username) {
        this.id = id;
        this.username = username;
        this.speed = PLand.USER_INITIAL_MOVING_SPEED;
        this.angle = PLand.USER_INITIAL_ANGLE;
    }

    @Getter @Setter
    private int id;

    @Getter @Setter
    private String username;

    @Getter @Setter
    private int locX;

    @Getter @Setter
    private int locY;

    @Getter @Setter
    private int angle;

    @Setter @Getter
    private int speed;

    @Getter @Setter
    private boolean movingUp;

    @Getter @Setter
    private boolean movingDown;

    @Getter @Setter
    private boolean movingLeft;

    @Getter @Setter
    private boolean movingRight;



}
