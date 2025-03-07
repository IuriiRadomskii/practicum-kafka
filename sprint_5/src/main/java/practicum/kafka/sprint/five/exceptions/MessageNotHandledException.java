package practicum.kafka.sprint.five.exceptions;

public class MessageNotHandledException extends RuntimeException {

    public MessageNotHandledException(String message) {
        super(message);
    }
}
