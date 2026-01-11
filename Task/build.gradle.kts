plugins {
    application
    java
}

repositories {
    mavenCentral()
}

val javafxVersion = "21"
val os = org.gradle.internal.os.OperatingSystem.current()
val platform = when {
    os.isWindows -> "win"
    os.isMacOsX -> "mac"
    else -> "linux"
}

dependencies {
    implementation("org.openjfx:javafx-base:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-controls:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-fxml:$javafxVersion:$platform")

    implementation("org.fxmisc.richtext:richtextfx:0.11.0")
}

application {
    mainClass.set("org.example.task.Main")
    applicationDefaultJvmArgs = listOf(
        "--module-path", "${'$'}{System.getProperty(\"java.library.path\")};${'$'}{project.buildDir}/libs",
        "--add-modules", "javafx.controls,javafx.fxml"
    )
}

tasks.named<JavaExec>("run") {
    val javafxModules = listOf("javafx.controls", "javafx.fxml")
    val javafxConfig = configurations.runtimeClasspath.get().filter { it.name.contains("javafx") }
    val javafxPath = javafxConfig.joinToString(separator = System.getProperty("path.separator")) { it.absolutePath }
    jvmArgs = listOf(
        "--module-path", javafxPath,
        "--add-modules", javafxModules.joinToString(",")
    )
}
