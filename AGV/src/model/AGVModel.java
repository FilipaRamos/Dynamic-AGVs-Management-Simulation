package model;

import agents.AGVAgent;
import agents.MachineAgent;
import spaces.Space;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;

import java.awt.*;
import java.util.ArrayList;

public class AGVModel extends SimModelImpl {
    // Default Values
    private static final int WORLDXSIZE = 40;
    private static final int WORLDYSIZE = 40;
    private static final int POWER = 50;
    private static final int NUMMACHINES = 10;
    private static final int NUMAGV = 50;
    private static final int MAXCAPACITY = 30;


    private Schedule schedule;

    private int numAGV = NUMAGV;
    private int numMachines = NUMMACHINES;
    private int worldXSize = WORLDXSIZE;
    private int worldYSize = WORLDYSIZE;
    private int AgvPower = POWER;
    private int MaxCapacity = MAXCAPACITY;


    private Space space;
    private DisplaySurface displaySurf;
    private ArrayList agvList;
    private ArrayList machinesList;


    public String getName(){
        return "AGV Model";
    }

    public void setup(){
        System.out.println("Running setup");
        space = null;
        agvList = new ArrayList();
        machinesList = new ArrayList();

        schedule = new Schedule(1);

        if (displaySurf != null){
            displaySurf.dispose();
        }
        displaySurf = null;


        // Create Displays
        displaySurf = new DisplaySurface(this, "AGV Model Window 1");

        // Register Displays
        registerDisplaySurface("AGV Model Window 1", displaySurf);
    }

    public void begin(){
        buildModel();
        buildSchedule();
        buildDisplay();

        displaySurf.display();
    }

    public void buildModel(){
        System.out.println("Running BuildModel");
        space = new Space(worldXSize, worldYSize);
        //space.spreadMoney(money);

        for(int i = 0; i < numAGV; i++){
            addNewAGV();
        }
        for(int i = 0; i < numMachines;i++){
            addNewMachine();
        }

        for(int i = 0; i < agvList.size(); i++){
            AGVAgent cda = (AGVAgent)agvList.get(i);
            cda.report();
        }
        for(int i = 0; i < machinesList.size();i++){
            MachineAgent mach = (MachineAgent)machinesList.get(i);
            mach.report();
        }
    }

    private void addNewMachine() {
        /*MachineAgent machine = new MachineAgent();
        machinesList.add(machine);*/

    }

    private void addNewAGV(){
        AGVAgent a = new AGVAgent();
        a.setPower(AgvPower);
        a.setCapacity(MaxCapacity);
        agvList.add(a);
        space.addAGV(a);
    }

    public void buildSchedule(){
        System.out.println("Running BuildSchedule");
        /*class CarryDropStep extends BasicAction {
            public void execute() {
                SimUtilities.shuffle(agentList);
                for(int i =0; i < agentList.size(); i++){
                    CarryDropAgent cda = (CarryDropAgent)agentList.get(i);
                    cda.step();
                }
                int deadAgents = reapDeadAgents();
                for(int i =0; i < deadAgents; i++){
                    addNewAgent();
                }
                displaySurf.updateDisplay();
            }
        }
        //schedule.scheduleActionBeginning(0, new CarryDropStep());
        schedule.scheduleActionAtInterval(1000,new CarryDropStep());

        class CarryDropCountLiving extends BasicAction {
            public void execute(){
                countLivingAgents();
            }
        }

        schedule.scheduleActionAtInterval(10, new CarryDropCountLiving());*/
    }

    public void buildDisplay(){
        System.out.println("Running BuildDisplay");
        ColorMap map = new ColorMap();

        for(int i = 1; i<16; i++){
            map.mapColor(i, new Color((int)(i * 8 + 127), 0, 0));
        }
        map.mapColor(0, Color.white);

        Value2DDisplay displayBackground= new Value2DDisplay(space.getCurrentBackgroundSpace(),map);

        Object2DDisplay displayAGV= new Object2DDisplay(space.getCurrentAGVSpace());
        displayAGV.setObjectList(agvList);
       /* Object2DDisplay displayMachines= new Object2DDisplay(space.getCurrentMachineSpace());
        displayAGV.setObjectList(machinesList);*/

        displaySurf.addDisplayable(displayBackground,"Background");
        displaySurf.addDisplayableProbeable(displayAGV, "AGV");
        //displaySurf.addDisplayableProbeable(displayMachines, "Machines");
    }

    public Schedule getSchedule(){
        return schedule;
    }

    public String[] getInitParam(){
        String[] initParams = { "NumAGV","NumMachines","AgvPower","MaxCapacity", "WorldXSize", "WorldYSize"};
        return initParams;
    }


    public int getNumAGV(){
        return numAGV;
    }

    public void setNumAGV(int na){
        numAGV = na;
    }

    public int getWorldXSize(){
        return worldXSize;
    }

    public void setWorldXSize(int wxs){
        worldXSize = wxs;
    }

    public int getWorldYSize(){
        return worldYSize;
    }

    public void setWorldYSize(int wys){
        worldYSize = wys;
    }

    public int getNumMachines() {
        return numMachines;
    }

    public void setNumMachines(int num) {
        numMachines=num;
    }

    public int getAgvPower() {
        return AgvPower;
    }

    public void setAgvPower(int agvPower) {
        AgvPower = agvPower;
    }

    public int getMaxCapacity() {
        return MaxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        MaxCapacity = maxCapacity;
    }

    /*private int countLivingAgents(){
        int livingAgents = 0;
        for(int i = 0; i < agentList.size(); i++){
            CarryDropAgent cda = (CarryDropAgent)agentList.get(i);
            if(cda.getStepsToLive() > 0) livingAgents++;
        }
        System.out.println("Number of living agents is: " + livingAgents);

        return livingAgents;
    }

    private int reapDeadAgents(){
        int count = 0;
        for(int i = (agentList.size() - 1); i >= 0 ; i--){
            CarryDropAgent cda = (CarryDropAgent)agentList.get(i);
            if(cda.getStepsToLive() < 1){
                cdSpace.removeAgentAt(cda.getX(), cda.getY());
                cdSpace.spreadMoney(cda.getMoney());
                agentList.remove(i);
                count++;
            }
        }
        return count;
    }*/


    public static void main(String[] args) {
        SimInit init = new SimInit();
        AGVModel model = new AGVModel();
        init.loadModel(model, "", false);
    }

}