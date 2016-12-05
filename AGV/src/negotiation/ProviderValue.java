package negotiation;

import TEST.onto.ContractOutcome;
import sajas.core.AID;

/**
 * Created by utilizador on 05/12/2016.
 */
public class ProviderValue implements Comparable<ProviderValue> {
    private final double INITIAL_VALUE = 0.5;

    private AID provider;
    private int nSuccess = 0;
    private int nFailure = 0;
    private double value;

    ProviderValue(AID provider) {
        this.provider = provider;
        this.value = INITIAL_VALUE;
    }

    public AID getProvider() {
        return provider;
    }

    public void addOutcome(ContractOutcome.Value outcome) {
        switch(outcome) {
            case SUCCESS:
                nSuccess++;
                break;
            case FAILURE:
                nFailure++;
                break;
        }
        value = (double) nSuccess/(nSuccess+nFailure);
    }

    public double getValue() {
        return value;
    }

    @Override
    public int compareTo(ProviderValue o) {
        // descending order
        if(o.value < value) {
            return -1;
        } else if(o.value > value) {
            return 1;
        } else {
            return provider.compareTo(o.getProvider());
        }
    }

}
