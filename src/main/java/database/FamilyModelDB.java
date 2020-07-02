package database;

import domain.CodeElement;
import domain.CodeFile;
import domain.Code_VariationPoint;
import domain.Feature;
import domain.Part;
import domain.VariationPoint;
import miners.FamilyModelMiner;

public class FamilyModelDB {
	
	private static final String INSERT_CODE_ELEMENT = "INSERT INTO CODE_ELEMENT (ID,PATH,TYPE,PARENT,SPL_ID) VALUES (:id,:path,:type,:parent_id,:spl_id);";
	private static final String INSERT_DIRECTORY = "INSERT INTO DIRECTORY (ID) VALUES (:id);";
	private static final String INSERT_PART = "INSERT INTO PART (ID,PART_TYPE) VALUES (:id,:part_type);";
	private static final String INSERT_CODEFILE = "INSERT INTO CODEFILE (ID,FILENAME) VALUES (:id,:filename);";
	private static final String INSERT_VARIATION_POINT = "INSERT INTO VARIATION_POINT (ID,CODE_ELEMENT_ID,EXPRESION,VP_SIZE) VALUES (:id,:codeElement_id,:expresion,:vpSize);";
	private static final String INSERT_VARIATION_POINT_FEATURE = "INSERT INTO VARIATION_POINT_FEATURE (VP_ID,FEATURE_ID) VALUES (:vp_id,:feature_id);";
	private static final String INSERT_CODE_VARIATION_POINT = "INSERT INTO CODE_VARIATION_POINT (VP_ID,START_LINE,END_LINE,CONTENT,NESTING_LEVEL) VALUES (:vp_id,:start_line,:end_line,:content,:nesting_level);";
	
	public static void generateAllInserts(){
		
		// 1: All Feature's inserts, beginning from root, recursively
		// That way, all cross-references (FK) will be solved.
		CodeElement root = FamilyModelMiner.findRootCodeElement();
		
		if(root != null) {
			createCodeElementsInsertsRecursive(root);
		}
		
		
	}

	private static void createCodeElementsInsertsRecursive(CodeElement ce) {
		
		String parent_id = ce.getParent() == null ? "NULL" : MainSql.str(ce.getParent().getId());
		
		// 1: CodeElement's (generic) insert
		String g = INSERT_CODE_ELEMENT
				.replace(":id", MainSql.str(ce.getId()))
				.replace(":path",MainSql.str(ce.getPath()))
				.replace(":type", MainSql.str(ce.getType().toString()))
				.replace(":parent_id",parent_id)
				.replace(":spl_id", MainSql.str(ce.getSpl().getId()));
		
		// 2: CodeElement's (specific) insert
		String s;
		if(ce instanceof CodeFile) {
			CodeFile cf = (CodeFile) ce;
			s = INSERT_CODEFILE
					.replace(":id", MainSql.str(ce.getId()))
					.replace(":filename", MainSql.str(cf.getFilename()));
		}else if(ce instanceof Part) {
			Part p = (Part) ce;
			s = INSERT_PART
					.replace(":id", MainSql.str(ce.getId()))
					.replace(":part_type", MainSql.str(p.getPartType()));
		}else {
			s = INSERT_DIRECTORY
					.replace(":id", MainSql.str(ce.getId()));
		}
		
		MainSql.addInsert(g);
		MainSql.addInsert(s);
		
		// 3: VPs
		for(VariationPoint vp : ce.getVariationPoints()) {
			String vpI = INSERT_VARIATION_POINT
							.replace(":id", MainSql.str(vp.getId()))
							.replace(":codeElement_id",MainSql.str(vp.getFile().getId()))
							.replace(":expresion",MainSql.str(vp.getExpresion()))
							.replace(":vpSize",Integer.toString(vp.getVpSize()));
			
			MainSql.addInsert(vpI);
			
			for(Feature f : vp.getReferencedFeatures()) {
				String vpf = INSERT_VARIATION_POINT_FEATURE
									.replace(":vp_id", MainSql.str(vp.getId()))
									.replace(":feature_id", MainSql.str(f.getId()));
				
				MainSql.addInsert(vpf);
			}
			
			if(vp instanceof Code_VariationPoint) {
				Code_VariationPoint cvp = (Code_VariationPoint) vp;
				String cvpI = INSERT_CODE_VARIATION_POINT
								.replace(":vp_id", MainSql.str(vp.getId()))
								.replace(":start_line", Integer.toString(cvp.getStartLine()))
								.replace(":end_line", Integer.toString(cvp.getEndLine()))
								.replace(":content", MainSql.str(cvp.getContent()))
								.replace(":nesting_level",Integer.toString(cvp.getNestingLevel()));
				
				MainSql.addInsert(cvpI);
			}
		}
		
		
		// 4: Child's inserts
		for(CodeElement child : ce.getChildren()) {
			createCodeElementsInsertsRecursive(child);
		}
	}

}
