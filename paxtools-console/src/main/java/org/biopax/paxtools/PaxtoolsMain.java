package org.biopax.paxtools;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.*;
import org.biopax.paxtools.converter.LevelUpgrader;
import org.biopax.paxtools.converter.psi.PsiToBiopax3Converter;
import org.biopax.paxtools.io.*;
import org.biopax.paxtools.io.gsea.GSEAConverter;
import org.biopax.paxtools.io.sbgn.L3ToSBGNPDConverter;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level2.entity;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.miner.*;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.query.QueryExecuter;
import org.biopax.paxtools.query.algorithm.Direction;
import org.biopax.paxtools.client.BiopaxValidatorClient;
import org.biopax.paxtools.client.BiopaxValidatorClient.RetFormat;
import org.biopax.validator.jaxb.Behavior;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * A command line accessible utility for basic Paxtools operations.
 */
public class PaxtoolsMain {

    public static Log log = LogFactory.getLog(PaxtoolsMain.class);
    private static SimpleIOHandler io = new SimpleIOHandler();

    public static void main(String[] argv) throws IOException, 
    InvocationTargetException, IllegalAccessException 
    {
        io.mergeDuplicates(true);
        if (argv.length == 0) {
            help();
        } else {
	        String command = argv[0];
	        if(command.startsWith("--"))  //accept both w and w/out --
		        command = command.substring(2);
	        Command.valueOf(command).run(argv);
        }
    }

    public static void toGSEA(String[] argv) throws IOException
    {
    	Model model = io.convertFromOWL(getInputStream(argv[1]));
		GSEAConverter gseaConverter; //to be initialized below

    	boolean specCheckEnabled = (argv.length>4) && Boolean.parseBoolean(argv[4]);

		if(argv.length < 6 || argv[5].equalsIgnoreCase("false")) {
			log.info("Collecting proteins for a pathway, the converter will also consider " +
					"its sub-pathways, their sub-pathways, etc.");
			gseaConverter = new GSEAConverter(argv[3], specCheckEnabled);
		} else {
			boolean skipAllSubPathways = Boolean.parseBoolean(argv[5]);
			if (skipAllSubPathways) {
				log.info("The converter won't traverse into sub-pathways of any pathway to collect its protein IDs.");
				gseaConverter = new GSEAConverter(argv[3], specCheckEnabled, true);
			} else { //must be a list of data source URIs, or it's an error...
				final Set<Provenance> skipSubPathways = new HashSet<Provenance>();

				for(String uri: argv[5].split(";")) {
					Provenance pro = (Provenance) model.getByID(uri);
					if(pro != null) {
						skipSubPathways.add(pro);
						log.info("GSEA converter won't traverse into sub-pathways of pathways from the data source: "
								+ uri);
					} else {
						log.error("No Provenance found by uri: " + uri + " (thus - ignored)");
					}
				}

				if(skipSubPathways.isEmpty()) {
					throw new IllegalArgumentException("The last arg. of the Paxtools command is bad; " +
							"no known Provenance found (if these were semicolon-separated Provenance URIs): " + argv[5]);
				} else {
					log.info("Collecting proteins, the converter will skip sub-pathways of pathways " +
							"of the following datasources: " + skipSubPathways.toString());
					gseaConverter = new GSEAConverter(argv[3], specCheckEnabled, skipSubPathways);
				}
			}
		}

		//convert and write
		gseaConverter.writeToGSEA(model, new FileOutputStream(argv[2]));
    }

