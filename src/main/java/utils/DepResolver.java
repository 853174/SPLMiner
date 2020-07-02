package utils;

import java.util.ArrayList;
import java.util.List;

import domain.Feature;
import domain.RelType;

public class DepResolver {

	// This class saves the dependencies info (IDs and type) before all the Features
	// are created so then the correct
	// relations can be done.
	public static class FeatureDependency {

		// Attributes
		private String sourceFeature;
		private List<String> targetFeatures;
		private RelType type;

		public FeatureDependency(String sourceFeature, String type) {
			this.sourceFeature = sourceFeature;
			this.targetFeatures = new ArrayList<>();
			this.type = RelType.parsePVStringToRelType(type);
		}

		public String getSourceFeature() {
			return sourceFeature;
		}

		public void setSourceFeature(String sourceFeature) {
			this.sourceFeature = sourceFeature;
		}

		public List<String> getTargetFeatures() {
			return targetFeatures;
		}

		public void setTargetFeatures(List<String> targetFeatures) {
			this.targetFeatures = targetFeatures;
		}

		public void addTargetFeature(String t) {
			this.targetFeatures.add(t);
		}

		public RelType getType() {
			return type;
		}

		public void setType(RelType type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return sourceFeature + " --(" + type.toString() + ")--> " + targetFeatures;
		}

	}

	// This class saves the dependencies info for CodeElement level VPs
	public static class VPDependency {

		// Attributes
		private String fileId;
		private String expresion;
		private List<Feature> referencedFeatures;

		public VPDependency(String fileId, String expresion,List<Feature> fs) {
			super();
			this.fileId = fileId;
			this.expresion = expresion;
			this.referencedFeatures = fs;
		}

		public String getFileId() {
			return fileId;
		}

		public void setFileId(String fileId) {
			this.fileId = fileId;
		}

		public String getExpresion() {
			return expresion;
		}

		public void setExpresion(String expresion) {
			this.expresion = expresion;
		}

		public List<Feature> getReferencedFeatures() {
			return referencedFeatures;
		}

		public void setReferencedFeatures(List<Feature> referencedFeatures) {
			this.referencedFeatures = referencedFeatures;
		}
		
		

	}

	// GENERIC

	private DepResolver() {
	}

	public static void emptyRelations() {
		deps = new ArrayList<>();
		vpDeps = new ArrayList<>();
	}

	// FEATURE DEPENDENCIES

	private static ArrayList<FeatureDependency> deps = new ArrayList<>();

	public static void addDep(FeatureDependency d) {
		deps.add(d);
	}

	public static ArrayList<FeatureDependency> getDeps() {
		return deps;
	}

	// CODE ELEMENT VP DEPENDENCIES

	private static ArrayList<VPDependency> vpDeps = new ArrayList<>();

	public static void addVPDep(VPDependency d) {
		vpDeps.add(d);
	}

	public static ArrayList<VPDependency> getVPDeps() {
		return vpDeps;
	}

}
