package domain;

public abstract class VariantComponent {

	// Attributes
	private String id;
	private boolean isSelected;

	// Relations
	private VariantModel variantModel;

	public VariantComponent(String id, boolean isSelected, VariantModel variantModel) {
		super();
		this.id = id;
		this.isSelected = isSelected;
		this.variantModel = variantModel;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public VariantModel getVariantModel() {
		return variantModel;
	}

	public void setVariantModel(VariantModel variantModel) {
		this.variantModel = variantModel;
	}

}
