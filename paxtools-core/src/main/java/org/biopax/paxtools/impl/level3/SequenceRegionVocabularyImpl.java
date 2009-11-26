package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;

/**
 */
public class SequenceRegionVocabularyImpl extends ControlledVocabularyImpl
	implements SequenceRegionVocabulary
{
    @Override
    public Class<? extends SequenceRegionVocabulary> getModelInterface() {
        return SequenceRegionVocabulary.class;
    }
}