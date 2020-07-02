package domain.cmap;

import java.util.ArrayList;
import java.util.List;

import domain.Feature;

public abstract class LinkElement {
	
	private String id;
	private String label;
	
	// Connection with SPL Features
	private List<Feature> connFeatures = new ArrayList<>();
	
	
	public LinkElement(String id, String label) {
		this.id = id;
		this.label = label;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<Feature> getConnFeatures() {
		return connFeatures;
	}

	public void setConnFeatures(List<Feature> connFeatures) {
		this.connFeatures = connFeatures;
	}
	
	public void addConnFeature(Feature connF) {
		this.connFeatures.add(connF);
	}

}
