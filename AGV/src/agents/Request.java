package agents;

/**
 * Created by danie on 03/12/2016.
 */
public class Request {
    private int totalItems;
    private MachineAgent originMachine;
    private MachineAgent destinationMachine;

    public Request(int totalItems, MachineAgent originMachine, MachineAgent destinationMachine){
        this.totalItems=totalItems;
        this.originMachine=originMachine;
        this.destinationMachine=destinationMachine;
    }

}

