package GameObjects;

public class MotherShip extends GameObject{

    private static MotherShip motherShip = null;

    private int health;
    private int score;


    private moveDirection direction;

    private MotherShip(){
        super(300, 250);
        health = 100;
        score = 0;
        direction = moveDirection.RIGHT;

        loadImage("Icons/001-rocket.png");
        getImageDimensions();
    }
    public void updateScore(int score){
        this.score+= score;
    }

    public void updateHealth(int damage) {
        health  = health - damage;
    }

    public moveDirection getDirection() {
        return direction;
    }

    public int getHealth(){
        return health;
    }

    public static MotherShip getInstance() {
        if (motherShip == null)
            motherShip = new MotherShip();
        return motherShip;
    }

    public void move(moveDirection d){
        switch(d){
            case UP:
                setY(getY() - this.getSpeed());
                break;
            case DOWN:
                setY(getY() + this.getSpeed());
                break;
            case LEFT:
                setX(getX() - this.getSpeed());
                direction = moveDirection.LEFT;
                break;
            case RIGHT:
                setX(getX() + this.getSpeed());
                direction = moveDirection.RIGHT;
                break;
        }

        if (this.getX() > 600) setX(20);
        if (this.getX() < 0)   setX(580);
        if (this.getY() > 500) setY(10);
        if (this.getY() < 0)   setY(480);

    }

    public Projectile fire(){
        int x = this.getCoordinates().getX();
        int y = this.getCoordinates().getY();
        Coordinate ship_Coordinate = new Coordinate(x,y);
        moveDirection newDirection = this.getDirection();
        return new ShipProjectile(ship_Coordinate, newDirection);
    }
}
