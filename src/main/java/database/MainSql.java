package database;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import main.MainClass;

public class MainSql {
	
	private static ArrayList<String> inserts = new ArrayList<>();
	
	private final static String INSERT_SPL = "INSERT INTO SPL (ID,NAME) VALUES (:id,:name);";
	private final static String INSERT_GIT_SPL = "INSERT INTO GIT_SPL (ID,URL,LAST_CHANGED) VALUES (:splId,:url,:last_changed);";
	
	public static void addInsert(String s) {
		inserts.add(s);
	}
	
	public static ArrayList<String> getInserts(){
		return inserts;
	}
	
	public static void emptyInserts() {
		inserts = new ArrayList<>();
	}
	
	public static String str(String str) {
		return "\"" + 
				str.replaceAll("\"", "'")
					.replaceAll("\n", "#LINE_BREAK#")
				+ "\"";
	}

	public static void generateSPLInsert() {
		
		String s = INSERT_SPL
					.replace(":id", MainSql.str(MainClass.getSPL().getId()))
					.replace(":name", MainSql.str(MainClass.getSPL().getName()));
		inserts.add(s);
		
		SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		String g = INSERT_GIT_SPL
					.replace(":splId", MainSql.str(MainClass.getSPL().getId()))
					.replace(":url", MainSql.str(MainClass.getSPL().getGitHubUrl()))
					.replace(":last_changed", MainSql.str(dateFormater.format(MainClass.getSPL().getLastChange())));
		inserts.add(g);
	}
	
	public static void exportToFile(String filepath) {
		try {
			PrintWriter writer = new PrintWriter(filepath, "UTF-8");
			for (String i : inserts) {
				writer.println(i);
			}
			writer.close();
		} catch (Exception e) {

		}
	}

}
