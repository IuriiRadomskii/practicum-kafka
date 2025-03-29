package practicum.kafka.sprint.two.exceptions;

public class MessageNotHandledException extends RuntimeException {

    public MessageNotHandledException(String message) {
        super(message);
    }
}
