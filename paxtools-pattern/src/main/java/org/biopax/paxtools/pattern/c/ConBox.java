package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.pattern.Constraint;
import org.biopax.paxtools.pattern.MappedConst;

import java.util.Collection;
import java.util.Set;

/**
 * Some predefined constraints.
 *
 * @author Ozgun Babur
 */
public class ConBox
{
	public static Constraint erToPE()
	{
		return new PathConstraint("EntityReference/entityReferenceOf");
	}

	public static Constraint peToER()
	{
		return new PathConstraint("SimplePhysicalEntity/entityReference");
	}

	public static Constraint peToControl()
	{
		return new PathConstraint("PhysicalEntity/controllerOf");
	}

	public static Constraint controlled()
	{
		return new PathConstraint("Control/controlled");
	}

	public static Constraint controllerPE()
	{
		return new PathConstraint("Control/controller:PhysicalEntity");
	}

	public static Constraint controlToConv()
	{
		return new PathConstraint("Control/controlled*:Conversion");
	}

	public static Constraint controlToTempReac()
	{
		return new PathConstraint("Control/controlled*:TemplateReaction");
	}

	public static Constraint controlToInter()
	{
		return new PathConstraint("Control/controlled*:Interaction");
	}

	public static Constraint convToControl()
	{
		return new PathConstraint("Conversion/controlledOf*");
	}

	public static Constraint controlsConv()
	{
		return new PathConstraint("PhysicalEntity/controllerOf/controlled*:Conversion");
	}

	public static Constraint genericEquiv()
	{
		return new SelfOrThis(new MultiPathConstraint("PhysicalEntity/memberPhysicalEntity*",
			"PhysicalEntity/memberPhysicalEntityOf*"));
	}

	public static Constraint complexMembers()
	{
		return new PathConstraint("Complex/component*");
	}

	public static Constraint simpleMembers()
	{
		return new PathConstraint("Complex/component*:SimplePhysicalEntity");
	}

	public static Constraint withComplexMembers()
	{
		return new SelfOrThis(complexMembers());
	}

	public static Constraint withSimpleMembers()
	{
		return new SelfOrThis(simpleMembers());
	}

	public static Constraint complexes()
	{
		return new PathConstraint("PhysicalEntity/componentOf*");
	}

	public static Constraint withComplexes()
	{
		return new SelfOrThis(complexes());
	}

	public static Constraint left()
	{
		return new PathConstraint("Conversion/left");
	}

	public static Constraint right()
	{
		return new PathConstraint("Conversion/right");
	}

	public static Constraint participatesInConv()
	{
		return new PathConstraint("PhysicalEntity/participantOf:Conversion");
	}

	public static Constraint participatesInInter()
	{
		return new PathConstraint("PhysicalEntity/participantOf");
	}

	public static Constraint compToER()
	{
		return new MultiPathConstraint("Complex/component*:SimplePhysicalEntity/entityReference",
			"SimplePhysicalEntity/entityReference");
	}

	public static Constraint nameEquals(Collection names)
	{
		return new FieldConstraint("Named/name", names);
	}

	public static Constraint notUbique(Set<String> ubiques)
	{
		return new NOT(new IDConstraint(ubiques));
	}
	
	public static Constraint differentialActivity(boolean activating)
	{
		if (activating) return new AND(new MappedConst(new ActivityConstraint(true), 1), new MappedConst(new ActivityConstraint(false), 0));
		else return new AND(new MappedConst(new ActivityConstraint(true), 0), new MappedConst(new ActivityConstraint(false), 1));
	}

	public static Constraint inOrOutConv()
	{
		return new PathConstraint("PhysicalEntity/participantOf:Conversion");
	}

	public static Constraint participant()
	{
		return new PathConstraint("Interaction/participant:PhysicalEntity");
	}

	public static Constraint participantER()
	{
		new PathAccessor("Interaction/participant:PhysicalEntity/entityReference");
		return new MultiPathConstraint("Interaction/participant:PhysicalEntity/entityReference",
			"Interaction/participant:PhysicalEntity/component*/entityReference");
	}

	public static Constraint product()
	{
		return new PathConstraint("TemplateReaction/product");
	}

	public static Constraint notControlled()
	{
		return new Empty(new PathConstraint("Interaction/controlledOf"));
	}

	public static Constraint isHuman()
	{
		return new Field(new PathAccessor("SequenceEntityReference/organism/displayName"), "Homo sapiens");
	}
}