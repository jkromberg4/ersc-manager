package com.jkromberg.erscm.gui;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import com.jkromberg.erscm.updater.Ini;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * The main scene for the gui.
 */
public class View {

	public static final String TITLE = "Elden Ring Seamless Coop Manager";

	private static int spacing = 5;

	private Model model;
	private Controller controller;

	private boolean confirmed;
	private boolean shouldClearConfigSelection;

	/* -- GUI Controls -- */
	private Scene configScene;

	private BorderPane configPane;

	private VBox configLeft;
	private ListView<String> configListView;
	private HBox configSelectControls;
	private Button deletePresetButton;
	private Button savePresetButton;

	private GridPane configCenter;
	private CheckBox allowInvadersCheck;
	private CheckBox deathDebuffsCheck;
	private CheckBox allowSummonsCheck;
	private ChoiceBox<String> overheadDisplayChoice;
	private CheckBox skipSplashCheck;
	private ChoiceBox<String> defaultVolumeChoice;
	private TextField enemyHealthField;
	private TextField enemyDamageField;
	private TextField enemyPostureField;
	private Button enemyConvertButton;
	private TextField bossHealthField;
	private TextField bossDamageField;
	private TextField bossPostureField;
	private Button bossConvertButton;
	private TextField passwordField;
	private ChoiceBox<String> saveFileChoice;
	private Button createSaveExtButton;
	private TextField languageField;
	private HBox configEditControls;
	private Button manualEditButton;
	private Button undoButton;
	private Button applyButton;

	private HBox configBottom;
	private Button updateButton;
	private Button launchButton;
	private Button closeButton;

	/**
	 * Creates the view with access to a model for observation and a controller.
	 * 
	 * @param model      The model
	 * @param controller The controller
	 */
	public View(Model model, Controller controller) {
		this.model = model;
		this.controller = controller;

		setupPane();
		loadInitialConfig();
		bindProperties();
		addHandlers();

		configPane.requestFocus();
	}

