package BackendInternAssignment.example.Organization.Management.Service.dto;

import java.time.Instant;

public record AuthResponse(
        String token,
        Instant expiresAt,
        String organizationId,
        String organizationName
) {
}

