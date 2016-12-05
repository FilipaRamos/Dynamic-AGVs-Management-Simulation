package behaviours;

import agents.MachineAgent;
import sajas.core.behaviours.FSMBehaviour;

import static utils.Constants.NEGOTIATED_MACHINE;
import static utils.Constants.PROCESSED;

/**
 * Created by utilizador on 05/12/2016.
 */
public class LotProcessingCycleBehaviour extends FSMBehaviour {

    protected MachineAgent mag;

    public LotProcessingCycleBehaviour(MachineAgent mag){
        this.mag = mag;

        setupStates();
    }

    /**
     * States: LotProcessing, NegotiateMachineBehaviour, NegotiateAGV
     */
    public void setupStates() {
        registerFirstState(new LotProcessingBehaviour(mag), "Process");
        registerState(new NegotiateMachineBehaviour(mag), "NegotiationMachine");
        //registerLastState(new NegotiateAGVBehaviour(), "NegotiationAGV");
        registerTransition("Process", "NegotiationMachine", PROCESSED);
        registerTransition("NegotiationMachine", "NegotiationAGV", NEGOTIATED_MACHINE);
    }


}
