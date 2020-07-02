package domain;

public class Part extends CodeElement{
	
	private String partType;

	public Part(String id, String path, String type, SPL spl, CodeElement parent, String partType) {
		super(id, path, type, spl, parent);
		this.partType = partType;
	}

	public String getPartType() {
		return partType;
	}

	public void setPartType(String partType) {
		this.partType = partType;
	}
	
	

}
