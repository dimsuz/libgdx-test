package ru.dimsuz.libgdx.test;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import ru.dimsuz.libgdx.test.MyGdxGame;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("dz-test");
		boolean emulateLandscape = false;
		if (emulateLandscape) {
			config.setWindowedMode(920, 480);
		} else {
			config.setWindowedMode(480, 920);
		}
		new Lwjgl3Application(new MyGdxGame(), config);
	}
}
