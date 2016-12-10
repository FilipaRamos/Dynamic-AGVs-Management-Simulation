package agents;

import Utils.Images;
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
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import static model.AGVSajasModel.images;

/**
 * Created by danie on 03/12/2016.
 */
public class AGVAgent extends Agent implements Drawable {

    // AGV variables
    private int x;
    private int y;
    private int maxCapacity;
    private int currentCapacity;
    private int power;
    private int initialPower;
    private int tick = 50;

    // PowerStation Coordinates
    private int powerX;
    private int powerY;

    // queue of requests
    private Queue<String> requests = new LinkedList<String>();
    private Queue<String> modifiedRequests = new LinkedList<>();
    // location of the machines
    private ArrayList<MachineLocation> machinesLocations;
    // list of points to visit
    private ArrayList<Point> points;
    private ArrayList<Point> dropPoints;
    private ArrayList<Point> pickupPoints;

    // space of the simulation
    private AID[] machines;

    private Point currentDestiny;
    private Point[] currentPath;

    private static int IDNumber = 0;
    private int ID;

    private Point powerStation;

    /**
     * Constructor of an AGV agent
     * @param x x coordinate
     * @param y y coordinate
     * @param power ammount of moves the AGV can still do
     * @param maxCapacity maximum capacity of transport
     */
    public AGVAgent(int x,int y,int power,int maxCapacity, int powerX, int powerY){
        this.maxCapacity = maxCapacity;
        this.currentCapacity = 0;
        this.x=x;
        this.y=y;
        this.power=power;
        initialPower = power;
        this.powerX = powerX;
        this.powerY = powerY;
        powerStation = new Point(powerX, powerY, "power");
        IDNumber++;
        ID = IDNumber;

        machinesLocations = new ArrayList<>();
        machines = null;
        currentPath = null;
        currentDestiny = null;
        points = new ArrayList<>();
        dropPoints = new ArrayList<>();
        pickupPoints = new ArrayList<>();
    }

    /**
     * Setup the agent
     */
    public void setup() {

        System.out.println("Setup " + getLocalName());
        report();

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

        addBehaviour(proposals);
        addBehaviour(handler);

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

    /**
     * Evaluates the possibility of the agv fulfilling the request
     * @param newStepStart the start point of the step that is being evaluated to be added to the queue
     * @param newStepEnd the end point of the step that is being evaluated to be added to the queue
     */
    protected boolean evaluateStep(Point newStepStart, Point newStepEnd){

        ArrayList<Point> requests = points;

        double distancePower = calculateDistance(newStepStart.x, newStepStart.y, powerX, powerY);

        System.out.println("x1: "+newStepEnd.x);
        System.out.println("y1: "+newStepEnd.y);
        System.out.println("x2: "+powerX);
        System.out.println("y2: "+powerY);
        double distancePowerFinal = calculateDistance(newStepEnd.x, newStepEnd.y, powerX, powerY);

        if(distancePower > initialPower)
            return false;
        if(distancePowerFinal > initialPower)
            return false;

        return true;

    }

    /**
     * Calculates the total distance of all paths in the points to visit list
     * @param points the list of points to visit
     * @return distance calculated
     */
    private double calculateTotalDistance(ArrayList<Point> points){

        double totalDist = calculateDistance(x, y, points.get(0).x, points.get(0).y);

        for(int i = 0; i < points.size()-1; i++){
            totalDist += calculateDistance(points.get(i).x, points.get(i).y, points.get(i+1).x, points.get(i+1).y);
        }

        return totalDist;
    }

    /**
     * Calculates the distance from the end point of a request to the start point of another
     * @param requestsTemp the queue of requests
     * @return distance calculated
     */
    private double calculateExtraDistance(Queue<String> requestsTemp){

        ArrayList<String> extra = new ArrayList<>();
        String start = null;
        String end;
        double dExtra = 0;
        int counter = 1;

        for(String request : requestsTemp){
            String[] splited = request.split("&");
            if(counter%2 == 0) {
                end = splited[0];
                if(start != end)
                    extra.add(start + "&" + end);
            }
            else {
                start = splited[1];
            }
            counter++;
        }

        for(int i = 0; i < extra.size(); i++){
            String[] decomposed = extra.get(i).split("&");
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
            if(machinesLocations.get(i).aid.toString().equals(start))
                mlStart = machinesLocations.get(i);
            if(machinesLocations.get(i).aid.toString().equals(end))
                mlEnd = machinesLocations.get(i);

        }

        if(mlStart == null || mlEnd == null)
            return -1;

        return Math.sqrt((Math.pow((double)(mlEnd.x - mlStart.x), 2)) + (Math.pow((double)(mlEnd.y - mlStart.y), 2)));

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
            if(machinesLocations.get(i).aid.toString().equals(point))
                location = machinesLocations.get(i);
        }

        if(location != null){
            return Math.sqrt((Math.pow((x - location.x), 2)) + (Math.pow((y - location.y), 2)));
        }else{
            return -1;
        }

    }

