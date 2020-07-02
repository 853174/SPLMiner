package utils;

import java.util.HashMap;
import java.util.Map;

import domain.Feature;
import domain.FeatureModel;
import main.MainClass;

public class FeatureSizeUtil {

	private static Map<String, Integer> featureSizes;
	
	public static Map<String,Integer> getFeatureSizes(){
		return featureSizes;
	}

	public static void updateFeatureSizePlus(Feature f, int add) {
		updateFeature(f, add, true);
	}

	public static void udapteFeatureSizeMinus(Feature f, int sub) {
		updateFeature(f, sub, false);
	}

	private static void updateFeature(Feature f, int n, boolean add) {

		if (featureSizes == null) {
			
			featureSizes = new HashMap<String, Integer>();

			for (FeatureModel fm : MainClass.getSPL().getFeatureModels()) {
				for (Feature feat : fm.getFeatures()) {
					featureSizes.put(feat.getId(), 0);
				}
			}
		}

		// Update feature and it's ancestors

		Feature now = f;

		while (now != null) {
			String id = now.getId();

			int prevSize = featureSizes.get(id);

			if (add)
				featureSizes.put(id, prevSize + n);
			else
				featureSizes.put(id, prevSize - n);

			now = now.getParent();

		}

	}

}
