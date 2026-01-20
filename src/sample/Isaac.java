package sample;

//import com.sun.javafx.scene.paint.GradientUtils;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import java.util.concurrent.atomic.AtomicInteger;
import static sample.Main.*;

//player
public class Isaac extends Movable {

    //int sizeH, sizeW;
    int live, damage = 10;
    AtomicInteger xPos = new AtomicInteger();
    AtomicInteger yPos = new AtomicInteger();
    //image = new Image("images/player3.png");
    //ImageView pI = new ImageView(image);

    public Isaac(int x, int y, int sH, int sW, int l){
        posX = x;
        posY = y;
        xPos.set(x);
        yPos.set(y);
        sizeH = sH;
        sizeW = sW;
        live = l;
        //image = new Image("images/player3.png");
        image = new Image("images/isaac.png");
        rotate = false;
    }

    //draws Isaac to the graphic content
    void draw(){
        gc.drawImage(image, xPos.get() - xWindow.get(), yPos.get() - yWindow.get(), sizeH, sizeW);
    }

    //shoot into direction, starts at players position
    void shoot(int direction){
        bulletList.add(new Bullet(xPos.get(),yPos.get(),20, direction, damage, "player"));
    }
    void shoot(Point2D target){
        bulletList.add(new Bullet(xPos.get(),yPos.get(),20, damage, "playerT", target));
    }

    void checkNextRoom(){
        if(!enemyList.isEmpty()) return;

        if(xPos.get() <= roomSizeX / 60 && yPos.get() < roomSizeY / 1.5 && yPos.get() > roomSizeY / 2.5 && roomPosX > 0)
            nextRoom("left");
        else if(xPos.get() >= roomSizeX - sizeW - roomSizeX / 50 && yPos.get() < roomSizeY / 1.5 && yPos.get() > roomSizeY / 2.5 && roomPosX < floorWidth - 1)
            nextRoom("right");
        else if(xPos.get() < roomSizeX / 2 && xPos.get() > roomSizeX / 2.5 && yPos.get() <= roomSizeY / 60 && roomPosY > 0)
            nextRoom("up");
        else if(xPos.get() < roomSizeX / 2 && xPos.get() > roomSizeX / 2.5 && yPos.get() >= roomSizeY - sizeH - roomSizeY / 60 && roomPosY < floorHeight - 1)
            nextRoom("down");

    }

    void nextRoom(String dir){
        setup(dir);
    }
    //moving the player
    void move(){
        if(checkBlocked("xy")) {
            xVelocity.set(0);
            yVelocity.set(0);
            return;
        }
        if((xVelocity.get() > 0 && xPos.get() < roomSizeX - xVelocity.get() - sizeW) || (xVelocity.get() < 0 && xPos.get() >= -xVelocity.get())) {
            xPos.addAndGet(xVelocity.get());
        }
        if((yVelocity.get() > 0 && yPos.get() < roomSizeY - yVelocity.get() - sizeH) || (yVelocity.get() < 0 && yPos.get() > -yVelocity.get())) {
            yPos.addAndGet(yVelocity.get());
        }
        Controls.moveRoom();
        checkNextRoom();
    }

    //unused, maybe implemented later
    int roomVelocityX(){
        /*if(xPos.get() > xWindow + WIDTH / 2){
            return xVelocity.get() * 5 + xPos.get() - (xWindow + WIDTH / 2);
            //if(xVelocity.get() > 0) return xVelocity.get() * 5 + xPos.get() - (xWindow + WIDTH / 2);
            //else return xPos.get() - (xWindow + WIDTH / 2);
        }
        else if(xPos.get() < xWindow + WIDTH / 2){
            if(xVelocity.get() < 0) return xPos.get() + ((xWindow + WIDTH) / 2);
            else return xVelocity.get() - xPos.get() + ((xWindow + WIDTH) / 2);
        }
        return xVelocity.get();*/
        return 1;
    }

    //repositioning player
    void reposition(){
        xPos.set(xPos.get() * WIDTH / lastWidth);
        yPos.set(yPos.get() * HEIGHT / lastHeight);
    }

    boolean checkBlocked(String dir){
        /*AtomicInteger xNew = new AtomicInteger(xPos.get());
        AtomicInteger yNew = new AtomicInteger(yPos.get());
        xNew.addAndGet(xVelocity.get());
        yNew.addAndGet(yVelocity.get());*/
        int xNew = xPos.get() + xVelocity.get();
        int yNew = yPos.get() + yVelocity.get();

        for (Obstacle o : obstacleList) {
            if (xNew >= o.posX - isaac.sizeW && xNew < o.posX + o.sizeW && yNew >= o.posY - isaac.sizeH && yNew < o.posY + o.sizeH)
                return true;
        }
        for (Enemy o : enemyList) {
            if (xNew >= o.posX - isaac.sizeW && xNew < o.posX + o.sizeW && yNew >= o.posY - isaac.sizeH && yNew < o.posY + o.sizeH)
                return true;
        }
        return false;
    }

}
