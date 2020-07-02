package domain;

public enum RelType {
	
	REQUIRES,
	REQUIRES_ALL,
	REQUIRES_ONE,
	CONFLICTS,
	CONFLICTS_ALL,
	CONFLICTS_ONE;
	
	public static RelType parsePVStringToRelType(String pvStr) {
		switch(pvStr) {
		case "ps:requires":
			return REQUIRES;
		case "ps:requiresAll":
			return REQUIRES_ALL;
		case "ps:requiresOne":
			return REQUIRES_ONE;
		case "ps:conflicts":
			return CONFLICTS;
		case "ps:conflictsAll":
			return CONFLICTS_ALL;
		case "ps:conflictsOne":
			return CONFLICTS_ONE;
		default:
			return REQUIRES;
		}
	}

}
