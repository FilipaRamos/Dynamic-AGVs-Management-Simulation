package spaces;

import agents.AGVAgent;
import agents.CarryDropAgent;
import uchicago.src.sim.space.Object2DGrid;


public class Space {
    private Object2DGrid AGVSpace;
    private Object2DGrid machineSpace;

    /**
     * Constructor of AGV and machines spaces
     * @param xSize width of the Space
     * @param ySize height of the Space
     */
    public Space(int xSize, int ySize){
        AGVSpace = new Object2DGrid(xSize, ySize);
        machineSpace = new Object2DGrid(xSize, ySize);
    }

    /**
     * move an AGV
     * @param x x coordinate of AGV current position
     * @param y y coordinate of AGV current position
     * @param newX new x coordinate of AGV position
     * @param newY new y coordinate of AGV position
     * @return if the AGV can move
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
     * @return if the cell is occupied
     */
    public boolean isCellOccupied(int x, int y){
        boolean retVal = false;
        if(AGVSpace.getObjectAt(x, y)!=null)
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
     * returns the AGVagent at x, y in AGVSpace
     * @param x x coordinate
     * @param y y coordinate
     * @return AGVAgent in x,y
     */
    public AGVAgent getAGVAt(int x, int y){
        AGVAgent retVal = null;
        if(AGVSpace.getObjectAt(x, y) != null){
            retVal = (AGVAgent)AGVSpace.getObjectAt(x,y);
        }
        return retVal;
    }

    /**
     * Move an AGV from x,y to newX,newY
     * @param x x coordinate of origin
     * @param y y coordinate of origin
     * @param newX x coordinate of destination
     * @param newY y coordinate of destination
     * @return if the AGV moved or not
     */
    public boolean moveAGVAt(int x, int y, int newX, int newY){
        boolean retVal = false;
        if(!isCellOccupied(newX, newY)){
            CarryDropAgent cda = (CarryDropAgent)AGVSpace.getObjectAt(x, y);
            removeAGVAt(x,y);
            cda.setXY(newX, newY);
            AGVSpace.putObjectAt(newX, newY, cda);
            retVal = true;
        }
        return retVal;
    }

    /**
     * remove an AGV from x,y
     * @param x x coordinate
     * @param y y coordinate
     */
    private void removeAGVAt(int x, int y){
        AGVSpace.putObjectAt(x, y, null);
    }
}
