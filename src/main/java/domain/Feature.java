package domain;

import java.util.ArrayList;
import java.util.List;

public class Feature {

	// Attributes
	private String id;
	private String name;
	private Type type;

	// Relations
	private FeatureModel featureModel;
	private Feature parent;
	private List<Feature> children;
	private List<Relation> dependencies;
	private List<Attribute> attributes;

	public Feature(String id, String name, String type, Feature parent, FeatureModel fm) {
		this.id = id;
		this.name = name;
		this.type = Type.parsePVStringToType(type);
		this.parent = parent;
		this.featureModel = fm;

		children = new ArrayList<>();
		dependencies = new ArrayList<>();
		attributes = new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public FeatureModel getFeatureModel() {
		return featureModel;
	}

	public void setFeatureModel(FeatureModel featureModel) {
		this.featureModel = featureModel;
	}

	public Feature getParent() {
		return parent;
	}

	public void setParent(Feature parent) {
		this.parent = parent;
	}

	public List<Feature> getChildren() {
		return children;
	}

	public void setChildren(List<Feature> children) {
		this.children = children;
	}

	public void addChild(Feature child) {
		this.children.add(child);
	}

	public List<Relation> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<Relation> dependencies) {
		this.dependencies = dependencies;
	}

	public void addDependency(RelType type, List<Feature> f) {
		Relation r = new Relation(type,this);
		r.setTargetFeatures(f);
		this.dependencies.add(r);
	}
	
	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void addAttribute(Attribute attribute) {
		this.attributes.add(attribute);
	}
	
	public static String parseId(String fullId) {

		int p = fullId.lastIndexOf("/");
		return (p == -1) ? fullId : fullId.substring(p + 1);

	}

	@Override
	public String toString() {
		return this.name;
	}

}
