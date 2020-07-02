package domain;

import java.util.ArrayList;
import java.util.List;

import utils.GenericUtils;

public class VariationPoint {

	// Attributes
	private String id;
	private String expresion;
	private int vpSize;
	
	// Relations
	private CodeElement file;
	private List<Feature> referencedFeatures;

	public VariationPoint(String expresion, CodeElement file, int vpSize) {
		super();
		this.id = GenericUtils.generateID();
		this.expresion = expresion;
		this.file = file;
		this.referencedFeatures = new ArrayList<>();
		this.vpSize = vpSize;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getExpresion() {
		return expresion;
	}

	public void setExpresion(String expresion) {
		this.expresion = expresion;
	}
	
	public CodeElement getFile() {
		return file;
	}

	public void setFile(CodeElement file) {
		this.file = file;
	}

	public List<Feature> getReferencedFeatures() {
		return referencedFeatures;
	}
	
	public void addReferencedFeature(Feature f) {
		this.referencedFeatures.add(f);
	}

	public int getVpSize() {
		return vpSize;
	}

	public void setVpSize(int vpSize) {
		this.vpSize = vpSize;
	}
}