    /**
     * Calculates the distance between 2 positions
     * @param x1 x coordinate of the first point
     * @param y1 y coordinate of the first point
     * @param x2 x coordinate of the second point
     * @param y2 y coordinate of the second point
     * @return
     */
    private double calculateDistance(int x1, int y1, int x2, int y2){
        return Math.sqrt((Math.pow((x2 - x1), 2)) + (Math.pow((y2 - y1), 2)));
    }

    /**
     * Behaviour to respond to a contract Net request
     */
    protected class ResponderContractNetBehaviour extends ContractNetResponder {

        public ResponderContractNetBehaviour(Agent a, MessageTemplate mt) {
            super(a, mt);
            System.out.println("Set up of the agv " + a.getName() + " - Contract Net Responder");
            if(machines == null)
                search();
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
            System.out.println("Agent " + getLocalName() + ": CFP received from " + cfp.getSender().getName() + ". Action is " + cfp.getContent());

            // agent has capacity to take the lot
            if (getCapacityBalance() > 0) {
                // if the step can be carried out
                if(evaluateStep(getPoint(cfp.getSender().toString(), "pickup"), getPoint(cfp.getContent(), "drop"))) {
                    // We provide a proposal
                    System.out.println("Agent " + getLocalName() + ": Proposing " + calculateCost(cfp.getSender().toString(), cfp.getContent()));
                    ACLMessage propose = cfp.createReply();
                    propose.setPerformative(ACLMessage.PROPOSE);
                    propose.setContent(String.valueOf(calculateCost(cfp.getSender().toString(), cfp.getContent())));
                    return propose;
                }
            }
            else {
                // We refuse to provide a proposal because the agv does not have capacity to transport another lot
                System.out.println("Agent " + getLocalName() + ": Refuse");
                throw new RefuseException("transportation-refused");
            }

            return null;

        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
            System.out.println("--- Agent " + getLocalName() + ": Proposal accepted ---");
            System.out.println("Agent " + getLocalName() + " will get the lot");
            addRequest(cfp.getSender() + "&" + cfp.getContent());

            ACLMessage response = cfp.createReply();
            response.setPerformative(ACLMessage.INFORM);
            response.setContent("got it");

            return response;
        }

        protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
            System.out.println("Agent " + getLocalName() + ": Proposal rejected");
        }

