package classes;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(String errMsg) {
        super(errMsg);
    }
}
