package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import jdk.dynalink.beans.StaticClass;

import javax.naming.ldap.Control;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static javafx.scene.input.KeyCode.*;

public class Main extends Application {

    static int WIDTH = 800;
    static int HEIGHT = 600;

    static AtomicInteger xWindow = new AtomicInteger(0);
    static AtomicInteger yWindow = new AtomicInteger(0);

    static int roomSizeX;
    static int roomSizeY;

    static Pane pane;

    static int shootCount = 0;
    static final int maxShots = 10;
    static int enemyShootCount = 0;
    static int enemyCount;
    static boolean fullScreen = false;

    static int lastWidth;
    static int lastHeight;

    double timer = 0.0;
    boolean pause;
    boolean inMenu;

    //creating menu contents
    BorderPane menuPane = new BorderPane();
    BorderPane menuTopPane = new BorderPane();
    Label menuTop = new Label("Menu");
    Button endButton = new Button("Spiel beenden");

    static List<Bullet> bulletList;
    static List<Bullet> enemyBulletList;

    static List<Enemy> enemyList;
    static List<Obstacle> obstacleList;

    static List<Image> backgroundList = new CopyOnWriteArrayList<>();

    static Isaac isaac;
    static AtomicInteger xVelocity = new AtomicInteger(0);
    static AtomicInteger yVelocity = new AtomicInteger(0);

    static int[][] rooms = new int[8][8];
    static int roomPosX = 4;
    static int roomPosY = 4;
    static int floorWidth = rooms.length;
    static int floorHeight = rooms[0].length;


    static boolean dead = false;

    //Movement movement = new Movement();
    ExecutorService executor = Executors.newSingleThreadExecutor(); //needed through the entire game, so a shutdown is not necessary
    Runnable moving = () -> {
        while (true){
            isaac.move();
            if(xVelocity.get() < WIDTH / 800 && xVelocity.get() > -(WIDTH / 800)) xVelocity.set(0);
            else if(xVelocity.get() > 0) xVelocity.getAndUpdate(p -> xVelocity.get() - (WIDTH / 800));
            else if(xVelocity.get() < 0) xVelocity.getAndUpdate(p -> xVelocity.get() + (WIDTH / 800));

            if(yVelocity.get() < HEIGHT / 600 && yVelocity.get() > -(HEIGHT / 600)) yVelocity.set(0);
            else if(yVelocity.get() > 0) yVelocity.getAndUpdate(p -> yVelocity.get() - (HEIGHT / 600));
            else if(yVelocity.get() < 0) yVelocity.getAndUpdate(p -> yVelocity.get() + (HEIGHT / 600));

            try {
                Thread.sleep(50);
            }catch (InterruptedException e){System.out.println("movement got interrupted");}
        }
    };

    public static GraphicsContext gc;

    @Override
    public void start(Stage stage) throws Exception {
        /*Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();*/
        stage.setTitle("TBOI - Renewed");
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.grayRgb(20));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        setup("start");

        pane = new Pane();
        pane.setPrefSize(WIDTH,HEIGHT);
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();

        pane.getChildren().add(canvas);
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        WIDTH = (int) pane.getWidth();
        HEIGHT = (int) pane.getHeight();
        lastWidth = WIDTH;
        lastHeight = HEIGHT;

        executor.execute(moving);

        //creating map
        for (int i = 0; i < rooms.length; i++){
            for (int j = 0; j < rooms[i].length; j++)
                rooms[i][j] = 0;
        }
        rooms[roomPosX][roomPosY] = 1;

