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
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class AGVSajasModel extends Repast3Launcher {

	private static final boolean BATCH_MODE = true;
	public static final boolean SEPARATE_CONTAINERS = false;
	public static final boolean SHOW_LOTS_PER_PHASE_GRAPH = true;
	public static final boolean SHOW_NUM_REQUESTS_PER_AGV = true;
	public static Images images;

	private Space space;
	private DisplaySurface displaySurf;


	private ArrayList<Phase> lotsPerPhase;
	//opensequencegraph
	private OpenSequenceGraph lotsOnPhases;
	private OpenSequenceGraph numRequestsAgvs;


	class Phases implements DataSource, Sequence {
		private int phase = -1;


		public Phases(int phase){
			this.phase=phase;
		}
		public Object execute() {
			if(phase == -1)
				return 0;
			else
				return new Double(getSValue());
		}

		public double getSValue() {
			return (double)lotsPerPhase.get(phase).getLots();
		}
	}

	class Requests implements DataSource, Sequence {
		private int agv = -1;


		public Requests(int agv){
			this.agv=agv;
		}
		public Object execute() {
			return new Double(getSValue());
		}

		public double getSValue() {
			if(agv == -1)
				return 0;
			else
				return (double)agvAgents.get(agv).getPointsSize();
		}

	}


	private Parser parser;


	private ContainerController mainContainer;
	private ContainerController agentsContainer;

	private ArrayList<AGVAgent> agvAgents;
	private ArrayList<MachineAgent> machineAgents;

	//info init
	private Schedule schedule;

	private boolean separate_containers = SEPARATE_CONTAINERS;
	private boolean Show_lots_per_phase_graph = SHOW_LOTS_PER_PHASE_GRAPH;
	private boolean Show_num_requests_per_agv = SHOW_NUM_REQUESTS_PER_AGV;

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
		return new String[] {"Show_lots_per_phase_graph","Show_num_requests_per_agv"};
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
		space = new Space(parser.getWorld_x_size(), parser.getWorld_y_size(),parser.getPower_station_x(),parser.getPower_station_y());
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

		if(Show_lots_per_phase_graph)
			buildLotsPerPhaseGraph();
		if(Show_num_requests_per_agv)
			buildNumRequests_per_agv();
		displaySurf.display();

	}

	private void buildNumRequests_per_agv(){
		if(numRequestsAgvs !=null)
			numRequestsAgvs.dispose();
		numRequestsAgvs = null;
		numRequestsAgvs = new OpenSequenceGraph("Requests per AGV over ticks",this);
		numRequestsAgvs.setYRange(0,1);
		this.registerMediaProducer("Plot", numRequestsAgvs);

		class requestsNumUpdate extends BasicAction {
			public void execute(){
				numRequestsAgvs.step();
			}
		}
		schedule.scheduleActionAtInterval(10, new requestsNumUpdate());

		for(int i = 0; i < agvAgents.size();i++){
			numRequestsAgvs.addSequence("AGV "+ agvAgents.get(i).getAID().getLocalName(), new Requests(i));
		}
		numRequestsAgvs.display();
	}

	private void buildLotsPerPhaseGraph() {
		lotsPerPhase = getNumberLotsPerPhase();
		//graph
		if (lotsOnPhases != null){
			lotsOnPhases.dispose();
		}
		lotsOnPhases = null;
		lotsOnPhases = new OpenSequenceGraph("Lots Per Phase over ticks",this);
		this.registerMediaProducer("Plot", lotsOnPhases);

		class phasesUpdateInGraph extends BasicAction {
			public void execute(){
				updateHistogramNumberLotsPerPhase();
				lotsOnPhases.step();
			}
		}
		schedule.scheduleActionAtInterval(10, new phasesUpdateInGraph());

		for(int i = 0; i < lotsPerPhase.size();i++){
			lotsOnPhases.addSequence("Lots in phase "+(i+1), new Phases(i));
		}
		lotsOnPhases.display();
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

	public ArrayList<Phase>  getNumberLotsPerPhase(){
		HashMap<Integer,Integer> lotsPerPhase = new HashMap<>();

		for(int i = 0; i < machineAgents.size();i++){
			MachineAgent m = machineAgents.get(i);
			int phase = m.getProcessingStep();
			if(lotsPerPhase.containsKey(phase)){
				int temp = lotsPerPhase.get(phase);
				lotsPerPhase.replace(phase,temp + m.getLotsProducing());
			}
			else
				lotsPerPhase.put(m.getProcessingStep(),m.getLotsProducing());
		}
		ArrayList<Phase> res = new ArrayList<>();
		Iterator<Integer> keySetIterator = lotsPerPhase.keySet().iterator();
		while(keySetIterator.hasNext()){
			Integer key = keySetIterator.next();
			res.add(new Phase(key,lotsPerPhase.get(key)));
		}
		return res;
	}

	public void updateHistogramNumberLotsPerPhase(){
		ArrayList<Phase> temp = getNumberLotsPerPhase();
		for(int i = 0; i < temp.size();i++){
			Phase p = temp.get(i);
			lotsPerPhase.get(i).setLots(p.getLots());
		}
	}

	class Phase {
		private int phase;
		private int lots;
		public Phase(int phase, int lots){
			this.phase=phase;
			this.lots=lots;
		}

		public int getPhase() {
			return phase;
		}

		public int getLots() {
			return lots;
		}

		public void setLots(int lots){
			this.lots=lots;
		}
	}

	public boolean isShow_num_requests_per_agv() {
		return Show_num_requests_per_agv;
	}

	public void setShow_num_requests_per_agv(boolean show_num_requests_per_agv) {
		Show_num_requests_per_agv = show_num_requests_per_agv;
	}

	public boolean isShow_lots_per_phase_graph() {
		return Show_lots_per_phase_graph;
	}

	public void setShow_lots_per_phase_graph(boolean show_lots_per_phase_graph) {
		Show_lots_per_phase_graph = show_lots_per_phase_graph;
	}

	public int getTotalRequests(){
		int total = 0;
		for(int i = 0; i < agvAgents.size();i++){
			total+= agvAgents.get(i).getStatistics().size();
		}
		return total;
	}
}
