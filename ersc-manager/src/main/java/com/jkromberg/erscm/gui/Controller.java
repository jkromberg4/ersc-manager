package com.jkromberg.erscm.gui;

import java.io.File;
import java.io.IOException;

import com.jkromberg.erscm.updater.Ini;

import javafx.application.HostServices;

/**
 * Used by the view to control non-view related items.
 */
public class Controller {

	private Model model;
	private HostServices hostServices;

	/**
	 * Creates the controller with access to the model.
	 * 
	 * @param model The model that this controller should have access to
	 */
	public Controller(Model model) {
		this.model = model;
	}

	public void setHostServices(HostServices hostServices) {
		this.hostServices = hostServices;
	}

	/**
	 * Opens ersc_settings.ini with system specified program
	 */
	public void openSettings() {
		hostServices.showDocument(model.getCurrentSettings().getPath());
	}

	public void applyConfig() {
		model.applyConfig();
	}

	public void createConfig(String configName, Ini config) {
		model.createConfig(configName, config);
	}

	public void deleteConfig(String configName) {
		model.deleteConfig(configName);
	}

	public void setConfigUpdated(boolean configUpdated) {
		model.setConfigUpdated(configUpdated);
	}

	/**
	 * Checks the specified path for eldenring.exe then sets the game path if it is
	 * found. The path can be to the exe itself, the "/.../steamapps/common/ELDEN
	 * RING" folder, or the "/.../steamapps/common/ELDEN RING/Game" folder.
	 * 
	 * @param path The path to check
	 * @return True if eldenring.exe could be found, false if not
	 */
	public boolean checkAndSetDirForER(String path) {
		File erLoc = new File(path);
		if (erLoc.exists()) {
			if (erLoc.isDirectory()) { // Path is to a directory
				if (new File(path + "\\eldenring.exe").exists()) {
					model.setGamePath(path);
					return true;
				} else if (new File(path + "\\Game\\eldenring.exe").exists()) {
					model.setGamePath(path + "\\Game");
					return true;
				}
			} else { // Path is to a file
				if (new File(path.substring(0, path.lastIndexOf('\\')) + "\\eldenring.exe").exists()) {
					model.setGamePath(path.substring(0, path.lastIndexOf('\\')));
					return true;
				}
			}
		}

		return false;
	}

	public void addSaveFileExt(String newSaveExt) {
		if (!model.getSaveFileExtensions().contains(newSaveExt)) {
			model.getSaveFileExtensions().add(newSaveExt);
		}
	}

	public void updateERSC(String pathToStoreDefaultConfig) {
		model.updateERSC(pathToStoreDefaultConfig);
	}

	public void launchERSC() {
		try {
			Runtime.getRuntime().exec(new String[] { "cmd", "/c start ersc_launcher.exe" }, null, new File(model.getGamePath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
