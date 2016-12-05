package behaviours;

import agents.MachineAgent;
import sajas.core.behaviours.Behaviour;
import utils.Lot;

/**
 * Created by utilizador on 05/12/2016.
 */
public class LotProcessingBehaviour extends Behaviour {

    protected MachineAgent mag;
    protected Lot lot;

    public LotProcessingBehaviour(MachineAgent mag, Lot lot){
        this.mag = mag;
        this.lot = lot;
    }

    @Override
    public void action() {

        int speed = mag.getVelocity();
        int time = 0;



    }

    @Override
    public boolean done() {
        return false;
    }
}
