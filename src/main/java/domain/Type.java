package domain;

public enum Type {
	
	MANDATORY, 
	ALTERNATIVE, 
	OPTIONAL, 
	OR;

	public static Type parsePVStringToType(String pvStr) {
		switch (pvStr) {
		case "ps:mandatory":
			return MANDATORY;
		case "ps:alternative":
			return ALTERNATIVE;
		case "ps:optional":
			return OPTIONAL;
		case "ps:or":
			return OR;
		default:
			return MANDATORY;
		}
	}
}
