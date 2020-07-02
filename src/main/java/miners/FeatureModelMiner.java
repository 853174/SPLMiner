package miners;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import domain.Attribute;
import domain.Feature;
import domain.FeatureModel;
import domain.RelType;
import main.MainClass;
import utils.DepResolver;
import utils.DepResolver.FeatureDependency;
import utils.GenericUtils;

public class FeatureModelMiner {

	private FeatureModelMiner() {
	}

	public static boolean mineAll() {

		// 1: Mine all the FeatureModels
		for (FeatureModel fm : MainClass.getSPL().getFeatureModels()) {
			if (!mine(fm)) {
				MainClass.getLogger().severe("Error while mining Feature Model. Id: " + fm.getId());
				return false;
			}
		}

		// 2: Resolve dependencies
		resolveDependencies();

		return true;

	}

	private static boolean mine(FeatureModel fm) {

		// 1: Check if FeatureModel exists
		if (fm == null) {
			MainClass.getLogger().severe("Feature Model doesn't exist, aborting Feature Mining...");
			return false;
		}

		// 2: Extract all Features
		if (!extractAllFeaturesFromFeatureModel(fm)) {
			return false;
		}
		
		MainClass.getLogger().info("(" + fm.getId() + ") Features from model " + fm.getFilename() + ":");
		// 3: [DEBUG] Show Feature info
		for (Feature f : fm.getFeatures()) {
			MainClass.getLogger().info("(" + f.getId() + ") Feature with name \"" + f.getName() + "\" found.");
		}

		return true;
	}

