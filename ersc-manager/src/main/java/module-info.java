module com.jkromberg.erscm {
	exports com.jkromberg.erscm.updater;
	exports com.jkromberg.erscm.gui;
	exports com.jkromberg.erscm;

	requires javafx.base;
	requires javafx.controls;
	requires transitive javafx.graphics;
	requires jdk.crypto.ec;
}