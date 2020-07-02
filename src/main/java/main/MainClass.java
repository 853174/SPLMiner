package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import database.*;
import database.cmap.CMapDB;
import domain.FeatureModel;
import domain.SPL;
import domain.VariantModel;
import domain.cmap.CMap;
import miners.*;
import miners.cmap.CMapMiner;
import utils.DepResolver;

public class MainClass {

	/*
	 * CONFIGURATION
	 */

	// Folder where it's located .git/ folder
	private static final String SPL_LOCAL_GIT_REPO = "/home/user/Mahaigaina/GrAL/eclipse/WacLine";

	// Folder where are all the code files, images...
	private static final String SPL_CODE_FOLDER = SPL_LOCAL_GIT_REPO + "/input";

	// SPL info
	private static final String SPL_NAME = "WacLine";
	
	// Mining type
	private static final int MINING_TYPE = 3;
	
	// CMap configuration (for MINING_TYPE == 2 OR 3)
	private static final String CMAPS_FOLDER = "/cmaps";
	private static final String[] CMAPS = {
				SPL_LOCAL_GIT_REPO + CMAPS_FOLDER + "/WebAnnotation.cxl"
			};

	/*
	 * IMPLEMENTATION
	 */

	public static final String[] VARIATION_POINT_NO_XML_STATEMENTS = { "PV:IFCOND", "PVSCL:IFCOND" };
	public static final String[] VARIATION_POINT_NO_XML_STATEMENTS_END = { "PV:ENDCOND", "PVSCL:ENDCOND" };

	private static SPL spl;

	public static SPL getSPL() {
		return spl;
	}

	public static String getCodeFolder() {
		return SPL_CODE_FOLDER;
	}
	
	private static Logger logger = Logger.getLogger(MainClass.class.getName());
	
	public static Logger getLogger() {
		return logger;
	}
	
	private static void clean() {
		DepResolver.emptyRelations();
		MainSql.emptyInserts();
	}

	// This function will try to read the Git repository and automatically get all
	// the files configured.
	// When the process is ended, a sql file is saved on SPL_LOCAL_GIT_REPO with the
	// name (SPL_NAME + _INSERTS.sql)
	
