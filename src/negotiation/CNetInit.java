package negotiation;

import agents.MachineAgent;
import jade.lang.acl.ACLMessage;
import sajas.core.AID;
import sajas.core.Agent;
import sajas.proto.ContractNetInitiator;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by utilizador on 05/12/2016.
 */
public class CNetInit extends ContractNetInitiator {

    private static final long serialVersionUID = 1L;

    public CNetInit(Agent owner, ACLMessage cfp) {
        super(owner, cfp);
    }

    @Override
    public Vector prepareCfps(ACLMessage cfp) {
        // select best providers
        ArrayList<AID> bestProviders = ((MachineAgent) myAgent).getBestProviders();
        for(AID provider : bestProviders) {
            cfp.addReceiver(provider);
        }

        return super.prepareCfps(cfp);
    }

    @Override
    protected void handleRefuse(ACLMessage refuse) {
    }

    @Override
    protected void handlePropose(ACLMessage propose, Vector acceptances) {
    }

    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {

        double servicePrice;
        double bestServicePrice = Double.MAX_VALUE;
        ACLMessage response;
        ACLMessage bestServiceProposalMessage = null;
        for(Object obj : responses) {
            response = (ACLMessage) obj;
            if (response.getPerformative() == ACLMessage.PROPOSE) {
/*
                try {
                    servicePrice = ((ServiceProposal) getContentManager().extractContent(response)).getPrice();
                    if(servicePrice < bestServicePrice) {
                        // new best proposal
                        if(bestServiceProposalMessage != null) {
                            // reject previous best
                            ACLMessage reject = bestServiceProposalMessage.createReply();
                            reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
                            acceptances.add(reject);
                        }
                        // update best
                        bestServicePrice = servicePrice;
                        bestServiceProposalMessage = response;
                    } else {
                        // reject proposal
                        ACLMessage reject = response.createReply();
                        reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        acceptances.add(reject);
                    }
                } catch (ACLCodec.CodecException | OntologyException e) {
                    e.printStackTrace();
                }*/
            }
        }

        if(bestServiceProposalMessage != null) {
            // accept winner
            ACLMessage accept = bestServiceProposalMessage.createReply();
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            acceptances.add(accept);

        } else {
            System.out.println(myAgent.getLocalName() + ": no provider available");
            //contractOutcomes.add(new ContractOutcome(ContractOutcome.Value.FAILURE));
        }
    }

    @Override
    protected void handleFailure(ACLMessage failure) {
        //((MachineAgent) myAgent).addProviderOutcome(failure.getSender(), ContractOutcome.Value.FAILURE);
        //contractOutcomes.add(new ContractOutcome(ContractOutcome.Value.FAILURE));
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        /*((MachineAgent) myAgent).addProviderOutcome(inform.getSender(), ContractOutcome.Value.SUCCESS);
        contractOutcomes.add(new ContractOutcome(ContractOutcome.Value.SUCCESS));*/
    }

    @Override
    protected void handleAllResultNotifications(Vector resultNotifications) {
    }

}
