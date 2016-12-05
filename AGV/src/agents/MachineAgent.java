package agents;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import negotiation.ContractOutcome;
import negotiation.ProviderValue;
import sajas.core.AID;
import sajas.core.Agent;
import sajas.core.behaviours.Behaviour;
import sajas.core.behaviours.FSMBehaviour;
import sajas.core.behaviours.ParallelBehaviour;
import sajas.core.behaviours.SimpleBehaviour;
import sajas.domain.DFService;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static utils.Constants.NEGOTIATED_MACHINE;
import static utils.Constants.PROCESSED;

public class MachineAgent extends Agent implements Drawable{
    private int capacity;
    private int velocity;
    private boolean maintenance;
    private int x, y;
    private int potential;
    private int lotsProducing;
    private int timeToFinishLot = 10/velocity;

    private int processingStep;
    private int stepID;

    // Contractual Net Services Utilities
    public int nBestProviders;
    public ArrayList<ContractOutcome> contractOutcomes = new ArrayList<ContractOutcome>();
    public Map<AID,ProviderValue> providersTable = new HashMap<AID,ProviderValue>();
    public ArrayList<ProviderValue> providersList = new ArrayList<ProviderValue>();

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
        this.timeToFinishLot = 0;
    }

    public MachineAgent(){
        //this.processingStep
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
        sd.setName(getLocalName() + "-service-provider");
        sd.setType("service-provider");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            System.err.println(e.getMessage());
        }

        // behaviours registration
        FSMBehaviour productionCycle = new FSMBehaviour();

        productionCycle.registerFirstState(new LotProcessingBehaviour(), "Process");
        productionCycle.registerState(new NegotiateMachineBehaviour(), "NegotiationMachine");
        //productionCycle.registerLastState(new NegotiateAGVBehaviour(), "NegotiationAGV");
        productionCycle.registerTransition("Process", "NegotiationMachine", PROCESSED);
        productionCycle.registerTransition("NegotiationMachine", "NegotiationAGV", NEGOTIATED_MACHINE);

        ParallelBehaviour paralel = new ParallelBehaviour();

        // cycle: process->negotiate machine->negotiate transport
        paralel.addSubBehaviour(productionCycle);
        paralel.addSubBehaviour(new sajas.core.behaviours.OneShotBehaviour() {
            @Override
            public void action() {
                lotsProducing++;
            }
        });

    }

    /**
     * Get the contratual net best providers
     * @return ArrayList<AID> the AID of the providers
     */
    public ArrayList<AID> getBestProviders() {

        ArrayList<AID> bestProviders = new ArrayList<AID>();

        Collections.sort(providersList);
        for(int i = 0; i < nBestProviders && i < providersList.size(); i++) {
            bestProviders.add(providersList.get(i).getProvider());
        }

        return bestProviders;
    }

    /**
     *
     */
    public void addProviderOutcome(AID provider, ContractOutcome.Value outcome) {
        ProviderValue pv = providersTable.get(provider);
        //pv.addOutcome(outcome);
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
            if(lotsProducing > 0) {
                // if the lot is not finished processing
                if (timeToFinishLot > 0) {
                    decrementTimeLot();
                } else if (timeToFinishLot == 0) {
                    // new lot to be processed
                    done();
                }
            }

        }

        @Override
        public boolean done() {
            return true;
        }
    }

    /**
     * Behaviour to negotiate with machines
     */
    protected class NegotiateMachineBehaviour extends Behaviour {

        @Override
        public void action() {
            // negotiation protocol
        }

        @Override
        public boolean done() {
            return false;
        }
    }


}
