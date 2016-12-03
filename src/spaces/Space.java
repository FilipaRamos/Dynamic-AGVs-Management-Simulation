package spaces;

import agents.AGVAgent;
import agents.CarryDropAgent;
import uchicago.src.sim.space.Discrete2DSpace;
import uchicago.src.sim.space.Object2DGrid;


public class Space {
    private Object2DGrid AGVSpace;
    private Object2DGrid machineSpace;
    private Object2DGrid backgroundSpace;

    /**
     * Constructor of AGV and machines spaces
     * @param xSize width of the Space
     * @param ySize height of the Space
     */
    public Space(int xSize, int ySize){
        AGVSpace = new Object2DGrid(xSize, ySize);
        machineSpace = new Object2DGrid(xSize, ySize);
        backgroundSpace = new Object2DGrid(xSize,ySize);
        for(int i = 0; i < xSize; i++){
            for(int j = 0; j < ySize; j++){
                backgroundSpace.putObjectAt(i,j,new Integer(0));
            }
        }
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
     * remove an AGV from x,y
     * @param x x coordinate
     * @param y y coordinate
     */
    private void removeAGVAt(int x, int y){
        AGVSpace.putObjectAt(x, y, null);
    }

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

    public Object2DGrid getCurrentBackgroundSpace() {
        return backgroundSpace;
    }
}
