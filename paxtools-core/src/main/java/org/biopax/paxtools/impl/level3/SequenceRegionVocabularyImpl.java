package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */
@Entity
public class SequenceRegionVocabularyImpl extends ControlledVocabularyImpl
	implements SequenceRegionVocabulary
{
    @Override @Transient
    public Class<? extends SequenceRegionVocabulary> getModelInterface() {
        return SequenceRegionVocabulary.class;
    }
}