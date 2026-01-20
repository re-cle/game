package sample;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import static sample.Main.*;

//bullets
public class Bullet extends Movable {

    int direction, damage, size, lastSX, lastSY;
    String type;
    Color color;
    Point2D target;

    Bullet(int x, int y, int s, int d, int schaden, String t){
        posX = x+15;
        posY = y+15;
        sizeW = s;
        sizeH = s;
        direction = d;
        damage = schaden;
        type = t;
        if(t == "enemy") color = Color.RED;
        else if(t == "player") color = Color.GRAY;
    }

    Bullet(int x, int y, int s, int schaden, String t, Point2D point){
        posX = x+15;
        posY = y+15;
        sizeW = s;
        sizeH = s;
        damage = schaden;
        type = t;
        target = point;
        if(t == "enemyT") color = Color.DARKRED;
        else if(t == "playerT") color = Color.BLACK;

    }

    //moving the bullet
    void move(){
        int sX, sY;
        if(type != "playerT" && type != "enemyT") {
            sX = Controls.getSpeedX(this);
            sY = Controls.getSpeedY(this);
            switch (direction) {
                case 1:     //UP
                    //posY -= 2 * Main.Controls.getSpeedY(isaac);
                    Controls.moveUp(this);
                    if (!(posY - sX > 0)) {
                        if (type == "player") {
                            bulletList.remove(this);
                            shootCount--;
                        } else {
                            enemyBulletList.remove(this);
                            enemyShootCount--;
                        }
                    }
                    break;
                case 2:     //UP-RIGHT
                    //posY -= 2 * Main.Controls.getSpeedY(isaac);
                    //posX += 2 * Main.Controls.getSpeedX(isaac);
                    Controls.moveRightUp(this);
                    if (!((posX < roomSizeX - sX - sizeW) && posY - sY > 0)) {
                        if (type == "player") {
                            bulletList.remove(this);
                            shootCount--;
                        } else {
                            enemyBulletList.remove(this);
                            enemyShootCount--;
                        }
                    }
                    break;
                case 3:     //RIGHT
                    //posX += 2 * Main.Controls.getSpeedX(isaac);
                    Controls.moveRight(this);
                    if (!(posX < roomSizeX - sX - sizeW)) {
                        if (type == "player") {
                            bulletList.remove(this);
                            shootCount--;
                        } else {
                            enemyBulletList.remove(this);
                            enemyShootCount--;
                        }
                    }
                    break;
                case 4:     //RIGHT-DOWN
                    //posX += 2 * Main.Controls.getSpeedX(isaac);
                    //posY += 2 * Main.Controls.getSpeedY(isaac);
                    Controls.moveRightDown(this);
                    if (!((posX < roomSizeX - sX - sizeW) && posY < roomSizeY - sY - sizeH)) {
                        if (type == "player") {
                            bulletList.remove(this);
                            shootCount--;
                        } else {
                            enemyBulletList.remove(this);
                            enemyShootCount--;
                        }
                    }
                    break;
                case 5:     //DOWN
                    //posY += 2 * Main.Controls.getSpeedY(isaac);
                    Controls.moveDown(this);
                    if (!(posY < roomSizeY - sY - sizeH)) {
                        if (type == "player") {
                            bulletList.remove(this);
                            shootCount--;
                        } else {
                            enemyBulletList.remove(this);
                            enemyShootCount--;
                        }
                    }
                    break;
                case 6:     //DOWN-LEFT
                    //posY += 2 * Main.Controls.getSpeedY(isaac);
                    //posX -= 2 * Main.Controls.getSpeedX(isaac);
                    Controls.moveLeftDown(this);
                    if (!(posX - sX > 0 && (posY < roomSizeY - sY - sizeH))) {
                        if (type == "player") {
                            bulletList.remove(this);
                            shootCount--;
                        } else {
                            enemyBulletList.remove(this);
                            enemyShootCount--;
                        }
                    }
                    break;
                case 7:     //LEFT
                    //posX -= 2 * Main.Controls.getSpeedX(isaac);
                    Controls.moveLeft(this);
                    if (!(posX - sX > 0)) {
                        if (type == "player") {
                            bulletList.remove(this);
                            shootCount--;
                        } else {
                            enemyBulletList.remove(this);
                            enemyShootCount--;
                        }
                    }
                    break;
                case 8:     //LEFT-UP
                    //posX -= 2 * Main.Controls.getSpeedX(isaac);
                    //posY -= 2 * Main.Controls.getSpeedY(isaac);
                    Controls.moveLeftUp(this);
                    if (!(posX - sX > 0 && posY - sX > 0)) {
                        if (type == "player") {
                            bulletList.remove(this);
                            shootCount--;
                        } else {
                            enemyBulletList.remove(this);
                            enemyShootCount--;
                        }
                    }
                    break;
            }
        }
        else {
            direction = getDirection();

            if(lastSX != 0 && lastSY != 0) {
                //sX = lastSX;
                //sY = lastSY;
                Controls.move(this, lastSX, lastSY);
            }
            else {
                int xDistance = -(posX - (int) target.getX());
                int yDistance = -(posY - (int) target.getY());
                int speed = (WIDTH / 40 + HEIGHT / 30) / 2;

                double zDistance = Math.sqrt(Math.pow(xDistance, 2) + Math.pow(yDistance, 2));
                double duration = zDistance / speed;
                sX = (int) (xDistance / duration);
                sY = (int) (yDistance / duration);

                Controls.move(this, sX, sY);

                lastSX = sX;
                lastSY = sY;

                //Deprecated, tests if the bullet passed its target and has turned around
                if ((sX < 0 & lastSX > 0) || (sX > 0 & lastSX < 0) || (sY < 0 & lastSY > 0) || (sY > 0 & lastSY < 0)) {
                    if(type == "playerT") {
                        bulletList.remove(this);
                        shootCount--;
                    }
                    else if(type == "enemyT") {
                        enemyBulletList.remove(this);
                    }
                }
            }


        }

        if(posX <= 0 || posX >= roomSizeX || posY <= 0 || posY >= roomSizeY ) {
            if(type == "player" || type == "playerT") {
                bulletList.remove(this);
                shootCount--;
            }
            else if(type == "enemy" || type == "enemyT"){
                enemyBulletList.remove(this);
                enemyShootCount--;
            }
        }
    }

