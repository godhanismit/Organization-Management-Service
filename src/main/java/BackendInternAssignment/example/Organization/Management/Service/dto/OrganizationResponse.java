package BackendInternAssignment.example.Organization.Management.Service.dto;

import java.time.Instant;

public record OrganizationResponse(
        String id,
        String organizationName,
        String collectionName,
        String connectionDetails,
        String adminEmail,
        Instant createdAt,
        Instant updatedAt
) {
}

