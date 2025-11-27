package BackendInternAssignment.example.Organization.Management.Service.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}

