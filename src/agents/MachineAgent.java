package agents;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.core.behaviours.SequentialBehaviour;
import sajas.core.behaviours.SimpleBehaviour;
import sajas.domain.DFService;
import sajas.proto.ContractNetInitiator;
import sajas.proto.ContractNetResponder;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

public class MachineAgent extends Agent implements Drawable{
    private int capacity;
    private int velocity;
    private boolean maintenance;
    private int x, y;
    private double potential;
    private int lotsProducing;
    private int timeToFinishLot;
    private boolean lotProduced = false;

    private int processingStep;
    private int stepID;
    private Image image;

    // AID of the AGVs
    private AID[] agvs;
    // AID of the machines of the next fase
    private AID[] machines;

    private static final String PROCESS = "process";
    private static final String NEGOTIATE_MACHINE = "negotiate-machine";

    /**
     * Constructor of a machine agent
     * @param processID the id of the processing the machine belongs to
     * @param stepID the id inside the processID
     * @param cap processing capacity of the machine
     * @param vel processing velocity of the machine
     */
    public MachineAgent(int processID, int stepID,int x,int y, int cap, int vel){
        this.processingStep = processID;
        this.stepID = stepID;
        this.capacity = cap;
        this.velocity = vel;
        this.maintenance = false;
        this.x = x;
        this.y = y;
        this.lotsProducing = 0;
        this.potential = ((double)1/(double)cap)*((double)1/(double)vel);
        try {
            image = ImageIO.read(new File("src/machine.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        timeToFinishLot = 10/velocity;

        agvs = null;
        machines = null;
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
     * remove a lot because it has finished processing
     */
    public void removeLot(){
        lotsProducing--;
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
     * Setup the agent - including DF and Behaviours
     */
    protected void setup() {
        // hello message agent
    	System.out.println("Hello! Machine Agent " + getAID().getName() + " is ready.");
    	report();

        // register provider at DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addProtocols(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName() + "-machine");
        sd.setType("machine-" + processingStep);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            System.err.println(e.getMessage());
        }

        setupBehaviours();

    }

    /**
     * Setup the behaviours needed
     */
    public void setupBehaviours(){

        System.out.println("---- Setup behaviours ----");

        // responder of a net machine contractor
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP) );
        ResponderContractNetMachineBehaviour responderContract = new ResponderContractNetMachineBehaviour(this, template);

        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        cfp.setContent(" Do you want the lot? ;)");

        InitContractNetMachineBehaviour init = new InitContractNetMachineBehaviour(this, cfp);

        SequentialBehaviour sb = new SequentialBehaviour(){
            public int onEnd() {
                reset();

                removeSubBehaviour(init);
                InitContractNetMachineBehaviour init = new InitContractNetMachineBehaviour(myAgent, cfp);
                addSubBehaviour(init);

                myAgent.addBehaviour(this);
                return super.onEnd();
            }
        };
        sb.addSubBehaviour(new LotProcessingBehaviour());
        sb.addSubBehaviour(init);

        // responder of a agv machine request
        MessageTemplate t = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        AGVInteraction agvInteraction = new AGVInteraction(t);

        // cycle: process->negotiate machine->negotiate transport
        this.addBehaviour(sb);
        this.addBehaviour(responderContract);
        this.addBehaviour(agvInteraction);

    }

    /**
     * Search the DF for the desired agents
     * @param type the type of agents to search
     */
    public void search(String type){

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        if(type == "machine") {
            int followingStep = processingStep + 1;
            sd.setType(type + "-" + followingStep);
        }else
            sd.setType(type);
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if(type == "machine"){
                machines = new AID[result.length];
                for (int i = 0; i < result.length; ++i) {
                    machines[i] = result[i].getName();
                }
            }else if (type == "agv") {
                agvs = new AID[result.length];
                for (int i = 0; i < result.length; ++i) {
                    agvs[i] = result[i].getName();
                }
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

    }

    /**
     * Behaviours
     */

    /**
     * Behaviour of processing lots
     */
    protected class LotProcessingBehaviour extends SimpleBehaviour {

        @Override
        public void action() {

            // if there are lots to process
            if(lotsProducing > 0 && !lotProduced) {
                System.out.println("[ " + myAgent.getAID() +  "] Producing lot");
                // if the lot is not finished processing
                if (timeToFinishLot > 0) {
                    decrementTimeLot();
                    int print = timeToFinishLot+1;
                    System.out.println("[ " + myAgent.getAID() +  "] Decrementing lot time. Goes from " + print + " to " + timeToFinishLot);
                } else if (timeToFinishLot == 0) {
                    System.out.println("[ " + myAgent.getAID() +  "] Lot finished");
                    // decrement number of lots producing
                    removeLot();
                    // restart the timeToFinishLot variable
                    timeToFinishLot = 10/velocity;
                    // negotiate passage of the lot to another machine
                    System.out.println("[ " + myAgent.getAID() +  "] lotsLeft: " + lotsProducing + " and timeToFinish: " + timeToFinishLot);
                    lotProduced = true;
                    done();
                }
            }

        }

        @Override
        public boolean done() {
            if(lotProduced)
                return true;
            return false;
        }

    }

    /**
     * Behaviour to initiate a ContractNet
     */
    protected class InitContractNetMachineBehaviour extends ContractNetInitiator {

        protected AID bestProposer;

        public InitContractNetMachineBehaviour(Agent a, ACLMessage cfp) {
            super(a, cfp);

            bestProposer = null;
            System.out.println("Set up of the machine " + a.getName() + " - Contract Net Initiator");
        }

        @Override
        public Vector prepareCfps(ACLMessage cfp) {
            if (machines == null) {
                search("machine");
                for (int i = 0; i < machines.length; i++) {
                    cfp.addReceiver(machines[i]);
                    System.out.println(machines[i].toString() + " added to " + myAgent.getAID());
                }
            }
            return super.prepareCfps(cfp);
        }

        protected void handlePropose(ACLMessage propose, Vector v) {
            System.out.println("Agent " + propose.getSender() + " proposed " + propose.getContent());
        }

        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("Agent " + refuse.getSender() + " refused because it has no more capacity.");
        }

        protected void handleFailure(ACLMessage failure) {
            if (failure.getSender().equals(myAgent.getAMS())) {
                // FAILURE notification from the JADE runtime: the receiver
                // does not exist
                System.out.print("Failure Sender " + failure.getSender());
                System.out.println("Responder does not exist " + myAgent.getAMS());
            }
            else {
                System.out.println("Agent " + failure.getSender() + " failed");
            }
            // Immediate failure --> we will not receive a response from this agent
        }

        protected void handleAllResponses(Vector responses, Vector acceptances) {
            if (responses.size() < machines.length) {
                // Some responder didn't reply within the specified timeout
                System.out.println("Timeout expired: missing " + (machines.length - responses.size()) + " responses");
            }

            // Evaluate proposals.
            double bestProposal = 999999;
            ACLMessage accept = null;
            Enumeration e = responses.elements();

            while (e.hasMoreElements()) {

                ACLMessage msg = (ACLMessage) e.nextElement();

                if (msg.getPerformative() == ACLMessage.PROPOSE) {

                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);

                    acceptances.addElement(reply);

                    double proposal = Double.parseDouble(msg.getContent());

                    if (proposal < bestProposal) {

                        bestProposal = proposal;
                        bestProposer = msg.getSender();
                        accept = reply;

                    }
                }
            }

            // Accept the proposal of the best proposer (smallest value of potential)
            if (accept != null) {
                System.out.println("Accepting proposal " + bestProposal + " from responder " + bestProposer);
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            }
        }

        protected void handleInform(ACLMessage inform) {
            System.out.println(myAgent.getLocalName() + " successfully informed " + inform.getSender());
            lotProduced = false;
        }

    }

    /**
     * Behaviour to respond to a contract Net request
     */
    protected class ResponderContractNetMachineBehaviour extends ContractNetResponder{

        public ResponderContractNetMachineBehaviour(Agent a, MessageTemplate mt) {
            super(a, mt);
            System.out.println("Set up of the machine " + a.getName() + " - Contract Net Responder");
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
            System.out.println("Agent " + getID() + ": CFP received from " + cfp.getSender() + ". - " + cfp.getContent());

            if (capacity > 0) {
                // We provide a proposal
                System.out.println("Agent " + getAID() + ": Proposing " + potential);
                ACLMessage propose = cfp.createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContent(String.valueOf(potential));
                return propose;
            }
            else {
                // We refuse to provide a proposal because the machine does not have capacity to process another lot
                System.out.println("Agent " + getID() + ": Refuse");
                throw new RefuseException("evaluation-failed");
            }
        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
            System.out.println("--- " + getLocalName() + ": Proposal accepted ---");
            System.out.println(getLocalName() + " will get the lot");

            ACLMessage response = cfp.createReply();
            response.setPerformative(ACLMessage.INFORM);
            response.setContent("got it");

            return response;
        }

        protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
            System.out.println("Agent " + getLocalName() + ": Proposal rejected");
        }

    }

    /**
     * Behaviour to listen for AGV messages (drop lot/request location)
     */
    protected class AGVInteraction extends CyclicBehaviour{

        MessageTemplate mt;

        public AGVInteraction(MessageTemplate mt){
            this.mt = mt;
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // Message received. Process it
                if(msg.getContent() == "location")
                    myAgent.send(requestLocation(msg));
                else
                    myAgent.send(requestLotPassage(msg));
            }
        }

        protected ACLMessage requestLocation(ACLMessage msg){
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(x + "-" + y);

            return reply;
        }
        protected ACLMessage requestLotPassage(ACLMessage msg){
            lotsProducing++;

            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent("ok");

            return reply;
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

    public int getTimeLot(){return timeToFinishLot;}

    public void setTimeLot(int newTime){timeToFinishLot = newTime;}

    public void decrementTimeLot(){timeToFinishLot--;}

    @Override
    public void draw(SimGraphics G) {
        G.drawImageToFit(image);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public double getPotential() {
        return potential;
    }

    public void setPotential(double potential) {
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

    public String getID() {
        return processingStep + "." + stepID;
    }

}