        protected double calculateCost(String start, String destiny){

            double distFinalCargo;
            double nrPowers = 0;
            double totalDist = 0;

            if(points.size() > 0) {
                distFinalCargo = calculateDistance(points.get(points.size() - 1).x, points.get(points.size() - 1).y,
                        getPoint(start, "pickup").x, getPoint(start, "pickup").y);
                nrPowers = Collections.frequency(points, powerStation);
                totalDist = calculateTotalDistance(points);
            }else{
                distFinalCargo = calculateDistance(x, y,
                        getPoint(start, "pickup").x, getPoint(start, "pickup").y);
            }

            return ((float)1/(distFinalCargo + nrPowers + totalDist));
        }

    }

    /**
     * Behaviour class to move agv (fullfil requests and recharge actions)
     */
    protected class AGVHandlerBehaviour extends CyclicBehaviour{

        private boolean charging = false;

        @Override
        public void action() {

            if(requests.size() > 0)
                postRequests();

            if(points.size() > 0) {
                if(tick == 0){
                    // it's at the destination
                    if(x == points.get(0).x && y == points.get(0).y){
                        // it's a delivery point
                        if(points.get(0).type.equals("drop")){
                            if(request(points.get(0), "drop"))
                                currentCapacity--;
                        }
                        //it's a pickup location
                        else if(points.get(0).type.equals("pickup")){
                            if(request(points.get(0), "pickup"))
                                currentCapacity++;
                        }
                        // it's the power station
                        else if(points.get(0).type.equals("power")){
                            if(!charging)
                                charging = true;
                        }
                        points.remove(0);
                    }
                    // if the agv is out of energy, add the path to the powerStation to the first place on the list
                    if(evaluateEnergy()){
                        updatePoints();
                    }
                    if(points.size() > 0) {
                        if(!charging) {
                            // update coordinates on the movement to the next destination
                            updateCoordinates(points.get(0));
                        }
                    }
                }
            }

            if(charging){
                tick = 500;
                power = initialPower;
                charging = false;
            }else {
                if (tick > 0)
                    tick--;
                else
                    tick = 50;
            }
        }
    }

    /**
     * Sort the points to visit in order to make the path ore efficient
     */
    protected void sortPoints(){

        ArrayList<Point> pointsTemp = points;
        ArrayList<Point> pickups = new ArrayList<>();

        for(int i = 0; i < pointsTemp.size(); i++){
            if(pointsTemp.get(i).type.equals("pickup")) {
                System.out.println("O I:" + pointsTemp.get(i).x + pointsTemp.get(i).y);
                for (int j = i + 1; j < pointsTemp.size() - getTrimSize(i, pointsTemp); j++) {
                    System.out.println(j);
                    if (pointsTemp.get(j).x == pointsTemp.get(i).x &&
                            pointsTemp.get(j).y == pointsTemp.get(i).y &&
                            pointsTemp.get(j).type.equals(pointsTemp.get(i).type)) {
                        System.out.println("INSIDE IF first:" + pointsTemp.get(i).x + pointsTemp.get(i).y);
                        if (simulateOrder(pointsTemp, pointsTemp.get(j), i + 1)) {
                            pickups.add(pointsTemp.get(j));
                            System.out.println("Added to pickup.");
                        }
                    }
                }
                for (int k = 0; k < pickups.size(); k++)
                    points.add(i + 1, pickups.get(k));
                pickups.clear();
                System.out.println("INSIDE PICKUP");
            }
        }

        System.out.println();
        for(int k = 0; k < points.size(); k++){
            System.out.print("-> (" + points.get(k).x + ", " + points.get(k).y + ")");
        }

        System.out.println();

    }

    /**
     * Returns the size of the array after the index
     * @param index from which to count
     * @param points arraylist to count
     * @return number of elements
     */
    protected int getTrimSize(int index, ArrayList<Point> points){
        return points.size() - (index+1);
    }

    protected boolean simulateOrder(ArrayList<Point> pointsTemp, Point p, int index){
        pointsTemp.add(index, p);
        System.out.println("INSIDE SIMULATE ORDER");
        if(calculateCargo(pointsTemp) == -1)
            return false;
        if(calculateCargo(pointsTemp) > maxCapacity)
            return false;
        return true;
    }

    /**
     * Calculates the total cargo of the current points arranjment
     * @param pointsArray
     * @return the number of lots
     */
    protected int calculateCargo(ArrayList<Point> pointsArray){
        int cargo = 0;

        for(int i = 0; i < pointsArray.size(); i++){
            if(pointsArray.get(i).type.equals("pickup"))
                cargo++;
            else if(pointsArray.get(i).equals("drop"))
                cargo--;
            if(cargo > maxCapacity)
                return -1;
        }

        System.out.println("CALCULATE CARGO:" + cargo);

        return cargo;
    }

    /**
     * Makes a list of points to visit from the request queue
     */
    public void postRequests(){

        System.out.println(getLocalName() + " POST REQUESTS");

        for(int i = 0; i < requests.size(); i++){
            // pass requests to arraylist of points
            String[] splitted = requests.poll().split("&");
            Point pickup = getPoint(splitted[0], "pickup");
            Point drop = getPoint(splitted[1], "drop");
            points.add(pickup);
            points.add(drop);
        }

        sortPoints();

    }

    /**
     * Update the list of points to visit based on the requests queue
     */
    public void updatePoints(){

        ArrayList<Point> copy = new ArrayList<>();
        copy.add(powerStation);

        for(int i = 0; i < points.size(); i++){
            copy.add(points.get(i));
        }

        points = copy;

    }

    /**
     * Get the point location of the object
     * @param AID of the object
     * @return point location
     */
    public Point getPoint(String AID, String type){
        Point p = null;

        for(int i = 0; i < machinesLocations.size(); i++){
            if(machinesLocations.get(i).aid.toString().equals(AID)){
                p = new Point(machinesLocations.get(i).x, machinesLocations.get(i).y, type);
            }
        }

        return p;
    }

    /**
     * Update the AGV coordinates on movement
     * @param p point of destination
     */
    public void updateCoordinates(Point p){
        if(x > p.x)
            x--;
        else if (x < p.x)
            x++;
        if(y > p.y)
            y--;
        else if (y < p.y)
            y++;

        power--;
    }

    /**
     * Send a drop or pickup request to machines upon picking up or delivering a lot
     * @param p Point of delivery or pickup
     * @param type type of action
     * @return true in case the request was sent successfully, false otherwise
     */
    protected boolean request(Point p, String type){

        AID toSend = getPointAID(p);

        if(toSend != null) {
            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.setContent(type);
            request.addReceiver(toSend);

            send(request);

            System.out.println("AGV requesting " + type + " from " + getPointAID(p));

            return true;

        }else{
            System.out.println("AID not found!!! Point does not correspond to a machine");
            return false;
        }

    }

    /**
     * Get AID of the object on that point
     * @param p point
     * @return AID of the object
     */
    protected AID getPointAID(Point p){

        for(int i = 0; i < machinesLocations.size(); i++){
            if(machinesLocations.get(i).x == p.x && machinesLocations.get(i).y == p.y){
                return machinesLocations.get(i).aid;
            }
        }

        return null;

    }

    /**
     * Evaluates the need to go energy recharging
     */
    protected boolean evaluateEnergy(){

        double distance = calculateDistance(x, y, powerX, powerY);
        if(power > distance){
            return false;
        }
        return true;

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
        System.out.println("AGV agent " + getAID().getName() + " terminating.");
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

        public AID aid;
        public int x;
        public int y;

        public MachineLocation(AID aid, int x, int y){
            this.aid = aid;
            this.x = x;
            this.y = y;
        }

    }

    public void addMachineLocation(AID machine, int x, int y){
        MachineLocation mc = new MachineLocation(machine, x, y);
        machinesLocations.add(mc);
    }

    protected class Point{

        public int x;
        public int y;
        public String type;

        public Point(int x, int y, String type){
            this.x = x;
            this.y = y;
            this.type = type;
        }

    }

    public void addRequest(String request){
        requests.add(request);
    }

    @Override
    public void draw(SimGraphics G) {
        Image im =  images.getImage(initialPower,power,maxCapacity,currentCapacity);
        G.drawImageToFit(im);
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
                " at (" +
                x + ", " + y +
                "), has " +
                getPower() + " power" +
                " and " +
                getCurrentCapacity() + "/"+ getMaxCapacity() +" capacity.");//+getRequests().size()+" total Requests");

    }

    public void setCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

}
