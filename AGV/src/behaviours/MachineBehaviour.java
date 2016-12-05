package behaviours;

import agents.MachineAgent;
import sajas.core.behaviours.ParallelBehaviour;

/**
 * Created by utilizador on 05/12/2016.
 */
public class MachineBehaviour extends ParallelBehaviour{

    protected MachineAgent mag;

    public MachineBehaviour(MachineAgent mag){
        this.mag = mag;

        //addSubBehaviour(new LotProcessingCycleBehaviour(mag));
        //addSubBehaviour(new ReceiveMachineRequestBehaviour());
        //addSubBehaviour(new ReceiveAGVRequestBehaviour());
        //addSubBehaviour(new ReceiveLot());

    }

}
