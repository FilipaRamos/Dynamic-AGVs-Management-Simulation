package TEST_SAJAS;

import agents.AGVAgent;
import agents.MachineAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
import spaces.Space;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AGVSajasModel extends Repast3Launcher {

	private static final boolean BATCH_MODE = true;
	public static final boolean SEPARATE_CONTAINERS = false;

	//map
	private static final int WORLDXSIZE = 30;
	private static final int WORLDYSIZE = 30;
	private Space space;
	private DisplaySurface displaySurf;

	//AGVAgent
	private static final int NUM_AGV_AGENTS = 10;
	private static final int AGV_POWER = 50;

	//MachineAgent
	private static String MACHINES_AND_PHASES = "[2,2,2]";
	private static final int MACHINES_MAX_CAPACITY = 50;
	private static final int MACHINES_SPEED = 5;


	private ContainerController mainContainer;
	private ContainerController agentsContainer;

	private ArrayList<AGVAgent> agvAgents;
	private ArrayList<MachineAgent> machineAgents;

	//info init
	private Schedule schedule;
	private int WORLD_X_SIZE = WORLDXSIZE;
	private int WORLD_Y_SIZE = WORLDYSIZE;
	private int num_agv_agents = NUM_AGV_AGENTS;
	private int agv_power = AGV_POWER;
	private String machines_and_phases = MACHINES_AND_PHASES;
	private int machines_max_capacity = MACHINES_MAX_CAPACITY;
	private int machines_speed = MACHINES_SPEED;
	private boolean separate_containers = SEPARATE_CONTAINERS;

	
	private boolean runInBatchMode;
	
	public AGVSajasModel(boolean runInBatchMode) {
		super();
		space = null;
		agvAgents = new ArrayList();
		machineAgents = new ArrayList();
		this.runInBatchMode = runInBatchMode;
	}



	//Variáveis que defini e que são necessárias!
	@Override
	public String[] getInitParam() {
		return new String[] {"separate_containers","WORLD_X_SIZE","WORLD_Y_SIZE","num_agv_agents","agv_power","machines_and_phases","machines_max_capacity","machines_speed"};
	}

	@Override
	public String getName() {
		return "AGVModel -- SAJaS Repast3";
	}
	//TEMOS DE TIRAR ISTO DAQUI QUANDO APAGARMOS O MODEL DO PROF!(ISTO SE ALGUMA VEZ APAGARMOS! :)
	@SuppressWarnings("Duplicates")
	@Override
	protected void launchJADE() {
		
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);
		
		if(separate_containers) {
			Profile p2 = new ProfileImpl();
			agentsContainer = rt.createAgentContainer(p2);
		} else {
			agentsContainer = mainContainer;
		}
		
		launchAgents();
	}
	//TEMOS DE TIRAR ISTO DAQUI QUANDO APAGARMOS O MODEL DO PROF!(ISTO SE ALGUMA VEZ APAGARMOS! :)
	@SuppressWarnings("Duplicates")
	private void launchAgents() {
		try {

			/*agvAgents = new ArrayList<AGVAgent>();
			// create agv's
			// agv agents
			for (int i = 0; i < num_agv_agents; i++) {
				AGVAgent agv = new AGVAgent(agv_power);
				String name = "Agent: "+agv.getID();
				agvAgents.add(agv);
				agentsContainer.acceptNewAgent(name, agv).start();
			}*/

			// machines agents
			String pattern = "\\p{Punct}\\d+(\\p{Punct}\\d+)*\\p{Punct}";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher(machines_and_phases);
			if (m.matches()) {
				System.out.println("Encontrou!!");
			}else {
				System.out.println("NO MATCH");
				throw new Exception("ERROR IN MACHINES_AND_PHASES! ITS MALFORMED!");
			}
			String[] phases = machines_and_phases.split("\\p{Punct}");

			System.out.println("BEFORE THE CYCLES - GOING TO CREATE MACHINES");
			for(int i = 1; i < phases.length;i++){
				int num_machines = Integer.parseInt(phases[i]);
				System.out.println("ON FASE " + i + " WITH MACHINES: " + num_machines);
				for(int x = 1; x <= num_machines;x++){
					MachineAgent machine = null;
					if(i == 3 && x == 1)
						machine = new MachineAgent(i, x, 18, 7, 50, 10);
					else if(i == 2 && x == 2)
						machine = new MachineAgent(i, x, 5, 5, 50, 10);
					else
						machine = new MachineAgent(i, x, 10, 10, machines_max_capacity, machines_speed);
					if(i == 1 && x == 1)
						machine.setLotsProducing(20);
					String name = "Agent: " + machine.getID();
					machineAgents.add(machine);
					agentsContainer.acceptNewAgent(name, machine).start();
				}
			}
			//END machines agents

		} catch (StaleProxyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void begin() {
		super.begin();
		System.out.println("BEGIN!");
		if(!runInBatchMode) {
			System.out.println("asdasd");
			buildAndScheduleDisplay();
		}

	}

	@SuppressWarnings("Duplicates")
	private void buildAndScheduleDisplay() {
		//build space
		space = new Space(WORLD_X_SIZE, WORLD_Y_SIZE);
		// display surface
		if (displaySurf != null)
			displaySurf.dispose();
		displaySurf = null;
		displaySurf = new DisplaySurface(this, "AGV Model Window 1");

		// Register Displays
		registerDisplaySurface("AGV Model Window 1", displaySurf);

		//Build schedule
		schedule = getSchedule();

		System.out.println("Running BuildSchedule");
		schedule.scheduleActionAtInterval(1, displaySurf, "updateDisplay", Schedule.LAST);
		class MoveAgentPEPE extends BasicAction {
			public void execute(){
				moveAgent();
			}
		}
		schedule.scheduleActionAtInterval(500, new MoveAgentPEPE());

		addSimEventListener(displaySurf);
		System.out.println("Running BuildDisplay");
		buildDisplayAgents();

		displaySurf.display();
	}

	private void moveAgent() {
		MachineAgent m = machineAgents.get(0);
		int x = m.getX();
		int y = m.getY();
		x++;y++;
		m.setX(x);
		m.setY(y);
	}

	public void buildDisplayAgents() {

		Object2DDisplay displayBackground= new Object2DDisplay(space.getCurrentBackgroundSpace());

		//HARD CODED!
		Object2DDisplay displayAGV= new Object2DDisplay(space.getCurrentAGVSpace());
		displayAGV.setObjectList(agvAgents);
        Object2DDisplay displayMachines= new Object2DDisplay(space.getCurrentMachineSpace());
        displayAGV.setObjectList(machineAgents);

		displaySurf.addDisplayableProbeable(displayBackground,"Background");
		displaySurf.addDisplayableProbeable(displayAGV, "AGV");
		displaySurf.addDisplayableProbeable(displayMachines, "Machines");
	}
	/**
	 * Launching SAJASAGVModel
	 * @param args
	 */
	public static void main(String[] args) {
		boolean runMode = !BATCH_MODE;   // BATCH_MODE or !BATCH_MODE

		SimInit init = new SimInit();
		init.setNumRuns(1);   // works only in batch mode
		AGVSajasModel model = new AGVSajasModel(runMode);
		init.loadModel(model, null, runMode);
	}


	public int getWORLD_X_SIZE() {
		return WORLD_X_SIZE;
	}

	public void setWORLD_X_SIZE(int WORLD_X_SIZE) {
		this.WORLD_X_SIZE = WORLD_X_SIZE;
	}

	public int getWORLD_Y_SIZE() {
		return WORLD_Y_SIZE;
	}

	public void setWORLD_Y_SIZE(int WORLD_Y_SIZE) {
		this.WORLD_Y_SIZE = WORLD_Y_SIZE;
	}

	public int getNum_agv_agents() {
		return num_agv_agents;
	}

	public void setNum_agv_agents(int num_agv_agents) {
		this.num_agv_agents = num_agv_agents;
	}

	public int getAgv_power() {
		return agv_power;
	}

	public void setAgv_power(int agv_power) {
		this.agv_power = agv_power;
	}

	public String getMachines_and_phases() {
		return machines_and_phases;
	}

	public void setMachines_and_phases(String machines_and_phases) {
		this.machines_and_phases = machines_and_phases;
	}

	public int getMachines_max_capacity() {
		return machines_max_capacity;
	}

	public void setMachines_max_capacity(int machines_max_capacity) {
		this.machines_max_capacity = machines_max_capacity;
	}

	public int getMachines_speed() {
		return machines_speed;
	}

	public void setMachines_speed(int machines_speed) {
		this.machines_speed = machines_speed;
	}

	public boolean isSeparate_containers() {
		return separate_containers;
	}

	public void setSeparate_containers(boolean separate_containers) {
		this.separate_containers = separate_containers;
	}

	public ArrayList<MachineAgent> getMachineAgents() {
		return machineAgents;
	}

	public void setMachineAgents(ArrayList<MachineAgent> machineAgents) {
		this.machineAgents = machineAgents;
	}

}
