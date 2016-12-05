package TEST_SAJAS;

import java.util.ArrayList;

/**
 * Created by danie on 04/12/2016.
 */
public class MAIN_TEST {
    private static final int NUM_MACHINES_AGENTS = 6;
    private static final int NUM_MACHINE_PHASES = 3;
    private static ArrayList<Machine> machines;

    public static void main(String[] args) {
        machines = new ArrayList<>();
        int machines_per_phase = NUM_MACHINES_AGENTS/NUM_MACHINE_PHASES;
        for(int i = 1; i <= NUM_MACHINE_PHASES;i++){
            for(int x = 1; x <= machines_per_phase;x++){

            }
        }

       /* for(int i = 0; i < machines.size();i++){
            System.out.println(machines.get(i).printMachineName());
        }*/
    }

    public class Machine{
        private int ID;
        private int Phase;

        public Machine(int ID,int Phase){
            this.ID=ID;
            this.Phase=Phase;
        }
    }


}
