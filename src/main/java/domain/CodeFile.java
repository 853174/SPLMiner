package domain;

public class CodeFile extends CodeElement{
	
	private String filename;
	
	public CodeFile(String id, String path, String type, SPL spl, CodeElement parent, String fileName) {
		super(id, path, type, spl, parent);
		this.filename = fileName;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	

}
