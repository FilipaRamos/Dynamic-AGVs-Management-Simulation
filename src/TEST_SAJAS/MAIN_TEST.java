package TEST_SAJAS;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by danie on 04/12/2016.
 */
public class MAIN_TEST {
    public static void main(String[] args) {
        String line = "[222,23,2,2,24,24,24,20]";//O tamanho representa o número de fases e cada posição o número de máquinas

        String pattern = "\\p{Punct}\\d+(\\p{Punct}\\d+)*\\p{Punct}";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);
        if (m.matches()) {
            System.out.println("Encontrou!!");
        }else {
            System.out.println("NO MATCH");
        }
        String[] parts = line.split("\\p{Punct}");

        for(int i = 1; i < parts.length;i++)
            System.out.println("Parte "+i+" "+parts[i]);
    }
}
