package org.example.task.syntax;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxHighlighter {
    private final Pattern pattern;

    private static final String[] KOTLIN_KEYWORDS = {
        "fun", "val", "var", "if", "else", "when", "for", "while", "return", "class"
    };

    private static final String[] SWIFT_KEYWORDS = {
        "func", "let", "var", "if", "else", "switch", "for", "while", "return", "class"
    };

    public SyntaxHighlighter(String language) {
        String[] keywords = language.equals("Kotlin") ? KOTLIN_KEYWORDS : SWIFT_KEYWORDS;
        this.pattern = Pattern.compile("\\b(" + String.join("|", keywords) + ")\\b");
    }

    public StyleSpans<ObservableList<String>> computeHighlighting(String text) {
        StyleSpansBuilder<ObservableList<String>> spansBuilder = new StyleSpansBuilder<>();
        Matcher matcher = pattern.matcher(text);
        int lastKwEnd = 0;

        while (matcher.find()) {
            spansBuilder.add(FXCollections.observableArrayList(), matcher.start() - lastKwEnd);
            spansBuilder.add(FXCollections.observableArrayList("keyword"), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }

        spansBuilder.add(FXCollections.observableArrayList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}

