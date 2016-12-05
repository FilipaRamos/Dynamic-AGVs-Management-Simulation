package agents;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

import java.awt.*;

public class MachineAgent implements Drawable{
    private int capacity;
    private int velocity;
    private boolean maintenance;
    private int x, y;
    private int potential;
    private int lotsProducing;

    private int processingStep;
    private int stepID;

    /**
     * Constructor of a machine agent
     * @param processID the id of the processing the machine belongs to
     * @param stepID the id inside the processID
     * @param cap processing capacity of the machine
     * @param vel processing velocity of the machine
     * @param x x coordinate
     * @param y y coordinate
     */
    public MachineAgent(int processID, int stepID, int cap, int vel, int x, int y){
        this.processingStep = processID;
        this.stepID = stepID;
        this.capacity = cap;
        this.velocity = vel;
        this.maintenance = false;
        this.x = x;
        this.y = y;
        this.lotsProducing = 0;
        this.potential = (1/cap) * (1/vel);
    }
    public MachineAgent(){
        this.processingStep
    }

    /**
     * add a lot to the machine
     * @return if the lot was added
     */
    public boolean addLot(){
        if (capacity > lotsProducing) {
            lotsProducing++;
            return true;
        }
        else
            return false;
    }

    /**
     * report the state of the machine in console
     */
    public void report(){
        System.out.println(getProcessingStep() +
                "." + getStepID() + " with capacity of " +
                capacity + ", has " + lotsProducing +
                " lots processing with velocity of " +
                velocity + ", and is at [" +
                x + "," + y + "]");
    }

    /**
     * Setup the agent
     */
    protected void setup() {
    	System.out.println(“Hello! Machine Agent “ + getAID().getName() + ” is ready.”);
    	report();
    }

    /**
     * Get and set methods
     */
    
    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getVelocity() {
        return velocity;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }

    @Override
    public void draw(SimGraphics G) {
        G.drawFastRect(Color.BLUE);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getPotential() {
        return potential;
    }

    public void setPotential(int potential) {
        this.potential = potential;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getProcessingStep() {
        return processingStep;
    }

    public void setProcessingStep(int processingStep) {
        this.processingStep = processingStep;
    }

    public int getStepID() {
        return stepID;
    }

    public void setStepID(int stepID) {
        this.stepID = stepID;
    }

    public int getLotsProducing() {
        return lotsProducing;
    }

    public void setLotsProducing(int lotsProducing) {
        this.lotsProducing = lotsProducing;
    }
}
