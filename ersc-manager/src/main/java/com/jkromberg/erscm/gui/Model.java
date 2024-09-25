package com.jkromberg.erscm.gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.jkromberg.erscm.updater.Ini;
import com.jkromberg.erscm.updater.Updater;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Stores the data for the manager.
 */
public class Model {

	public static final String DEFAULT_CONFIG_NAME = "Seamless Coop Defaults";

	private String gamePath;
	private String progPath;
	private Updater updater;
	private Ini currentSettings;
	private Ini erscmSettings;

	private final ObservableList<String> overheadDisplayOptions;
	private final ObservableList<String> defaultVolumeOptions;
	private final ObservableList<String> saveFileExtensions;
	private final ObservableList<String> configNames;
	private final HashMap<String, Ini> configs;

	// Model controlled properties
	private final BooleanProperty configUpdated = new SimpleBooleanProperty(false);

	// View controlled properties
	private final BooleanProperty allowInvaders = new SimpleBooleanProperty();
	private final BooleanProperty deathDebuffs = new SimpleBooleanProperty();
	private final BooleanProperty allowSummons = new SimpleBooleanProperty();
	private final StringProperty overheadDisplay = new SimpleStringProperty();
	private final BooleanProperty skipSplash = new SimpleBooleanProperty();
	private final StringProperty defaultVolume = new SimpleStringProperty();
	private final StringProperty enemyHealth = new SimpleStringProperty();
	private final StringProperty enemyDamage = new SimpleStringProperty();
	private final StringProperty enemyPosture = new SimpleStringProperty();
	private final StringProperty bossHealth = new SimpleStringProperty();
	private final StringProperty bossDamage = new SimpleStringProperty();
	private final StringProperty bossPosture = new SimpleStringProperty();
	private final StringProperty password = new SimpleStringProperty();
	private final StringProperty saveFileExtension = new SimpleStringProperty();
	private final StringProperty language = new SimpleStringProperty();

