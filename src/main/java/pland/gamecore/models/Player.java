package pland.gamecore.models;

import lombok.Getter;
import lombok.Setter;
import pland.gamecore.PLand;


public class Player {

    final static double SQRT_OF_ONE_HALF = 0.707107;

    public Player(int id, String username, MapInfo mapInfo) {
        this.id = id;
        this.username = username;
        this.speed = PLand.USER_INITIAL_MOVING_SPEED;
        this.angle = PLand.USER_INITIAL_ANGLE;
        this.mapInfo = mapInfo;
        this.last_update_timestamp = System.currentTimeMillis();
    }

    @Getter @Setter
    private int id;

    @Getter @Setter
    private String username;

    private MapInfo mapInfo;

    @Getter @Setter
    private int locX;

    @Getter @Setter
    private int locY;

    @Getter @Setter
    private double dX;

    @Getter @Setter
    private double dY;

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

    @Getter @Setter
    private long last_update_timestamp;

    public void moveUp() {
        movingUp = true;
        if ( this.dY > 0 ) {
            this.dY = -this.dY;
        } else {
            this.dY = this.dX == 0 ? -this.speed : -this.speed * SQRT_OF_ONE_HALF;
        }
    }

    public void moveDown() {
        movingDown = true;
        if ( this.dY < 0 ) {
            this.dY = -this.dY;
        } else {
            this.dY = this.dX == 0 ? this.speed : this.speed * SQRT_OF_ONE_HALF;
        }
    }

    public void moveLeft() {
        movingLeft = true;
        if ( this.dX > 0 ) {
            this.dX = -this.dX;
        } else {
            this.dX = this.dY == 0 ? -this.speed : -this.speed * SQRT_OF_ONE_HALF;
        }
    }

    public void moveRight() {
        movingRight = true;
        if ( this.dX < 0 ) {
            this.dX = -this.dX;
        } else {
            this.dX = this.dY == 0 ? this.speed : this.speed * SQRT_OF_ONE_HALF;
        }
    }

    public void stopMoveUp() {
        movingUp = false;
        if ( this.movingDown ) {
            this.dY = this.dX == 0 ? this.speed : this.speed * SQRT_OF_ONE_HALF;
        } else {
            this.dY = 0;
            this.dX = this.dX > 0 ? this.speed : this.dX < 0 ? -this.speed : 0;
        }
    }

    public void stopMoveDown() {
        movingDown = false;
        if ( this.movingUp ) {
            this.dY = this.dX == 0 ? -this.speed : -this.speed * SQRT_OF_ONE_HALF;
        } else {
            this.dY = 0;
            this.dX = this.dX > 0 ? this.speed : this.dX < 0 ? -this.speed : 0;
        }
    }

    public void stopMoveLeft() {
        movingLeft = false;
        if ( this.movingRight ) {
            this.dX = this.dY == 0 ? this.speed : this.speed * SQRT_OF_ONE_HALF;
        } else {
            this.dX = 0;
            this.dY = this.dY > 0 ? this.speed : this.dY < 0 ? -this.speed : 0;
        }
    }

    public void stopMoveRight() {
        movingRight = false;
        if ( this.movingLeft ) {
            this.dX = this.dY == 0 ? -this.speed : -this.speed * SQRT_OF_ONE_HALF;
        } else {
            this.dX = 0;
            this.dY = this.dY > 0 ? this.speed : this.dY < 0 ? -this.speed : 0;
        }
    }

    public void refreshLocation() {
        long now = System.currentTimeMillis();
        long dt = now - this.last_update_timestamp;
        System.out.print("[Player "+this.id+"] ("+this.locX+","+this.locY+") + ("+
                (this.dX * dt / 1000)+","+(this.dY * dt / 1000)+") => ");
        int newX = (int) (this.locX + this.dX * dt / 1000);
        int newY = (int) (this.locY + this.dY * dt / 1000);
        if ( this.mapInfo.canGo(newX, newY) ) {
            this.locX = newX;
            this.locY = newY;
        } else if ( this.mapInfo.canGo(newX, this.locY) ) {
            this.locX = newX;
        } else if ( this.mapInfo.canGo(this.locX, newY) ) {
            this.locY = newY;
        }
        System.out.println("("+this.locX+","+this.locY+")");
        this.last_update_timestamp = now;
    }

    public void setLocation(int x, int y, int angle) {
        this.locX = x;
        this.locY = y;
        this.angle = angle;
    }

}
