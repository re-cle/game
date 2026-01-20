package sample;

import javafx.scene.paint.Color;

import static sample.Main.xWindow;
import static sample.Main.yWindow;

public abstract class Obstacle extends Main.Movable {
    //int posX, posY, sizeW, sizeH;

    Obstacle(int x, int y, int sW, int sH){
        posX = x;
        posY = y;
        sizeW = sW;
        sizeH = sH;
    }

    abstract void draw();
}

//just a block
class Block extends Obstacle{
    //int size;

    Block(int x, int y, int sW, int sH){
        super(x, y, (sW + sH) / 2, (sW + sH) / 2);
        size = (sW + sH) / 2;
    }

    public void draw(){
        //Main.gc
        Main.gc.setFill(Color.BLACK);
        Main.gc.fillRect(posX - xWindow.get(),posY - yWindow.get(),(sizeW + sizeH) / 2,(sizeW + sizeH) / 2);
    }

    //unused, maybe implemented later
    public Block getBlock(){
        return this;
    }

}
