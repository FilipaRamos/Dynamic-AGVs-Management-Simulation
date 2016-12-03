package agents;

import spaces.CarryDropSpace;
import spaces.Space;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by danie on 03/12/2016.
 */
public class AGVAgent implements Drawable {
    private int x;
    private int y;
    private int vX;
    private int vY;
    private int maxCapacity;
    private int currentCapacity;
    private int power;
    private ArrayList requests = new ArrayList();
    private Space space;


    private static int IDNumber = 0;
    private int ID;

    /**
     * Constructor of an AGV agent
     * @param x x coordinate
     * @param y y coordinate
     * @param power ammount of moves the AGV can still do
     * @param maxCapacity maximum capacity of transport
     */
    public AGVAgent(int x,int y,int power,int maxCapacity){
        this.maxCapacity=maxCapacity;
        this.currentCapacity=0;
        this.x=x;
        this.y=y;
        this.power=power;
        setVxVy();
        IDNumber++;
        ID = IDNumber;
    }

    /**
     * Set the move vector
     */
    public AGVAgent(){
        this.maxCapacity=3;
        this.currentCapacity=0;
        this.x=-1;
        this.y=-1;
        this.power=100;
        setVxVy();
        IDNumber++;
        ID = IDNumber;
    }

    private void setVxVy(){
        vX = 0;
        vY = 0;
        while((vX == 0) && ( vY == 0)){
            vX = (int)Math.floor(Math.random() * 3) - 1;
            vY = (int)Math.floor(Math.random() * 3) - 1;
        }
    }

    /**
     * set the position coordinates of the AGV
     * @param xNew new x coordinate
     * @param yNew new y coordinate
     */
    public void setXY(int xNew, int yNew){
        this.x = xNew;
        this.y=yNew;
    }

    public void setSpace(Space space){
        this.space=space;
    }

    @Override
    public void draw(SimGraphics G) {
        G.drawFastRect(Color.PINK);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getvX() {
        return vX;
    }

    public void setvX(int vX) {
        this.vX = vX;
    }

    public int getvY() {
        return vY;
    }

    public void setvY(int vY) {
        this.vY = vY;
    }

    public ArrayList getRequests() {
        return requests;
    }

    public void setRequests(ArrayList requests) {
        this.requests = requests;
    }

    public void addRequest(Request r){
        requests.add(r);
    }

    public Space getSpace() {
        return space;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getID() {
        return ID;
    }

    /**
     * Calculate the distance between 2 points
     * @param oX starting x coordinate
     * @param oY starting y coordinate
     * @param dX destination x coordinate
     * @param dY destination y coordinate
     * @return the distance between the 2 points
     */
    private double calcDist(int oX,int oY,int dX,int dY){
        return Math.sqrt(Math.pow((dX-oX),2)+Math.pow(dY-oY,2));
    }

    public void step(){
       /* int newX = x + vX;
        int newY = y + vY;

        Object2DGrid grid = space.getCurrentAGVSpace();
        newX = (newX + grid.getSizeX()) % grid.getSizeX();
        newY = (newY + grid.getSizeY()) % grid.getSizeY();

        if(tryMove(newX, newY)){
            //money += cdSpace.takeMoneyAt(x, y);
        }
        else{
            AGVAgent cda = space.getAGVAt(newX, newY);
            if (cda!= null){
                if(money > 0){
                    cda.receiveMoney(1);
                    money--;
                }
            }
            setVxVy();
        }
        power--;*/
    }

    /**
     * try to move the AGV
     * @param newX new x coordinate
     * @param newY new y coordinate
     * @return if the AGV moved or not
     */
    private boolean makeMove(int newX, int newY){
        return space.moveAGV(x, y, newX, newY);
    }

    /**
     * charge the power of the AGV
     * @param amount ammount to be charged
     */
    public void chargePower(int amount){
        power += amount;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getCurrentCapacity() {
        return currentCapacity;
    }

    /**
     * report the state of the AGV in console
     */
    public void report(){
        System.out.println(getID() +
                " at " +
                x + ", " + y +
                " has " +
                getPower() + " power" +
                " and " +
                getCurrentCapacity() + "/"+ getMaxCapacity() +" capacity and "+getRequests().size()+" total Requests");
    }

    public void setCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    //public double calCost()

}