        //setting the menu objects
        menuPane.setPrefSize(WIDTH, HEIGHT);
        menuTopPane.setPrefSize(WIDTH, HEIGHT / 30);
        menuTopPane.setCenter(menuTop);
        menuPane.setTop(menuTopPane);
        menuPane.setCenter(endButton);
        menuTop.setFont(Font.font((WIDTH+HEIGHT) / 50));
        endButton.setOnAction(key -> System.exit(0));

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(50), e -> run(gc, scene, pane, stage)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

    }

    //setup the game and the rooms
    static void setup(String dir){
        //setting the background color
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        //setting the player in the right position after going into a new room
        //Todo: room size for new Rooms
        switch (dir){
            case "up":
                isaac = new Isaac(400 * WIDTH / 800,400 * HEIGHT / 600, 50, 50, isaac.live);
                roomPosY--;
                rooms[roomPosX][roomPosY] = 1;
                break;
            case "left":
                isaac = new Isaac(700 * WIDTH / 800,300 * HEIGHT / 600, 50, 50, isaac.live);
                roomPosX--;
                rooms[roomPosX][roomPosY] = 1;
                break;
            case "right":
                isaac = new Isaac(100 * WIDTH / 800,400 * HEIGHT / 600, 50, 50, isaac.live);
                roomPosX++;
                rooms[roomPosX][roomPosY] = 1;
                break;
            case "down":
                isaac = new Isaac(400 * WIDTH / 800,100 * HEIGHT / 600, 50, 50, isaac.live);
                roomPosY++;
                rooms[roomPosX][roomPosY] = 1;
                break;
            case "start":
                //isaac = new Isaac(400 * WIDTH / 800 + xWindow,400 * HEIGHT / 600 + yWindow, 50, 50, 100);
                //isaac = new Isaac(400 * WIDTH / 800,400 * HEIGHT / 600, 50, 50, 100);
                isaac = new Isaac(WIDTH / 2,HEIGHT * 2 / 3, 50, 50, 100);
                break;
        }

        //adding background images to the list and drawing the background
        backgroundList.add(new Image("images/room2.png"));
        backgroundList.add(new Image("images/room_background1.png"));
        gc.drawImage(backgroundList.get(0), 0-xWindow.get(), 0-yWindow.get(), WIDTH, HEIGHT);
        gc.setFill(Color.RED);
        gc.fillText("Leben: " + isaac.live, WIDTH / 80, HEIGHT / 60);
        gc.fillText("Bullets: " + shootCount, WIDTH / 80, HEIGHT / 60 * 3);


        //setting up the room
        xWindow.set(0);
        yWindow.set(0);
        roomSizeX = WIDTH*2;
        roomSizeY = HEIGHT*2;
        lastWidth = 800;
        lastHeight = 600;
        Controls.resizeRoom();
        lastWidth = WIDTH;
        lastHeight = HEIGHT;

        //creating obstacle objects
        obstacleList = new CopyOnWriteArrayList<>();
        obstacleList.add(new Block(100 * WIDTH / 800, 100 * HEIGHT / 600, 50, 50));
        obstacleList.add(new Block(150 * WIDTH / 800, 100 * HEIGHT / 600, 50, 50));
        obstacleList.add(new Block(200 * WIDTH / 800, 100 * HEIGHT / 600, 50, 50));

        //repositioning and resizing of obstacle objects
        if(WIDTH != 800 || HEIGHT != 600){
            //lastWidth = 800; lastHeight = 600;
            obstacleList.forEach(Controls::reposition);
            obstacleList.forEach(Controls::resize);
        }


        shootCount = 0;
        enemyShootCount = 0;
        bulletList = new CopyOnWriteArrayList<>();
        enemyBulletList = new CopyOnWriteArrayList<>();

        //adding enemies Todo: add new enemy types
        enemyList = new CopyOnWriteArrayList<>();
        enemyCount = (int) (Math.random() * 5 + 1);
        for(int i = 1; i <= enemyCount; i++) {
            //enemyList.add(new Enemy((WIDTH-100) / i, 100, 50, 50, 50, 10));
            //enemyList.add(new Follower((WIDTH-100) / i, 100, 50, 50, 50, 10));
            enemyList.add(new Sniper((WIDTH-100) / i, 100, 50, 50, 50, 10));
        }
    }

    //method for updating the game constantly
    private void run(GraphicsContext gc, Scene scene, Pane pane, Stage stage){
        if(pause || inMenu) {
            gc.setFill(Color.BLACK);
            gc.fillText("game paused",WIDTH * 0.9, HEIGHT * 0.1);
            return;
        }
        timer += 0.1;

        WIDTH = (int) pane.getWidth();
        HEIGHT = (int) pane.getHeight();

        gc.setFill(Color.BROWN);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        //draw informations
        //gc.drawImage(backgroundList.get(0), 0-xWindow.get(), 0-yWindow.get(), WIDTH, HEIGHT);
        //gc.drawImage(backgroundList.get(0), WIDTH-xWindow.get(), 0-yWindow.get(), WIDTH, HEIGHT);
        //gc.drawImage(backgroundList.get(0), 0-xWindow.get(), HEIGHT-yWindow.get(), WIDTH, HEIGHT);
        //gc.drawImage(backgroundList.get(0), WIDTH-xWindow.get(), HEIGHT-yWindow.get(), WIDTH, HEIGHT);
        gc.drawImage(backgroundList.get(0), 0-xWindow.get(), 0-yWindow.get(), roomSizeX, roomSizeY);

        /*gc.drawImage(backgroundList.get(0), 0 + isaac.xPos.get() - WIDTH / 2, 0 + isaac.yPos.get() - HEIGHT / 2, WIDTH, HEIGHT);
        gc.drawImage(backgroundList.get(0), WIDTH + isaac.xPos.get() - WIDTH / 2, 0 + isaac.yPos.get() - HEIGHT / 2, WIDTH, HEIGHT);
        gc.drawImage(backgroundList.get(0), 0 + isaac.xPos.get() - WIDTH / 2, HEIGHT + isaac.yPos.get() - HEIGHT / 2, WIDTH, HEIGHT);
        gc.drawImage(backgroundList.get(0), WIDTH + isaac.xPos.get() - WIDTH / 2, HEIGHT + isaac.yPos.get() - HEIGHT / 2, WIDTH, HEIGHT);*/

        gc.setFill(Color.RED);
        gc.fillText("Leben: " + isaac.live, WIDTH / 80, HEIGHT / 60);
        gc.fillText("Bullets: " + shootCount, WIDTH / 80, HEIGHT / 60 * 3);



        if(dead && false) {
            gc.setFont(new Font("huge", WIDTH / 3));
            gc.fillText("Dead", WIDTH / 10, HEIGHT/3);
            gc.setFont(Font.getDefault());
        }
        //gc.fillText("Leben Gegner: " + enemyList.get(0).live, 500, 10);



        //updating player and bullets
        Controls.resize(isaac);
        if(lastWidth != WIDTH || lastHeight != HEIGHT) {
            //Controls.reposition(isaac);
            Controls.resizeRoom();
            Controls.repositionRoom();
            isaac.reposition();
            bulletList.forEach(Controls::reposition);
            enemyBulletList.forEach(Controls::reposition);
            obstacleList.forEach(Controls::reposition);
            obstacleList.forEach(Controls::resize);
        }
        obstacleList.forEach(Obstacle::draw);
        isaac.draw();

        //actions of enemies
        if(!enemyList.isEmpty()) {
            enemyList.forEach(enemy -> {
                if(lastWidth != WIDTH || lastHeight != HEIGHT)
                    Controls.reposition(enemy);
                Controls.resize(enemy);
                enemy.draw();
                if (timer >= 2) {
                    enemy.attack("shoot");
                    timer = 0;
                }
                if (timer >= 1) {
                    enemy.attack("move");
                }
            });
        }

        //setting key actions
        EventHandler<KeyEvent> keyHandler = new EventHandler<>() {
            @Override
            public void handle(KeyEvent e) {
                switch (e.getCode()){
                    case A:
                        if(e.getCode() == D){Controls.moveRightDown(isaac);}
                        //Controls.moveLeft(isaac);
                        if(xVelocity.get() >= 0) xVelocity.addAndGet(-(WIDTH / 50));
                        else xVelocity.set(-(WIDTH / 50));
                        //isaac.checkNextRoom();
                        break;
                    case D:
                        //Controls.moveRight(isaac);
                        if(xVelocity.get() <= 0) xVelocity.addAndGet((WIDTH / 50));
                        else xVelocity.set((WIDTH / 50));
                        //isaac.checkNextRoom();
                        break;
                    case W:
                        //Controls.moveUp(isaac);
                        if(yVelocity.get() >= 0) yVelocity.addAndGet(-(int)(HEIGHT / 37.5));
                        else yVelocity.set(-(int)(HEIGHT / 37.5));
                        //isaac.checkNextRoom();
                        break;
                    case S:
                        //Controls.moveDown(isaac);
                        if(yVelocity.get() <= 0) yVelocity.addAndGet((int)(HEIGHT / 37.5));
                        else yVelocity.set((int)(HEIGHT / 37.5));
                        //isaac.checkNextRoom();
                        break;
                    case SPACE:
                        xVelocity.set(0);
                        yVelocity.set(0);
                        //Controls.useItem(isaac);
                        break;
                    case LEFT:
                        Controls.shootLeft(isaac);
                        break;
                    case RIGHT:
                        Controls.shootRight(isaac);
                        break;
                    case UP:
                        Controls.shootUp(isaac);
                        break;
                    case DOWN:
                        Controls.shootDown(isaac);
                        break;
                    case ESCAPE:
                        if(inMenu) pane.getChildren().remove(menuPane);
                        else {
                            menuPane.setPrefSize(WIDTH, HEIGHT);
                            //BorderPane bp = new BorderPane();
                            //bp.setPrefSize(WIDTH, HEIGHT / 30);
                            //bp.setCenter(menuTop);
                            //menuPane.setTop(bp);
                            menuTop.setFont(Font.font((WIDTH+HEIGHT) / 50));
                            //menuTop.resize(WIDTH, HEIGHT / 30);
                            pane.getChildren().add(menuPane);
                        }

                        inMenu = !inMenu;
                        break;
                    case F:
                        stage.setFullScreen(fullScreen = !fullScreen);
                        break;
                    case R:
                        setup("start");
                        break;
                    case T:
                        System.exit(0);
                        break;
                    case P:
                        pause = !pause;
                }
            }
        };
        scene.setOnKeyPressed(keyHandler);

        scene.setOnMouseClicked(e -> {
            if (shootCount < maxShots){
                shootCount++;
                //System.out.println(e.getSceneX() + "; " + e.getSceneY() + ";    " + e.getX() + "; " + e.getY());
                isaac.shoot(new Point2D(e.getSceneX(), e.getSceneY()));
            }
        });

        //updating bullets
        bulletList.forEach(b -> {
            Controls.resize(b);
            b.move();
            b.checkHit("enemy");
            b.draw();
        });
        enemyBulletList.forEach(b -> {
            Controls.resize(b);
            b.move();
            b.checkHit("player");
            b.draw();
        });

        //updating map
        gc.setFill(Color.GHOSTWHITE);
        int size = (WIDTH / 80 + HEIGHT / 60) / 2;
        for (int i = 0; i < rooms.length; i++){
            for (int j = 0; j < rooms[i].length; j++){
                if(rooms[i][j] == 1)
                    gc.fillRect(WIDTH - WIDTH / 20 - (size + WIDTH / 800) * (6-i), HEIGHT / 20 + (size + HEIGHT / 600) * j, size, size);
            }
        }

        lastHeight = HEIGHT;
        lastWidth = WIDTH;
    }

    //superclass of moving objects
    static class Movable{
        int posX, posY, rotation, sizeW, sizeH, size;
        boolean rotate;
        Image image;

    }


    //control methods of the game
    static class Controls {

        //move in specific direction
        static void move(Movable m, int sX, int sY){
            if(checkBlocked(m, sX, sY)) {
                if(m instanceof Bullet){
                    if(((Bullet) m).type == "player" || ((Bullet) m).type == "playerT"){
                        bulletList.remove(m);
                        shootCount--;
                    }
                    else if(((Bullet) m).type == "enemy" || ((Bullet) m).type == "enemyT"){
                        enemyBulletList.remove(m);
                        enemyShootCount--;
                    }
                }
                return;
            }
            m.posX += sX;
            m.posY += sY;
        }

        static void moveRight(Movable m){
            int sX = getSpeedX(m); int sY = 0;
            if(checkBlocked(m, sX, sY)) {
                if(m instanceof Bullet){
                    if(((Bullet) m).type == "player"){
                        bulletList.remove(m);
                        shootCount--;
                    }
                    else if(((Bullet) m).type == "enemy"){
                        enemyBulletList.remove(m);
                        enemyShootCount--;
                    }
                }
                return;
            }
            if(m.posX < roomSizeX - sX - m.sizeW)
                m.posX += sX;
            rotate(m, "right");
            //gc.drawImage(playerImg, posX, posY, size,size);
        }
        static void moveRightDown(Movable m){
            int sX = getSpeedX(m), sY = getSpeedY(m);

            if(checkBlocked(m, sX, sY)) {
                if(m instanceof Bullet){
                    if(((Bullet) m).type == "player"){
                        bulletList.remove(m);
                        shootCount--;
                    }
                    else if(((Bullet) m).type == "enemy"){
                        enemyBulletList.remove(m);
                        enemyShootCount--;
                    }
                }
                return;
            }

            if(m.posX < roomSizeX - sX - m.sizeW)
                m.posX += sX;
            if(m.posY < roomSizeY - sY - m.sizeH)
                m.posY += sY;
        }
        static void moveRightUp(Movable m){
            int sX = getSpeedX(m), sY = getSpeedY(m);

            if(checkBlocked(m, sX, sY)) {
                if(m instanceof Bullet){
                    if(((Bullet) m).type == "player"){
                        bulletList.remove(m);
                        shootCount--;
                    }
                    else if(((Bullet) m).type == "enemy"){
                        enemyBulletList.remove(m);
                        enemyShootCount--;
                    }
                }
                return;
            }

            if(m.posX < roomSizeX - sX - m.sizeW)
                m.posX += sX;
            if(m.posY - sY > 0)
                m.posY -= sY;
        }
        static void moveLeft(Movable m){
            int sX = getSpeedX(m); int sY = 0;

            if(checkBlocked(m, sX, sY)) {
                if(m instanceof Bullet){
                    if(((Bullet) m).type == "player"){
                        bulletList.remove(m);
                        shootCount--;
                    }
                    else if(((Bullet) m).type == "enemy"){
                        enemyBulletList.remove(m);
                        enemyShootCount--;
                    }
                }
                return;
            }

            if(m.posX - sX > 0)
                m.posX -= sX;
            rotate(m, "left");
        }
        static void moveLeftDown(Movable m){
            int sX = getSpeedX(m), sY = getSpeedY(m);

            if(checkBlocked(m, sX, sY)) {
                if(m instanceof Bullet){
                    if(((Bullet) m).type == "player"){
                        bulletList.remove(m);
                        shootCount--;
                    }
                    else if(((Bullet) m).type == "enemy"){
                        enemyBulletList.remove(m);
                        enemyShootCount--;
                    }
                }
                return;
            }

            if(m.posX - sX > 0)
                m.posX -= sX;
            if(m.posY < roomSizeY - sY - m.sizeH)
                m.posY += sY;
        }
        static void moveLeftUp(Movable m){
            int sX = getSpeedX(m), sY = getSpeedY(m);

            if(checkBlocked(m, sX, sY)) {
                if(m instanceof Bullet){
                    if(((Bullet) m).type == "player"){
                        bulletList.remove(m);
                        shootCount--;
                    }
                    else if(((Bullet) m).type == "enemy"){
                        enemyBulletList.remove(m);
                        enemyShootCount--;
                    }
                }
                return;
            }

            if(m.posX - sX > 0)
                m.posX -= sX;
            if(m.posY - sY > 0)
                m.posY -= sY;
        }
        static void moveUp(Movable m){
            int sX = 0; int sY = getSpeedY(m);

            if(checkBlocked(m, sX, sY)) {
                if(m instanceof Bullet){
                    if(((Bullet) m).type == "player"){
                        bulletList.remove(m);
                        shootCount--;
                    }
                    else if(((Bullet) m).type == "enemy"){
                        enemyBulletList.remove(m);
                        enemyShootCount--;
                    }
                }
                return;
            }

            if(m.posY - sY > 0)
                m.posY -= sY;
            rotate(m, "up");
        }
        static void moveDown(Movable m){
            int sX = 0; int sY = getSpeedY(m);

            if(checkBlocked(m, sX, sY)) {
                if(m instanceof Bullet){
                    if(((Bullet) m).type == "player"){
                        bulletList.remove(m);
                        shootCount--;
                    }
                    else if(((Bullet) m).type == "enemy"){
                        enemyBulletList.remove(m);
                        enemyShootCount--;
                    }
                }
                return;
            }

            if(m.posY < roomSizeY - sY - m.sizeH)
                m.posY += sY;
            rotate(m, "down");
        }

        //get scaled speed for x and y
        static int getSpeedX(Movable m){
            if(m instanceof Bullet) return WIDTH / 40;
            return WIDTH / 80;
        }
        static int getSpeedY(Movable m){
            if(m instanceof Bullet) return HEIGHT / 30;
            return HEIGHT / 60;
        }

        static boolean checkBlocked(Movable m, int sX, int sY){
            /*int sX, sY;
            switch (dirX){
                case "r":
                    sX = getSpeedX(m);
                    break;
                case "l":
                    sX = -getSpeedX(m);
                    break;
                default:
                    sX = 0;
                    break;
            }
            switch (dirY){
                case "u":
                    sY = -getSpeedY(m);
                    break;
                case "d":
                    sY = getSpeedY(m);
                    break;
                default:
                    sY = 0;
                    break;
            }
            */

            int xNew = m.posX + sX;
            int yNew = m.posY + sY;
            for (Obstacle o : obstacleList) {
                if (xNew >= o.posX - m.sizeW && xNew < o.posX + o.sizeW && yNew >= o.posY - m.sizeH && yNew < o.posY + o.sizeH)
                    /*if (m instanceof Bullet){
                        if(bulletList.contains(m)) bulletList.remove(m);
                        else if(enemyBulletList.contains(m)) enemyBulletList.remove(m);
                    }*/
                    return true;
            }
            if (xNew >= isaac.xPos.get() - m.sizeW && xNew < isaac.xPos.get() + isaac.sizeW && yNew >= isaac.yPos.get() - m.sizeH && yNew < isaac.yPos.get() + isaac.sizeH && !(m instanceof Bullet && ((((Bullet) m).type == "player") || ((Bullet) m).type == "playerT"))) {
                //if (m instanceof Bullet && !(((Bullet) m).type == "player")) enemyBulletList.remove(m);
                return true;
            }
            return false;
        }

        //use item
        static void useItem(Movable i){
            //Todo
        }

        //shoot in specific direction
        static void shootLeft(Isaac i){
            if (shootCount < maxShots){
                shootCount++;
                i.shoot(7);
            }
            rotate(i, "left");
        }
        static void shootRight(Isaac i){
            if (shootCount < maxShots){
                shootCount++;
                i.shoot(3);
            }
            rotate(i, "right");
        }
        static void shootUp(Isaac i){
            if (shootCount < maxShots){
                shootCount++;
                i.shoot(1);
            }
            rotate(i, "up");
        }
        static void shootDown(Isaac i){
            if (shootCount < maxShots){
                shootCount++;
                i.shoot(5);
            }
            rotate(i, "down");
        }

        //make positions scalable
        static void reposition(Movable m){
            m.posX = m.posX * WIDTH / lastWidth;
            m.posY = m.posY * HEIGHT / lastHeight;
        }

        //make size scalable and resize objects after rotation
        static void resize(Movable m){
            int heightDiv = 1;
            int widthDiv = 1;
            if(m instanceof Isaac){
                heightDiv = 12;
                widthDiv = 16;
            }
            if(m instanceof Enemy){
                heightDiv = 12;
                widthDiv = 16;
            }
            if(m instanceof Bullet){
                heightDiv = 30;
                widthDiv = 40;
            }
            if(m instanceof Obstacle){
                heightDiv = 12;
                widthDiv = 16;
            }

            m.sizeH = HEIGHT / heightDiv;
            m.sizeW = WIDTH / widthDiv;
            if(m.rotation == 90 || m.rotation == 270){
                int s = m.sizeH;
                m.sizeH = m.sizeW;
                m.sizeW = s;
            }
        }

        //Rotate Images
        static void rotate(Movable m, String dir){
            if(!m.rotate) return;
            ImageView  iv = new ImageView(m.image);
            //if(dir == "right") iv.set;
            switch (m.rotation){
                case 0:
                    if(dir == "right") {
                        iv.setRotate(90);
                        m.rotation = 90;
                    }
                    else if(dir == "down") {
                        iv.setRotate(180);
                        m.rotation = 180;
                    }
                    else if(dir == "left") {
                        iv.setRotate(-90);
                        m.rotation = 270;
                    }
                    break;
                case 90:
                    if(dir == "up") {
                        iv.setRotate(-90);
                        m.rotation = 0;
                    }
                    else if(dir == "down") {
                        iv.setRotate(90);
                        m.rotation = 180;
                    }
                    else if(dir == "left") {
                        iv.setRotate(180);
                        m.rotation = 270;
                    }
                    break;
                case 180:
                    if(dir == "right") {
                        iv.setRotate(-90);
                        m.rotation = 90;
                    }
                    else if(dir == "left") {
                        iv.setRotate(90);
                        m.rotation = 270;
                    }
                    else if(dir == "up") {
                        iv.setRotate(90);
                        m.rotation = 0;
                    }
                    break;
                case 270:
                    if(dir == "right") {
                        iv.setRotate(180);
                        m.rotation = 90;
                    }
                    else if(dir == "down") {
                        iv.setRotate(-90);
                        m.rotation = 180;
                    }
                    else if(dir == "up") {
                        iv.setRotate(90);
                        m.rotation = 0;
                    }
                    break;
            }
            resize(m);
            m.image = iv.snapshot(new SnapshotParameters(), null);

        }

        //compare x position of enemy with position of target
        static int compX(Movable m){
            if(m.posX > isaac.xPos.get() + isaac.sizeW){      //to the right
                return -1;
            }
            if(m.posX < isaac.xPos.get()) return 1;       //to the left
            return 0;                 //equal

        }
        //compare y position of enemy with position of player
        static int compY(Movable m){
            if(m.posY > isaac.yPos.get() + isaac.sizeH){      //down
                return -1;
            }
            if(m.posY < isaac.yPos.get()) return 1;       //up
            return 0;                 //equal

        }

        static int compX(Movable m, int targetX){
            if(m.posX > targetX){      //to the right
                return -1;
            }
            if(m.posX < targetX) return 1;       //to the left
            return 0;                 //equal

        }
        static int compY(Movable m, int targetY){
            if(m.posY > targetY){      //down
                return -1;
            }
            if(m.posY < targetY) return 1;       //up
            return 0;                 //equal

        }


        static void resizeRoom(){
            roomSizeX = roomSizeX * WIDTH / lastWidth;
            roomSizeY = roomSizeY * HEIGHT / lastHeight;
        }

        //reposition of the room view point after changing the size of the window
        static void repositionRoom(){
            xWindow.set(xWindow.get() * WIDTH / lastWidth);
            yWindow.set(yWindow.get() * HEIGHT / lastHeight);
        }


        //setting the position of the window on the room // moving the part of the room you can see
        static void moveRoom(){
            if(xWindow.get() + xVelocity.get() + WIDTH < roomSizeX && xWindow.get() + xVelocity.get() > 0) {
                if(isaac.xPos.get() < xWindow.get() + WIDTH / 2 + getSpeedX(isaac) - 1 && isaac.xPos.get() > xWindow.get() + WIDTH / 2 - getSpeedX(isaac) + 1)
                    xWindow.addAndGet(xVelocity.get());
                else if(isaac.xPos.get() < xWindow.get() + WIDTH / 2 && xVelocity.get() < 0)
                    xWindow.addAndGet(xVelocity.get());
                else if(isaac.xPos.get() > xWindow.get() + WIDTH / 2 && xVelocity.get() > 0)
                    xWindow.addAndGet(xVelocity.get());
            }
            if(yWindow.get() + yVelocity.get() + HEIGHT < roomSizeY && yWindow.get() + yVelocity.get() > 0) {
                if(isaac.yPos.get() < yWindow.get() + HEIGHT / 2 + getSpeedY(isaac) - 1 && isaac.yPos.get() > yWindow.get() + HEIGHT / 2 - getSpeedY(isaac) + 1)
                    yWindow.addAndGet(yVelocity.get());
                else if(isaac.yPos.get() < yWindow.get() + HEIGHT / 2 && yVelocity.get() < 0)
                    yWindow.addAndGet(yVelocity.get());
                else if(isaac.yPos.get() > yWindow.get() + HEIGHT / 2 && yVelocity.get() > 0)
                    yWindow.addAndGet(yVelocity.get());
            }

        }
    }



    public static void main(String[] args) {
        launch(args);
    }
}