    public static void getNeighbors(String[] argv) throws IOException
    {
        // set strings vars
        String in = argv[1];
        String[] ids = argv[2].split(",");
        String out = argv[3];

        // read BioPAX from the file
        Model model = io.convertFromOWL(getInputStream(in));

        // get elements (entities)
        Set<BioPAXElement> elements = new HashSet<BioPAXElement>();
        for (Object id : ids) {
            BioPAXElement e = model.getByID(id.toString());
            if (e != null && (e instanceof Entity || e instanceof entity)) {
                elements.add(e);
            } else {
                log.warn("Source element not found: " + id);
            }
        }

        // execute the 'nearest neighborhood' query
        Set<BioPAXElement> result = QueryExecuter
        		.runNeighborhood(elements, model, 1, Direction.BOTHSTREAM);

        // auto-complete/clone the results in a new model
        // (this also cuts some less important edges, right?..)
        Completer c = new Completer(io.getEditorMap());
        result = c.complete(result, model);
        Cloner cln = new Cloner(io.getEditorMap(), io.getFactory());
        model = cln.clone(model, result); // new sub-model

        if (model != null) {
            log.info("Elements in the result model: " + model.getObjects().size());
            // export to OWL
            io.convertToOWL(model, new FileOutputStream(out));
        } else {
            log.error("NULL model returned.");
        }
    }

    public static void fetch(String[] argv) throws IOException {

        // set strings vars
        String in = argv[1];
        String[] uris = argv[2].split(",");
        String out = argv[3];

        Model model = io.convertFromOWL(getInputStream(in));
        io.setFactory(model.getLevel().getDefaultFactory());
        // extract and save the sub-model (defined by ids)
        io.convertToOWL(model, new FileOutputStream(out), uris);
    }

    
    /*
     * Detects and converts a BioPAX Level 1, 2,
     * or PSI-MI/MITAB model to BioPAX Level3.
     * 
     * (-Dpaxtools.converter.psi.interaction=complex
     * java option can be used to generate Complex 
     * instead of MolecularInteraction entities from PSI interactions).
     */
    public static void toLevel3(String[] argv) throws IOException {
    	final String input = argv[1];
    	final String output = argv[2];
    	InputStream is = getInputStream(input);
    	FileOutputStream os = new FileOutputStream(output);
    	
    	boolean forcePsiInteractionToComplex = false;
    	String val = System.getProperty("paxtools.converter.psi.interaction");
    	if("complex".equalsIgnoreCase(val))
    		forcePsiInteractionToComplex = true;    	
    	Type type = detect(input);

    	try {
    		switch (type) {

    		case BIOPAX:
    			Model model = io.convertFromOWL(is);
    			model = (new LevelUpgrader()).filter(model);
    			if (model != null) {
    				io.setFactory(model.getLevel().getDefaultFactory());
    				io.convertToOWL(model, os); //os is closed already
    			}
    			break;

    		case PSIMI:
    			PsiToBiopax3Converter psimiConverter = new PsiToBiopax3Converter();
    			psimiConverter.convert(is, os, forcePsiInteractionToComplex);
    			os.close();
    			break;

    		default: //MITAB
    			psimiConverter = new PsiToBiopax3Converter();
    			psimiConverter.convertTab(is, os, forcePsiInteractionToComplex);
    			os.close();
    			break;
    		}

    	} catch (Exception e) {
    		throw new RuntimeException("Failed to convert " +
    				input + "to BioPAX L3", e);
    	}
    }
    
    private enum Type {
    	BIOPAX,
    	PSIMI,
    	PSIMITAB
    }
    
