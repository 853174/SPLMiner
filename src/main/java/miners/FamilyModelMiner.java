package miners;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import domain.CodeElement;
import domain.CodeFile;
import domain.Directory;
import domain.Feature;
import domain.Part;
import domain.VariationPoint;
import main.MainClass;
import utils.GenericUtils;
import utils.DepResolver;
import utils.FeatureSizeUtil;
import utils.DepResolver.VPDependency;

public class FamilyModelMiner {

	private static String familyModelPath;
	private static Document doc;

	private static List<String> notConsideredVPFiles = Arrays.asList("png", "jpg", "svg","properties","gif");

	private FamilyModelMiner() {
	}
	
	public static boolean mineAll(List<String> familyModelPaths) {
		for(String fmp : familyModelPaths) {
			if(! mine(fmp)) {
				MainClass.getLogger().severe("Error mining Family Model. Path: " + fmp);
				return false;
			}
		}
		return true;
	}

	public static boolean mine(String fmp) {

		if (fmp == null) {
			MainClass.getLogger().severe("Family Model doesn't exist at: " + fmp + ". Aborting Family Mining...");
			return false;
		}

		familyModelPath = fmp;

		if (!extractAllCodeElementsFromFamilyModel())
			return false;

		resolveDependencies();

		return true;

	}

