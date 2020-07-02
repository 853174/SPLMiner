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

import domain.CodeElement;
import domain.CodeFile;
import domain.VariantCode;
import domain.VariantComponent;
import domain.VariantFeature;
import domain.VariantModel;
import main.MainClass;
import utils.GenericUtils;
import domain.Feature;
import domain.FeatureModel;

public class VariantModelMiner {

	public static void mineAll() {
		for (VariantModel vm : MainClass.getSPL().getVariantModels()) {
			MainClass.getLogger().info("(" + vm.getId() + ") Now mining: " + vm.getFilename());
			mine(vm);
		}
	}

	private static void mine(VariantModel vm) {

		extractAllVariantComponentsFromVariantModel(vm);

		List<Feature> untreatedFeatures = new ArrayList<>();
		for (FeatureModel fm : MainClass.getSPL().getFeatureModels()) {
			for (Feature f : fm.getFeatures()) {
				untreatedFeatures.add(f);
			}
		}

		List<CodeElement> untreatedCodeElements = new ArrayList<>();
		for (CodeElement ce : MainClass.getSPL().getCodeElements()) {
			untreatedCodeElements.add(ce);
		}

		// Remove treated Features and CodeElements from untreated list
		for (VariantComponent vc : vm.getVariants()) {
			String selected = vc.isSelected() ? "selected" : "unselected";
			if (vc instanceof VariantFeature) {
				VariantFeature vf = (VariantFeature) vc;
				MainClass.getLogger().info("(" + vf.getId() + ") Feature \"" + vf.getFeature() + "\" is " + selected);
				untreatedFeatures.remove(vf.getFeature());
			} else if (vc instanceof VariantCode) {
				VariantCode vf = (VariantCode) vc;
				String name = vf.getCodeFile() instanceof CodeFile
						? GenericUtils.combinePaths(vf.getCodeFile().getPath(), ((CodeFile) vf.getCodeFile()).getFilename())
						: vf.getCodeFile().getPath();
		
				MainClass.getLogger().info("(" + vf.getId() + ") CodeElement \"" + name + "\" is " + selected);
				untreatedCodeElements.remove(vf.getCodeFile());
			}
		}

		// Treat unselected Features that doesn't appear on the file
		for (Feature f : untreatedFeatures) {
			VariantFeature vf = new VariantFeature(GenericUtils.generateID(), false, vm, f);
			vm.addVariant(vf);
			MainClass.getLogger().info("(" + vf.getId() + ") Untreated Feature \"" + vf.getFeature() + "\" is unselected");
		}

		// Treat unselected CodeElements that doesn't appear on the file
		for (CodeElement ce : untreatedCodeElements) {
			VariantCode vf = new VariantCode(GenericUtils.generateID(), false, vm, ce);
			vm.addVariant(vf);
			MainClass.getLogger().info("(" + vf.getId() + ") Untreated CodeElement \"" + vf.getCodeFile() + "\" is unselected");
		}

	}

	private static void extractAllVariantComponentsFromVariantModel(VariantModel vm) {
		try {
			
			List<String> fmIds = new ArrayList<>();
			for(FeatureModel fm : MainClass.getSPL().getFeatureModels()) {
				fmIds.add(fm.getId());
			}

			// 1: Open VariantModel as XML
			File fXmlFile = new File(vm.getPath(), vm.getFilename());
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			// 2: Iterate all elements
			NodeList elements = doc.getElementsByTagName("cm:element");
			for (int i = 0; i < elements.getLength(); i++) {
				Node element = elements.item(i);
				if (element.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) element;

					// 3: Access the info of each element
					boolean selected = (e.getAttribute("cm:type").contentEquals("ps:selected")) ? true : false;
					String variantId = e.getAttribute("cm:id");

					NodeList targetNL = e.getElementsByTagName("cm:target");
					Node targetN = targetNL.item(0);
					Element targetE = (Element) targetN;
					String fullId = targetE.getTextContent().trim();

					// 4: Classify the element between VariantFeature and VariantCode
					String[] ids = fullId.split("/");

					VariantComponent vc;

					if (fmIds.contains(ids[0])) {
						// 4.1.1: It's a VariantFeature, find Feature
						Feature f = FeatureModelMiner.findFeatureById(ids[1]);

						// 4.1.2: Create new VariantFeature with the info
						vc = new VariantFeature(variantId, selected, vm, f);
					} else {
						// 4.2.1: It's a VariantCode, find CodeElement
						CodeElement ce = FamilyModelMiner.findCodeElementById(ids[1]);

						if (ce == null)
							continue;

						// 4.2.2: Create new VariantFeature with the info
						vc = new VariantCode(variantId, selected, vm, ce);
					}

					vm.addVariant(vc);

				}
			}

			// 5: Get the VariantModel ID for later
			NodeList fileIdNL = doc.getElementsByTagName("cm:consulmodel");
			Node fileIdN = fileIdNL.item(0);
			Element fileIdE = (Element) fileIdN;
			vm.setId(fileIdE.getAttribute("cm:id"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
