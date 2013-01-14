package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */
@Entity
@Proxy(proxyClass= SequenceRegionVocabulary.class)
@Indexed
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SequenceRegionVocabularyImpl extends ControlledVocabularyImpl
	implements SequenceRegionVocabulary
{
	public SequenceRegionVocabularyImpl() {
	}
	
    @Override @Transient
    public Class<? extends SequenceRegionVocabulary> getModelInterface() {
        return SequenceRegionVocabulary.class;
    }
}