package your_code;

import java.util.LinkedHashMap;
import java.util.Map;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.*;

/**
 * Singleton class for logging error messages in an application.
 * <p>
 * Errors are recorded with their message, count of occurrences,
 * and location (class, method, file, and line number) of the first
 * and last occurrence.
 * <p>
 * This logger can display the error summary in either a JavaFX or
 * Swing-based window. It is useful for debugging or monitoring
 * errors during development and runtime.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * ErrorLogger logger = ErrorLogger.getInstance();
 * logger.report("File not found.");
 * logger.report("File not found.");
 * logger.report("Null pointer exception.");
 * System.out.println(logger); // Print report to console
 *
 * logger.showErrorWindowSwing(); // Show report in Swing window
 * // or
 * logger.showErrorWindowFx(); // Show report in JavaFX window
 * }</pre>
 */
public class ErrorLogger {
    // Singleton instance
    private static final ErrorLogger instance = new ErrorLogger();

    /**
     * Inner class that stores information about a specific error.
     */
    private static class ErrorInfo {
        String description;
        int count = 0;
        String firstOccurrence = "";
        String lastOccurrence = "";

        ErrorInfo(String description, String location) {
            this.description = description;
            this.count = 1;
            this.firstOccurrence = location;
            this.lastOccurrence = location;
        }

        /**
         * Updates the last occurrence of the error and increments the count.
         */
        void updateOccurrence(String location) {
            count++;
            lastOccurrence = location;
        }
    }

    // Map to store unique errors with their info
    private final Map<String, ErrorInfo> errorMap = new LinkedHashMap<>();
    private int totalCount = 0;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private ErrorLogger() {}

    /**
     * Returns the singleton instance of the logger.
     *
     * @return singleton instance
     */
    public static ErrorLogger getInstance() {
        return instance;
    }

    /**
     * Reports a new error message to the logger.
     * Automatically records where the error was reported from.
     *
     * @param errorMessage the error message to report
     */
    public void report(String errorMessage) {
        String location = getCallerInfo();
        ErrorInfo info = errorMap.get(errorMessage);

        if (info == null) {
            errorMap.put(errorMessage, new ErrorInfo(errorMessage, location));
        } else {
            info.updateOccurrence(location);
        }

        totalCount++;
        System.err.printf("ErrorLogger error reported:%n%s%nOccurred at: %s%n%n", errorMessage, location);
    }

    /**
     * Returns the location (class, method, file, line) where the `report()` method was called.
     */
    private String getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // Find the index of ErrorLogger.report() in the stack trace
        int reportMethodIndex = -1;
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement element = stackTrace[i];
            if (element.getClassName().equals(ErrorLogger.class.getName()) &&
                element.getMethodName().equals("report")) {
                reportMethodIndex = i;
                break;
            }
        }

        // Get the caller of report() (should be at reportMethodIndex + 1)
        if (reportMethodIndex >= 0 && reportMethodIndex + 1 < stackTrace.length) {
            StackTraceElement caller = stackTrace[reportMethodIndex + 1];
            return String.format("%s.%s(%s:%d)",
                    caller.getClassName(),
                    caller.getMethodName(),
                    caller.getFileName(),
                    caller.getLineNumber());
        }

        return "Unknown Source";
    }

    /**
     * Returns the total number of error reports received.
     *
     * @return total error count
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Resets the logger by clearing all error entries and counters.
     */
    public void resetCounts() {
        totalCount = 0;
        errorMap.clear();
    }

    /**
     * Returns a human-readable summary of all logged errors.
     *
     * @return formatted error report string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Error Report:\n");
        sb.append("Total errors: ").append(totalCount).append("\n\n");

        for (Map.Entry<String, ErrorInfo> entry : errorMap.entrySet()) {
            ErrorInfo info = entry.getValue();
            sb.append(String.format("Error: %s\n", info.description));
            sb.append(String.format("  Count: %d\n", info.count));
            sb.append(String.format("  First Occurrence: %s\n", info.firstOccurrence));
            sb.append(String.format("  Last Occurrence: %s\n\n", info.lastOccurrence));
        }

        return sb.toString();
    }

    /**
     * Displays the error summary in a JavaFX window.
     * Should be called from a JavaFX Application thread.
     */
    public void showErrorWindowFx() {
        Stage stage = new Stage();
        VBox layout = new VBox(10);
        Label errorLabel = new Label(toString());
        layout.getChildren().add(errorLabel);
        Scene scene = new Scene(layout, 500, 600);
        stage.setScene(scene);
        stage.setTitle("Error Report");
        stage.show();
    }

    /**
     * Displays the error summary in a Swing window.
     */
    public void showErrorWindowSwing() {
        JTextArea textArea = new JTextArea(toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(500, 600));

        JOptionPane.showMessageDialog(
                null,
                scrollPane,
                "Error Report",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Demonstrates the usage of the ErrorLogger.
     * Uncomment JavaFX code to test in a JavaFX context.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        ErrorLogger logger = ErrorLogger.getInstance();
        logger.report("Failed to load texture.");
        logger.report("Failed to load texture.");
        logger.report("Buffer overflow.");
        System.out.println(logger);
    }
}
