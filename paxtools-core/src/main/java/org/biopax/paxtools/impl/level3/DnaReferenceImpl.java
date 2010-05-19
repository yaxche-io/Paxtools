package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.DnaReference;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;

@Entity
@Indexed(index=BioPAXElementImpl.SEARCH_INDEX_FOR_UTILILTY_CLASS)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class DnaReferenceImpl extends NucleicAcidReferenceImpl implements
		DnaReference
{
	public DnaReferenceImpl() {
	}

	@Override @Transient
	public Class<? extends DnaReference> getModelInterface()
	{                                        
		return DnaReference.class;
	}

}

