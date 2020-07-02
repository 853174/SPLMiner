package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

public class GenericUtils {
	
	private static ArrayList<String> ids = new ArrayList<>();

	public static String generateID() {
		// Generate random ID
		String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789_-"; // 63 chars

		Random rand = new Random();

		String result = "i";

		while (result.length() <= 16) {
			result += chars.charAt(rand.nextInt(63));
		}

		if (ids.contains(result)) // This ID is not unique
			return generateID();

		return result; // This ID is unique
	}
	
	public static String combinePaths(String base, String newPath) {

		if (base == null || newPath == null) {
			return null;
		}

		if (newPath.contentEquals(".")) {
			return base;
		}		
		
		String[] baseParts = base.split("/");
		String[] newPathParts = newPath.split("/");

		int baseStart = 0;
		int baseEnd = baseParts.length - 1;
		int newPathStart = 0;
		String start = "";

		if (newPathParts[0].contentEquals(".") || newPathParts[0].contentEquals("")) {
			newPathStart = 1;
		} else if (newPathParts[0].contentEquals("..")) {
			baseEnd -= 1;
			newPathStart = 1;
		}

		if (baseParts[0].contentEquals("")) {
			baseStart = 1;
			start = "/";
		}

		String path = start;
		for (int i = baseStart; i <= baseEnd; i++) {
			path += baseParts[i] + "/";
		}

		for (int j = newPathStart; j < newPathParts.length; j++) {
			path += newPathParts[j] + "/";
		}

		return path.substring(0, path.length() - 1);

	}
	
	public static int fileSize(String path) {
		int charCount = 0;

		try {
			BufferedReader bf = new BufferedReader(new FileReader(path));

			while (bf.readLine() != null) {
				charCount += 1;
			}

			bf.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return charCount;
	}
	
	public static String repeat(String s, int i) {
		String r = "";
		for(int j = 0; j < i; j++) {
			r += s;
		}
		
		return r;
	}

}
