package BackendInternAssignment.example.Organization.Management.Service.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