	/**
	 * Creates and arranges all the necessary gui elements.
	 */
	private void setupPane() {
		// Config Pane
		configPane = new BorderPane();
		configPane.getStyleClass().addAll("theme", "main-pane");

		/* LEFT -- config selector */
		configLeft = new VBox();
		configLeft.getStyleClass().add("config-left");
		configPane.setLeft(configLeft);

		Label configSelectLabel = new Label("Select Config");
		configSelectLabel.getStyleClass().add("config-header-label");
		configLeft.getChildren().add(configSelectLabel);

		configListView = new ListView<String>(model.getConfigNames());
		configListView.setPrefWidth(1);
		configLeft.getChildren().add(configListView);

		configSelectControls = new HBox();
		configSelectControls.getStyleClass().addAll("bottom-controls", "align-center");
		configSelectControls.setAlignment(Pos.CENTER);
		configSelectControls.setSpacing(spacing);
		configLeft.getChildren().add(configSelectControls);

		deletePresetButton = new Button("Delete preset");
		savePresetButton = new Button("Save preset");
		configSelectControls.getChildren().addAll(deletePresetButton, savePresetButton);

		/* CENTER -- config editor */
		configCenter = new GridPane();
		configCenter.getStyleClass().add("config-center");
		configPane.setCenter(configCenter);

		Label configEditLabel = new Label("Config settings");
		configEditLabel.getStyleClass().add("config-header-label");

		Label gameplayHeader = new Label("[GAMEPLAY]");
		gameplayHeader.getStyleClass().add("section-header-label");

		Label allowInvadersLabel = new Label("Allow invaders:");
		allowInvadersLabel.getStyleClass().add("config-body-label");
		allowInvadersCheck = new CheckBox();

		Label deathDebuffsLabel = new Label("Death Debuffs (Rot):");
		deathDebuffsLabel.getStyleClass().add("config-body-label");
		deathDebuffsCheck = new CheckBox();

		Label allowSummonsLabel = new Label("Allow summons:");
		allowSummonsLabel.getStyleClass().add("config-body-label");
		allowSummonsCheck = new CheckBox();

		Label overheadDisplayLabel = new Label("Overhead Player Display:");
		overheadDisplayLabel.getStyleClass().add("config-body-label");
		overheadDisplayChoice = new ChoiceBox<String>(model.getOverheadDisplayOptions());

		Label skipSplashLabel = new Label("Skip intro logos:");
		skipSplashLabel.getStyleClass().add("config-body-label");
		skipSplashCheck = new CheckBox();

		Label defaultVolumeLabel = new Label("Default boot master volume:");
		defaultVolumeLabel.getStyleClass().add("config-body-label");
		defaultVolumeChoice = new ChoiceBox<String>(model.getDefaultVolumeOptions());

		Label scalingHeader = new Label("[SCALING]");
		scalingHeader.getStyleClass().add("section-header-label");

		Label enemyHealthLabel = new Label("Enemy health scaling:");
		enemyHealthLabel.getStyleClass().add("config-body-label");
		enemyHealthField = new TextField();

		Label enemyDamageLabel = new Label("Enemy damage scaling:");
		enemyDamageLabel.getStyleClass().add("config-body-label");
		enemyDamageField = new TextField();

		Label enemyPostureLabel = new Label("Enemy posture absorption:");
		enemyPostureLabel.getStyleClass().add("config-body-label");
		enemyPostureField = new TextField();
		enemyConvertButton = new Button("Convert");

		Label bossHealthLabel = new Label("Boss health scaling:");
		bossHealthLabel.getStyleClass().add("config-body-label");
		bossHealthField = new TextField();

		Label bossDamageLabel = new Label("Boss damage scaling:");
		bossDamageLabel.getStyleClass().add("config-body-label");
		bossDamageField = new TextField();

		Label bossPostureLabel = new Label("Boss posture absorption:");
		bossPostureLabel.getStyleClass().add("config-body-label");
		bossPostureField = new TextField();
		bossConvertButton = new Button("Convert");

		Label passwordHeader = new Label("[PASSWORD]");
		passwordHeader.getStyleClass().add("section-header-label");

		Label passwordLabel = new Label("Session password:");
		passwordLabel.getStyleClass().add("config-body-label");
		passwordField = new TextField();

		Label saveHeader = new Label("[SAVE]");
		saveHeader.getStyleClass().add("section-header-label");

		Label saveFileLabel = new Label("Save file extenstion:");
		saveFileLabel.getStyleClass().add("config-body-label");
		saveFileChoice = new ChoiceBox<String>(model.getSaveFileExtensions());
		createSaveExtButton = new Button("Create");

		Label languageHeader = new Label("[LANGUAGE]");
		languageHeader.getStyleClass().add("section-header-label");

		Label languageLabel = new Label("Mod language override:");
		languageLabel.getStyleClass().add("config-body-label");
		languageField = new TextField();

		configEditControls = new HBox();
		configEditControls.getStyleClass().addAll("bottom-controls", "align-right");

		manualEditButton = new Button("Edit manually");
		undoButton = new Button("Undo");
		applyButton = new Button("Apply");
		configEditControls.getChildren().addAll(manualEditButton, undoButton, applyButton);

		int gridRow = 0;
		configCenter.add(configEditLabel, 0, gridRow++, 3, 1);

		configCenter.add(gameplayHeader, 0, gridRow++);
		configCenter.add(allowInvadersLabel, 0, gridRow);
		configCenter.add(allowInvadersCheck, 1, gridRow++);
		configCenter.add(deathDebuffsLabel, 0, gridRow);
		configCenter.add(deathDebuffsCheck, 1, gridRow++);
		configCenter.add(allowSummonsLabel, 0, gridRow);
		configCenter.add(allowSummonsCheck, 1, gridRow++);
		configCenter.add(overheadDisplayLabel, 0, gridRow);
		configCenter.add(overheadDisplayChoice, 1, gridRow++, 2, 1);
		configCenter.add(skipSplashLabel, 0, gridRow);
		configCenter.add(skipSplashCheck, 1, gridRow++);
		configCenter.add(defaultVolumeLabel, 0, gridRow);
		configCenter.add(defaultVolumeChoice, 1, gridRow++);

		configCenter.add(scalingHeader, 0, gridRow++);
		configCenter.add(enemyHealthLabel, 0, gridRow);
		configCenter.add(enemyHealthField, 1, gridRow++);
		configCenter.add(enemyDamageLabel, 0, gridRow);
		configCenter.add(enemyDamageField, 1, gridRow++);
		configCenter.add(enemyPostureLabel, 0, gridRow);
		configCenter.add(enemyPostureField, 1, gridRow);
		configCenter.add(enemyConvertButton, 2, gridRow++);
		configCenter.add(bossHealthLabel, 0, gridRow);
		configCenter.add(bossHealthField, 1, gridRow++);
		configCenter.add(bossDamageLabel, 0, gridRow);
		configCenter.add(bossDamageField, 1, gridRow++);
		configCenter.add(bossPostureLabel, 0, gridRow);
		configCenter.add(bossPostureField, 1, gridRow);
		configCenter.add(bossConvertButton, 2, gridRow++);

		configCenter.add(passwordHeader, 0, gridRow++);
		configCenter.add(passwordLabel, 0, gridRow);
		configCenter.add(passwordField, 1, gridRow++, 2, 1);

		configCenter.add(saveHeader, 0, gridRow++);
		configCenter.add(saveFileLabel, 0, gridRow);
		configCenter.add(saveFileChoice, 1, gridRow);
		configCenter.add(createSaveExtButton, 2, gridRow++);

		configCenter.add(languageHeader, 0, gridRow++);
		configCenter.add(languageLabel, 0, gridRow);
		configCenter.add(languageField, 1, gridRow++, 2, 1);
		configCenter.add(configEditControls, 0, gridRow++, 3, 1);

		/* BOTTOM -- update button and more controls */
		configBottom = new HBox();
		configBottom.getStyleClass().addAll("bottom-controls", "align-right");
		configPane.setBottom(configBottom);

		updateButton = new Button("Update Seamless Coop");
		launchButton = new Button("Launch Seamless Coop");
		closeButton = new Button("Close");
		configBottom.getChildren().addAll(updateButton, launchButton, closeButton);

		// Scene properties
		configScene = new Scene(configPane);
		configScene.getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());
	}

	/**
	 * Brings up a corresponding popup if either Elden Ring or the Seamless Coop mod
	 * could not be found. Then loads the current settings into the view.
	 */
	private void loadInitialConfig() {
		if (model.getCurrentSettings() == null) { // Either ER not found or no ERSC
			if (model.getGamePath() == null) { // Couldn't find ER
				noER();
			} else { // Found ER, but ERSC not installed
				noERSC();
			}
		} else { // ERSC settings found. Load them
			updateViewToReflectData(model.getCurrentSettings(), false);
			controller.setConfigUpdated(false);
		}
	}

	/**
	 * Popup when Elden Ring cannot be found. Asks for then validates a path to the
	 * game folder.
	 */
	private void noER() {
		final Stage noERpopup = new Stage();
		noERpopup.setTitle(TITLE);
		noERpopup.setOnCloseRequest(event -> {
			Platform.exit();
			System.exit(0);
		});

		VBox popupVBox = new VBox();
		popupVBox.getStyleClass().addAll("theme", "popup");

		Label popupLabel = new Label("Elden Ring installation not found. Please enter Elden Ring's location:\n\n"
				+ "Path to eldenring.exe:");
		popupLabel.getStyleClass().add("popup-label");

		HBox folderSelectBox = new HBox();
		folderSelectBox.setSpacing(spacing);
		VBox.setVgrow(folderSelectBox, Priority.ALWAYS);

		TextField locationField = new TextField();
		HBox.setHgrow(locationField, Priority.ALWAYS);

		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Executable", "*.exe"));

		Button browseButton = new Button("Browse...");
		browseButton.setOnAction(event -> {
			File erExe = fileChooser.showOpenDialog(noERpopup);

			if (erExe != null) {
				locationField.setText(erExe.getAbsolutePath());
			}
		});

		folderSelectBox.getChildren().addAll(locationField, browseButton);

		Label wrongDirLabel = new Label();
		wrongDirLabel.getStyleClass().add("error-label");

		HBox buttonBox = new HBox();
		buttonBox.getStyleClass().addAll("bottom-controls", "align-right");

		Button okButton = new Button("OK");
		okButton.setOnAction(event -> {
			// Check if location entered is valid
			if (controller.checkAndSetDirForER(locationField.getText())) {
				noERpopup.close();
				loadInitialConfig();
			} else {
				wrongDirLabel.setText("Incorrect path");
			}
		});

		Button exitButton = new Button("Close");
		exitButton.setOnAction(event -> {
			Platform.exit();
			System.exit(0);
		});

		buttonBox.getChildren().addAll(okButton, exitButton);

		popupVBox.getChildren().addAll(popupLabel, folderSelectBox, wrongDirLabel, buttonBox);

		Scene popupScene = new Scene(popupVBox);
		popupScene.getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());
		popupScene.setOnMousePressed(event -> {
			popupVBox.requestFocus();
		});

		popupVBox.requestFocus();
		noERpopup.setScene(popupScene);
		noERpopup.showAndWait();
	}

	/**
	 * Popup when the Seamless Coop mod cannot be found. Alerts the user that using
	 * the "Update Seamless Coop" feature will download and install the mod.
	 */
	private void noERSC() {
		configLeft.setDisable(true);
		configCenter.setDisable(true);
		launchButton.setDisable(true);

		final Stage noERSCpopup = new Stage();
		noERSCpopup.setTitle(TITLE);
		noERSCpopup.setResizable(false);
		noERSCpopup.setOnCloseRequest(event -> {
			Platform.exit();
			System.exit(0);
		});

		VBox popupVBox = new VBox();
		popupVBox.getStyleClass().addAll("theme", "popup");
		popupVBox.setAlignment(Pos.CENTER);

		Label popupLabel = new Label("Elden Ring Seamless Coop not installed.\n"
				+ "Click \"Update Seamless Coop\" on the next screen to download and install it.");
		popupLabel.getStyleClass().add("popup-label");
		popupLabel.setTextAlignment(TextAlignment.CENTER);

		HBox buttonBox = new HBox();
		buttonBox.getStyleClass().addAll("bottom-controls", "align-center");

		Button okButton = new Button("Got it");
		okButton.setOnAction(event -> {
			noERSCpopup.close();
		});

		buttonBox.getChildren().add(okButton);

		popupVBox.getChildren().addAll(popupLabel, buttonBox);

		Scene popupScene = new Scene(popupVBox);
		popupScene.getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());
		popupScene.setOnMousePressed(event -> {
			popupVBox.requestFocus();
		});

		popupScene.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
				okButton.fire();
			}
		});

		popupVBox.requestFocus();
		noERSCpopup.setScene(popupScene);
		noERSCpopup.showAndWait();
	}

	/**
	 * Updates all the gui elements to reflect a specified ini file.
	 * 
	 * @param settings          The ini file whose settings should be used to update
	 *                          the view
	 * @param preserveSelection If true, will maintain the current selection in the
	 *                          configListView
	 */
	private void updateViewToReflectData(Ini settings, boolean preserveSelection) {
		if (settings == null) {
			System.out.println("Cannot update view to reflect data because settings are null");
			return;
		}

		shouldClearConfigSelection = false;

		// Set all fields to their values from data
		allowInvadersCheck.setSelected(settings.get("allow_invaders").equals("0") ? false : true);
		deathDebuffsCheck.setSelected(settings.get("death_debuffs").equals("0") ? false : true);
		allowSummonsCheck.setSelected(settings.get("allow_summons").equals("0") ? false : true);

		try {
			overheadDisplayChoice.setValue(model.getOverheadDisplayOptions().get(
					Integer.parseInt(settings.get("overhead_player_display"))));
		} catch (NumberFormatException | IndexOutOfBoundsException e) {
			overheadDisplayChoice.setValue(settings.get("overhead_player_display"));
		}

		skipSplashCheck.setSelected(settings.get("skip_splash_screens").equals("0") ? false : true);
		defaultVolumeChoice.setValue(settings.get("default_boot_master_volume"));
		enemyHealthField.setText(settings.get("enemy_health_scaling"));
		enemyDamageField.setText(settings.get("enemy_damage_scaling"));
		enemyPostureField.setText(settings.get("enemy_posture_scaling"));
		bossHealthField.setText(settings.get("boss_health_scaling"));
		bossDamageField.setText(settings.get("boss_damage_scaling"));
		bossPostureField.setText(settings.get("boss_posture_scaling"));
		passwordField.setText(settings.get("cooppassword"));
		saveFileChoice.setValue(settings.get("save_file_extension"));
		languageField.setText(settings.get("mod_language_override"));

		// Clear config list view selection if needed
		shouldClearConfigSelection = true;
		if (!preserveSelection) {
			configListView.getSelectionModel().clearSelection();
		}
	}

	/**
	 * Binds the model properties for each Seamless Coop setting to the value in
	 * their respective gui component. Also sets the configUpdated flag in the model
	 * when any setting is changed.
	 */
	private void bindProperties() {
		model.allowInvadersProperty().bind(allowInvadersCheck.selectedProperty());
		model.allowInvadersProperty().addListener((obs, oldVal, newVal) -> {
			configChanged();
		});

		model.deathDebuffsProperty().bind(deathDebuffsCheck.selectedProperty());
		model.deathDebuffsProperty().addListener((obs, oldVal, newVal) -> {
			configChanged();
		});

		model.allowSummonsProperty().bind(allowSummonsCheck.selectedProperty());
		model.allowSummonsProperty().addListener((obs, oldVal, newVal) -> {
			configChanged();
		});

		model.overheadDisplayProperty().bind(overheadDisplayChoice.valueProperty());
		model.overheadDisplayProperty().addListener((obs, oldVal, newVal) -> {
			configChanged();
		});

		model.skipSplashProperty().bind(skipSplashCheck.selectedProperty());
		model.skipSplashProperty().addListener((obs, oldVal, newVal) -> {
			configChanged();
		});

		model.defaultVolumeProperty().bind(defaultVolumeChoice.valueProperty());
		model.defaultVolumeProperty().addListener((obs, oldVal, newVal) -> {
			configChanged();
		});

		model.enemyHealthProperty().bind(enemyHealthField.textProperty());
		model.enemyHealthProperty().addListener((obs, oldVal, newVal) -> {
			configChanged();
		});

		model.enemyDamageProperty().bind(enemyDamageField.textProperty());
		model.enemyDamageProperty().addListener((obs, oldVal, newVal) -> {
			configChanged();
		});

		model.enemyPostureProperty().bind(enemyPostureField.textProperty());
		model.enemyPostureProperty().addListener((obs, oldVal, newVal) -> {
			configChanged();
		});

		model.bossHealthProperty().bind(bossHealthField.textProperty());
		model.bossHealthProperty().addListener((obs, oldVal, newVal) -> {
			configChanged();
		});

		model.bossDamageProperty().bind(bossDamageField.textProperty());
		model.bossDamageProperty().addListener((obs, oldVal, newVal) -> {
			configChanged();
		});

		model.bossPostureProperty().bind(bossPostureField.textProperty());
		model.bossPostureProperty().addListener((obs, oldVal, newVal) -> {
			configChanged();
		});

		model.passwordProperty().bind(passwordField.textProperty());
		model.passwordProperty().addListener((obs, oldVal, newVal) -> {
			configChanged();
		});

		model.saveFileExtensionProperty().bind(saveFileChoice.valueProperty());
		model.saveFileExtensionProperty().addListener((obs, oldVal, newVal) -> {
			configChanged();
		});

		model.languageProperty().bind(languageField.textProperty());
		model.languageProperty().addListener((obs, oldVal, newVal) -> {
			configChanged();
		});
	}

	/**
	 * Updates the configUpdated flag in the model and clears the configListView
	 * selection if need be.
	 */
	private void configChanged() {
		controller.setConfigUpdated(true);
		if (shouldClearConfigSelection) {
			configListView.getSelectionModel().clearSelection();
		}
	}

	/**
	 * Adds the event handlers for all the main features available in the manager.
	 */
	private void addHandlers() {
		/* -- GENERAL -- */
		configScene.setOnMousePressed(event -> {
			if (!configListView.isFocused()) {
				configPane.requestFocus();
			}
		});

		/* -- LEFT -- */
		configListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				Platform.runLater(() -> {
					updateViewToReflectData(model.getConfigs().get(newVal), true);
				});
			}
		});

		deletePresetButton.disableProperty().bind(configListView.getSelectionModel().selectedIndexProperty().isEqualTo(-1)
				.or(configListView.getSelectionModel().selectedItemProperty().isEqualTo(Model.DEFAULT_CONFIG_NAME)));
		deletePresetButton.setOnAction(event -> {
			String currentConfigName = configListView.getSelectionModel().selectedItemProperty().get();
			configListView.getSelectionModel().clearSelection();
			if (currentConfigName != null) {
				controller.deleteConfig(currentConfigName);
			}
		});

		savePresetButton.setOnAction(event -> {
			createNewConfig();
		});

		/* -- CENTER -- */
		enemyConvertButton.setOnAction(event -> {
			convertAbsorption(enemyPostureField);
		});

		bossConvertButton.setOnAction(event -> {
			convertAbsorption(bossPostureField);
		});

		createSaveExtButton.setOnAction(event -> {
			createSaveExt();
		});

		manualEditButton.setOnAction(event -> {
			controller.openSettings();
		});

		undoButton.disableProperty().bind(model.configUpdatedProperty().not());
		undoButton.setOnAction(event -> {
			updateViewToReflectData(model.getCurrentSettings(), false);
			controller.setConfigUpdated(false);
			configPane.requestFocus();
		});

		applyButton.disableProperty().bind(model.configUpdatedProperty().not());
		applyButton.setOnAction(event -> {
			if (saveFileChoice.getValue().equals("sl2")) {
				if (confirmSL2()) {
					controller.applyConfig();
					configPane.requestFocus();
				}
			} else {
				controller.applyConfig();
				configPane.requestFocus();
			}
		});

		configScene.setOnKeyPressed(event -> {
			if (new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN).match(event)) {
				applyButton.fire();
			}
		});

		/* -- BOTTOM -- */
		updateButton.setOnAction(event -> {
			updateERSC();
		});

		launchButton.setOnAction(event -> {
			controller.launchERSC();
		});

		closeButton.setOnAction(event -> {
			Platform.exit();
		});
	}

	/**
	 * Popup to create a new config
	 */
	private void createNewConfig() {
		final Stage newConfigPopup = new Stage();
		newConfigPopup.setTitle("Create new config");
		newConfigPopup.initModality(Modality.APPLICATION_MODAL);
		newConfigPopup.setResizable(false);

		VBox popupVBox = new VBox();
		popupVBox.getStyleClass().addAll("theme", "popup");
		popupVBox.setAlignment(Pos.CENTER);

		Label popupLabel = new Label("Save the current settings as a new preset.\n"
				+ "Enter a name for this config.\n\n"
				+ "Name:");
		popupLabel.getStyleClass().add("popup-label");

		TextField configNameField = new TextField();
		configNameField.setTextFormatter(new TextFormatter<TextFormatter.Change>(c -> {
			if (c.isContentChange()) {
				if (c.getControlNewText().length() > 250) {
					c.setText(c.getControlNewText().substring(0, 250));
					c.setRange(0, 250);
				}
			}

			return c;
		}));

		Label invalidLabel = new Label();
		invalidLabel.getStyleClass().add("error-label");
		configNameField.textProperty().addListener((obs, oldVal, newVal) -> {
			invalidLabel.setText("");
		});

		HBox buttonBox = new HBox();
		buttonBox.getStyleClass().addAll("bottom-controls", "align-right");

		Button createButton = new Button("Create");
		createButton.setOnAction(event -> {
			String configName = configNameField.getText();
			if (configName.matches("[a-zA-Z0-9() _-]+")) {
				String configPath = model.getProgPath() + "\\ERSC Manager\\Configs\\" + configName + ".ini";
				try {
					if (new File(configPath).createNewFile()) {
						controller.createConfig(configName, new Ini(configPath));
						newConfigPopup.close();
						configListView.getSelectionModel().select(configName);
						configListView.requestFocus();
					} else {
						invalidLabel.setText("A config with that name already exists");
						configNameField.requestFocus();
						configNameField.selectAll();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				invalidLabel.setText("Name contains invalid characters");
				configNameField.requestFocus();
				configNameField.selectAll();
			}
		});

		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(event -> {
			newConfigPopup.close();
		});

		buttonBox.getChildren().addAll(createButton, cancelButton);

		popupVBox.getChildren().addAll(popupLabel, configNameField, invalidLabel, buttonBox);

		Scene popupScene = new Scene(popupVBox);
		popupScene.getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());

		popupScene.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				createButton.fire();
			}
		});

		configNameField.requestFocus();
		newConfigPopup.setScene(popupScene);
		newConfigPopup.showAndWait();
	}

	/**
	 * Popup to convert a posture scaling value (which may be more intuitive) to an
	 * absorption value.
	 * 
	 * @param fieldToUpdate The text field which should receive the calculated value
	 */
	private void convertAbsorption(TextField fieldToUpdate) {
		if (fieldToUpdate == null) {
			System.out.println("Conversion cannot occur because target field is null");
			return;
		}

		final Stage convertPopup = new Stage();
		convertPopup.setTitle("Convert posture scaling to absoprtion");
		convertPopup.initModality(Modality.APPLICATION_MODAL);
		convertPopup.setResizable(false);

		VBox popupVBox = new VBox();
		popupVBox.getStyleClass().addAll("theme", "popup");
		popupVBox.setAlignment(Pos.CENTER);

		Label popupLabel = new Label("Enter number of players and amount of extra posture per\n"
				+ "player to calculate the corresponding posture absorption.");
		popupLabel.getStyleClass().add("popup-label");

		Label spacerLabel = new Label();
		spacerLabel.getStyleClass().add("error-label");

		GridPane convertPane = new GridPane();
		convertPane.setStyle("-fx-hgap: 5; -fx-vgap: 1;");

		Label numPlayersLabel = new Label("Number of players:");
		TextField numPlayersField = new TextField();
		Label postureScalingLabel = new Label("Extra posture (%) per player:");
		TextField postureScalingField = new TextField();

		Label invalidLabel = new Label();
		invalidLabel.getStyleClass().add("error-label");
		numPlayersField.textProperty().addListener((obs, oldVal, newVal) -> {
			invalidLabel.setText("");
		});

		postureScalingField.textProperty().addListener((obs, oldVal, newVal) -> {
			invalidLabel.setText("");
		});

		HBox buttonBox = new HBox();
		buttonBox.getStyleClass().addAll("bottom-controls", "align-right");

		Button convertButton = new Button("Convert");
		convertButton.setOnAction(event -> {
			int numPlayers;
			double scaling;

			try {
				numPlayers = Integer.parseInt(numPlayersField.getText());
				if (numPlayers <= 1) {
					invalidLabel.setText("Player count must be an integer > 1");
					numPlayersField.requestFocus();
					numPlayersField.selectAll();
					return;
				}
			} catch (NumberFormatException e) {
				invalidLabel.setText("Player count must be an integer > 1");
				numPlayersField.requestFocus();
				numPlayersField.selectAll();
				return;
			}

			try {
				scaling = Double.parseDouble(postureScalingField.getText()) / 100;
				if (scaling < 0) {
					invalidLabel.setText("Posture percent must be positive");
					postureScalingField.requestFocus();
					postureScalingField.selectAll();
					return;
				}
			} catch (NumberFormatException e) {
				invalidLabel.setText("Posture percent must be a number");
				postureScalingField.requestFocus();
				postureScalingField.selectAll();
				return;
			}

			double absorption = (1 - (1 / (1 + (scaling * (numPlayers - 1))))) / (numPlayers - 1);
			DecimalFormat df = new DecimalFormat("#.##");
			df.setRoundingMode(RoundingMode.DOWN);
			fieldToUpdate.setText(df.format(absorption * 100));
			convertPopup.close();

		});

		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(event -> {
			convertPopup.close();
		});

		convertPane.add(numPlayersLabel, 0, 0);
		convertPane.add(numPlayersField, 1, 0);
		convertPane.add(postureScalingLabel, 0, 1);
		convertPane.add(postureScalingField, 1, 1);

		buttonBox.getChildren().addAll(convertButton, cancelButton);

		popupVBox.getChildren().addAll(popupLabel, spacerLabel, convertPane, invalidLabel, buttonBox);

		Scene popupScene = new Scene(popupVBox);
		popupScene.getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());
		popupScene.setOnMousePressed(event -> {
			popupVBox.requestFocus();
		});

		popupScene.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				convertButton.fire();
			}
		});

		numPlayersField.requestFocus();
		convertPopup.setScene(popupScene);
		convertPopup.showAndWait();
	}

	/**
	 * Popup to create a new save file extension.
	 */
	private void createSaveExt() {
		final Stage saveExtPopup = new Stage();
		saveExtPopup.setTitle("Create save file extension");
		saveExtPopup.initModality(Modality.APPLICATION_MODAL);
		saveExtPopup.setResizable(false);

		VBox popupVBox = new VBox();
		popupVBox.getStyleClass().addAll("theme", "popup");
		popupVBox.setAlignment(Pos.CENTER);

		Label popupLabel = new Label("Enter new save file extension (vanilla game uses .sl2).\n"
				+ "Use any alphanumeric characters (limit = 120).\n\n"
				+ "Extension:");
		popupLabel.getStyleClass().add("popup-label");

		TextField saveFileField = new TextField();
		saveFileField.setTextFormatter(new TextFormatter<TextFormatter.Change>(c -> {
			if (c.isContentChange()) {
				if (c.getControlNewText().length() > 120) {
					c.setText(c.getControlNewText().substring(0, 120));
					c.setRange(0, 120);
				}
			}

			return c;
		}));

		Label invalidExtLabel = new Label();
		invalidExtLabel.getStyleClass().add("error-label");
		saveFileField.textProperty().addListener((obs, oldVal, newVal) -> {
			invalidExtLabel.setText("");
		});

		HBox buttonBox = new HBox();
		buttonBox.getStyleClass().addAll("bottom-controls", "align-right");

		Button createButton = new Button("Create");
		createButton.setOnAction(event -> {
			String newSaveExt = saveFileField.getText();
			if (newSaveExt.matches("[a-zA-Z0-9]+")) {
				controller.addSaveFileExt(newSaveExt);
				saveFileChoice.setValue(newSaveExt);
				saveExtPopup.close();
			} else {
				invalidExtLabel.setText("File extension must be alphanumeric.");
				saveFileField.requestFocus();
				saveFileField.selectAll();
			}
		});

		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(event -> {
			saveExtPopup.close();
		});

		buttonBox.getChildren().addAll(createButton, cancelButton);

		popupVBox.getChildren().addAll(popupLabel, saveFileField, invalidExtLabel, buttonBox);

		Scene popupScene = new Scene(popupVBox);
		popupScene.getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());

		popupScene.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				createButton.fire();
			}
		});

		saveFileField.requestFocus();
		saveExtPopup.setScene(popupScene);
		saveExtPopup.showAndWait();
	}

	/**
	 * Popup to confirm a user wants to set the save file extension to .sl2 (Which
	 * contains vanilla save files).
	 * 
	 * @return True if the user selects "Yes", false if the user selects "No"
	 */
	private boolean confirmSL2() {
		confirmed = false;

		final Stage sl2Popup = new Stage();
		sl2Popup.setTitle(TITLE);
		sl2Popup.initModality(Modality.APPLICATION_MODAL);
		sl2Popup.setResizable(false);

		VBox popupVBox = new VBox();
		popupVBox.getStyleClass().addAll("theme", "popup");
		popupVBox.setAlignment(Pos.CENTER);

		Label popupLabel = new Label("Vanilla save file extension (.sl2) selected.\n"
				+ "Are you sure you want to use vanilla saves in seamless coop?");
		popupLabel.getStyleClass().add("popup-label");
		popupLabel.setTextAlignment(TextAlignment.CENTER);

		HBox buttonBox = new HBox();
		buttonBox.getStyleClass().addAll("bottom-controls", "align-center");

		Button yesButton = new Button("Yes");
		yesButton.setOnAction(event -> {
			confirmed = true;
			sl2Popup.close();
		});

		Button noButton = new Button("No");
		noButton.setOnAction(event -> {
			sl2Popup.close();
		});

		buttonBox.getChildren().addAll(yesButton, noButton);

		popupVBox.getChildren().addAll(popupLabel, buttonBox);

		Scene popupScene = new Scene(popupVBox);
		popupScene.getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());
		popupScene.setOnMousePressed(event -> {
			popupVBox.requestFocus();
		});

		popupVBox.requestFocus();
		sl2Popup.setScene(popupScene);
		sl2Popup.showAndWait();

		return confirmed;
	}

	/**
	 * Runs the updater to update Seamless Coop. A popup blocks all action until the
	 * update is complete.
	 */
	private void updateERSC() {
		// Create loading popup
		final Stage downloadPopup = new Stage();
		downloadPopup.setTitle("Updating Seamless Coop");
		downloadPopup.initModality(Modality.APPLICATION_MODAL);
		downloadPopup.initStyle(StageStyle.UNDECORATED);

		// Start update in new thread
		new Thread(() -> {
			controller.updateERSC(model.getProgPath() + "\\ERSC Manager\\Configs\\" + Model.DEFAULT_CONFIG_NAME + ".ini");

			// Close download popup once update is finished
			Platform.runLater(() -> {
				downloadPopup.close();
			});
		}).start();

		// Continue popup creation
		VBox popupVBox = new VBox();
		popupVBox.getStyleClass().addAll("theme", "popup");
		popupVBox.setStyle("-fx-background-color: yellow;");
		popupVBox.setAlignment(Pos.CENTER);

		Label popupLabel = new Label("Updating...");
		popupLabel.getStyleClass().add("popup-label");
		popupLabel.setTextAlignment(TextAlignment.CENTER);

		popupVBox.getChildren().addAll(popupLabel);

		Scene popupScene = new Scene(popupVBox, 300, 200);
		popupScene.getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());

		downloadPopup.setScene(popupScene);
		downloadPopup.showAndWait();

		// Once popup is closed, update is finished: Enable/resume normal operation
		configLeft.setDisable(false);
		configCenter.setDisable(false);
		launchButton.setDisable(false);
		updateViewToReflectData(model.getCurrentSettings(), false);
		controller.setConfigUpdated(false);
		configPane.requestFocus();
	}

	/**
	 * Returns the scene for use in the Main class
	 * 
	 * @return The scene for this view
	 */
	public Scene getScene() {
		return configScene;
	}

}
