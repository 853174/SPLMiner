package domain.cmap;

public class Connection {
	
	private String id;
	private LinkElement from;
	private LinkElement to;
	
	public Connection(String id, LinkElement from, LinkElement to) {
		this.id = id;
		this.from = from;
		this.to = to;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LinkElement getFrom() {
		return from;
	}

	public void setFrom(LinkElement from) {
		this.from = from;
	}

	public LinkElement getTo() {
		return to;
	}

	public void setTo(LinkElement to) {
		this.to = to;
	}

}
