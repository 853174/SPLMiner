package domain;

public class Attribute {

	// Attributes
	private String id;
	private String name;
	private String type;
	private String value;

	public Attribute(String id, String name, String type, String value) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.value = value;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
