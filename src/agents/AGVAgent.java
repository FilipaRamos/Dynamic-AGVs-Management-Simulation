package agents;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
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

    // PowerStation Coordinates
    private int powerX;
    private int powerY;

    // queue of requests
    private PriorityQueue<String> requests = new PriorityQueue();
    // location of the machines
    private ArrayList<MachineLocation> machinesLocations;

    // space of the simulation
    private Space space;
    private AID[] machines;

    private static int IDNumber = 0;
    private int ID;

    /**
     * Constructor of an AGV agent
     * @param x x coordinate
     * @param y y coordinate
     * @param power ammount of moves the AGV can still do
     * @param maxCapacity maximum capacity of transport
     */
    public AGVAgent(int x,int y,int power,int maxCapacity, int powerX, int powerY){
        this.maxCapacity=maxCapacity;
        this.currentCapacity=0;
        this.x=x;
        this.y=y;
        this.power=power;
        this.powerX = powerX;
        this.powerY = powerY;

        setVxVy();
        IDNumber++;
        ID = IDNumber;

        machinesLocations = new ArrayList();
        machines = null;
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

        // responder message template
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP) );
        ResponderContractNetBehaviour proposals = new ResponderContractNetBehaviour(this, template);

        AGVHandlerBehaviour handler = new AGVHandlerBehaviour();
        MachineComHandlerBehaviour machineComunication = new MachineComHandlerBehaviour();

        addBehaviour(proposals);
        addBehaviour(handler);
        addBehaviour(machineComunication);

    }

    /**
     * Search the DF for the desired agents
     */
    protected void search(){

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("machine-" + "/\\[[0-9]+\\]/");
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            for (int i = 0; i < result.length; ++i) {
                machines[i] = result[i].getName();
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

    }

    protected class MachineComHandlerBehaviour extends CyclicBehaviour{

        public MachineComHandlerBehaviour(){
        }

        @Override
        public void action() {
            machineLocationScout();
        }

        protected void machineLocationScout() {

            for (int i = 0; i < machines.length; i++) {
                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                request.setContent("location");
                send(request);
            }
        }


    }

    /**
     * Evaluates the addition of the new step to the requests queue
     * @param newStep the step that is being evaluated to be added to the queue
     */
    protected boolean evaluateStep(String newStep){

        PriorityQueue<String> requestsTemp = new PriorityQueue<String>(requests);
        requestsTemp.add(newStep);
        String[] splitStep = newStep.split("-");
        requestsTemp.add(splitStep[1] + "-station");

        double dT = calculateTotalDistance(requestsTemp);
        double dExtra = calculateExtraDistance(requestsTemp);
        double dE = calculateDistance(splitStep[1], powerX, powerY);
        double finalDistance = dT + dExtra + dE;

        if(finalDistance > power)
            return false;

        return true;

    }

    /**
     * Calculates the total distance of all paths in the requests queue
     * @param requestsTemp the queue of requests
     * @return distance calculated
     */
    private double calculateTotalDistance(PriorityQueue<String> requestsTemp){

        double totalDist = 0;

        for(String request : requestsTemp){
            String[] splited = request.split("-");
            double currentDist = calculateDistance(splited[0], splited[1]);
            if(currentDist == -1){
                System.out.println("[ERROR] Distance in between points could not be calculated! - " + splited[0] + "->" + splited[1]);
                return -1;
            }
            totalDist += currentDist;
        }

        return totalDist;
    }

    /**
     * Calculates the distance from the end point of a request to the start point of another
     * @param requestsTemp the queue of requests
     * @return distance calculated
     */
    private double calculateExtraDistance(PriorityQueue<String> requestsTemp){

        ArrayList<String> extra = new ArrayList<>();
        String start = null;
        String end;
        double dExtra = 0;
        int counter = 1;

        for(String request : requestsTemp){
            String[] splited = request.split("-");
            if(counter%2 == 0) {
                end = splited[0];
                if(start != end)
                    extra.add(start + "-" + end);
            }
            else {
                start = splited[1];
            }
            counter++;
        }

        for(int i = 0; i < extra.size(); i++){
            String[] decomposed = extra.get(i).split("-");
            double curDist = calculateDistance(decomposed[0], decomposed[1]);
            if(curDist != -1)
                dExtra += curDist;
        }

        return dExtra;

    }

    /**
     * Calculates the distance in between 2 machines
     * @param start the AID of the machine the path starts on
     * @param end the AID of the machine the path ends on
     * @return value of the distance
     */
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
     * Calculates the distance between a machine and a point with the given coordinates
     * @param point aid of the machine
     * @param x x coordinate of the location
     * @param y y coordinate of the location
     * @return value of the distance
     */
    private double calculateDistance(String point, int x, int y){
        MachineLocation location = null;

        for(int i = 0; i < machinesLocations.size(); i++){
            if(machinesLocations.get(i).AID == point)
                location = machinesLocations.get(i);
        }

        if(location != null){
            return Math.sqrt((Math.pow((x - location.x), 2)) + (Math.pow((y - location.y), 2)));
        }else{
            return -1;
        }

    }

    /**
     * Behaviour to respond to a contract Net request
     */
    protected class ResponderContractNetBehaviour extends ContractNetResponder {

        public ResponderContractNetBehaviour(Agent a, MessageTemplate mt) {
            super(a, mt);
            System.out.println("Set up of the machine " + a.getName() + " - Contract Net Responder");
            if(machines == null)
                search();
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
            System.out.println("Agent " + getLocalName() + ": CFP received from " + cfp.getSender().getName() + ". Action is " + cfp.getContent());

            if (getCapacityBalance() > 0) {
                // We provide a proposal
                // TODO add proposal failure evaluation - 3 conditions
                System.out.println("Agent " + getLocalName() + ": Proposing " + calculateCost(cfp.getSender().toString(), cfp.getContent()));
                ACLMessage propose = cfp.createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContent(String.valueOf(calculateCost(cfp.getSender().toString(), cfp.getContent())));
                return propose;
            }
            else {
                // We refuse to provide a proposal because the agv does not have capacity to transport another lot
                System.out.println("Agent " + getLocalName() + ": Refuse");
                throw new RefuseException("transportation-refused");
            }

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

        protected double calculateCost(String start, String destiny){
            PriorityQueue<String> requestsTemp = new PriorityQueue<>(requests);
            requestsTemp.add(start + "-" + destiny);

            double totalDist = calculateTotalDistance(requestsTemp);

            return (power - totalDist)/requests.size();
        }

    }

    /**
     * Behaviour class to move agv (fullfil requests and recharge actions)
     */
    protected class AGVHandlerBehaviour extends CyclicBehaviour{

        @Override
        public void action() {

            if(requests.size() > 0) {
                // evaluate energy
                // handle move
                // handle delivery -> send REQUEST message to machine
            }
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

    public Space getSpace() {
        return space;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    /**
     * Decrements the power by the provided factor
     * @param factor factor by which to decrement the power
     */
    public void decrementPower(int factor) {
        power -= factor;
    }

    /**
     * charge the power of the AGV
     * @param amount ammount to be charged
     */
    public void chargePower(int amount){
        power += amount;
    }

    public int getID() {
        return ID;
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

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getCurrentCapacity() {
        return currentCapacity;
    }

    public int getCapacityBalance(){
        return maxCapacity - currentCapacity;
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

}
