package domain;

public class VariantFeature extends VariantComponent{
	
	private Feature feature;
	
	public VariantFeature(String id, boolean isSelected, VariantModel variantModel, Feature f) {
		super(id, isSelected, variantModel);
		this.feature = f;
	}

	public Feature getFeature() {
		return feature;
	}

	public void setFeature(Feature feature) {
		this.feature = feature;
	}
	
	

}
