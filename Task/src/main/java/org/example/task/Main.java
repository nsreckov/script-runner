package org.example.task;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.example.task.config.PreferencesManager;
import org.example.task.error.ErrorParser;
import org.example.task.execution.ScriptExecutor;
import org.example.task.syntax.SyntaxHighlighter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        PreferencesManager prefsManager = new PreferencesManager(Main.class);
        ScriptExecutor scriptExecutor = new ScriptExecutor();
        ErrorParser errorParser = new ErrorParser();

        CodeArea editor = createEditor();
        CodeArea output = createOutput();

        Button runButton = new Button("Run Script");
        Label statusLabel = new Label("Idle");
        Label exitCodeLabel = new Label("");

        ComboBox<String> languageBox = new ComboBox<>();
        languageBox.getItems().addAll("Kotlin", "Swift");
        languageBox.setValue("Kotlin");

        TextField compilerPathField = new TextField();
        compilerPathField.setPromptText("Path to kotlinc or swift");
        compilerPathField.setPrefWidth(300);
        compilerPathField.setText(prefsManager.getCompilerPath("Kotlin"));

        Button browseButton = new Button("Browse");

        setupLanguageListener(languageBox, compilerPathField, prefsManager);

        setupBrowseButton(browseButton, compilerPathField, languageBox, prefsManager, primaryStage);

        SyntaxHighlighter[] highlighterRef = {new SyntaxHighlighter("Kotlin")};
        setupSyntaxHighlighting(editor, languageBox, highlighterRef);

        List<ErrorSpan> errorSpans = new ArrayList<>();
        setupErrorNavigation(output, editor, errorSpans);

        setupRunButton(runButton, statusLabel, exitCodeLabel, output, editor,
                      languageBox, compilerPathField, errorSpans, scriptExecutor, errorParser);

        HBox controls = new HBox(10, languageBox, compilerPathField, browseButton,
                                runButton, statusLabel, exitCodeLabel);
        SplitPane splitPane = new SplitPane(editor, output);
        splitPane.setDividerPositions(0.5);

        BorderPane root = new BorderPane();
        root.setTop(controls);
        root.setCenter(splitPane);

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/org/example/task/syntax-highlighting.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/org/example/task/error-link.css").toExternalForm());
        output.getStylesheets().add(getClass().getResource("/org/example/task/error-link.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Script Runner");
        primaryStage.show();
    }

    private CodeArea createEditor() {
        CodeArea editor = new CodeArea();
        editor.setParagraphGraphicFactory(LineNumberFactory.get(editor));
        editor.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 16;");
        editor.setWrapText(true);
        return editor;
    }

    private CodeArea createOutput() {
        CodeArea output = new CodeArea();
        output.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14;");
        output.setWrapText(true);
        return output;
    }

    private void setupLanguageListener(ComboBox<String> languageBox, TextField compilerPathField,
                                      PreferencesManager prefsManager) {
        languageBox.setOnAction(ev -> {
            String language = languageBox.getValue();
            compilerPathField.setPromptText("Path to " +
                (language.equals("Kotlin") ? "kotlinc or kotlinc.bat" : "swift or swift.exe"));
            compilerPathField.setText(prefsManager.getCompilerPath(language));
        });
    }

    private void setupBrowseButton(Button browseButton, TextField compilerPathField,
                                  ComboBox<String> languageBox, PreferencesManager prefsManager,
                                  Stage primaryStage) {
        browseButton.setOnAction(ev -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Compiler Executable");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                String path = file.getAbsolutePath();
                compilerPathField.setText(path);
                prefsManager.saveCompilerPath(languageBox.getValue(), path);
            }
        });
    }

    private void setupSyntaxHighlighting(CodeArea editor, ComboBox<String> languageBox,
                                        SyntaxHighlighter[] highlighterRef) {
        editor.textProperty().addListener((obs, oldText, newText) -> {
            Platform.runLater(() -> {
                int caret = editor.getCaretPosition();
                editor.setStyleSpans(0, highlighterRef[0].computeHighlighting(newText));
                editor.displaceCaret(caret);
            });
        });

        languageBox.valueProperty().addListener((obs, oldLang, newLang) -> {
            highlighterRef[0] = new SyntaxHighlighter(newLang);
            String text = editor.getText();
            if (!text.isEmpty()) {
                editor.setStyleSpans(0, highlighterRef[0].computeHighlighting(text));
            }
        });
    }

    private void setupErrorNavigation(CodeArea output, CodeArea editor, List<ErrorSpan> errorSpans) {
        output.setOnMouseClicked(event -> {
            int mouseOffset = output.hit(event.getX(), event.getY()).getInsertionIndex();
            for (ErrorSpan span : errorSpans) {
                if (mouseOffset >= span.start && mouseOffset <= span.end) {
                    int caretPos = getCaretPositionForLineCol(editor.getText(), span.line, span.col);
                    editor.requestFocus();
                    editor.moveTo(caretPos);
                    break;
                }
            }
        });
    }

    private void setupRunButton(Button runButton, Label statusLabel, Label exitCodeLabel,
                               CodeArea output, CodeArea editor, ComboBox<String> languageBox,
                               TextField compilerPathField, List<ErrorSpan> errorSpans,
                               ScriptExecutor scriptExecutor, ErrorParser errorParser) {
        runButton.setOnAction(e -> {
            runButton.setDisable(true);
            statusLabel.setText("Running...");
            exitCodeLabel.setText("");
            output.clear();
            errorSpans.clear();

            String language = languageBox.getValue();
            String compilerPath = compilerPathField.getText();
            String sourceCode = editor.getText();

            new Thread(() -> {
                try {
                    Process process = scriptExecutor.executeScript(language, sourceCode, compilerPath);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    int outputLen = 0;
                    String line;

                    while ((line = reader.readLine()) != null) {
                        final String lineCopy = line;
                        Optional<ErrorParser.ErrorLocation> errorLoc = errorParser.parseError(lineCopy);

                        if (errorLoc.isPresent()) {
                            ErrorParser.ErrorLocation loc = errorLoc.get();
                            errorSpans.add(new ErrorSpan(outputLen, outputLen + lineCopy.length() + 1,
                                                        loc.getLine(), loc.getColumn()));
                            Platform.runLater(() -> output.append(lineCopy + "\n", "error-link"));
                        } else {
                            Platform.runLater(() -> output.append(lineCopy + "\n", ""));
                        }
                        outputLen += lineCopy.length() + 1;
                    }
                    reader.close();

                    int exitCode = process.waitFor();
                    Platform.runLater(() -> exitCodeLabel.setText("Exit Code: " + exitCode));
                } catch (Exception ex) {
                    Platform.runLater(() -> output.replaceText("Error: " + ex.getMessage()));
                } finally {
                    Platform.runLater(() -> {
                        runButton.setDisable(false);
                        statusLabel.setText("Idle");
                    });
                }
            }).start();
        });
    }

    static class ErrorSpan {
        int start, end, line, col;
        ErrorSpan(int start, int end, int line, int col) {
            this.start = start;
            this.end = end;
            this.line = line;
            this.col = col;
        }
    }

    private static int getCaretPositionForLineCol(String text, int line, int col) {
        int caret = 0;
        int currentLine = 1;
        for (int i = 0; i < text.length(); i++) {
            if (currentLine == line) {
                caret = i + col - 1;
                break;
            }
            if (text.charAt(i) == '\n') {
                currentLine++;
            }
        }
        return Math.max(0, Math.min(caret, text.length()));
    }
}