    //check if a target got hit
    void checkHit(String target){
        if(target == "player"){
            if(posX >= isaac.posX && posX <= isaac.posX + isaac.sizeW && posY >= isaac.posY && posY < isaac.posY + isaac.sizeH) {
                isaac.live -= damage;
                enemyBulletList.remove(this);
                if(isaac.live <= 0) dead = true;
            }
        }
        else if(target == "enemy"){
            enemyList.forEach(enemy1 -> {
                //if(enemyList.isEmpty()) return;
                if(posX >= enemy1.posX && posX < enemy1.posX + enemy1.sizeW && posY >= enemy1.posY && posY < enemy1.posY + enemy1.sizeH) {
                    enemy1.live -= damage;
                    bulletList.remove(this);
                    shootCount--;
                }
                if(enemy1.live <= 0) {
                    enemyList.remove(enemy1);
                    enemyCount--;
                    //if (enemyList.isEmpty()) return;
                }
            });

                /*
                if(posX >= enemy.posX && posX < enemy.posX + 50 && posY >= enemy.posY && posY < enemy.posY +50) {
                    enemy.live -= damage;
                    bulletList.remove(this);
                    shootCount--;
                }

                 */
        }
    }

    void draw(){
        size = (int) ((sizeH + sizeW) / 2.5);
        gc.setFill(color);
        gc.fillOval(posX - xWindow.get(),posY - yWindow.get(),size,size);
        //gc.fillOval(posX,posY,size,size);
    }

    //get the direction to move in
    int getDirection(){
        int dirX = Controls.compX(this, (int) target.getX());
        int dirY = Controls.compY(this, (int) target.getY());
        int dir;
        Outer: switch (dirX){
            case(1):
                switch (dirY){
                    case(1):
                        return 4;
                    case(0):
                        return 3;
                    case(-1):
                        return 2;
                }
            case(0):
                switch (dirY){
                    case(1):
                        if(posX < isaac.posX){ Controls.moveRightDown(this); dir = 4; }
                        else {
                            return 5;
                        }
                        break Outer;
                    case(-1):
                        if(posX < isaac.posX){ Controls.moveRightUp(this); dir = 2; }
                        else {
                            return 1;
                        }
                        break Outer;
                }
            case(-1):
                switch (dirY){
                    case(1):
                        return 6;
                    case(0):
                        return 7;
                    case(-1):
                        return 8;
                }
        }
        return -1;
    }
}
