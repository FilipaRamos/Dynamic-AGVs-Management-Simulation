package agents;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sajas.core.Agent;
import sajas.domain.DFService;
import sajas.proto.ContractNetResponder;
import spaces.Space;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

import java.awt.*;
import java.util.ArrayList;
import java.util.PriorityQueue;

/**
 * Created by danie on 03/12/2016.
 */
public class AGVAgent extends Agent implements Drawable {

    // AGV variables
    private int x;
    private int y;
    private int vX;
    private int vY;
    private int maxCapacity;
    private int currentCapacity;
    private int power;

    // queue of requests
    private PriorityQueue<String> requests = new PriorityQueue();
    private ArrayList<MachineLocation> machinesLocations;
    private int energyLeft;
    private int totalDistance;
    private int rechargeDistance;

    // space of the simulation
    private Space space;
    private ArrayList<AID> machines;

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

        energyLeft = 0;
        totalDistance = 0;
        rechargeDistance = 0;
        machinesLocations = new ArrayList();
    }

    /**
     * Set the move vector
     */
    public AGVAgent(int power){
        this.maxCapacity=3;
        this.currentCapacity=0;
        this.x=-1;
        this.y=-1;
        this.power=power;
        setVxVy();
        IDNumber++;
        ID = IDNumber;
    }

    /**
     * Setup the agent
     */
    public void setup() {

        // register provider at DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addProtocols(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName() + "-agv");
        sd.setType("agv");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            System.err.println(e.getMessage());
        }

        // behaviours
        setupBehaviours();

    }

    /**
     * Setup all the behaviours for the agent
     */
    protected void setupBehaviours() {



    }

    /**
     * Updates the energy that's left
     * @param newStep the step that is being evaluated to be added to the queue
     */
    protected void updateLeftEnergy(String newStep){

        PriorityQueue<String> requestsTemp = new PriorityQueue<String>(requests);
        double totalDist = 0;

        for(String request : requestsTemp){
            String[] splitted = request.split("-");
            totalDist += calculateDistance(splitted[0], splitted[1]);
        }



    }

    private double calculateDistance(String start, String end) {

        MachineLocation mlStart = null;
        MachineLocation mlEnd = null;

        for(int i = 0; i < machinesLocations.size(); i++){
            if(machinesLocations.get(i).AID == start)
                mlStart = machinesLocations.get(i);
            if(machinesLocations.get(i).AID == end)
                mlEnd = machinesLocations.get(i);

        }

        if(mlStart == null || mlEnd == null)
            return -1;

        return Math.sqrt((Math.pow((mlEnd.x - mlStart.x), 2)) + (Math.pow((mlEnd.y - mlStart.y), 2)));

    }

    /**
     * Behaviour to respond to a contract Net request
     */
    protected class ResponderContractNetMachineBehaviour extends ContractNetResponder {

        public ResponderContractNetMachineBehaviour(Agent a, MessageTemplate mt) {
            super(a, mt);
            System.out.println("Set up of the machine " + a.getName() + " - Contract Net Responder");
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
            System.out.println("Agent " + getLocalName() + ": CFP received from " + cfp.getSender().getName() + ". Action is " + cfp.getContent());

            /*if (capacity > 0) {
                // We provide a proposal
                System.out.println("Agent " + getLocalName() + ": Proposing " + potential);
                ACLMessage propose = cfp.createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContent(String.valueOf(potential));
                return propose;
            }
            else {
                // We refuse to provide a proposal because the machine does not have capacity to process another lot
                System.out.println("Agent " + getLocalName() + ": Refuse");
                throw new RefuseException("evaluation-failed");
            }*/
            /**
             * TODO
             */
            return null;
        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
            System.out.println("--- Agent " + getLocalName() + ": Proposal accepted ---");
            System.out.println("Agent " + getLocalName() + " will get the lot");
            return null;
        }

        protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
            System.out.println("Agent " + getLocalName() + ": Proposal rejected");
        }

    }

    /**
     * Delete the agent
     */
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Printout a dismissal message
        System.out.println("Machine-agent " + getAID().getName() + " terminating.");
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

    /**
     * Data Structure that saves the machine location
     */
    protected class MachineLocation{

        public String AID;
        public int x;
        public int y;

        public MachineLocation(String AID, int x, int y){
            this.AID = AID;
            this.x = x;
            this.y = y;
        }

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

    /*public ArrayList getRequests() {
        return requests;
    }*/

    /*public void setRequests(ArrayList requests) {
        this.requests = requests;
    }*/

    /*public void addRequest(Request r){
        requests.add(r);
    }*/

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

    /*public void step(){
        int newX = x + vX;
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
        power--;
    }*/

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
                getCurrentCapacity() + "/"+ getMaxCapacity() +" capacity and ");//+getRequests().size()+" total Requests");
    }

    public void setCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    //public double calCost()

}