	private static boolean extractAllFeaturesFromFeatureModel(FeatureModel fm) {
		try {

			String featureModelPath = GenericUtils.combinePaths(fm.getPath(), fm.getFilename());

			// 1: Open FeatureModel as XML
			File fXmlFile = new File(featureModelPath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			// 2: Check if actually is FeatureModel
			NodeList fileIdNL = doc.getElementsByTagName("cm:consulmodel");
			if (fileIdNL.getLength() != 1) {
				// It's not the FeatureModel!
				MainClass.getLogger().severe("Specified FeatureModel isn't valid, aborting...");
				return false;
			}
			Node fileIdN = fileIdNL.item(0);
			Element fileIdE = (Element) fileIdN;
			if (!fileIdE.getAttribute("cm:type").contentEquals("ps:fm")) {
				// It's not the FeatureModel!
				MainClass.getLogger().severe("Specified FeatureModel isn't valid, aborting...");
				return false;
			}

			// It's FeatureModel, get ID
			fm.setId(fileIdE.getAttribute("cm:id"));

			// 3: Find root Feature ID
			NodeList rootF = doc.getElementsByTagName("cm:elements");
			Element rootElem = (Element) rootF.item(0);
			String rootID = rootElem.getAttribute("cm:rootid");

			// 4: Once we have root Feature ID, find Feature name
			String name = getFeatureNameByID(featureModelPath, rootID);

			// 5: Create Feature with all the info
			Feature f = new Feature(rootID, name, "ps:mandatory", null, fm);
			
			getAllAttributes(getFeatureElementByID(featureModelPath, rootID),f);

			// 6: Get all child Features tree starting from root
			f.setChildren(getFeatureTreeRecursively(fm, featureModelPath, rootID, f));

			// 7: Now, we have completed our Feature's info. Save to the FeatureModel.
			fm.addFeature(f);

			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private static void getAllAttributes(Element e, Feature f) {
		
		
		
		if(e != null) {			
			NodeList attributes = e.getElementsByTagName("cm:property");
			for(int i = 0; i < attributes.getLength(); i++) {
				Node attr = attributes.item(i);
				if(attr.getNodeType() == Node.ELEMENT_NODE) {
					Element attrE = (Element) attr;
					if(! attrE.hasAttribute("cm:readonly")) {
						String id = attrE.getAttribute("cm:id");
						String name = attrE.getAttribute("cm:name");
						
						// Same attribute can contain more than one value (same ID, different value)
						NodeList attrVals = attrE.getElementsByTagName("cm:constant");
						for(int j = 0; j < attrVals.getLength(); j++) {
							Node attrVal = attrVals.item(j);
							if(attrVal.getNodeType() == Node.ELEMENT_NODE) {
								Element attrValE = (Element) attrVal;
								String type = attrValE.getAttribute("cm:type");
								String value = attrValE.getTextContent();
								Attribute a = new Attribute(id, name, type, value);
								f.addAttribute(a);
							}
						}
					}
				}
			}
		}else {
			// [ERROR]
		}
		
	}

	private static ArrayList<Feature> getFeatureTreeRecursively(FeatureModel fm, String path, String nowId,
			Feature parentFeature) {

		ArrayList<Feature> resultChildren = new ArrayList<Feature>();

		// 1: Get XML element of 'nowId'
		Element nowElement = getFeatureElementByID(path, nowId);

		if (nowElement != null) {

			// 2: Iterate over all type of relations this element has
			NodeList relations = nowElement.getElementsByTagName("cm:relation");
			for (int i = 0; i < relations.getLength(); i++) {
				Node relation = relations.item(i);
				if (relation.getNodeType() == Node.ELEMENT_NODE) {
					Element rElem = (Element) relation;
					if (rElem.getAttribute("cm:type").contains("ps:requires")
							|| rElem.getAttribute("cm:type").contains("ps:conflicts")) {

						// 2.1: Relations here are dependencies (requires, requiresAll, conflicts...)
						NodeList targets = relation.getChildNodes();
						List<String> targetIds = new ArrayList<>();
						for (int j = 0; j < targets.getLength(); j++) {
							Node target = targets.item(j);
							if (target.getNodeType() == Node.ELEMENT_NODE) {

								// Save the target IDs
								Element tElem = (Element) target;
								targetIds.add(Feature.parseId(tElem.getTextContent().substring(2)));
							}
						}

						// 2.2: Create a Dependency with the information
						String type = rElem.getAttribute("cm:type");
						if (type.contentEquals("ps:requires") && targetIds.size() != 1) {
							type = "ps:requiresOne";
						} else if (type.contentEquals("ps:conflicts") && targetIds.size() != 1) {
							type = "ps:conflictsOne";
						}

						FeatureDependency d = new FeatureDependency(parentFeature.getId(), type);
						d.setTargetFeatures(targetIds);
						DepResolver.addDep(d);

					} else if (!rElem.getAttribute("cm:type").equals("ps:parent")) {

						// 2.3: Relations here are children
						NodeList targets = relation.getChildNodes();
						for (int j = 0; j < targets.getLength(); j++) {
							Node target = targets.item(j);
							if (target.getNodeType() == Node.ELEMENT_NODE) {
								Element tElem = (Element) target;

								// 2.3.1: Create child Feature whit all the info
								String id = tElem.getTextContent().substring(2);
								String name = getFeatureNameByID(path, id);
								Feature child = new Feature(id, name, rElem.getAttribute("cm:type"), parentFeature, fm);
								child.setParent(parentFeature);
								
								getAllAttributes(getFeatureElementByID(path, id), child);

								// 2.3.2: Get all child Features recursively
								child.setChildren(getFeatureTreeRecursively(fm, path, id, child));

								// 2.3.3: Now, we have completed our Feature's info. Save to the FeatureModel.
								fm.addFeature(child);

								// 2.3.4: Add this Feature to result list
								resultChildren.add(child);
							}
						}
					}
				}
			}

		}

		// 3: Return children list
		return resultChildren;

	}

	private static void resolveDependencies() {

		for (FeatureDependency d : DepResolver.getDeps()) {
			Feature source = findFeatureById(d.getSourceFeature());
			List<Feature> targets = new ArrayList<>();
			RelType type = d.getType();

			for (String id : d.getTargetFeatures()) {
				targets.add(findFeatureById(id));
			}

			if (source != null && !targets.contains(null)) {

				source.addDependency(type, targets);
				MainClass.getLogger().info("Dependency found: " + d);
			} else {
				MainClass.getLogger().severe("Some error occurred while solving deps.");
				MainClass.getLogger().severe("	Source id: " + d.getSourceFeature());
				MainClass.getLogger().severe("	Targets id: " + d.getTargetFeatures());
				MainClass.getLogger().severe("	Type: " + d.getType());
			}

		}
	}

	private static String getFeatureNameByID(String path, String findId) {
		String featureName = "";

		try {
			// 1: Open FeatureModel as XML
			File fXmlFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			// 2: Iterate over all elements while looking for 'findId'
			NodeList nList = doc.getElementsByTagName("cm:element");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (findId.trim().equals(eElement.getAttribute("cm:id").trim())) {

						// 3.1: Feature found! Return name
						featureName = eElement.getAttribute("cm:name").trim();
						return featureName;
					}

				}
			}
		} catch (Exception e) {
			// [ERROR]
			e.printStackTrace();
		}
		// 3.2: No Feature found...
		return featureName;
	}

	private static Element getFeatureElementByID(String path, String findId) {

		try {
			// 1: Open FeatureModel as XML
			File fXmlFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			// 2: Iterate over all elements while looking for 'findId'
			NodeList nList = doc.getElementsByTagName("cm:element");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (findId.trim().equals(eElement.getAttribute("cm:id").trim())) {

						// 3.1: Feature found! Return XML element
						return eElement;
					}

				}
			}
		} catch (Exception e) {
			// [ERROR]
			e.printStackTrace();
		}
		// 3.2: No Feature found...
		return null;
	}

	public static Feature findFeatureById(String id) {
		if (id == null) {
			return null;
		}

		if (MainClass.getSPL() != null) {
			for (FeatureModel fm : MainClass.getSPL().getFeatureModels()) {
				for (Feature f : fm.getFeatures()) {
					if (f.getId().contentEquals(id))
						return f;
				}
			}

			return null;
		} else {
			MainClass.getLogger().severe("Feature with id \"" + id + "\" not found... You have to mine the FeatureModel before doing that.");
			return null;
		}
	}

	

}
