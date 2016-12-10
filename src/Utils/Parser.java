package Utils;

import agents.AGVAgent;
import agents.MachineAgent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by danie on 07/12/2016.
 */
public class Parser {
    private File xmlFile;
    private Document doc;
    private int world_x_size=30,world_y_size=30;
    private int power_station_x=0,power_station_y=0;
    private ArrayList<AGVAgent> agvs;
    private ArrayList<MachineAgent> machines;

    public Parser(String path) throws ParserConfigurationException, IOException, SAXException {
        agvs = new ArrayList<>();
        machines = new ArrayList<>();
        xmlFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();
    }

    public void parse() throws IOException {
        NodeList worldSize = doc.getElementsByTagName("WORLD_SIZE");
        NodeList agvAgents = doc.getElementsByTagName("AGV_AGENTS");
        NodeList machineTag = doc.getElementsByTagName("MACHINES_AGENTS");
        NodeList machinesAgents = doc.getElementsByTagName("MACHINE_AGENT");
        NodeList power_station = doc.getElementsByTagName("POWER_STATION");

        if(worldSize.getLength() != 1)
            throw new IOException("Too many or non existent tags: WORLD_SIZE");
        else if(agvAgents.getLength() !=1 )
            throw new IOException("Too many or non existent tags: AGV_AGENTS");
        else if(machineTag.getLength() ==0)
            throw new IOException("Non existent tags: MACHINES_AGENTS");
        else if(power_station.getLength() != 1)
            throw new IOException("Non existent tags: POWER_STATION");
        else if(machinesAgents.getLength() == 0)
            throw new IOException("Thera are no machines!");

        //Get world sizes
        Node nNode = worldSize.item(0);
        Element eElement = (Element) nNode;
        world_x_size = Integer.parseInt(eElement.getElementsByTagName("WORLD_X_SIZE").item(0).getTextContent());
        world_y_size = Integer.parseInt(eElement.getElementsByTagName("WORLD_Y_SIZE").item(0).getTextContent());

        //Get power station
        nNode = power_station.item(0);
        eElement = (Element) nNode;
        power_station_x = Integer.parseInt(eElement.getElementsByTagName("x").item(0).getTextContent());
        power_station_y = Integer.parseInt(eElement.getElementsByTagName("y").item(0).getTextContent());

        //Get and build agvAgents
        nNode = agvAgents.item(0);
        eElement = (Element)nNode;
        int numAgvs = Integer.parseInt(eElement.getElementsByTagName("AGV_AGENTS_NUM").item(0).getTextContent());
        int powerAgvs = Integer.parseInt(eElement.getElementsByTagName("AGV_AGENT_POWER").item(0).getTextContent());
        int capacityAgvs = Integer.parseInt(eElement.getElementsByTagName("AGV_AGENT_CAPACITY").item(0).getTextContent());

        Random rand = new Random();
        for(int i = 0; i < numAgvs; i++){
            int x = rand.nextInt(world_x_size-1);
            int y = rand.nextInt(world_y_size-1);
            agvs.add(new AGVAgent(x,y,powerAgvs,capacityAgvs,0,0));/*power_station_x,power_station_y)*/
        }

        //Get and build Machine agents
        for(int i = 0; i < machinesAgents.getLength();i++){
            Node node = machinesAgents.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                int processID = Integer.parseInt(element.getAttribute("phase"));
                int stepID = Integer.parseInt(element.getAttribute("id"));
                int x = Integer.parseInt(element.getElementsByTagName("x").item(0).getTextContent());
                int y = Integer.parseInt(element.getElementsByTagName("y").item(0).getTextContent());
                int capacity = Integer.parseInt(element.getElementsByTagName("MACHINE_CAPACITY").item(0).getTextContent());
                int velocity = Integer.parseInt(element.getElementsByTagName("MACHINE_SPEED").item(0).getTextContent());

                MachineAgent m = new MachineAgent(processID,stepID,x,y,capacity,velocity);
                if(element.hasAttribute("lots")){
                    int lots = Integer.parseInt(element.getAttribute("lots"));
                    m.setLotsProducing(lots);
                }
                machines.add(m);
            }
        }
    }

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        Parser p = new Parser("src/XML_INIT.xml");
        p.parse();
        System.out.println("x: "+p.getPower_station_x()+" y: "+p.getPower_station_y());
    }

    public ArrayList<MachineAgent> getMachines() {
        return machines;
    }

    public void setMachines(ArrayList<MachineAgent> machines) {
        this.machines = machines;
    }

    public ArrayList<AGVAgent> getAgvs() {
        return agvs;
    }

    public void setAgvs(ArrayList<AGVAgent> agvs) {
        this.agvs = agvs;
    }

    public int getWorld_x_size() {
        return world_x_size;
    }

    public void setWorld_x_size(int world_x_size) {
        this.world_x_size = world_x_size;
    }

    public int getWorld_y_size() {
        return world_y_size;
    }

    public void setWorld_y_size(int world_y_size) {
        this.world_y_size = world_y_size;
    }

    public int getPower_station_x() {
        return power_station_x;
    }

    public void setPower_station_x(int power_station_x) {
        this.power_station_x = power_station_x;
    }

    public int getPower_station_y() {
        return power_station_y;
    }

    public void setPower_station_y(int power_station_y) {
        this.power_station_y = power_station_y;
    }
}
