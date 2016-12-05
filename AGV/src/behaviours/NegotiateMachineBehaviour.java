package behaviours;

import agents.MachineAgent;
import sajas.core.behaviours.Behaviour;

/**
 * Created by utilizador on 05/12/2016.
 */
public class NegotiateMachineBehaviour extends Behaviour{

    protected MachineAgent mag;

    public NegotiateMachineBehaviour(MachineAgent mag){
        this.mag = mag;
    }

    @Override
    public void action() {
        // negotiation protocol
    }

    @Override
    public boolean done() {
        return false;
    }
}