    //read a few lines to detect it's a BioPAX vs. PSI-MI vs. PSI-MITAB data.
	private static Type detect(String input) {		
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(input));
			int linesToCheck = 20;
			while (linesToCheck-- > 0) {
				sb.append(reader.readLine()).append('\n');
			}
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		String buf = sb.toString();
		if (buf.contains("<rdf:RDF") && buf.contains("http://www.biopax.org/release/biopax")) {
			return Type.BIOPAX;
		}
		else if (buf.contains("<entrySet") && buf.contains("http://psidev.sourceforge.net/mi/rel25/")) {
			return Type.PSIMI;
		}
		else
			return Type.PSIMITAB; //default/guess
	}

    /*
     *  Converts a BioPAX file to SBGN and saves it in a file.
	 *
     */
    public static void toSBGN(String[] argv) throws IOException
    {
        String input = argv[1];
        String output = argv[2];

        Model model = io.convertFromOWL(getInputStream(input));
        L3ToSBGNPDConverter l3ToSBGNPDConverter = new L3ToSBGNPDConverter();
        l3ToSBGNPDConverter.writeSBGN(model, output);
    }

    
    /*
     * Checks files by the online BioPAX validator using the built-in BioPAX validator client.
     * 
     * See about <a href="http://www.biopax.org/validator">BioPAX Validator Webservice</a>
     */
    public static void validate(String[] argv) throws IOException 
    {
        String input = argv[1];
        String output = argv[2];
        // default options
        RetFormat outf = RetFormat.HTML;
        boolean fix = false;
        Integer maxErrs = null;
        Behavior level = null; //will report both errors and warnings
        String profile = null;
        
        // match optional args
		for (int i = 3; i < argv.length; i++) {
			if ("html".equalsIgnoreCase(argv[i])) {
				outf = RetFormat.HTML;
			} else if ("xml".equalsIgnoreCase(argv[i])) {
				outf = RetFormat.XML;
			} else if ("biopax".equalsIgnoreCase(argv[i])) {
				outf = RetFormat.OWL;
			} else if ("auto-fix".equalsIgnoreCase(argv[i])) {
				fix = true;
			} else if ("only-errors".equalsIgnoreCase(argv[i])) {
				level = Behavior.ERROR;
			} else if ((argv[i]).toLowerCase().startsWith("maxerrors=")) {
				String num = argv[i].substring(10);
				maxErrs = Integer.valueOf(num);
			} else if ("notstrict".equalsIgnoreCase(argv[i])) {
				profile = "notstrict";
			}
		}

        Collection<File> files = new HashSet<File>();
        File fileOrDir = new File(input);
        if (!fileOrDir.canRead()) {
            System.out.println("Cannot read " + input);
        }

        // collect files
        if (fileOrDir.isDirectory()) {
            // validate all the OWL files in the folder
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".owl"));
                }
            };
            for (String s : fileOrDir.list(filter)) {
                files.add(new File(fileOrDir.getCanonicalPath()
                        + File.separator + s));
            }
        } else {
            files.add(fileOrDir);
        }

        // upload and validate using the default URL:
        // http://www.biopax.org/biopax-validator/check.html
        OutputStream os = new FileOutputStream(output);
        try {
            if (!files.isEmpty()) {
                BiopaxValidatorClient val = new BiopaxValidatorClient();
                val.validate(fix, profile, outf, level, maxErrs, null, files.toArray(new File[]{}), os);
            }
        } catch (Exception ex) {
            // fall-back: not using the remote validator; trying to read files
            String msg = "Unable to check with the biopax-validator web service: \n " +
            		ex.toString() + 
            		"\n Fall-back: trying to parse the file(s) with paxtools " +
            		"(up to the first syntax error in each file)...\n";
            log.error(msg, ex);
            os.write(msg.getBytes());

            for (File f : files) {
                try {
                    Model m = io.convertFromOWL(getInputStream(f.getPath()));
                    msg = "Model that contains "
                            + m.getObjects().size()
                            + " elements is successfully created from " 
                            + f.getPath() 
                            + " (check the console output for warnings).\n";
                    os.write(msg.getBytes());
                } catch (Exception e) {
                    msg = "Error: " + e + 
                    		" in building a BioPAX Model from: " +
                    		f.getPath() + "\n";
                    os.write(msg.getBytes());
                    e.printStackTrace();
                    log.error(msg);
                }
                os.flush();
            }
        }
    }

    public static void toSifnx(String[] argv) throws IOException {
        CommonIDFetcher idFetcher = new CommonIDFetcher();
		idFetcher.setUseUniprotIDs(argv.length > 3 && argv[3].equals("uniprot"));
		SIFSearcher searcher = new SIFSearcher(idFetcher, SIFEnum.values());
		File blacklistFile = new File("blacklist.txt");
		if(blacklistFile.exists()) {
			log.info("toSifnx: will use the blacklist.txt (found in the current directory)");
			searcher.setBlacklist(new Blacklist(new FileInputStream(blacklistFile)));
		} else {
			log.info("toSifnx: not blacklisting ubiquitous molecules (no blacklist.txt found)");
		}
        Model model = getModel(io, argv[1]);
        ModelUtils.mergeEquivalentInteractions(model);
		Set<SIFInteraction> binaryInts = searcher.searchSIF(model);
		OldFormatWriter.write(binaryInts, new FileOutputStream(argv[2]));
    }

    public static void toSif(String[] argv) throws IOException {
		CommonIDFetcher idFetcher = new CommonIDFetcher();

		List<String> otherParam = new ArrayList<String>();
		otherParam.addAll(Arrays.asList(argv).subList(3, argv.length));

		idFetcher.setUseUniprotIDs(otherParam.contains("uniprot"));
		SIFSearcher searcher = new SIFSearcher(idFetcher, SIFEnum.values());
		File blacklistFile = new File("blacklist.txt");
		if(blacklistFile.exists()) {
			log.info("toSif: will use the blacklist.txt (found in the current directory)");
			searcher.setBlacklist(new Blacklist(new FileInputStream(blacklistFile)));
		} else {
			log.info("toSif: not blacklisting ubiquitous molecules (no blacklist.txt found)");
		}

		// check for custom fields
		List<String> fieldList = new ArrayList<String>();
		for (String param : otherParam)
		{
			OutputColumn.Type type = OutputColumn.Type.getType(param);
			if ((type != null && type != OutputColumn.Type.CUSTOM) ||
				param.contains("/"))
			{
				fieldList.add(param);
			}
		}

		Model model = getModel(io, argv[1]);
		ModelUtils.mergeEquivalentInteractions(model);

		if (fieldList.isEmpty())
		{
			searcher.searchSIF(model, new FileOutputStream(argv[2]), false);
		}
		else if (fieldList.size() == 1 &&
			fieldList.contains(OutputColumn.Type.MEDIATOR.name().toLowerCase()))
		{
			searcher.searchSIF(model, new FileOutputStream(argv[2]), true);
		}
		else
		{
			searcher.searchSIF(model, new FileOutputStream(argv[2]),
				new CustomFormat(fieldList.toArray(new String[fieldList.size()])));
		}
    }

    public static void integrate(String[] argv) throws IOException {

        Model model1 = getModel(io, argv[1]);
        Model model2 = getModel(io, argv[2]);

        Integrator integrator =
                new Integrator(SimpleEditorMap.get(model1.getLevel()), model1, model2);
        integrator.integrate();

        io.setFactory(model1.getLevel().getDefaultFactory());
        io.convertToOWL(model1, new FileOutputStream(argv[3]));
    }

    public static void merge(String[] argv) throws IOException {

        Model model1 = getModel(io, argv[1]);
        Model model2 = getModel(io, argv[2]);

        Merger merger = new Merger(SimpleEditorMap.get(model1.getLevel()));
        merger.merge(model1, model2);

        io.setFactory(model1.getLevel().getDefaultFactory());
        io.convertToOWL(model1, new FileOutputStream(argv[3]));
    }

	/*
     * Generates a blacklist file
     * (to optionally use it to exclude ubiquitous small molecules, 
     * like ATP, when performing graph queries and exporting to
     * SIF formats).
     */
    public static void blacklist(String[] argv) throws IOException {
    	Model model = getModel(io, argv[1]);
		BlacklistGenerator gen = new BlacklistGenerator();
		Blacklist blacklist = gen.generateBlacklist(model);
		blacklist.write(new FileOutputStream(argv[2]));
    }
   
    public static void help() {

        System.out.println("(Paxtools Console) Available Operations:\n");
        for (Command cmd : Command.values()) {
            System.out.println(cmd.name() + " " + cmd.description);
        }
        System.out.println("Commands can also use compressed input files (only '.gz').\n");
    }

	public static void pattern(String[] argv) {
		Dialog.main(argv);
	}

    private static Model getModel(BioPAXIOHandler io, String fName) throws IOException {
        return io.convertFromOWL(getInputStream(fName));
    }

	//----- Section: Printing summary -------------------------------------------------------------|
	
	public static void summarize(String[] argv) throws IOException {

		log.debug("Importing the input model from " + argv[1] + "...");
		Model model = getModel(io, argv[1]);
		log.debug("Analyzing...");
		summarize(model, argv.length > 2 ? new PrintStream(argv[2]) : null);
	}
		
	public static void summarize(Model model, PrintStream out) throws IOException {
        // Produce a simplified version of the summary

		if (out == null) out = System.out;

        HashMap<String, Integer> hm = new HashMap<String, Integer>();

		final SimpleEditorMap em = (model.getLevel() == BioPAXLevel.L3) 
				? SimpleEditorMap.L3 : SimpleEditorMap.L2;

		for (Class<? extends BioPAXElement> clazz : sortToName(em.getKnownSubClassesOf(BioPAXElement.class)))
		{
			Set<? extends BioPAXElement> set = model.getObjects(clazz);
			int initialSize = set.size();
			set = filterToExactClass(set, clazz);
			
			String s = clazz.getSimpleName() + " = " + set.size();

			if (initialSize != set.size()) 
				s += " (and " + (initialSize - set.size()) + " children)";
			
			out.println(s);

			Set<PropertyEditor> editors = em.getEditorsOf(clazz);
			for (PropertyEditor editor : editors)
			{
				Method getMethod = editor.getGetMethod();
				Class<?> returnType = getMethod.getReturnType();

				Map<Object, Integer> cnt = new HashMap<Object, Integer>();

				if (returnType.isEnum() ||
					implementsInterface(returnType, ControlledVocabulary.class))
				{
					for (BioPAXElement ele : set)
					{
						Set<?> values = editor.getValueFromBean(ele);
						if (values.isEmpty())
						{
							increaseCnt(cnt, NULL);
						}
						else
						{
							increaseCnt(cnt, values.iterator().next());
						}
					}
				}
				else if (returnType.equals(Set.class) &&
					implementsInterface(editor.getRange(), ControlledVocabulary.class))
				{
					for (BioPAXElement ele : set)
					{
						Set<?> values = editor.getValueFromBean(ele);
						if (values.isEmpty())
						{
							increaseCnt(cnt, EMPTY);
						}
						for (Object val : values)
						{
							increaseCnt(cnt, val);
						}
					}
				}

				if (!cnt.isEmpty())
				{
					String name = "-" 
						+ (returnType.equals(Set.class) ? editor.getRange().getSimpleName() : returnType.getSimpleName());

					out.print("\t" + name + ":");
					for (Object key : getOrdering(cnt))
					{
						out.print("\t" + key + " = " + cnt.get(key));
					}
					out.println();
				}
			}
		}

		String[] props = (model.getLevel() == BioPAXLevel.L3) 
			? new String[]{"UnificationXref/db","RelationshipXref/db"}
			: new String[]{"unificationXref/DB","relationshipXref/DB"};

		out.println("\nOther property counts\n");

		for (String prop : props)
		{
			Map<Object, Integer> cnt = new HashMap<Object, Integer>();
			List<String> valList = new ArrayList<String>();
			PathAccessor acc = new PathAccessor(prop, model.getLevel());
			
			boolean isString = false;
			
			for (Object o : acc.getValueFromModel(model))
			{
				if (o instanceof String) isString = true;
				
				String s = o.toString();
				valList.add(s);
				if (!cnt.containsKey(s)) cnt.put(s, 1);
				else cnt.put(s, cnt.get(s) + 1);
			}

			out.println(prop + "\t(" + cnt.size() + " distinct values):");
            hm.put(prop, cnt.size());

			// If the object is String, then all counts are 1, no need to print counts.
			if (isString)
			{
				Collections.sort(valList);
				for (String s : valList)
				{
					out.print("\t" + s);
				}
			}
			else
			{
				for (Object key : getOrdering(cnt))
				{
					out.print("\t" + key + " = " + cnt.get(key));
				}
			}
			out.println();
		}

	}

	private static List<Class<? extends BioPAXElement>> sortToName(Set<? extends Class<? extends BioPAXElement>>
			classes)
	{
		List<Class<? extends BioPAXElement>> list = new ArrayList<Class<? extends BioPAXElement>>(classes);

		Collections.sort(
				list, new Comparator<Class<? extends  BioPAXElement>>()
		{
			public int compare(Class<? extends BioPAXElement> clazz1, Class<? extends BioPAXElement> clazz2)
			{
				return clazz1.getName().substring(clazz1.getName().lastIndexOf(".")+1).compareTo(
					clazz2.getName().substring(clazz2.getName().lastIndexOf(".")+1));
			}
		});
		return list;
	}

	private static List<Object> getOrdering(final Map<Object, Integer> map)
	{
		List<Object> list = new ArrayList<Object>(map.keySet());
		Collections.sort(list, new Comparator<Object>()
		{
			public int compare(Object key1, Object key2)
			{
				int cnt1 = map.get(key1);
				int cnt2 = map.get(key2);

				if (cnt1 == cnt2) return key1.toString().compareTo(key2.toString());
				else return cnt2 - cnt1;
			}
		});
		return list;
	}
	
	private static Set<BioPAXElement> filterToExactClass(Set<? extends BioPAXElement> classSet, Class<?> clazz)
	{
		Set<BioPAXElement> exact = new HashSet<BioPAXElement>();
		for (BioPAXElement ele : classSet)
		{
			if (ele.getModelInterface().equals(clazz)) exact.add(ele);
		}
		return exact;
	}

	private static final Object NULL = new Object(){
		@Override
		public String toString()
		{
			return "NULL";
		}
	};

	private static final Object EMPTY = new Object(){
		@Override
		public String toString()
		{
			return "EMPTY";
		}
	};

	private static boolean implementsInterface(Class clazz, Class inter)
	{
		for (Class anInter : clazz.getInterfaces())
		{
			if (anInter.equals(inter)) return true;
		}
		return false;
	}

	private static void increaseCnt(Map<Object, Integer> cnt, Object key)
	{
		if (!cnt.containsKey(key)) cnt.put(key, 0);
		cnt.put(key, cnt.get(key) + 1);
	}
	
	
	// gets new IS - either FIS or GzipIS if .gz extension present
	private static InputStream getInputStream(String path) throws IOException {
		InputStream is = new FileInputStream(path);
		return (path.endsWith(".gz")) ? new GZIPInputStream(is) : is ;
	}

	//-- End of Section; Printing summary ---------------------------------------------------------|
	
    enum Command {
        merge("<file1> <file2> <output>\n" +
        		"\t- merges file2 into file1 and writes it into output")
		        {public void run(String[] argv) throws IOException{merge(argv);} },
        toSif("<input> <output> [hgnc|uniprot]\n" +
        		"\t- converts model to the simple interaction format; will use blacklist.txt file in the current directory, if present")
		        {public void run(String[] argv) throws IOException{toSif(argv);} },
        toSifnx("<input> <output> [hgnc|uniprot] [mediator] [pubmed] [pathway] [resource] [source_loc] [target_loc] [path/to/a/mediator/field]\n" +
        		"\t- converts model to the extended simple interaction format; will use blacklist.txt file in the current directory, if present")
		        {public void run(String[] argv) throws IOException{toSifnx(argv);} },
        toSbgn("<biopax.owl> <output.sbgn>\n" +
        		"\t- converts model to the SBGN format.")
                {public void run(String[] argv) throws IOException { toSBGN(argv); } },
        validate("<path> <out> [xml|html|biopax] [auto-fix] [only-errors] [maxerrors=n] [notstrict]\n" +
        		"\t- validate BioPAX file/directory (up to ~25MB in total size, -\n" +
        		"\totherwise download and run the stand-alone validator)\n" +
        		"\tin the directory using the online validator service\n" +
        		"\t(generates html or xml report, or gets the processed biopax\n" +
        		"\t(cannot be perfect though) see http://www.biopax.org/validator)")
		        {public void run(String[] argv) throws IOException{validate(argv);} },
        integrate("<file1> <file2> <output>\n" +
        		"\t- integrates file2 into file1 and writes it into output (experimental)")
		        {public void run(String[] argv) throws IOException{integrate(argv);} },
        toLevel3("<input> <output>\n" +
        		"\t- converts BioPAX level 1 or 2, PSI-MI 2.5 and PSI-MITAB to the level 3 file;\n" +
        		"\tuse -Dpaxtools.converter.psi.interaction=complex java option \n" +
        		"\tto force PSI Interaction to BioPAX Complex convertion instead of \n" +
        		"\tto MolecularInteraction (default).")
		        {public void run(String[] argv) throws IOException{toLevel3(argv);} },
        toGSEA("<input> <output> <database> [crossSpeciesCheck] [skipSubPathways]\n" +
        		"\t- converts Level 1 or 2 or 3 to GSEA output.\n"
                + "\tUses that database identifier or the biopax URI if database is \"NONE\".\n"
                + "\t[crossSpeciesCheck] - optional cross-species check ensures participant protein is from same species\n"
                + "\tas pathway (values: true/false; if false, organism there will be always 'unspecified').\n"
				+ "\t[skipSubPathways] - optional, true (always), false (never), or a semicolon-separated list of\n"
				+ "\tProvenance_uri1;Provenance_uri2;.. for which the converter won't traverse\n"
				+ "\tinto sub-pathways of each pathway in order to collect all the proteins (useful e.g., with KEGG data).")
		        {public void run(String[] argv) throws IOException{toGSEA(argv);} },
        fetch("<input> <Uri1,Uri2,..> <output>\n" +
        		"\t- extracts a self-integral BioPAX sub-model from file1 and writes to the output.")
		        {public void run(String[] argv) throws IOException{fetch(argv);} },
        getNeighbors("<input> <id1,id2,..> <output>\n" +
        		"\t- nearest neighborhood graph query (id1,id2 - of Entity sub-class only)")
		        {public void run(String[] argv) throws IOException{getNeighbors(argv);} },
        summarize("<input> [<output>]\n" +
        		"\t- prints a summary of the contents of the model to the output file (if not provided - to stdout)")
		        {public void run(String[] argv) throws IOException{summarize(argv);} },
		blacklist("<input> <output>\n" +
		        "\t- creates a blacklist of ubiquitous small molecules, like ATP, \n"
		        + "\tfrom the BioPAX model and writes it to the output file. The blacklist can be used with\n "
		        + "\tpaxtools graph queries or when converting from the SAME BioPAX data to the SIF formats.")
				{public void run(String[] argv) throws IOException{blacklist(argv);} },
		pattern("\n\t- BioPAX pattern search tool (opens a new dialog window)")
				{public void run(String[] argv) throws IOException{pattern(argv);} },
        help("\n\t- prints this screen and exits\n")
		        {public void run(String[] argv) throws IOException{help();} };

        String description;
        int params;

        Command(String description) {
            this.description = description;
        }

        public abstract void run(String[] argv) throws IOException;
    }
}
