package sample;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import static sample.Main.*;

//Todo: make Enemy the superclass of all enemies / add new enemy types
public class Enemy extends Movable {

    int live, damage;

    Enemy(int x, int y, int sizeH, int sizeW, int live, int damage){
        posX = x;
        posY = y;
        this.sizeH = sizeH;
        this.sizeW = sizeW;
        this.live = live;
        this.damage = damage;
    }

    void draw(){
        gc.setFill(Color.VIOLET);
        size = (sizeH + sizeW) / 2;
        gc.fillOval(posX -xWindow.get(), posY-yWindow.get(), size, size);
    }

    void attack(String type, String... enemyType){
        int dir = 1;
        int dirX = Controls.compX(this);
        int dirY = Controls.compY(this);

        Outer: switch (dirX){
            case(1):
                switch (dirY){
                    case(1):
                        Controls.moveRightDown(this);
                        dir = 4;
                        break Outer;
                    case(0):
                        Controls.moveRight(this);
                        dir = 3;
                        break Outer;
                    case(-1):
                        Controls.moveRightUp(this);
                        dir = 2;
                        break Outer;
                }
            case(0):
                switch (dirY){
                    case(1):
                        if(posX < isaac.posX){ Controls.moveRightDown(this); dir = 4; }
                        else {
                            Controls.moveDown(this);
                            dir = 5;
                        }
                        break Outer;
                    case(-1):
                        if(posX < isaac.posX){ Controls.moveRightUp(this); dir = 2; }
                        else {
                            Controls.moveUp(this);
                            dir = 1;
                        }
                        break Outer;
                }
            case(-1):
                switch (dirY){
                    case(1):
                        Controls.moveLeftDown(this);
                        dir = 6;
                        break Outer;
                    case(0):
                        Controls.moveLeft(this);
                        dir = 7;
                        break Outer;
                    case(-1):
                        Controls.moveLeftUp(this);
                        dir = 8;
                        break Outer;
                }
        }

        if (type == "shoot") {
            if(this instanceof Sniper){
                Point2D target = new Point2D(isaac.xPos.get(), isaac.yPos.get());
                enemyBulletList.add(new Bullet(posX, posY, 10, damage, "enemyT", target));
            }
            else
                enemyBulletList.add(new Bullet(posX,posY, 10, dir, damage, "enemy"));
        }
        //move(dir);
    }
}

//enemy type that always follows the player
class Follower extends Enemy{

    Image image = new Image("images/enemies1.png");

    Follower(int x, int y, int sizeH, int sizeW, int live, int damage){
        super(x,y,sizeH,sizeW,live,damage);
    }

    void draw(){
        size = (sizeH + sizeW) / 2;
        gc.drawImage(image, posX - xWindow.get(), posY - yWindow.get(), size, size);
    }

}

class Sniper extends Enemy {

    Image image = new Image("images/7.png");

    Sniper(int x, int y, int sizeH, int sizeW, int live, int damage) {
        super(x,y,sizeH,sizeW,live,damage);
    }

    void draw(){
        size = (sizeH + sizeW) / 2;
        gc.drawImage(image, posX - xWindow.get(), posY - yWindow.get(), size, size);
    }

}