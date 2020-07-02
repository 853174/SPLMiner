package domain.cmap;

public class Resource {
	
	private String id;
	private String label;
	private String description;
	private String url;
	private LinkElement parent;
	
	public Resource(String id, String label, String description, String url, LinkElement parent) {
		super();
		this.id = id;
		this.label = label;
		this.description = description;
		this.url = url;
		this.parent = parent;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public LinkElement getParent() {
		return parent;
	}

	public void setParent(LinkElement parent) {
		this.parent = parent;
	}

}
