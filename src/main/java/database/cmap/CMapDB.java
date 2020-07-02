package database.cmap;

import java.io.PrintWriter;
import java.util.ArrayList;

import domain.Feature;
import domain.cmap.CMap;
import domain.cmap.Concept;
import domain.cmap.Connection;
import domain.cmap.LinkingPhrase;
import domain.cmap.Resource;

public class CMapDB {
	
	// SQL INSERTS
	private final static String INSERT_CMAP = "INSERT INTO CMAP VALUES (:id,:name,:spl_id);";
	private final static String INSERT_LINK_ELEMENT = "INSERT INTO LINK_ELEMENT VALUES (:id,:label,:cmap);";
	private final static String INSERT_CONCEPT = "INSERT INTO CONCEPT VALUES (:id);";
	private final static String INSERT_LINKING_PHRASE = "INSERT INTO LINKING_PHRASE VALUES (:id);";
	private final static String INSERT_CONNECTION = "INSERT INTO CONNECTION VALUES (:id,:from_id,:to_id,:cmap);";
	private final static String INSERT_RESOURCE = "INSERT INTO RESOURCE VALUES (:id,:label,:description,:url,:parent_id,:cmap);";
	
	// Connection between cmap and spl
	private final static String INSERT_FEATURE_LINK_ELEMENT_CONNECTION = "INSERT INTO FEATURE_LINK_ELEMENT VALUES (:link_element_id,:feature_id);";
	
	private static ArrayList<String> inserts = new ArrayList<>();
	
	public static ArrayList<String> getInserts(){
		return inserts;
	}
	
	public static void exportToFile(String filepath) {
		try {
			PrintWriter writer = new PrintWriter(filepath, "UTF-8");
			for (String i : inserts) {
				writer.println(i);
			}
			writer.close();
		} catch (Exception e) {

		}
	}
	
	public static void generateInserts(CMap cmap) {
		
		inserts = new ArrayList<>();
		
		// 1: CMap insert
		String insertCmap = INSERT_CMAP
				.replace(":id", str(cmap.getId()))
				.replace(":name", str(cmap.getName()))
				.replace(":spl_id", str(cmap.getSplId()));
		inserts.add(insertCmap);
		
		// 2: Concept inserts
		for(Concept c : cmap.getConcepts()) {
			String insertLinkElement = INSERT_LINK_ELEMENT
					.replace(":id", str(c.getId()))
					.replace(":label", str(c.getLabel()))
					.replace(":cmap", str(cmap.getId()));
			
			String insertConcept = INSERT_CONCEPT.replace(":id",str(c.getId()));
			
			inserts.add(insertLinkElement);
			inserts.add(insertConcept);
			
			for(Feature f : c.getConnFeatures()) {
				String insertFeatureConn = INSERT_FEATURE_LINK_ELEMENT_CONNECTION
						.replace(":link_element_id", str(c.getId()))
						.replace(":feature_id", str(f.getId()));
				inserts.add(insertFeatureConn);
			}
		}
		
		// 3: LinkingPhrase inserts
		for(LinkingPhrase lp : cmap.getLinkingPhrases()) {
			String insertLinkElement = INSERT_LINK_ELEMENT
					.replace(":id", str(lp.getId()))
					.replace(":label", str(lp.getLabel()))
					.replace(":cmap", str(cmap.getId()));
			
			String insertLinkingPhrase = INSERT_LINKING_PHRASE
					.replace(":id", str(lp.getId()));
			
			inserts.add(insertLinkElement);
			inserts.add(insertLinkingPhrase);
			
			for(Feature f : lp.getConnFeatures()) {
				String insertFeatureConn = INSERT_FEATURE_LINK_ELEMENT_CONNECTION
						.replace(":link_element_id", str(lp.getId()))
						.replace(":feature_id", str(f.getId()));
				inserts.add(insertFeatureConn);
			}
		}
		
		// 4: Connection inserts
		for(Connection c : cmap.getConnections()) {
			String insertConnection = INSERT_CONNECTION
					.replace(":id",str(c.getId()))
					.replace(":from_id", str(c.getFrom().getId()))
					.replace(":to_id", str(c.getTo().getId()))
					.replace(":cmap", str(cmap.getId()));
			
			inserts.add(insertConnection);
		}
		
		// 5: Resources inserts
		for(Resource r : cmap.getResources()) {
			String insertResource = INSERT_RESOURCE
					.replace(":id", str(r.getId()))
					.replace(":label", str(r.getLabel()))
					.replace(":description", str(r.getDescription()))
					.replace(":url", str(r.getUrl()))
					.replace(":parent_id", str(r.getParent().getId()))
					.replace(":cmap", str(cmap.getId()));
			
			inserts.add(insertResource);
		}
		
	}
	
	private static String str(String str) {
		return "\"" + 
				str.replaceAll("\"", "'")
					.replaceAll("\n", "#LINE_BREAK#")
				+ "\"";
	}

}
