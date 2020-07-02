package domain;

public class VariantCode extends VariantComponent{
	
	private CodeElement codeFile;
	
	public VariantCode(String id, boolean isSelected, VariantModel variantModel, CodeElement ce) {
		super(id, isSelected, variantModel);
		this.codeFile = ce;
	}

	public CodeElement getCodeFile() {
		return codeFile;
	}

	public void setCodeFile(CodeElement code) {
		this.codeFile = code;
	}
	
	

}
