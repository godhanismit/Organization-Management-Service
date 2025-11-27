package BackendInternAssignment.example.Organization.Management.Service.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

