package org.example.task.execution;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScriptExecutor {
    private final boolean isWindows;

    public ScriptExecutor() {
        this.isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    }

    public Process executeScript(String language, String code, String compilerPath) throws IOException {
        File sourceFile = createScriptFile(language, code);
        ProcessBuilder processBuilder = createProcessBuilder(language, compilerPath, sourceFile);
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

    private File createScriptFile(String language, String code) throws IOException {
        String extension = language.equals("Kotlin") ? ".kts" : ".swift";
        File sourceFile = File.createTempFile("script", extension);

        try (FileOutputStream fos = new FileOutputStream(sourceFile)) {
            if (language.equals("Swift") && !isWindows) {
                String shebang = "#!/usr/bin/env swift\n";
                fos.write(shebang.getBytes());
            }
            fos.write(code.getBytes());
            fos.flush();
        }

        if (!isWindows) {
            sourceFile.setExecutable(true);
        }

        sourceFile.deleteOnExit();
        return sourceFile;
    }

    private ProcessBuilder createProcessBuilder(String language, String compilerPath, File sourceFile) {
        ProcessBuilder processBuilder = new ProcessBuilder();

        if (isWindows) {
            if (language.equals("Kotlin")) {
                processBuilder.command(compilerPath, "-script", sourceFile.getAbsolutePath(), "-Xuse-fir-lt=false");
            } else {
                processBuilder.command(compilerPath, sourceFile.getAbsolutePath());
            }
        } else {
            if (language.equals("Kotlin")) {
                processBuilder.command("/usr/bin/env", "kotlin", sourceFile.getAbsolutePath());
            } else {
                processBuilder.command("/usr/bin/env", "swift", sourceFile.getAbsolutePath());
            }
        }

        return processBuilder;
    }
}

