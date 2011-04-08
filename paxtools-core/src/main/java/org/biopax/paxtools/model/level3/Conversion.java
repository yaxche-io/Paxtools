package org.biopax.paxtools.model.level3;

import java.util.Set;


public interface Conversion extends Interaction {


	public Set<PhysicalEntity> getLeft();

	void addLeft(PhysicalEntity left);

	void removeLeft(PhysicalEntity left);



	// Property PARTICIPANT-STOICHIOMETRY

	Set<Stoichiometry> getParticipantStoichiometry();

	void addParticipantStoichiometry(Stoichiometry stoichiometry);

	void removeParticipantStoichiometry(Stoichiometry stoichiometry);


	// Property RIGHT

	public Set<PhysicalEntity> getRight();

	void addRight(PhysicalEntity right);

	void removeRight(PhysicalEntity right);



	// Property SPONTANEOUS

    public Boolean getSpontaneous();

    public void setSpontaneous(Boolean spontaneous);

    //Property CONVERSION DIRECTION

    public ConversionDirectionType getConversionDirection();

	public void setConversionDirection(ConversionDirectionType conversionDirection);


}
