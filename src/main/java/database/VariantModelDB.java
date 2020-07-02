package database;

import domain.VariantCode;
import domain.VariantComponent;
import domain.VariantFeature;
import domain.VariantModel;
import main.MainClass;

public class VariantModelDB {
	
	private static final String INSERT_VARIANT_MODEL = "INSERT INTO VARIANT_MODEL (ID,FILENAME,PATH,SPL_ID) VALUES (:id,:filename,:path,:spl_id);";
	private static final String INSERT_VARIANT_COMPONENT = "INSERT INTO VARIANT_COMPONENT (ID,VARIANT_MODEL,IS_SELECTED) VALUES (:id,:variant_model_id,:is_selected);";
	private static final String INSERT_VARIANT_FEATURE = "INSERT INTO VARIANT_FEATURE (VC_ID,FEATURE_ID) VALUES (:vc_id,:feature_id);";
	private static final String INSERT_VARIANT_CODE = "INSERT INTO VARIANT_CODE (VC_ID,CODE_ELEMENT_ID) VALUES (:vc_id,:code_element_id);";
	
	public static void generateAllInserts() {
		
		for(VariantModel vm : MainClass.getSPL().getVariantModels()) {
			// 1: Variant Model's insert
			String vmInsert = INSERT_VARIANT_MODEL
					.replace(":id", MainSql.str(vm.getId()))
					.replace(":filename", MainSql.str(vm.getFilename()))
					.replace(":path", MainSql.str(vm.getPath()))
					.replace(":spl_id", MainSql.str(MainClass.getSPL().getId()));
		
			MainSql.addInsert(vmInsert);
			
			// 2: VariantComponents' inserts
			for(VariantComponent vc : vm.getVariants()) {
				
				String isSelected = vc.isSelected() ? "TRUE" : "FALSE";
				
				String vcGenericInsert = INSERT_VARIANT_COMPONENT
						.replace(":id", MainSql.str(vc.getId()))
						.replace(":variant_model_id", MainSql.str(vm.getId()))
						.replace(":is_selected", isSelected);				
				
				MainSql.addInsert(vcGenericInsert);
				
				String vcSpecificInsert;
				
				if(vc instanceof VariantFeature) {
					vcSpecificInsert = INSERT_VARIANT_FEATURE
								.replace(":vc_id", MainSql.str(vc.getId()))
								.replace(":feature_id", MainSql.str(((VariantFeature) vc).getFeature().getId()));
				}else if(vc instanceof VariantCode) {
					vcSpecificInsert = INSERT_VARIANT_CODE
							.replace(":vc_id", MainSql.str(vc.getId()))
							.replace(":code_element_id", MainSql.str(((VariantCode) vc).getCodeFile().getId()));
				}else {
					continue;
				}
				
				MainSql.addInsert(vcSpecificInsert);
				
			}
			
		}
		
	}
	
}
