package spaces;

import agents.AGVAgent;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Space {
    private Object2DGrid AGVSpace;
    private Object2DGrid machineSpace;
    private Object2DGrid backgroundSpace;
    private Image imageTile;
    private Image imageCharging;

    public Space(int xSize, int ySize,int chargeX,int chargeY){
        try {
            imageTile = ImageIO.read(new File("src/tile.jpg"));
            imageCharging = ImageIO.read(new File("src/charge.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        AGVSpace = new Object2DGrid(xSize, ySize);
        machineSpace = new Object2DGrid(xSize, ySize);
        backgroundSpace = new Object2DGrid(xSize,ySize);
        for(int i = 0; i < xSize; i++){
            for(int j = 0; j < ySize; j++){
                if(i==chargeX && j== chargeY)
                    backgroundSpace.putObjectAt(i,j,new ChargingTile(i,j));
                else
                    backgroundSpace.putObjectAt(i,j,new Tile(i,j));
            }
        }
    }

    public class Tile implements Drawable {
        private int x,y;

        public Tile(int x,int y){
            this.x=x;
            this.y=y;
        }
        @Override
        public void draw(SimGraphics simGraphics) {
            simGraphics.drawImageToFit(imageTile);
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }
    }

    public Object2DGrid getCurrentAGVSpace() {
        return AGVSpace;
    }

    public Object2DGrid getCurrentMachineSpace() {
        return machineSpace;
    }
    public Object2DGrid getCurrentBackgroundSpace() {
        return backgroundSpace;
    }

    private class ChargingTile implements Drawable {
        private int x,y;

        public ChargingTile(int x,int y){
            this.x=x;
            this.y=y;
        }
        @Override
        public void draw(SimGraphics simGraphics) {
            simGraphics.drawImageToFit(imageCharging);
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }
    }
}
