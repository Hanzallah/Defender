package Controllers;

import GameObjects.*;
import UserInterface.Menu.GameOver;
import UserInterface.Menu.HighScore;
import UserInterface.Menu.PauseMenu;
import UserInterface.MyApplication;
import UserInterface.SceneGenerator.SceneGenerator;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

import static GameObjects.AlienProjectile.ALIEN_FIRE_RATE;

public class GameEngine implements EventHandler<KeyEvent> {

    private static GameEngine gameEngine = null;

    private Timeline sceneRefresher = null;
    private Timeline levelRefresher = null;
    private Timeline powerUpRefresher = null;

    private ArrayList<PowerUp> powerUps;
    private ArrayList<Alien> aliens;
    private ArrayList<Projectile> projectiles;
    private ArrayList<Explosion> explosions;
    private ArrayList<Human> humans;
    private MotherShip motherShip;
    private LevelManager levelManager;
    private CollisionDetector collisionDetector;
    private SceneGenerator sceneGenerator;
    private boolean levelTransitionState;
    private int score = 0;

    //movement and fire
    private boolean isFire = false;
    private boolean isUp = false;
    private boolean isDown = false;
    private boolean isLeft = false;
    private boolean isRight = false;
    private int totalScore = 0;

    private GameEngine() {
        levelTransitionState = false;
        powerUps = new ArrayList<>();
        explosions = new ArrayList<>();
        motherShip = MotherShip.getInstance();
        levelManager = LevelManager.getInstance();
        collisionDetector = CollisionDetector.getInstance();
        sceneGenerator = SceneGenerator.getInstance();

        sceneGenerator.setOnKeyPressed(this);

        sceneGenerator.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                switch (e.getCode()) {
                    case SPACE:
                        isFire = false;
                        break;
                    case UP:
                        isUp = false;
                        break;
                    case DOWN:
                        isDown = false;
                        break;
                    case LEFT:
                        isLeft = false;
                        break;
                    case RIGHT:
                        isRight = false;
                        break;
                    case ESCAPE:
                        sceneRefresher.pause();
                        levelRefresher.pause();
                        MyApplication.setScene(PauseMenu.getInstance());
                        break;
                }
            }

        });

    }

    public static GameEngine getInstance() {
        if (gameEngine == null)
            gameEngine = new GameEngine();
        return gameEngine;
    }

    public void createUniverse() {
        if (levelManager.getLevel() > 0) {
            gameOver(false);
        }

        // the gameEngine part is very important do not remove
        // serious oop problem
        // for extra inquiries contact Gledis :)
        gameEngine.nextLevel();
    }

    public void refresh() {
        //counter to limit number of ship projectiles
        int count[] = new int[1];
        count[0] = 10;
        //start timer
        if (sceneRefresher == null) {
            sceneRefresher = new Timeline(new KeyFrame(
                    Duration.millis(16), e ->
                    refreshFrame(count)
            ));
            sceneRefresher.setCycleCount(Animation.INDEFINITE);
        }

        if (levelRefresher == null) {
            levelRefresher = new Timeline(new KeyFrame(
                    Duration.millis(10000), e -> {
                nextLevel();
            }
            ));
            levelRefresher.setCycleCount(Animation.INDEFINITE);
        }

        if (powerUpRefresher == null) {
            powerUpRefresher = new Timeline(new KeyFrame(
                    Duration.millis(10000), e -> {
                motherShip.setPowerUp(null);
                motherShip.setInvincible(false);
            }
            ));
            powerUpRefresher.setCycleCount(Animation.INDEFINITE);
        }
        sceneRefresher.play();
        powerUpRefresher.play();
    }


    private void dropPowerUp() {
        Random random = new Random();
        powerUps.clear();

        int option = random.nextInt(6);

        switch (option) {
            case 0:
                powerUps.add(new PowerUp(random.nextInt(600), 10, PowerUp.Type.REFILL_HEALTH));
                break;
            case 1:
                powerUps.add(new PowerUp(random.nextInt(600), 10, PowerUp.Type.SHIELD));
                break;
            case 2:
                powerUps.add(new PowerUp(random.nextInt(600), 10, PowerUp.Type.TRIPLE_SHOT));
                break;
            case 3:
                powerUps.add(new PowerUp(random.nextInt(600), 10, PowerUp.Type.EMPOWERED_SHOT));
                break;
            case 4:
                powerUps.add(new PowerUp(random.nextInt(600), 10, PowerUp.Type.EXPLOSIVE_SHOT));
                break;
            case 5:
                powerUps.add(new PowerUp(random.nextInt(600), 10, PowerUp.Type.FROST));
                break;
        }
    }

    private synchronized void refreshFrame(int[] count) {
        motherShip.move(isUp, isDown, isLeft, isRight);
        //firing issue

        if (isFire && count[0] > 7) {
            projectiles.addAll(motherShip.fire());
            count[0] = 0;
        } else
            count[0]++;

        collisionDetector.checkAllCollisions(motherShip, aliens, humans, projectiles, powerUps);

        // Game over condition
        if (!motherShip.isAlive()) {
            sceneRefresher.stop();
            gameOver(true);
            return;
        }

        if (motherShip.getPowerUp() != null) {
            switch (motherShip.getPowerUp().getType()) {
                case REFILL_HEALTH:
                    motherShip.refillHealth();
                    break;
                case SHIELD:
                    motherShip.setInvincible(true);
                    break;
            }
        }

        ArrayList<Alien> tempAliens = new ArrayList<>();
        ArrayList<Projectile> tempProjectiles = new ArrayList<>();
        ArrayList<Human> tempHumans = new ArrayList<>();
        ArrayList<PowerUp> tempPowerUps = new ArrayList<>();
        ArrayList<Explosion> tempExplosions = new ArrayList<>();
        int countDeadAliens = 0;


        //remove dead aliens
        // calculate bias
        int midScreen = MyApplication.WIDTH / 2;
        int perifScreen = (int) (0.2 * MyApplication.WIDTH);
        int centerOfMothership = motherShip.getX() + (int) motherShip.getImage().getWidth() / 2;
        double biasPerc;

        if (motherShip.getDirection() == MotherShip.moveDirection.RIGHT &&
                centerOfMothership >= midScreen)
            biasPerc = -(double) (centerOfMothership - midScreen) / (midScreen - perifScreen);
        else if (motherShip.getDirection() == MotherShip.moveDirection.LEFT &&
                centerOfMothership < midScreen)
            biasPerc = (double) (midScreen - centerOfMothership) / (midScreen - perifScreen);
        else
            biasPerc = 0;

        int bias = (int) (motherShip.getSpeed() * biasPerc);

        motherShip.applyBias(bias);

        //remove dead power-ups and move live ones
        for (PowerUp powerUp : powerUps) {
            if (powerUp.isAlive()) {
                powerUp.applyBias(bias);
                tempPowerUps.add(powerUp);
                powerUp.move();
            }
        }

        // remove the explosions that are done and scale the ones that are not
        if (explosions != null) {
            for (Explosion explosion : explosions) {
                if (explosion.isAlive()) {
                    explosion.applyBias(bias);
                    tempExplosions.add(explosion);
                }
            }
        }

        ArrayList<Alien> nearbyAliens = new ArrayList<>();

        //remove dead aliens and apply bias
        // populate nearbyAliens list
        for (Alien alien : aliens) {
            if (alien.isAlive()) {
                alien.applyBias(bias);
                tempAliens.add(alien);
                if ((motherShip.getPowerUp() == null) || (motherShip.getPowerUp().getType() != PowerUp.Type.FROST))
                    alien.move();
                if(checkNearby(alien))
                    nearbyAliens.add(alien);
            } else {
                countDeadAliens++;
                score += alien.getScore();
                totalScore += alien.getScore();
            }
        }

        for( Alien alien : nearbyAliens) {
            if( Math.random() < ALIEN_FIRE_RATE) {
                Projectile projectile2 = alien.fire();
                if (projectile2 != null)
                    tempProjectiles.add(projectile2);
            }
        }

        //remove projectile
        for (Projectile projectile : projectiles) {
            // create a new explosion
            if (projectile.getExplosive() && !projectile.isAlive() && (countDeadAliens > 0)) {
                countDeadAliens--;
                tempExplosions.add(new Explosion(projectile.getX(), projectile.getY()));
            }

        }
        //remove projectile and apply bias
        for (Projectile projectile2 : projectiles)
            if (projectile2.isAlive()) {
                projectile2.applyBias(bias);
                tempProjectiles.add(projectile2);
                if (!(projectile2 instanceof Mine))
                    projectile2.move(projectile2.getDirection());
            }

        //remove mutated humans and add mutants
        for (Human human : humans) {
            if (human.isAlive()) {
                human.applyBias(bias);
                tempHumans.add(human);

            } else {
                tempAliens.add(new Mutant(human.getX(), human.getY()));
                score -= Lander.SCORE;
                totalScore -= Lander.SCORE;
            }
        }

        if (score >= levelManager.getLevelTarget()) {
            sceneRefresher.stop();
            nextLevel();
            return;
        }

        //repopulate original lists
        aliens = tempAliens;
        humans = tempHumans;
        projectiles = tempProjectiles;
        powerUps = tempPowerUps;
        explosions = tempExplosions;

        sceneGenerator.updateMap(motherShip, aliens, humans, projectiles, score, totalScore, levelManager.getLevelTarget(),
                levelManager.getLevel(), motherShip.getHealth(), powerUps, explosions);
    }

    private boolean checkNearby(Alien alien){
        if (alien == null)
            return false;
        return -50 <= alien.getX() && alien.getX() <= MyApplication.WIDTH + 50;
    }

    private void nextLevel() {
        motherShip.resetPos();
        isFire =false;
        isUp = false;
        isDown = false;
        isRight = false;
        isLeft = false;
        score = 0;
        levelManager.incrementLevel();

        levelTransitionState = true;
        sceneGenerator.showLevelTransition(levelManager.getLevel(), levelManager.getLevelTarget());

        ArrayList<Alien> tempAliens = new ArrayList<>();
        ArrayList<Projectile> tempProjectiles = new ArrayList<>();
        ArrayList<Human> tempHumans = new ArrayList<>();

        for (int i = 0; i < levelManager.getNumOfLanders(); i++) {
            tempAliens.add(new Lander());
        }

        for (int i = 0; i < levelManager.getNumOfBaiters(); i++) {
            tempAliens.add(new Baiter());
        }

        for (int i = 0; i < levelManager.getNumOfBombers(); i++) {
            tempAliens.add(new Bomber());
        }

        for (int i = 0; i < levelManager.getNumOfHumans(); i++) {
            tempHumans.add(new Human());
        }

        System.out.println("PowerUps");
        dropPowerUp();

        aliens = tempAliens;
        humans = tempHumans;
        projectiles = tempProjectiles;
        explosions = null;
    }

    // gameFinished is false when user presses play from the pause menu
    private void gameOver(boolean gameFinished) {
        if (gameFinished)
            recordHighScore();

        MotherShip.renew();
        LevelManager.renew();
        setInstance();
        gameEngine = getInstance();

        if (gameFinished)
            MyApplication.setScene(GameOver.getInstance());
    }

    private void recordHighScore() {
        // high score
        try {
//            InputStream inputStream = getClass().getResourceAsStream("Defender/res/TextFiles/highScores.txt");
//            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            BufferedReader br = new BufferedReader(new FileReader(new File("Defender/res/TextFiles/highScores.txt")));

            ArrayList<Integer> scores = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>();
            String st;
            while ((st = br.readLine()) != null) {
                if (st.replace(" ","").length() > 0) {
                    Integer i = Integer.parseInt(st.substring(st.lastIndexOf(" ") + 1));
                    scores.add(i);
                    String string = st.substring(0, st.indexOf(" "));
                    names.add(string.replace(" ", ""));
                }
            }
            //inputStream.close();
            br.close();

            if (scores.size() == 0) {
                scores.add(totalScore);
                names.add(HighScore.getInstance(false).getUsername());
            }
            else if (scores.get(scores.size() - 1) >= totalScore && scores.size() < 10) {
                scores.add(scores.size(), totalScore);
                names.add(names.size(), HighScore.getInstance(false).getUsername());
            }
            else{
                for (int i = 0; i < scores.size() && i <= 10; i++) {
                    if (totalScore > scores.get(i)) {
                        scores.add(i, totalScore);
                        names.add(i, HighScore.getInstance(false).getUsername());
                        break;
                    }
                }
            }
            int pad = 30;
            FileWriter fr = new FileWriter("Defender/res/TextFiles/highScores.txt");
            for (int i = 0; i < scores.size() && i <= 10; i++) {
                String str = names.get(i);
                String scoreStr = scores.get(i) + "\n";
                String padded = String.format("%-" + pad + "s", str);
                scoreStr = String.format("%10s", scoreStr);
                padded += scoreStr;
                fr.write(padded);
            }
            fr.flush();
            fr.close();

            FileWriter fr1 = new FileWriter("Defender/out/production/Defender/TextFiles/highScores.txt");
            for (int i = 0; i < scores.size() && i <= 10; i++) {
                String str = names.get(i);
                String scoreStr = scores.get(i) + "\n";
                String padded = String.format("%-" + pad + "s", str);
                scoreStr = String.format("%10s", scoreStr);
                padded += scoreStr;
                fr1.write(padded);
            }
            fr1.flush();
            fr1.close();

            HighScore.setInstance();
        } catch (Exception e) {
            System.out.println("File Not Found in GameEngine");
        }
    }

    public static void setInstance() {
        gameEngine = null;
    }


    @Override
    public void handle(KeyEvent event) {
        if (levelTransitionState) {
            if (event.getCode() == KeyCode.ENTER) {
                levelTransitionState = false;
                sceneGenerator.showGame();
                gameEngine.refresh();
            }
            return;
        }

        switch (event.getCode()) {
            case SPACE:
                isFire = true;
                break;
            case UP:
                isUp = true;
                break;
            case DOWN:
                isDown = true;
                break;
            case LEFT:
                isLeft = true;
                break;
            case RIGHT:
                isRight = true;
                break;
            case ESCAPE:
                sceneRefresher.pause();
                MyApplication.setScene(PauseMenu.getInstance());
                break;
        }
    }
}
