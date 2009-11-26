package org.biopax.paxtools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.Level2Factory;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.sequenceParticipant;
import org.biopax.paxtools.model.level2.sequenceFeature;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * This class contains methods for handling reused PEPs.
 */
public class ReusedPEPHelper
{
    private static final Log log = LogFactory.getLog(ReusedPEPHelper.class);
    private final Model model;
    private final Level2Factory factory;
    private final Map<physicalEntityParticipant, physicalEntityParticipant> duplicatedPeps;

    /**
     * @param model
     * @param factory
     */
    public ReusedPEPHelper(Model model, Level2Factory factory)
    {
        this.model = model;
        this.factory = factory;
        duplicatedPeps =
                new HashMap<physicalEntityParticipant, physicalEntityParticipant>();

    }

    public Object fixReusedPEP(physicalEntityParticipant pep, BioPAXElement bpe)
    {
        if (duplicated(pep, bpe))
        {

            log.warn(pep.getRDFId() +
                     " is reused, duplicating it to fix");

            physicalEntityParticipant duplicated =
                    (physicalEntityParticipant) factory.reflectivelyCreate(
                    pep.getModelInterface());


            String syntheticID = createSyntheticID(pep, bpe);
            if (model.containsID(syntheticID))
            {
                pep = (physicalEntityParticipant) model.getByID(syntheticID);
            }
            else
            {
                duplicated.setRDFId(syntheticID);
                duplicatedPeps.put(duplicated, pep);
                model.add(duplicated);
                pep = duplicated;
            }
        }
        return pep;
    }

    private boolean duplicated(physicalEntityParticipant pep, BioPAXElement bpe)
    {
        boolean result = false;

        if (!pep.isPARTICIPANTSof().isEmpty())
        {
            if (pep.isPARTICIPANTSof().iterator().next().equals(bpe))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Unexpected multiple participant statements");
                }
            }
            else
            {
                result = true;
            }
        }
        else if (pep.isCOMPONENTof() != null)
        {
            if (pep.isCOMPONENTof().equals(bpe))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Unexpected multiple participant statements");
                }
            }
            else
            {
                result = true;
            }
        }

        return result;

    }

    private String createSyntheticID(physicalEntityParticipant pep,
                                     BioPAXElement bpe)
    {
        return "http://patywaycommons.org/synthetic"
               + createDataStringFromURI(pep.getRDFId(),bpe.getRDFId());
    }

    private String createDataStringFromURI(String... uris)
    {
        String ssp = "";
        String fragment = "";

        for (String uri : uris)
        {
            try
            {
                URI suri = new URI(uri);
                ssp += suri.getSchemeSpecificPart() + "_";
                fragment += suri.getFragment() + "_";
            }
            catch (URISyntaxException e)
            {
                throw new RuntimeException(e);
            }
        }
        return ssp + "#" + fragment;
    }

    public void copyPEPFields()
    {
        Set<physicalEntityParticipant> physicalEntityParticipants =
                duplicatedPeps.keySet();
        for (physicalEntityParticipant dup : physicalEntityParticipants)
        {
            copyPEPFields(dup, duplicatedPeps.get(dup));
        }

    }

    private void copyPEPFields(physicalEntityParticipant duplicated,
                               physicalEntityParticipant pep)
    {
        duplicated.setCELLULAR_LOCATION(pep.getCELLULAR_LOCATION());
        duplicated.setCOMMENT(pep.getCOMMENT());
        duplicated
                .setSTOICHIOMETRIC_COEFFICIENT(
                        pep.getSTOICHIOMETRIC_COEFFICIENT());
        duplicated.setPHYSICAL_ENTITY(pep.getPHYSICAL_ENTITY());
        if (pep instanceof sequenceParticipant)
        {
            Set<sequenceFeature> sfSet =
                    ((sequenceParticipant) pep)
                            .getSEQUENCE_FEATURE_LIST();
            for (sequenceFeature sf : sfSet)
            {
                ((sequenceParticipant) duplicated)
                        .addSEQUENCE_FEATURE_LIST(sf);
            }
        }
	}


}
