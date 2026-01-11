# Script Runner

GUI tool for writing and executing Kotlin and Swift scripts with live output.

## Prerequisites

- **Java 11+**
- **Kotlin compiler** (for Kotlin scripts): https://kotlinlang.org/
- **Swift compiler** (for Swift scripts): https://www.swift.org/

## Build

**Windows:**
```powershell
.\gradlew.bat build
```

**Unix/macOS:**
```bash
./gradlew build
```

## Run

**Windows:**
```powershell
.\gradlew.bat run
```

**Unix/macOS:**
```bash
./gradlew run
```

## Usage

1. Select language (Kotlin or Swift) from dropdown
2. Set compiler path or use defaults (`kotlinc`/`kotlinc.bat` or `swift`/`swift.exe`)
3. Write script in left editor pane
4. Click "Run Script"
5. View output in right pane

## Features

- **Editor and output panes**: Side-by-side layout
- **Script execution**: Uses `/usr/bin/env kotlin/swift` on Unix/macOS, direct paths on Windows
- **Live output**: Real-time streaming as script executes
- **Running indicator**: Shows "Running..." status and exit code
- **Syntax highlighting**: 10 keywords per language (fun/func, val/let, var, if, else, when/switch, for, while, return, class)
- **Error navigation**: Click error locations to jump to line/column in editor

## Example Scripts

**Kotlin:**
```kotlin
fun greet(name: String) {
    println("Hello, $name!")
}
greet("World")
```

**Swift:**
```swift
func greet(name: String) {
    print("Hello, \(name)!")
}
greet(name: "World")
```

## Project Structure

```
src/main/java/org/example/task/
├── Main.java                    # UI and application entry
├── config/PreferencesManager    # User preferences
├── execution/ScriptExecutor     # Script execution logic
├── syntax/SyntaxHighlighter     # Keyword highlighting
└── error/ErrorParser            # Error parsing
```

## Technologies

- JavaFX 21
- RichTextFX 0.11.0
- Gradle

