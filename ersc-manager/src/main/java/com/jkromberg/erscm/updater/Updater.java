package com.jkromberg.erscm.updater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

/**
 * Class used to update Elden Ring Seamless Coop
 */
public class Updater {

	private String gamePath;
	private String progPath;

	/**
	 * Creates an Elden Ring Seamless Coop Updater without specifying Elden Ring's
	 * Game folder location. The Game folder will be found automatically, if
	 * possible.
	 */
	public Updater() {
		// Attempt to find game folder
		if (!findERDir("C:\\Program Files (x86)\\Steam")) {
			gamePath = null;
		}

		progPath = System.getProperty("user.dir");
	}

	/**
	 * Creates an Elden Ring Seamless Coop Updater with specified game and program
	 * paths.
	 * 
	 * @param gamePath Path to Elden Ring's Game folder
	 * @param progPath Path to this program's execution location
	 */
	public Updater(String gamePath, String progPath) {
		this.gamePath = gamePath;
		this.progPath = progPath;
	}

	/**
	 * Attempts to locate Elden Ring's Game folder and set gamePath to its path.
	 * 
	 * @param steamPath File path to Steam's install location
	 * @return True if the path to the Game folder was found, false if not
	 */
	private boolean findERDir(String steamPath) {
		// Use Steam's libraryfolders.vdf to locate all game directories
		String vdfPath = steamPath + "\\steamapps\\libraryfolders.vdf";
		File vdf = new File(vdfPath);
		if (!vdf.exists()) {
			return false;
		}

		// Extract library paths from vdf
		ArrayList<String> libPaths = new ArrayList<String>();
		try (BufferedReader vdfReader = new BufferedReader(new FileReader(vdf));) {
			String line = vdfReader.readLine();
			while (line != null) {
				line = line.trim();

				if (line.length() >= 5 && line.substring(1, 5).equals("path")) {
					libPaths.add(line.substring(9, line.length() - 1) + "\\steamapps\\common\\ELDEN RING\\Game");
				}

				line = vdfReader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		// Check each path for Elden Ring
		for (String path : libPaths) {
			File erDir = new File(path);
			if (erDir.exists()) {
				gamePath = path;
				return true;
			}
		}

		return false;
	}

	/**
	 * Updates Elden Ring Seamless Coop to the most recent version on GitHub:
	 * https://github.com/LukeYui/EldenRingSeamlessCoopRelease/releases/latest/download/ersc.zip
	 * 
	 * @return True if it successfully updated, false if not
	 */
	public boolean update(String copyPath) {
		if (gamePath == null || progPath == null) {
			System.out.println("Game or progam path null");
			return false;
		}

		// Locate existing config if there is one
		Ini ini = new Ini(gamePath + "\\SeamlessCoop\\ersc_settings.ini");
		if (!ini.exists()) {
			ini = null;
		}

		// Download new zip file to program location
		String zipLoc = progPath + "\\ersc.zip";

		URL seamlessURL = null;
		try {
			seamlessURL = URI.create("https://github.com/LukeYui/EldenRingSeamlessCoopRelease/releases/latest/download/ersc.zip").toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}

		try (ReadableByteChannel rbc = Channels.newChannel(seamlessURL.openStream()); FileOutputStream fos = new FileOutputStream(zipLoc);) {
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		// Extract files into game folder
		if (!ZipUtility.unzip(zipLoc, gamePath)) {
			System.out.println("Could not extract ersc files");
			return false;
		}

		// Delete zip
		File zip = new File(zipLoc);
		if (zip.exists()) {
			zip.delete();
		}

		// Copy default config file if path is specified
		if (copyPath != null) {
			Ini erscDefaults = new Ini(gamePath + "\\SeamlessCoop\\ersc_settings.ini");
			try {
				Files.copy(erscDefaults.toPath(), Paths.get(copyPath), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Reapply config
		if (ini != null && ini.exists()) {
			ini.rewrite();
		}

		return true;
	}
	
	public String getGamePath() {
		return gamePath;
	}

	public void setGamePath(String gamePath) {
		this.gamePath = gamePath;
	}

	public String getProgPath() {
		return progPath;
	}

	public void setProgPath(String progPath) {
		this.progPath = progPath;
	}

}
