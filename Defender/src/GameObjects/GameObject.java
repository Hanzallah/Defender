package GameObjects;
import javafx.scene.image.ImageView;
import javafx.scene.image.*;
import javafx.scene.shape.Rectangle;

import java.io.FileInputStream;
//import javafx.scene.shape.Rectangle;

class GameObject {
    public static enum moveDirection {LEFT, RIGHT, UP, DOWN}
    private Coordinate coordinates;
    private int width, height, speed;
    private boolean alive;
    private Image image;
    private Rectangle hitbox;


    public GameObject(int x, int y) {
//        width = (int)image.getWidth();
//        height = (int)image.getHeight();
//        hitbox = new Rectangle(x, y, width, height);
        coordinates = new Coordinate(x, y);
        setSpeed(10);
        alive = true;
    }

    public GameObject(int x, int y, int speed) {
        coordinates.setX(x);
        coordinates.setY(y);
        setSpeed(speed);
        alive = true;
    }

    public GameObject() {
        coordinates = new Coordinate((int)(Math.random()) * 500, 470);
        setSpeed(10);
        alive = true;
    }

    public void setCoordinates(Coordinate coordinates) {
        this.coordinates.setX(coordinates.getX());
        this.coordinates.setY(coordinates.getY());
    }
    public void setCoordinates(int x, int y ){ this.setX(x); this.setY(y);}
    protected void getImageDimensions() {
        width = (int)image.getWidth();
        height = (int)image.getHeight();
        hitbox = new Rectangle(coordinates.getX(), coordinates.getY(), width, height);
    }

    protected void loadImage(String imageName) {
        try {
            int width = 30;
            int height = 30;
            if (this instanceof Projectile){
                width = 10;
                height = 10;
            }
            image = new Image(getClass().getClassLoader().getResource(imageName).toString(),
                    width, height, false, false);
        } catch (NullPointerException e){
            System.out.println("Resource not found on " + imageName);
        }
    }

    public Rectangle getHitbox() {
        hitbox = new Rectangle(coordinates.getX(), coordinates.getY(), width, height);
        return hitbox;
    }

    public Coordinate getCoordinates(){
        return coordinates;
    }

    public Image getImage() {
        return image;
    }

    public void kill() {
        alive = false;
    }

    public boolean isAlive(){
        return alive;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setX(int x) {
        if (hitbox != null)
            hitbox = new Rectangle(x, hitbox.getY(), width, height);
        coordinates.setX(x);
    }

    public void setY(int y) {
        if (hitbox != null)
            hitbox = new Rectangle(hitbox.getX(), y, width, height);
        coordinates.setY(y);
    }

    public int getX() {
        if (hitbox != null)
            return (int)hitbox.getX();
        return coordinates.getX();
    }

    public int getY() {
        if (hitbox != null)
            return (int)hitbox.getY();
        return coordinates.getY();
    }

    public int getSpeed() {
        return speed;
    }

    public void move(moveDirection direction) {
        switch(direction){
            case UP:
                setY(getY() - speed);
                break;
            case DOWN:
                setY(getY() + speed);
                break;
            case LEFT:
                setX(getX() + speed);
                break;
            case RIGHT:
                setX(getX() - speed);
                break;
        }

        if (this.getX() > 600) setX(20);
        if (this.getX() < 0)   setX(580);
        if (this.getY() > 500) setX(10);
        if (this.getY() < 0)   setX(480);

    }

    public void draw() {

    }

}

class Coordinate {
    private int x, y;

    public Coordinate(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }
    public int getX(){return x;}
    public int getY(){return y;}
}

