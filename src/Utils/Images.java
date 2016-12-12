package Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by danie on 10/12/2016.
 */
public class Images {
    private final HashMap images = new HashMap();

    public Images() {
        try {
            for (int i = 1; i <= 5; i++) {
                for (int x = 1; x <= 8; x++) {
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
        if(cap == 0)
            cap = 1;
        String im = bat+"_"+cap;
        return (Image) images.get(im);
    }

    public static void main(String[] args) {
        Images im = new Images();
        Image img = im.getImage(10,10,50,20);
        if(img == null)
            System.out.println("é nulo");
    }
}

