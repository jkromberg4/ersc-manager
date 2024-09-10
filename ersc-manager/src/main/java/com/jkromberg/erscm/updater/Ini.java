package com.jkromberg.erscm.updater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class to read and write .ini files
 */
@SuppressWarnings("serial")
public class Ini extends File {
	
	private HashMap<String, String> data;

	/**
	 * Creates a new Ini instance using the file at the specified path.
	 * 
	 * @param path A path string
	 */
	public Ini(String path) {
		super(path);
		parseData();
	}

	/**
	 * Parses this file as an ini file and stores all data in the data field.
	 */
	public void parseData() {
		data = new HashMap<String, String>();

		if (!this.exists()) {
			return;
		}

		// Read file and find all data fields
		try (BufferedReader iniReader = new BufferedReader(new FileReader(this))) {
			String line = iniReader.readLine();

			while (line != null) {
				line = line.trim();

				// Ignore empty lines, comments, and headers
				if (line.length() > 0 && line.charAt(0) != ';' && line.charAt(0) != '[') {
					String[] kv = line.split("=");
					if (kv.length == 1) {
						data.put(kv[0].trim(), "");
					} else {
						data.put(kv[0].trim(), line.substring(line.indexOf('=') + 1).trim());
					}
				}

				line = iniReader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the value that the specified setting is set to.
	 * 
	 * @param setting The setting whose current value is to be returned
	 * @return The value that the specified setting is set to, or null if there is
	 *         no such setting
	 */
	public String get(String setting) {
		return data.get(setting);
	}

	/**
	 * Associates the specified value with the specified key in this ini.
	 * 
	 * @param key   Key with which the specified value is to be associated
	 * @param value Value to be associated with the specified key
	 */
	public void set(String key, String value) {
		if (data == null) {
			return;
		}

		data.put(key, value);
		rewrite();
	}

	/**
	 * Sets the values of all keys to those from the specified map. Any keys present
	 * in this ini that do not have new values in the map will retain their old
	 * values. The file will then be rewritten with the updated values.
	 * 
	 * @param newData A map containing all the key value pairs to be stored
	 */
	public void setAll(HashMap<String, String> newData) {
		if (data == null || newData == null) {
			return;
		}

		data.putAll(newData);
		rewrite();
	}

	/**
	 * Rewrites the ini file to make it reflect any changes to the data.
	 */
	public void rewrite() {
		if (!this.exists() || data == null) {
			return;
		}

		// Stores keys that the file already has
		HashSet<String> usedKeys = new HashSet<String>();

		// Read in file while updating values
		String writeBuffer = "";
		try (BufferedReader iniReader = new BufferedReader(new FileReader(this))) {
			String line = iniReader.readLine();
			while (line != null) {
				line = line.trim();
				if (line.length() > 0 && line.charAt(0) != ';' && line.charAt(0) != '[') { // data field
					String[] kv = line.split("=");
					String key = kv[0].trim();
					if (data.containsKey(key)) {
						writeBuffer += kv[0].trim() + " = " + data.get(key);
						usedKeys.add(key);
					} else {
						writeBuffer += line;
					}
				} else { // comment, header, or empty line
					writeBuffer += line;
				}

				writeBuffer += "\r\n";
				line = iniReader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Add lines for any new keys
		for (String key : data.keySet()) {
			if (!usedKeys.contains(key)) {
				writeBuffer += key + " = " + data.get(key) + "\r\n";
			}
		}

		// Rewrite file in place
		try (BufferedWriter iniWriter = new BufferedWriter(new FileWriter(this))) {
			iniWriter.write(writeBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
