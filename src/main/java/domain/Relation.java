package domain;

import java.util.ArrayList;
import java.util.List;

import utils.GenericUtils;

public class Relation {

	// Attributes
	private String id;
	private RelType type;

	// Relations
	private Feature sourceFeature;
	private List<Feature> targetFeatures;

	public Relation(RelType type, Feature sourceFeature) {
		this.id = GenericUtils.generateID();
		this.type = type;
		this.sourceFeature = sourceFeature;
		this.targetFeatures = new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public RelType getType() {
		return type;
	}

	public void setType(RelType type) {
		this.type = type;
	}

	public Feature getSourceFeature() {
		return sourceFeature;
	}

	public void setSourceFeature(Feature sourceFeature) {
		this.sourceFeature = sourceFeature;
	}

	public List<Feature> getTargetFeatures() {
		return targetFeatures;
	}

	public void setTargetFeatures(List<Feature> targetFeatures) {
		this.targetFeatures = targetFeatures;
	}

	public void addTargetFeature(Feature f) {
		this.targetFeatures.add(f);
	}

}
