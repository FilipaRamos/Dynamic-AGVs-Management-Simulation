package Utils;

import com.bbn.openmap.omGraphics.grid.GridData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by danie on 10/12/2016.
 */
public class Images {
    private HashMap images = new HashMap();

    public Images() {
        try {
            for (int i = 1; i <= 5; i++) {
                for (int x = 0; x < 8; x++) {
                    String name = i + "_" + x;
                    Image image = ImageIO.read(new File("src/AGVimages/" + name+".jpg"));
                    images.put(name, image);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Image getImage(int initialPower,int power,int maxCapacity, int currentCapacity) {
        int bat = power*5/initialPower;
        int cap = currentCapacity*8/maxCapacity;
        if(bat == 0)
            bat=1;
        String im = bat+"_"+cap;
        return (Image) images.get(im);
    }

    /*public static void main(String[] args) {
        Images im = new Images();
        Image img = im.getImage(10,10,50,20);
        if(img == null)
            System.out.println("é nulo");
    }*/
    public static void main(String[] args)
    {
        HashMap<Integer,Integer> lotsPerPhase = new HashMap<Integer,Integer>();
        lotsPerPhase.put(1,5);
        lotsPerPhase.put(2,2);
        lotsPerPhase.put(3,7);
        lotsPerPhase.put(4,10);

        int x = lotsPerPhase.get(1);
        lotsPerPhase.replace(1,x+5);
        System.out.println("lots: "+lotsPerPhase.get(1));

    }
}

