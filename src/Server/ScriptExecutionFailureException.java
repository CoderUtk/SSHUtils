package Server;

public class ScriptExecutionFailureException extends Exception {

    private static final long serialVersionUID = 1L;

    ScriptExecutionFailureException(String s) {
        super("Error while executing Script: " + s);
    }
}
