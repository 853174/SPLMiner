package main;

import database.cmap.CMapDB;
import domain.cmap.CMap;
import miners.cmap.CMapMiner;

public class MainCMap {

	// This method mines CMap (cxl) files independently to the SPL mining, but it
	// does not make relations between LinkElements and Features.
	// [!] An existing SPL id is necessary
	public static void main(String[] args) {

		CMap cmap = CMapMiner.mineAlone("/home/user/Mahaigaina/GrAL/eclipse/WacLine/cmaps/WebAnnotation.cxl","cc5c2e3343e1398b8f7a8042f7741295a222fbf8");
		
		CMapDB.generateInserts(cmap);
		
		CMapDB.exportToFile("/home/user/Mahaigaina/GrAL/eclipse/WacLine/cmaps/WebAnnotation.sql");

	}

}
