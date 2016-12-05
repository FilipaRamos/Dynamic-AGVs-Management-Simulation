package TEST_SAJAS;

import TEST.ConsumerAgent;
import TEST.ProviderAgent;
import TEST.ResultsCollector;
import agents.AGVAgent;
import agents.MachineAgent;
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Network2DDisplay;
import uchicago.src.sim.gui.OvalNetworkItem;
import uchicago.src.sim.network.DefaultDrawableNode;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AGVSajasModel extends Repast3Launcher {

	private static final boolean BATCH_MODE = true;
	public static final boolean SEPARATE_CONTAINERS = false;
	private static final int NUM_AGV_AGENTS = 10;
	private static int NUM_MACHINES_AGENTS = 6;
	private static final int NUM_MACHINE_PHASES = 3;
	private static final int MACHINES_CAPACITY = 50;
	private static final int MACHINES_VELOCITY = 5;

	private ContainerController mainContainer;
	private ContainerController agentsContainer;

	private ArrayList<AGVAgent> agvAgents;
	private ArrayList<MachineAgent> machineAgents;

	
	private boolean runInBatchMode;
	
	public AGVSajasModel(boolean runInBatchMode) {
		super();
		this.runInBatchMode = runInBatchMode;
	}




	@Override
	public String[] getInitParam() {
		return new String[] {"SeparateContainers"};
	}

	@Override
	public String getName() {
		return "AGVModel -- SAJaS Repast3";
	}

	@SuppressWarnings("Duplicates")
	@Override
	protected void launchJADE() {
		
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);
		
		if(SEPARATE_CONTAINERS) {
			Profile p2 = new ProfileImpl();
			agentsContainer = rt.createAgentContainer(p2);
		} else {
			agentsContainer = mainContainer;
		}
		
		launchAgents();
	}
	@SuppressWarnings("Duplicates")
	private void launchAgents() {
		agvAgents = new ArrayList<AGVAgent>();
		machineAgents = new ArrayList<MachineAgent>();

		
		try {
			
			AID resultsCollectorAID = null;
			
			// create agv's
			// agv agents
			for (int i = 0; i < NUM_AGV_AGENTS; i++) {
				AGVAgent agv = new AGVAgent();
				String name = "Agent: "+agv.getID();
				agvAgents.add(agv);
				agentsContainer.acceptNewAgent(name, agv).start();
			}
			// machines agents
			int machines_per_phase = NUM_MACHINES_AGENTS/NUM_MACHINE_PHASES;
			for(int i = 1; i <= NUM_MACHINE_PHASES;i++){
				for(int x = 1; x <= machines_per_phase;x++){

				}
			}
			/*for (int i = 0; i < NUM_MACHINES_AGENTS; i++) {
				MachineAgent pa = new MachineAgent();
				agentContainer.acceptNewAgent("BadProvider" + i, pa).start();
				DefaultDrawableNode node = 
						generateNode("BadProvider" + i, Color.GRAY,
								WIDTH/2+random.nextInt(WIDTH/2),random.nextInt(HEIGHT/2));
			}*/

		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
	}

	private DefaultDrawableNode generateNode(String label, Color color, int x, int y) {
        OvalNetworkItem oval = new OvalNetworkItem(x,y);
        oval.allowResizing(false);
        oval.setHeight(5);
        oval.setWidth(5);
        
		DefaultDrawableNode node = new DefaultDrawableNode(label, oval);
		node.setColor(color);
        
		return node;
	}

	@Override
	public void begin() {
		super.begin();
		if(!runInBatchMode) {
			buildAndScheduleDisplay();
		}
	}
	
	private DisplaySurface dsurf;
	private int WIDTH = 200, HEIGHT = 200;

	@SuppressWarnings("Duplicates")
	private void buildAndScheduleDisplay() {

		// display surface
		if (dsurf != null)
			dsurf.dispose();
		dsurf = new DisplaySurface(this, "Service Consumer/Provider Display");
		registerDisplaySurface("Service Consumer/Provider Display", dsurf);
		Network2DDisplay display = new Network2DDisplay(nodes,WIDTH,HEIGHT);
		dsurf.addDisplayableProbeable(display, "Network Display");
        dsurf.addZoomable(display);
        addSimEventListener(dsurf);
		dsurf.display();


		
		getSchedule().scheduleActionAtInterval(1, dsurf, "updateDisplay", Schedule.LAST);
	}


	/**
	 * Launching SAJASAGVModel
	 * @param args
	 */
	public static void main(String[] args) {
		boolean runMode = !BATCH_MODE;   // BATCH_MODE or !BATCH_MODE

		SimInit init = new SimInit();
		init.setNumRuns(1);   // works only in batch mode
		init.loadModel(new AGVSajasModel(runMode), null, runMode);
	}

}
