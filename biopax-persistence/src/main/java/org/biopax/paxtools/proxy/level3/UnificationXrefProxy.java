/*
 * UnificationXrefProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * Proxy for unificationXref
 */
@Entity(name="l3unificationxref")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class UnificationXrefProxy extends XrefProxy implements UnificationXref, Serializable {
	public UnificationXrefProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return UnificationXref.class;
	}
}

