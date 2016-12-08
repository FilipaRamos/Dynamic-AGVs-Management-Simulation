package spaces;

import agents.AGVAgent;
import agents.CarryDropAgent;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Discrete2DSpace;
import uchicago.src.sim.space.Object2DGrid;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Space {
    private Object2DGrid AGVSpace;
    private Object2DGrid machineSpace;
    private Object2DGrid backgroundSpace;
    private Image image;

    /**
     * Constructor of AGV and machines spaces
     * @param xSize width of the Space
     * @param ySize height of the Space
     */
    public Space(int xSize, int ySize){
        try {
            image = ImageIO.read(new File("src/tile.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        AGVSpace = new Object2DGrid(xSize, ySize);
        machineSpace = new Object2DGrid(xSize, ySize);
        backgroundSpace = new Object2DGrid(xSize,ySize);
        for(int i = 0; i < xSize; i++){
            for(int j = 0; j < ySize; j++){
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
            simGraphics.drawImageToFit(image);
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

    /**
     * moves an AGV
     * @param x x coordinate of AGV current position
     * @param y y coordinate of AGV current position
     * @param newX new x coordinate of AGV position
     * @param newY new y coordinate of AGV position
     * @return true in case the movement is possible, false otherwise
     */
    public boolean moveAGV(int x, int y, int newX, int newY){
        boolean retVal = false;
        if(!isCellOccupied(newX, newY)){
            AGVAgent agv = (AGVAgent)AGVSpace.getObjectAt(x, y);
            removeAGV(x, y);
            agv.setXY(newX, newY);
            AGVSpace.putObjectAt(newX, newY, agv);
            retVal = true;
        }
        return retVal;
    }

    /**
     * verifies if cell (x, y) is occupied
     * @param x x coordinate
     * @param y y coordinate
     * @return true in case the cell is occupied, false otherwise
     */
    public boolean isCellOccupied(int x, int y){
        boolean retVal = false;
        if(AGVSpace.getObjectAt(x, y)!=null || machineSpace.getObjectAt(x,y)!=null)
            retVal = true;
        return retVal;
    }

    /**
     * remove an AGV from AGVSpace in (x,y)
     * @param x x coordinate
     * @param y y coordinate
     */
    public void removeAGV(int x, int y){
        AGVSpace.putObjectAt(x, y, null);
    }

    public Object2DGrid getCurrentAGVSpace() {
        return AGVSpace;
    }

    public Object2DGrid getCurrentMachineSpace() {
        return machineSpace;
    }
    /**
     * remove an AGV from the position (x,y)
     * @param x x coordinate
     * @param y y coordinate
     */
    private void removeAGVAt(int x, int y){
        AGVSpace.putObjectAt(x, y, null);
    }
    /**
     * adds an AGV agent to the space
     //* @param agv the agent to add
     * @return true in case the agent was successfully added, false otherwise
     */
    /*
    public boolean addAGV(AGVAgent agv) {
            boolean retVal = false;
            int count = 0;
            int countLimit = 10 * AGVSpace.getSizeX() * AGVSpace.getSizeY();

            while((retVal==false) && (count < countLimit)){
                int x = (int)(Math.random()*(AGVSpace.getSizeX()));
                int y = (int)(Math.random()*(AGVSpace.getSizeY()));
                if(isCellOccupied(x,y) == false){
                    AGVSpace.putObjectAt(x,y,agv);
                    agv.setXY(x,y);
                    agv.setSpace(this);
                    retVal = true;
                }
                count++;
            }

            return retVal;
    }
*/
    public Object2DGrid getCurrentBackgroundSpace() {
        return backgroundSpace;
    }
}
