package domain;

import java.util.ArrayList;
import java.util.List;

public abstract class CodeElement {

	// Attributes
	private String id;
	private String path;
	private Type type;

	// Relations
	private SPL spl;
	private CodeElement parent;
	private List<CodeElement> children;
	private List<VariationPoint> variationPoints;

	public CodeElement(String id, String path, String type, SPL spl,
			CodeElement parent) {
		this.id = id;
		this.path = path;
		this.type = Type.parsePVStringToType(type);
		this.spl = spl;
		this.parent = parent;
		
		this.variationPoints = new ArrayList<VariationPoint>();

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public SPL getSpl() {
		return spl;
	}

	public void setSpl(SPL spl) {
		this.spl = spl;
	}

	public CodeElement getParent() {
		return parent;
	}

	public void setParent(CodeElement parent) {
		this.parent = parent;
	}

	public List<VariationPoint> getVariationPoints() {
		return variationPoints;
	}

	public void addVariationPoint(VariationPoint cs) {
		this.variationPoints.add(cs);
	}

	public List<CodeElement> getChildren() {
		return children;
	}

	public void setChildren(List<CodeElement> children) {
		this.children = children;
	}
	
	public void addChildren(CodeElement child) {
		this.children.add(child);
	}

}