	// This function can make different mining processes (configure on MINING_TYPE variable):
	//	[1] Whole SPL mining
	//	[2] Partial SPL mining only for CMap connections (without SPL inserts)
	//	[3] Whole SPL mining and CMap connections
	public static void main(String[] args) {

		logger.setLevel(Level.INFO);
		
		List<String> familyModelPaths = new ArrayList<String>();

		try {

			/* GIT */

			// Get Git repository of specified folder
			Git git = Git.open(new File(SPL_LOCAL_GIT_REPO + "/.git"));
			Repository repo = git.getRepository();
			
			// By default takes master branch
			String branchRemote = repo.getConfig().getString("branch", "master", "remote");
			String gitHubUrl = repo.getConfig().getString("remote", branchRemote, "url");
			
			// Get the basic info of the repository
			ObjectId lastCommitId = repo.resolve(Constants.HEAD);
			RevWalk revWalk = new RevWalk(repo);
			RevCommit lastCommit = revWalk.parseCommit(lastCommitId);

			// Create the SPL object with the info
			spl = new SPL(lastCommitId.getName(), SPL_NAME, lastCommit.getAuthorIdent().getWhen(),gitHubUrl);

			// Autodetect all files on specified folder
			RevTree tree = lastCommit.getTree();
			TreeWalk treeWalk = new TreeWalk(repo);
			treeWalk.addTree(tree);
			treeWalk.setRecursive(true);

			while (treeWalk.next()) {
				String path = treeWalk.getPathString();

				if (isFeatureModel(path)) {
					FeatureModel fm = new FeatureModel(SPL_LOCAL_GIT_REPO + "/" + path);
					spl.addFeatureModel(fm);
				} else if (isFamilyModel(path)) {
					familyModelPaths.add(SPL_LOCAL_GIT_REPO + "/" + path);
				} else if (isVariantModel(path)) {
					VariantModel vm = new VariantModel(SPL_LOCAL_GIT_REPO + "/" + path);
					spl.addVariantModel(vm);
				}

			}

			revWalk.dispose();

			/* MINING */
			logger.info("--------------------------------");
			logger.info("-- STARTING SPL TO DB PROCESS --");
			
			
			switch (MINING_TYPE) {
			case 1:
				logger.info("--     Whole SPL minging      --");
				break;
			case 2:
				logger.info("--    Connect: CMap mining    --");
				break;
			case 3:
				logger.info("-- Whole SPL and CMap mining  --");
				break;
			}
			
			logger.info("--------------------------------");

			// 0: Start clean
			clean();

			if (spl.getFeatureModels().size() == 0 || familyModelPaths.isEmpty()) {
				// ERRORS FOUND, ABORT!
				logger.severe("SPL creation encountered problems, no Feature or Family Model found...");
				clean();
				return;
			}

			// 1: Mine all the files

			// 1.1: Mine the Feature Model
			logger.info("1. Starting Feature mining process");

			if (!FeatureModelMiner.mineAll()) {
				logger.severe("Feature mining process encountered problems, stoping...");
				clean();
				return;
			}

			logger.info("Ending Feature mining process");

			if(MINING_TYPE != 1) {
				
				for(String cmapPath : CMAPS) {
					CMap cmap = CMapMiner.mineWithConnections(cmapPath,spl);
					
					CMapDB.generateInserts(cmap);
					
					CMapDB.exportToFile(SPL_LOCAL_GIT_REPO + CMAPS_FOLDER +  "/" + cmap.getName() + "_INSERTS.sql");
				
					logger.info("Concept map '" + cmap.getName() + "' processed");
				}
				
				if(MINING_TYPE == 2)
					return;
			}

			// 1.2: Mine the Family Model
			logger.info("2. Starting Family Model mining process");

			if (!FamilyModelMiner.mineAll(familyModelPaths)) {
				logger.severe("Family Model mining process encountered problems, stoping...");
				clean();
				return;
			}

			logger.info("Ending Family Model mining process");

			// 1.3: Mine the Variant Models
			logger.info("3. Starting Variant Model mining process");

			VariantModelMiner.mineAll();

			logger.info("Ending Variant Model mining process");

			// 2: Generate inserts for filling the DB

			// 2.1: SPL related insert
			MainSql.generateSPLInsert();

			// 2.2: Feature related inserts
			FeatureModelDB.generateAllInserts();

			// 2.3: CodeElement related inserts
			FamilyModelDB.generateAllInserts();

			// 2.4: Variants related inserts
			VariantModelDB.generateAllInserts();

			// 3: Save them to a file
			MainSql.exportToFile(SPL_LOCAL_GIT_REPO + "/" + SPL_NAME + "_INSERTS.sql");

			logger.info("SPL mining process ended, " + MainSql.getInserts().size() + " inserts generated.");
			logger.info("Inserts are stored at: " + SPL_LOCAL_GIT_REPO + "/" + SPL_NAME + "_INSERTS.sql");
			
			clean();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// Regex used for autodetection
	private static final String FEATURE_MODEL_PATTERN = ".*\\.xfm$";
	private static final String FAMILY_MODEL_PATTERN = ".*\\.ccfm$";
	private static final String VARIANT_MODEL_PATTERN = ".*\\.vdm$";

	private static boolean isFeatureModel(String s) {
		Pattern p = Pattern.compile(FEATURE_MODEL_PATTERN);
		Matcher m = p.matcher(s);

		return m.find();
	}

	private static boolean isFamilyModel(String s) {
		Pattern p = Pattern.compile(FAMILY_MODEL_PATTERN);
		Matcher m = p.matcher(s);

		return m.find();
	}

	private static boolean isVariantModel(String s) {
		Pattern p = Pattern.compile(VARIANT_MODEL_PATTERN);
		Matcher m = p.matcher(s);

		return m.find();
	}

}
