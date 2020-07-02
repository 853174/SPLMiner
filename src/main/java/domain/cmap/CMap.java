package domain.cmap;

import java.util.ArrayList;

import utils.GenericUtils;

public class CMap {
	
	private String id;
	private String name;
	private String splId;
	private ArrayList<Concept> concepts;
	private ArrayList<Connection> connections;
	private ArrayList<LinkingPhrase> linkingPhrases;
	private ArrayList<Resource> resources;
	
	public CMap(String splId,String name) {
		this.id = GenericUtils.generateID();
		this.name = name;
		this.splId = splId;
		concepts = new ArrayList<>();
		connections = new ArrayList<>();
		linkingPhrases = new ArrayList<>();
		resources = new ArrayList<>();
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getSplId() {
		return this.splId;
	}
	
	public String getName() {
		return this.name;
	}
	
	public ArrayList<Concept> getConcepts() {
		return concepts;
	}
	public void setConcepts(ArrayList<Concept> concepts) {
		this.concepts = concepts;
	}
	public ArrayList<Connection> getConnections() {
		return connections;
	}
	public void setConnections(ArrayList<Connection> connections) {
		this.connections = connections;
	}
	public ArrayList<LinkingPhrase> getLinkingPhrases() {
		return linkingPhrases;
	}
	public void setLinkingPhrases(ArrayList<LinkingPhrase> linkingPhrases) {
		this.linkingPhrases = linkingPhrases;
	}
	
	public void addConcept(Concept c) {
		this.concepts.add(c);
	}
	
	public void addConnection(Connection c) {
		this.connections.add(c);
	}
	
	public void addLinkingPhrase(LinkingPhrase lp) {
		this.linkingPhrases.add(lp);
	}

	public ArrayList<Resource> getResources() {
		return resources;
	}

	public void setResources(ArrayList<Resource> resources) {
		this.resources = resources;
	}
	
	public void addResource(Resource r) {
		this.resources.add(r);
	}

	public void setName(String name) {
		this.name = name;
	}
	
	

}
