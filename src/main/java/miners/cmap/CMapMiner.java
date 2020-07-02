package miners.cmap;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import domain.Feature;
import domain.FeatureModel;
import domain.SPL;
import domain.cmap.CMap;
import domain.cmap.Concept;
import domain.cmap.Connection;
import domain.cmap.LinkElement;
import domain.cmap.LinkingPhrase;
import domain.cmap.Resource;
import main.MainClass;

public class CMapMiner {
	
	private static CMap cmap;
	private static SPL spl;
	
	public static CMap mineAlone(String filepath, String relatedSplId) {
		return mine(filepath,relatedSplId);
	}
	
	private static CMap mine(String filepath, String relatedSPLId) {
		
		File cmapFile = new File(filepath);
		
		cmap = new CMap(relatedSPLId,cmapFile.getName().replaceAll("\\..*", ""));
		
		try {
			// 1: Open CMap file as XML
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(cmapFile);
			doc.getDocumentElement().normalize();

			// 2: Iterate over concepts
			NodeList concepts = doc.getElementsByTagName("concept");
			for (int i = 0; i < concepts.getLength(); i++) {
				Node concept = concepts.item(i);
				if (concept.getNodeType() == Node.ELEMENT_NODE) {
					Element conceptE = (Element) concept;
					String id = conceptE.getAttribute("id");
					String label = conceptE.getAttribute("label");

					Concept c = new Concept(id, label);
					
					if(spl != null) {
						// Try to make a connection with SPL's Features
						String comment = conceptE.getAttribute("short-comment");
						c.setConnFeatures(connectLinkElementWithFeatures(comment));
					}
					
					cmap.addConcept(c);
				}
			}

			// 3: Iterate over linking phrases
			NodeList linkingPhrases = doc.getElementsByTagName("linking-phrase");
			for (int i = 0; i < linkingPhrases.getLength(); i++) {
				Node linkingPhrase = linkingPhrases.item(i);
				if (linkingPhrase.getNodeType() == Node.ELEMENT_NODE) {
					Element linkingPhraseE = (Element) linkingPhrase;
					String id = linkingPhraseE.getAttribute("id");
					String label = linkingPhraseE.getAttribute("label");

					LinkingPhrase lp = new LinkingPhrase(id, label);
					
					if(spl != null) {
						// Try to make a connection with SPL's Features
						String comment = linkingPhraseE.getAttribute("short-comment");
						lp.setConnFeatures(connectLinkElementWithFeatures(comment));
					}
					
					cmap.addLinkingPhrase(lp);
				}
			}

			// 4: Iterate over connections
			NodeList connections = doc.getElementsByTagName("connection");
			for (int i = 0; i < connections.getLength(); i++) {
				Node connection = connections.item(i);
				if (connection.getNodeType() == Node.ELEMENT_NODE) {
					Element connectionE = (Element) connection;
					
					String id = connectionE.getAttribute("id");
					LinkElement from = findLinkElementById(connectionE.getAttribute("from-id"));
					LinkElement to = findLinkElementById(connectionE.getAttribute("to-id"));
					
					if( from != null && to != null) {
						Connection c = new Connection(id, from, to);
						cmap.addConnection(c);
					}else {
						MainClass.getLogger().severe("Connection failed! Id: " + id);
					}
						
				}
			}
			
			// 5: Iterate over resources
			NodeList resourcegroups = doc.getElementsByTagName("resource-group");
			for (int i = 0; i < resourcegroups.getLength(); i++) {
				Node resourcegroup = resourcegroups.item(i);
				if (resourcegroup.getNodeType() == Node.ELEMENT_NODE) {
					Element resourcegroupE = (Element) resourcegroup;
					
					LinkElement parent = findLinkElementById(resourcegroupE.getAttribute("parent-id"));
					
					if(parent == null) {
						MainClass.getLogger().severe("Resource error! Element with id " + resourcegroupE.getAttribute("parent-id") + " not found!");
						continue;
					}
					
					NodeList resources = resourcegroupE.getChildNodes();
					for(int j = 0 ; j < resources.getLength(); j++) {
						Node resource = resources.item(j);
						if(resource.getNodeType() == Node.ELEMENT_NODE) {
							Element resourceE = (Element) resource;
							
							String id = resourceE.getAttribute("id");
							String label = resourceE.getAttribute("label");
							String description = resourceE.getAttribute("description");
							String url = resourceE.getAttribute("resource-url");
						
							Resource r = new Resource(id, label, description, url,parent);
							cmap.addResource(r);
						}
					}
					
					
						
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return cmap;
	}

	// It's necesary to mine at least FeatureModel to connect Features and LinkElements
	public static CMap mineWithConnections(String filepath,SPL relatedSPL) {
		
		spl = relatedSPL;
		
		return mine(filepath,relatedSPL.getId());

	}
	
	private static ArrayList<Feature> connectLinkElementWithFeatures(String line) {
		
		if(spl == null)
			return new ArrayList<>();
		
		ArrayList<Feature> listfeatures = new ArrayList<>();
		for (FeatureModel fm : spl.getFeatureModels()) {
			for (Feature f : fm.getFeatures()) {
				Pattern p = Pattern.compile("\\b("+f.getName()+")\\b");
				Matcher m = p.matcher(line);
				if (m.find()) {
					listfeatures.add(f);
				}
			}
		}
		return listfeatures;
	}

	private static LinkElement findLinkElementById(String id) {
		// It's Concept?
		for (Concept c : cmap.getConcepts()) {
			if (c.getId().contentEquals(id))
				return c;
		}

		// It's LinkingPhrase?
		for (LinkingPhrase lp : cmap.getLinkingPhrases()) {
			if (lp.getId().contentEquals(id))
				return lp;
		}
		
		return null;
	}

}