	private static boolean extractAllCodeElementsFromFamilyModel() {
		try {

			// 1: Open FamilyModel as XML
			File fXmlFile = new File(familyModelPath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			// 2: Check if actually is FamilyModel
			NodeList fileIdNL = doc.getElementsByTagName("cm:consulmodel");
			if (fileIdNL.getLength() != 1) {
				// It's not the FamilyModel!
				MainClass.getLogger().severe("Specified FamilyModel isn't valid, aborting...");
				return false;
			}
			Node fileIdN = fileIdNL.item(0);
			Element fileIdE = (Element) fileIdN;
			if (!fileIdE.getAttribute("cm:type").contentEquals("ps:ccfm")) {
				MainClass.getLogger().severe("Specified FamilyModel isn't valid, aborting...");
				return false;
			}

			// 3: Find root Element ID
			NodeList rootF = doc.getElementsByTagName("cm:elements");
			Element rootElem = (Element) rootF.item(0);
			String rootID = rootElem.getAttribute("cm:rootid");

			// 4: Create CodeElement with all root's the info
			CodeElement ce = new Directory(rootID, "", "ps:mandatory", MainClass.getSPL(), null);

			// 5: Get all child CodeElements tree starting from root
			ce.setChildren(getCodeElementTreeRecursively(rootID, ce));

			// 6: Now, we have completed our CodeElement's info. Save to the SPL.
			MainClass.getSPL().addCodeElement(ce);

			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;

	}

	private static ArrayList<CodeElement> getCodeElementTreeRecursively(String nowId, CodeElement parentCodeElement) {

		ArrayList<CodeElement> resultChildren = new ArrayList<>();

		// 1: Get XML element of 'nowId' (parentID)
		Element nowElement = getCodeElementByID(nowId);

		if (nowElement != null) {

			if (nowElement.hasAttribute("cm:constraint") | nowElement.hasAttribute("cm:restriction")) {

				// Save File level VPs for later
				storeFileLevelVPs(nowElement);

			}

			// 2: Iterate over all type of relations this element has
			NodeList relations = nowElement.getElementsByTagName("cm:relation");
			for (int i = 0; i < relations.getLength(); i++) {
				Node relation = relations.item(i);
				if (relation.getNodeType() == Node.ELEMENT_NODE) {
					Element rElem = (Element) relation;
					if (rElem.getAttribute("cm:type").contains("ps:requires")
							|| rElem.getAttribute("cm:type").contains("ps:conflicts")) {

						// IGNORE
						// Relations between CodeElement and Feature will only be read on "Restrictions"

						// 2.1: Relations here are dependencies (requires, requiresAll, conflicts...)
						/*
						 * NodeList targets = relation.getChildNodes(); List<String> targetIds = new
						 * ArrayList<>(); for (int j = 0; j < targets.getLength(); j++) { Node target =
						 * targets.item(j); if (target.getNodeType() == Node.ELEMENT_NODE) {
						 * 
						 * // Save the target IDs Element tElem = (Element) target;
						 * targetIds.add(Feature.parseId(tElem.getTextContent().substring(2))); } }
						 * 
						 * // 2.2: Create a Dependency with the information String type =
						 * rElem.getAttribute("cm:type"); if(type.contentEquals("ps:requires") &&
						 * targetIds.size() != 1) { type = "ps:requiresOne"; }else
						 * if(type.contentEquals("ps:conflicts") && targetIds.size() != 1) { type =
						 * "ps:conflictsOne"; }
						 * 
						 * Dependency d = new Dependency(parentCodeElement.getId(),type);
						 * d.setTargetFeatures(targetIds); DepResolver.addDep(d);
						 */

					} else if (!rElem.getAttribute("cm:type").equals("ps:parent")) {

						// 2.3: Relations here are children
						NodeList targets = relation.getChildNodes();
						for (int j = 0; j < targets.getLength(); j++) {
							Node target = targets.item(j);
							if (target.getNodeType() == Node.ELEMENT_NODE) {
								Element tElem = (Element) target;
								String id = tElem.getTextContent().substring(2);
								Element childElem = getCodeElementByID(id);

								CodeElement ce = null;

								// 3: Classify the XML element (file,directory or part)
								if (childElem.getAttribute("cm:class").contentEquals("ps:source")) {
									// 3.1: Element is a CodeFile
									String name = getCodeElementsFilenameById(id);
									ce = new CodeFile(id, parentCodeElement.getPath(), rElem.getAttribute("cm:type"),
											MainClass.getSPL(), parentCodeElement, name);

									if(isVPFile(name))
										CodeMiner.extractVPsFromFile((CodeFile) ce, childElem.getAttribute("cm:type"));

								} else if (childElem.getAttribute("cm:class").contentEquals("ps:component")) {
									// 3.2: Element is a Directory
									String path = getCodeElementsPathById(id);
									ce = new Directory(id, path, rElem.getAttribute("cm:type"), MainClass.getSPL(),
											parentCodeElement);

								} else if (childElem.getAttribute("cm:class").contentEquals("ps:part")) {
									// 3.3: Element is a Part
									ce = new Part(id, parentCodeElement.getPath(), rElem.getAttribute("cm:type"),
											MainClass.getSPL(), parentCodeElement, childElem.getAttribute("cm:type"));
								} else {
									MainClass.getLogger().severe("Unkown type of element at Family Model: " + childElem.getAttribute("cm:class"));
								}

								if (ce != null) {
									ce.setParent(parentCodeElement);

									// 3.2: Get all child CodeElements recursively
									ce.setChildren(getCodeElementTreeRecursively(id, ce));

									// 3.3: Now, we have completed our CodeElement's info. Save to the FamilyModel.
									MainClass.getSPL().addCodeElement(ce);

									// 3.4: Add this Feature to result list
									resultChildren.add(ce);
								}
							}
						}
					}
				}
			}
		}

		// 5: Return children list
		return resultChildren;

	}

	private static boolean isVPFile(String name) {
		String[] splitName = name.split("\\.");
		String format = splitName[splitName.length - 1];
		if(notConsideredVPFiles.contains(format))
			return false;
		
		return true;
	}

	private static void storeFileLevelVPs(Element nowElement) {

		if (doc != null) {
			// 1: Get restriction ids
			String restId = nowElement.getAttribute("cm:restriction");
			String constId = nowElement.getAttribute("cm:constraint");

			// 2: Find'em on <restrictions> part
			NodeList rests = doc.getElementsByTagName("cm:restset");
			for (int i = 0; i < rests.getLength(); i++) {
				Node rest = rests.item(i);
				if (rest.getNodeType() == Node.ELEMENT_NODE) {
					Element restElem = (Element) rest;
					String id = restElem.getAttribute("cm:id");
					if (id.contentEquals(restId) | id.contentEquals(constId)) {
						// Here is the restriction
						NodeList scrList = restElem.getElementsByTagName("cm:script");
						for (int j = 0; j < scrList.getLength(); j++) {
							Node scr = scrList.item(j);
							if (scr.getNodeType() == Node.ELEMENT_NODE) {
								Element scrElem = (Element) scr;
								// Save it for later
								ArrayList<Feature> fs = CodeMiner.extractVPsFromStatement(scrElem.getTextContent());
								VPDependency vpd = new VPDependency(nowElement.getAttribute("cm:id"),
										scrElem.getTextContent(), fs);
								DepResolver.addVPDep(vpd);
							}
						}
					}

				}
			}
		}

	}

	private static String getCodeElementsFilenameById(String id) {
		Element file = getCodeElementByID(id);

		// 1: Get element's properties and inspect'em
		NodeList properties = file.getElementsByTagName("cm:property");
		for (int i = 0; i < properties.getLength(); i++) {
			Node prop = properties.item(i);
			if (prop.getNodeType() == Node.ELEMENT_NODE) {
				Element propElem = (Element) prop;

				// 2: Check if it's the correct property
				if (propElem.getAttribute("cm:type").equals("ps:path")) {
					// 3: It's the property we are looking for, take it's value
					return propElem.getTextContent().trim();
				}
			}

		}

		return null;
	}

	private static String getCodeElementsPathById(String id) {
		Element file = getCodeElementByID(id);

		// 1: Get element's properties and inspect'em
		NodeList properties = file.getElementsByTagName("cm:property");
		for (int i = 0; i < properties.getLength(); i++) {
			Node prop = properties.item(i);
			if (prop.getNodeType() == Node.ELEMENT_NODE) {
				Element propElem = (Element) prop;

				// 2: Check if it's the correct property
				if (propElem.getAttribute("cm:type").equals("ps:directory")
						&& propElem.getAttribute("cm:name").equals("dir")) {
					// 3: It's the property we are looking for, take it's value
					return propElem.getTextContent().trim().replaceAll("\\.\\/", "");
				}
			}

		}

		return null;
	}

	private static Element getCodeElementByID(String findId) {

		try {
			// 1: Open FamilyModel as XML
			File fXmlFile = new File(familyModelPath);
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

						// 3.1: CodeElement found! Return XML element
						return eElement;
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 3.2: No CodeElement found...
		return null;
	}

	public static CodeElement findCodeElementById(String id) {
		if (MainClass.getSPL() != null) {
			for (CodeElement ce : MainClass.getSPL().getCodeElements()) {
				if (ce.getId().equals(id))
					return ce;
			}

			return null;
		} else {
			MainClass.getLogger().severe("You have to mine the Family Model before doing that.");
			return null;
		}
	}

	public static CodeElement findRootCodeElement() {
		if (MainClass.getSPL() != null) {
			for (CodeElement ce : MainClass.getSPL().getCodeElements()) {
				if (ce.getParent() == null)
					return ce;
			}

			return null;
		} else {
			MainClass.getLogger().severe("You have to mine the Family Model before doing that.");
			return null;
		}
	}

	private static void resolveDependencies() {

		for (VPDependency vpd : DepResolver.getVPDeps()) {
			CodeElement ce = findCodeElementById(vpd.getFileId());
			if (ce != null) {
				int vpSize = calculateCodeElementVPSize(ce);
				VariationPoint vp = new VariationPoint(vpd.getExpresion(), ce, vpSize);
				
				for(Feature f : vpd.getReferencedFeatures()) {
					vp.addReferencedFeature(f);
					FeatureSizeUtil.updateFeatureSizePlus(f, vpSize);
				}
				
				ce.addVariationPoint(vp);
				MainClass.getLogger().info("(" + vp.getFile().getId() + ") CodeElement level VP found.");
			} else {
				MainClass.getLogger().severe("Problem solving File level VP:");
				MainClass.getLogger().severe("    -> File ID: " + vpd.getFileId());
				MainClass.getLogger().severe("    -> Referenced features: " + vpd.getReferencedFeatures());
			}
		}
	}

	private static int calculateCodeElementVPSize(CodeElement ce) {
		if (ce instanceof CodeFile) {
			// VP size is File size
			CodeFile cf = (CodeFile) ce;
			if(isVPFile(cf.getFilename()))
				return GenericUtils.fileSize(MainClass.getCodeFolder() + "/" + cf.getPath() + "/" + cf.getFilename());
			else
				return 1; // Images, videos... have less importance than VP Files
		} else {
			// VP size is the sum of all the files that contains this folder/part
			int total = 0;
			for (CodeElement child : ce.getChildren()) {
				total += calculateCodeElementVPSize(child);
			}
			return total;
		}
	}

}
