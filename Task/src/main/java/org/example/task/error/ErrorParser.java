package org.example.task.error;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorParser {
    private static final Pattern ERROR_PATTERN = Pattern.compile(".*\\.(kts|swift):([0-9]+):([0-9]+): error: (.*)");

    public Optional<ErrorLocation> parseError(String line) {
        Matcher matcher = ERROR_PATTERN.matcher(line);
        if (matcher.find()) {
            int lineNumber = Integer.parseInt(matcher.group(2));
            int columnNumber = Integer.parseInt(matcher.group(3));
            return Optional.of(new ErrorLocation(lineNumber, columnNumber));
        }
        return Optional.empty();
    }

    public static class ErrorLocation {
        private final int line;
        private final int column;

        public ErrorLocation(int line, int column) {
            this.line = line;
            this.column = column;
        }

        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }
    }
}

