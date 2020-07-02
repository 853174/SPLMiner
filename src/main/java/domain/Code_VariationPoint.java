package domain;

public class Code_VariationPoint extends VariationPoint{
	
	// Atributes
	private int startLine;
	private int endLine;
	private String content;
	private int nestingLevel;
	
	// Relations are given by VariationPoint Class
	
	public Code_VariationPoint(int startLine, int endLine, String expresion, CodeFile file,int vpSize, String content, int nestingLevel) {
		super(expresion,file,vpSize);
		this.startLine = startLine;
		this.endLine = endLine;
		this.content = content;
		this.nestingLevel = nestingLevel;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getNestingLevel() {
		return nestingLevel;
	}

	public void setNestingLevel(int nestingLevel) {
		this.nestingLevel = nestingLevel;
	}

}
