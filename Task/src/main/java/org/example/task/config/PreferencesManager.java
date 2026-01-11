package org.example.task.config;

import java.util.prefs.Preferences;

public class PreferencesManager {
    private final Preferences prefs;
    private static final String COMPILER_PATH_KOTLIN = "compilerPathKotlin";
    private static final String COMPILER_PATH_SWIFT = "compilerPathSwift";

    public PreferencesManager(Class<?> clazz) {
        this.prefs = Preferences.userNodeForPackage(clazz);
    }

    public String getCompilerPath(String language) {
        String key = language.equals("Kotlin") ? COMPILER_PATH_KOTLIN : COMPILER_PATH_SWIFT;
        String defaultPath = getDefaultCompilerPath(language);
        return prefs.get(key, defaultPath);
    }

    public void saveCompilerPath(String language, String path) {
        String key = language.equals("Kotlin") ? COMPILER_PATH_KOTLIN : COMPILER_PATH_SWIFT;
        prefs.put(key, path);
    }

    private String getDefaultCompilerPath(String language) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        if (language.equals("Kotlin")) {
            return isWindows ? "kotlinc.bat" : "kotlinc";
        } else {
            return isWindows ? "swift.exe" : "swift";
        }
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}

