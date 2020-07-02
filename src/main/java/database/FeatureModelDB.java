package database;

import java.util.ArrayList;
import java.util.Map;

import domain.Attribute;
import domain.Feature;
import domain.FeatureModel;
import domain.Relation;
import main.MainClass;
import utils.FeatureSizeUtil;

public class FeatureModelDB {

	private static final String INSERT_FEATURE_MODEL = "INSERT INTO FEATURE_MODEL (ID,FILENAME,PATH,SPL) VALUES (:id,:filename,:path,:spl_id);";
	private static final String INSERT_FEATURE = "INSERT INTO FEATURE (ID,NAME,TYPE,PARENT,FEATURE_MODEL) VALUES (:id,:name,:type,:parent_id,:feature_model_id);";
	private static final String INSERT_DEPENDENCY = "INSERT INTO DEPENDENCY (ID,TYPE,SOURCE_FEATURE,TARGET_FEATURE) VALUES (:id,:type,:sf_id,:tf_id);";
	private static final String INSERT_ATTRIBUTE = "INSERT INTO ATTRIBUTE (ID,NAME,TYPE,VALUE,TARGET_FEATURE) VALUES (:id,:name,:type,:value,:target_feature);";
	private static final String INSERT_FEATURE_SIZE = "INSERT INTO FEATURE_SIZE (FEATURE_ID,SIZE) VALUES (:feature_id,:size);";
	
	private static ArrayList<Relation> deps;

	public static void generateAllInserts() {

		deps = new ArrayList<>();

		for (FeatureModel fm : MainClass.getSPL().getFeatureModels()) {
			if (fm == null) {
				MainClass.getLogger().severe("Feature Model doesn't exist, aborting Feature DB...");
				return;
			}

			// 1: Feature Model's insert
			String fmInsert = INSERT_FEATURE_MODEL.replace(":id", MainSql.str(fm.getId()))
					.replace(":filename", MainSql.str(fm.getFilename())).replace(":path", MainSql.str(fm.getPath()))
					.replace(":spl_id", MainSql.str(MainClass.getSPL().getId()));

			MainSql.addInsert(fmInsert);

			// 2: All Feature's inserts, beginning from root, recursively
			// That way, all parent cross-references (FK) will be solved. (NOT DEPENDENCIES)
			Feature root = findRootFeature(fm);

			if (root != null) {
				createFetureInsertsRecursive(root);
			}

		}

		// 3: Once we have all the Features, insert depencencies and exclusions
		for (Relation r : deps) {
			for (Feature t : r.getTargetFeatures())
				MainSql.addInsert(createDependencyInsert(r.getId(), r.getType().toString(),
						r.getSourceFeature().getId(), t.getId()));
		}
		
		// 4: Insert features' sizes
		Map<String, Integer> featureSizes = FeatureSizeUtil.getFeatureSizes();
		for(Map.Entry<String, Integer> featureSize : featureSizes.entrySet()) {
			String q = INSERT_FEATURE_SIZE
					.replace(":feature_id",MainSql.str(featureSize.getKey()))
					.replace(":size", Integer.toString(featureSize.getValue()));
			
			MainSql.addInsert(q);
		}

	}

	private static void createFetureInsertsRecursive(Feature f) {

		String parent_id = f.getParent() == null ? "NULL" : MainSql.str(f.getParent().getId());

		// 1: Feature's insert
		String s = INSERT_FEATURE.replace(":id", MainSql.str(f.getId())).replace(":name", MainSql.str(f.getName()))
				.replace(":type", MainSql.str(f.getType().toString())).replace(":parent_id", parent_id)
				.replace(":feature_model_id", MainSql.str(f.getFeatureModel().getId()));

		MainSql.addInsert(s);

		// 2: Feature's dependencies (for later)
		for (Relation r : f.getDependencies()) {
			deps.add(r);
		}
		
		// 3: Feature's attributes
		for (Attribute at : f.getAttributes()) {
			String a = INSERT_ATTRIBUTE
							.replace(":id",MainSql.str(at.getId()))
							.replace(":name", MainSql.str(at.getName()))
							.replace(":type", MainSql.str(at.getType()))
							.replace(":value", MainSql.str(at.getValue()))
							.replace(":target_feature", MainSql.str(f.getId()));
			
			MainSql.addInsert(a);
		}

		// 4: Child's inserts
		for (Feature child : f.getChildren()) {
			createFetureInsertsRecursive(child);
		}
	}

	private static String createDependencyInsert(String id, String type, String sf_id, String tf_id) {
		String s = INSERT_DEPENDENCY.replace(":id", MainSql.str(id)).replace(":type", MainSql.str(type))
				.replace(":sf_id", MainSql.str(sf_id)).replace(":tf_id", MainSql.str(tf_id));
		return s;
	}

	private static Feature findRootFeature(FeatureModel fm) {
		for (Feature f : fm.getFeatures()) {
			if (f.getParent() == null)
				return f;
		}

		return null;
	}

}
