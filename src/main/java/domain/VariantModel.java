package domain;

import java.util.ArrayList;
import java.util.List;

import main.MainClass;

public class VariantModel {

	// Attributes
	private String id;
	private String filename;
	private String path;

	// Relations
	private SPL spl;
	private List<VariantComponent> variants;
	
	public VariantModel(String fullPath) {
		int i = fullPath.lastIndexOf("/");
		if(i == -1) {
			this.path = "/";
			this.filename = fullPath;
		}else {
			this.path = fullPath.substring(0, i);
			this.filename = fullPath.substring(i+1);
		}
		
		this.variants = new ArrayList<>();
		
		MainClass.getLogger().info("VariantModel found at: " + fullPath);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public SPL getSpl() {
		return spl;
	}

	public void setSpl(SPL spl) {
		this.spl = spl;
	}

	public List<VariantComponent> getVariants() {
		return variants;
	}

	public void setVariants(List<VariantComponent> variants) {
		this.variants = variants;
	}

	public void addVariant(VariantComponent v) {
		this.variants.add(v);
	}
}