	/**
	 * Creates the model:
	 * <ul>
	 * <li>Creates "ERSC Manager" folder in program directory if needed</li>
	 * <li>Pulls game path from erscm_settings.ini if possible, otherwise attempts
	 * to automatically find game path</li>
	 * <li>Loads configs from "ERSC Manager/configs"</li>
	 * <li>Gets save file extensions from save data in "%appdata%/EldenRing.
	 * Currently gathers them from all steam users on the device.</li>
	 * </ul>
	 */
	public Model() {
		progPath = System.getProperty("user.dir");

		// Create ERSC Manager folder if it doesn't exist already
		File configDir = new File(progPath + "\\ERSC Manager\\configs");
		if (!configDir.exists()) {
			configDir.mkdirs();
		}

		// Get manager settings if they exist, otherwise create them
		erscmSettings = new Ini(progPath + "\\ERSC Manager\\erscm_settings.ini");
		if (!erscmSettings.exists()) {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(erscmSettings))) {
				erscmSettings.createNewFile();
				writer.write("game_path = ");
				writer.flush();
				erscmSettings.parseData();
			} catch (IOException e) {
				e.printStackTrace();
				erscmSettings = null;
			}
		}

		// Locate game dir and create updater
		if (erscmSettings != null) {
			gamePath = erscmSettings.get("game_path");
			if (gamePath == null) {
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(erscmSettings))) {
					writer.write("game_path = ");
					writer.flush();
					erscmSettings.parseData();
				} catch (IOException e) {
					e.printStackTrace();
					erscmSettings = null;
				}

				updater = new Updater();
			} else if (gamePath.equals("")) {
				updater = new Updater();
			} else {
				updater = new Updater(gamePath, progPath);
			}
		} else { // Error occurred, cannot use erscm_settings.ini
			updater = new Updater();
		}

		// Check if there is a valid game path already
		if (updater.getGamePath() != null && new File(updater.getGamePath() + "\\eldenring.exe").exists()) {
			setGamePath(updater.getGamePath());
		} else {
			gamePath = null;
			currentSettings = null;
		}

		// Create overhead display and volume options
		overheadDisplayOptions = FXCollections.observableArrayList("Normal", "None", "Ping", "Level", "Deaths", "Level & Ping");
		defaultVolumeOptions = FXCollections.observableArrayList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

		// Load existing config presets
		configNames = FXCollections.observableArrayList();
		configs = new HashMap<String, Ini>();
		refreshConfigList();

		// Search for save file extensions in appdata folder
		// Currently gets extensions across all steam users
		saveFileExtensions = FXCollections.observableArrayList();
		String erSavesPath = System.getenv("APPDATA") + "\\EldenRing";
		for (File dir : new File(erSavesPath).listFiles()) {
			if (dir.isDirectory()) {
				for (File save : dir.listFiles()) {
					String[] fileNameArr = save.getName().split("\\.");
					if (fileNameArr.length > 1 && fileNameArr[0].equals("ER0000")
							&& !saveFileExtensions.contains(fileNameArr[1])) {
						saveFileExtensions.add(fileNameArr[1]);
					}
				}
			}
		}
	}

	/**
	 * Updates Seamless Coop while storing the default settings as a config.
	 * 
	 * @param pathToStoreDefaultConfig Location that the default settings should be
	 *                                 copied to
	 */
	public boolean updateERSC(String pathToStoreDefaultConfig) {
		if (updater.update(pathToStoreDefaultConfig)) {
			reacquireSettings();
			Platform.runLater(() -> {
				refreshConfigList();
			});
			
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Re-obtains the Seamless Coop settings in case currentSettings is null.
	 */
	private void reacquireSettings() {
		if (gamePath != null) {
			currentSettings = new Ini(gamePath + "\\SeamlessCoop\\ersc_settings.ini");
			if (!currentSettings.exists()) {
				currentSettings = null;
			}
		}
	}

	/**
	 * Updates the config list to reflect what is in the configs folder. Also sorts
	 * the config list to put the default settings at the top.
	 */
	private void refreshConfigList() {
		File configDir = new File(progPath + "\\ERSC Manager\\configs");
		if (!configDir.exists()) {
			System.out.println("Cannot refresh config list: Config directory does not exist");
			return;
		}

		configNames.clear();
		configs.clear();

		for (File config : configDir.listFiles()) {
			String fileName = config.getName();
			if (config.isFile()
					&& fileName.contains(".")
					&& fileName.substring(fileName.lastIndexOf('.'), fileName.length()).equals(".ini")) {
				String configName = fileName.substring(0, fileName.lastIndexOf('.'));
				configNames.add(configName);
				configs.put(configName, new Ini(config.getAbsolutePath()));
			}
		}

		// Make default config the first one
		Collections.sort(configNames, new Comparator<String>() {
			@Override
			public int compare(String name1, String name2) {
				if (name1.equals(DEFAULT_CONFIG_NAME) || name2.equals(DEFAULT_CONFIG_NAME)) {
					if (name1.equals(name2)) {
						return 0;
					}

					return name1.equals(DEFAULT_CONFIG_NAME) ? -1 : 1;
				}

				return name1.compareTo(name2);
			}
		});
	}

	/**
	 * Updates ersc_settings.ini to reflect the data currently entered in the view
	 */
	public void applyConfig() {
		currentSettings.setAll(getDataFromView());
		configUpdated.set(false);
	}

	/**
	 * Creates a config with the specified settings and stores it in "ERSC
	 * Manager/configs".
	 * 
	 * @param configName Name for the config
	 * @param config     Settings to save as a config
	 */
	public void createConfig(String configName, Ini config) {
		config.setAll(getDataFromView());
		configs.put(configName, config);
		configNames.add(configName);
	}

	private HashMap<String, String> getDataFromView() {
		HashMap<String, String> newData = new HashMap<String, String>();

		newData.put("allow_invaders", allowInvaders.get() ? "1" : "0");
		newData.put("death_debuffs", deathDebuffs.get() ? "1" : "0");
		newData.put("allow_summons", allowSummons.get() ? "1" : "0");

		int overhead = overheadDisplayOptions.indexOf(overheadDisplay.get());
		if (overhead == -1) {
			newData.put("overhead_player_display", overheadDisplay.get());
		} else {
			newData.put("overhead_player_display", Integer.toString(overhead));
		}

		newData.put("skip_splash_screens", skipSplash.get() ? "1" : "0");
		newData.put("default_boot_master_volume", defaultVolume.get());
		newData.put("enemy_health_scaling", enemyHealth.get());
		newData.put("enemy_damage_scaling", enemyDamage.get());
		newData.put("enemy_posture_scaling", enemyPosture.get());
		newData.put("boss_health_scaling", bossHealth.get());
		newData.put("boss_damage_scaling", bossDamage.get());
		newData.put("boss_posture_scaling", bossPosture.get());
		newData.put("cooppassword", password.get());
		newData.put("save_file_extension", saveFileExtension.get());
		newData.put("mod_language_override", language.get());

		return newData;
	}

	/**
	 * Deletes a config, removing it from "ERSC Manager/configs".
	 * 
	 * @param configName Name of config to delete
	 */
	public void deleteConfig(String configName) {
		if (configName != DEFAULT_CONFIG_NAME) {
			configNames.remove(configName);
			configs.get(configName).delete();
			configs.remove(configName);
		}
	}

	public String getGamePath() {
		return gamePath;
	}

	public void setGamePath(String gamePath) {
		this.gamePath = gamePath;
		updater.setGamePath(gamePath);

		// Write change to erscm_settings.ini
		if (erscmSettings != null) {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(erscmSettings))) {
				writer.write("game_path = " + gamePath);
				writer.flush();
				erscmSettings.parseData();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Check if Seamless Coop is on this path and get current settings if so
		currentSettings = new Ini(gamePath + "\\SeamlessCoop\\ersc_settings.ini");
		if (!currentSettings.exists()) {
			currentSettings = null;
		}
	}

	public String getProgPath() {
		return progPath;
	}

	public Updater getUpdater() {
		return updater;
	}

	public Ini getCurrentSettings() {
		return currentSettings;
	}

	public ObservableList<String> getOverheadDisplayOptions() {
		return overheadDisplayOptions;
	}

	public ObservableList<String> getDefaultVolumeOptions() {
		return defaultVolumeOptions;
	}

	public ObservableList<String> getSaveFileExtensions() {
		return saveFileExtensions;
	}

	public ObservableList<String> getConfigNames() {
		return configNames;
	}

	public HashMap<String, Ini> getConfigs() {
		return configs;
	}

	public BooleanProperty configUpdatedProperty() {
		return configUpdated;
	}

	public void setConfigUpdated(boolean configUpdated) {
		this.configUpdated.set(configUpdated);
	}

	public BooleanProperty allowInvadersProperty() {
		return allowInvaders;
	}

	public BooleanProperty deathDebuffsProperty() {
		return deathDebuffs;
	}

	public BooleanProperty allowSummonsProperty() {
		return allowSummons;
	}

	public StringProperty overheadDisplayProperty() {
		return overheadDisplay;
	}

	public BooleanProperty skipSplashProperty() {
		return skipSplash;
	}

	public StringProperty defaultVolumeProperty() {
		return defaultVolume;
	}

	public StringProperty enemyHealthProperty() {
		return enemyHealth;
	}

	public StringProperty enemyDamageProperty() {
		return enemyDamage;
	}

	public StringProperty enemyPostureProperty() {
		return enemyPosture;
	}

	public StringProperty bossHealthProperty() {
		return bossHealth;
	}

	public StringProperty bossDamageProperty() {
		return bossDamage;
	}

	public StringProperty bossPostureProperty() {
		return bossPosture;
	}

	public StringProperty passwordProperty() {
		return password;
	}

	public StringProperty saveFileExtensionProperty() {
		return saveFileExtension;
	}

	public StringProperty languageProperty() {
		return language;
	}

}
