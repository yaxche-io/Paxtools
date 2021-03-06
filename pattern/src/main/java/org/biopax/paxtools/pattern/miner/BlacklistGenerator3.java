package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.pattern.util.RelType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This class generates a blacklist for the given model. It uses a list of known ubiquitous molecule names.
 *
 * @author Ozgun Babur
 */
public class BlacklistGenerator3
{
	/**
	 * Known ubiquitous small molecule names along with their classification (ubiquitous as input, as output or both)
	 */
	private Map<String, RelType> knownNames;

	/**
	 * Constructor.
	 *
	 * @param knownNamesIS a special file that lists the known names of ubiquitous unwanted-to-traverse chemicals
	 */
	public BlacklistGenerator3(InputStream knownNamesIS)
	{
		knownNames = new HashMap<String, RelType>();
		Scanner sc = new Scanner(knownNamesIS);
		while (sc.hasNextLine())
		{
			String[] token = sc.nextLine().split("\t");
			RelType type = token.length < 2 ? null : token[1].equals("I") ?
				RelType.INPUT : RelType.OUTPUT;
			knownNames.put(token[0], type);
		}
	}

	/**
	 * Default Constructor.
	 *
	 * Uses the blacklisted small molecule names file from the classpath.
	 */
	public BlacklistGenerator3()
	{
		this(BlacklistGenerator3.class.getResourceAsStream("blacklist-names.txt"));
	}

	/**
	 * Generates the blacklist.
	 * @param model model to use
	 * @return the blacklist
	 */
	public Blacklist generateBlacklist(Model model)
	{
		Blacklist blacklist = new Blacklist();

		// populate the blacklist

		for (SmallMoleculeReference smr : model.getObjects(SmallMoleculeReference.class))
		{
			String name = smr.getDisplayName();
			if (name == null) continue;
			name = name.toLowerCase();

			if (knownNames.containsKey(name))
			{
				blacklist.addEntry(smr.getUri(), 1, knownNames.get(name));

				for (SimplePhysicalEntity spe : smr.getEntityReferenceOf())
				{
					blacklist.addEntry(spe.getUri(), 1, knownNames.get(name));
				}
			}
		}

		return blacklist;
	}
}
