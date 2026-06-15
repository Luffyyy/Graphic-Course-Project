package app_interface;


class FileParsingException extends Exception {
	private static final long serialVersionUID = 1L;

	public FileParsingException(String message) {
        super(message);
    }

    public FileParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
