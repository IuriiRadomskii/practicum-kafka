package practicum.kafka.sprint.six.exceptions;

public class MessageNotHandledException extends RuntimeException {

    public MessageNotHandledException(String message) {
        super(message);
    }
}
