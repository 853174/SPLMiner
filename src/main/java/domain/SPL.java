package domain;

import java.io.File;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.MainClass;

public class SPL {

	// Attributes
	private String id;
	private String name;
	private String gitHubUrl;
	private Date lastChange;

	// Relations
	private List<CodeElement> codeElements;
	private List<FeatureModel> featureModels;
	private List<VariantModel> variantModels;

	public SPL(String id, String name, Date lastChange, String ghu) {
		this.id = id;
		this.name = name;
		this.lastChange = lastChange;
		this.gitHubUrl = ghu;
		this.codeElements = new ArrayList<>();
		this.featureModels = new ArrayList<>();
		this.variantModels = new ArrayList<>();
		
		MainClass.getLogger().info("(" + this.id + ") SPL with name \"" + this.name + "\" created.");
	}

	public void setVariantModelsFolder(String variantModelFolderPath) {
		// Find variant models on folder...
		File folder = new File(variantModelFolderPath);

		if (folder.exists() && folder.isDirectory()) {
			Pattern isVariantModel = Pattern.compile(".*\\.vdm");
			for (File f : folder.listFiles()) {
				Matcher m = isVariantModel.matcher(f.getName());
				if (f.isFile() && m.matches()) {
					VariantModel vm = new VariantModel(f.getAbsolutePath());
					vm.setSpl(this);
					variantModels.add(vm);
				}

			}
		}
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

	public List<FeatureModel> getFeatureModels() {
		return featureModels;
	}
	
	public void addFeatureModel(FeatureModel fm) {
		this.featureModels.add(fm);
	}

	public List<VariantModel> getVariantModels() {
		return variantModels;
	}

	public void addVariantModel(VariantModel vm) {
		this.variantModels.add(vm);
	}

	public List<CodeElement> getCodeElements() {
		return codeElements;
	}

	public void addCodeElement(CodeElement ce) {
		this.codeElements.add(ce);
	}

	public Date getLastChange() {
		return lastChange;
	}

	public void setLastChange(Date lastChange) {
		this.lastChange = lastChange;
	}

	public String getGitHubUrl() {
		return gitHubUrl;
	}

	public void setGitHubUrl(String gitHubUrl) {
		this.gitHubUrl = gitHubUrl;
	}
	
	

}
