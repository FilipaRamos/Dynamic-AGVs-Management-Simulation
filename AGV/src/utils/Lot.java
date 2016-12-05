package utils;

/**
 * Created by utilizador on 05/12/2016.
 */
public class Lot {

    protected String name;
    protected int pieces;

    /**
     * Lot Object Constructor
     * @param name
     * @param processingTime
     */
    public Lot(String name, int processingTime, int nrPieces){
        this.name = name;
        this.pieces = nrPieces;
    }

    public String getName(){
        return name;
    }

    public int getPieces(){
        return pieces;
    }

    public void setName(String newName){
        name = newName;
    }

    public void setPieces(int nrPieces){
        pieces = nrPieces;
    }

}
