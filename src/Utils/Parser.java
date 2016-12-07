package Utils;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * Created by danie on 07/12/2016.
 */
public class Parser {
    private File xmlFile;
    private Document doc;
    private int world_x_size=30,getWorld_y_size=30;

    public Parser(String path) throws ParserConfigurationException, IOException, SAXException {
        xmlFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();
        System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
    }

    public void parse() throws IOException {
        NodeList worldsize = doc.getElementsByTagName("staff");
        if(worldsize.getLength() != 1)
            throw new IOException("ERROR ON WORLD SIZES");
        else{
            //world_x_size = worldsize.item(0).getUserData("")
        }
    }

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        Parser p = new Parser("src/XML_INIT.xml");
    }
}
