/*
 * DnaProxy.java
 *
 * 2007.04.05 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.dna;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * Proxy for dna
 */
@Entity(name="l2dna")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class dnaProxy extends sequenceEntityProxy implements dna, Serializable {
	public dnaProxy() {
	}
	@Transient
	public Class getModelInterface()
	{
		return dna.class;
	}

}

