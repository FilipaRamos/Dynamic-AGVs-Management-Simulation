package behaviours;

import agents.MachineAgent;
import sajas.core.behaviours.SimpleBehaviour;

/**
 * Created by utilizador on 05/12/2016.
 */
public class LotProcessingBehaviour extends SimpleBehaviour {

    protected MachineAgent mag;

    public LotProcessingBehaviour(MachineAgent mag){
        this.mag = mag;
    }

    @Override
    public void action() {

        // if there are lots to process
        if(mag.getLotsProducing() > 0) {
            // if the lot is not finished processing
            if (mag.getTimeLot() > 0) {
                mag.decrementTimeLot();
            } else if (mag.getTimeLot() == 0) {
                // new lot to be processed
                mag.setTimeLot(0);
            }
        }

    }

    @Override
    public boolean done() {
        return false;
    }
}
