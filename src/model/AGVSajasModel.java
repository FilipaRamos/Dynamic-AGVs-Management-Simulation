package model;

import Utils.Images;
import Utils.Parser;
import agents.AGVAgent;
import agents.MachineAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import org.xml.sax.SAXException;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
import spaces.Space;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;

public class AGVSajasModel extends Repast3Launcher {

	private static final boolean BATCH_MODE = true;
	public static final boolean SEPARATE_CONTAINERS = false;
	public static Images images;

	private Space space;
	private DisplaySurface displaySurf;


	private Parser parser;


	private ContainerController mainContainer;
	private ContainerController agentsContainer;

	private ArrayList<AGVAgent> agvAgents;
	private ArrayList<MachineAgent> machineAgents;

	//info init
	private Schedule schedule;

	private boolean separate_containers = SEPARATE_CONTAINERS;


	private boolean runInBatchMode;
	
	public AGVSajasModel(boolean runInBatchMode) {
		super();
		try {
			parser = new Parser("src/XML_INIT.xml");
			parser.parse();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		images = new Images();
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

	private void launchAgents() {
		try {
			//MACHINES INITIALIZATION
			ArrayList<MachineAgent> m = parser.getMachines();
			for(int i = 0; i < m.size();i++){
				MachineAgent mach = m.get(i);
				String name = "Agent: " + mach.getID();
				machineAgents.add(mach);
				agentsContainer.acceptNewAgent(name, mach).start();
			}

			// create agv's
			ArrayList<AGVAgent> a = parser.getAgvs();
			for (int i = 0; i < a.size(); i++) {
				//AGVAgent agv = new AGVAgent(agv_power);
				AGVAgent agv = a.get(i);
				String name = "Agent: " + agv.getID();
				for(int x = 0; x < machineAgents.size();x++){
					agv.addMachineLocation(machineAgents.get(x).getAID(),machineAgents.get(x).getX(),machineAgents.get(x).getY());
				}
				agvAgents.add(agv);
				agentsContainer.acceptNewAgent(name, agv).start();
			}

			/*agvAgents.get(0).addRequest(machineAgents.get(0).getAID() + "&" + machineAgents.get(3).getAID());
            agvAgents.get(0).addRequest(machineAgents.get(3).getAID() + "&" + machineAgents.get(5).getAID());
            agvAgents.get(0).addRequest(machineAgents.get(1).getAID() + "&" + machineAgents.get(2).getAID());
			agvAgents.get(0).addRequest(machineAgents.get(2).getAID() + "&" + machineAgents.get(4).getAID());

            agvAgents.get(1).addRequest(machineAgents.get(0).getAID() + "&" + machineAgents.get(2).getAID());
            agvAgents.get(1).addRequest(machineAgents.get(1).getAID() + "&" + machineAgents.get(3).getAID());

            agvAgents.get(2).addRequest(machineAgents.get(2).getAID() + "&" + machineAgents.get(5).getAID());
            agvAgents.get(2).addRequest(machineAgents.get(3).getAID() + "&" + machineAgents.get(4).getAID());
            agvAgents.get(2).addRequest(machineAgents.get(1).getAID() + "&" + machineAgents.get(2).getAID());

            agvAgents.get(3).addRequest(machineAgents.get(3).getAID() + "&" + machineAgents.get(5).getAID());
            agvAgents.get(3).addRequest(machineAgents.get(1).getAID() + "&" + machineAgents.get(4).getAID());

            agvAgents.get(4).addRequest(machineAgents.get(3).getAID() + "&" + machineAgents.get(4).getAID());
            agvAgents.get(4).addRequest(machineAgents.get(2).getAID() + "&" + machineAgents.get(3).getAID());
            agvAgents.get(4).addRequest(machineAgents.get(2).getAID() + "&" + machineAgents.get(5).getAID());*/

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
			System.out.println("!SETUP!");
			buildAndScheduleDisplay();
		}

	}

	private void buildAndScheduleDisplay() {
		//build space
		space = new Space(parser.getWorld_x_size(), parser.getWorld_y_size());
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

		addSimEventListener(displaySurf);
		System.out.println("Running BuildDisplay");
		buildDisplayAgents();

		displaySurf.display();
	}

	public void buildDisplayAgents() {

		Object2DDisplay displayBackground= new Object2DDisplay(space.getCurrentBackgroundSpace());

		Object2DDisplay displayAGV= new Object2DDisplay(space.getCurrentAGVSpace());
		displayAGV.setObjectList(agvAgents);

        Object2DDisplay displayMachines= new Object2DDisplay(space.getCurrentMachineSpace());
		displayMachines.setObjectList(machineAgents);

		displaySurf.addDisplayable(displayBackground,"Background");
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

	public boolean isSeparate_containers() {
		return separate_containers;
	}

	public void setSeparate_containers(boolean separate_containers) {
		this.separate_containers = separate_containers;
	}
}
