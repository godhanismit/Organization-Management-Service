package BackendInternAssignment.example.Organization.Management.Service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "organizations")
public class Organization {

    @Id
    private String id;

    @Indexed(unique = true)
    private String organizationName;

    @Indexed(unique = true)
    private String collectionName;

    private String connectionDetails;

    private String adminUserId;

    private Instant createdAt;

    private Instant updatedAt;

    public Organization() {
    }

    public Organization(String organizationName, String collectionName, String connectionDetails,
                        String adminUserId, Instant createdAt, Instant updatedAt) {
        this.organizationName = organizationName;
        this.collectionName = collectionName;
        this.connectionDetails = connectionDetails;
        this.adminUserId = adminUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getConnectionDetails() {
        return connectionDetails;
    }

    public void setConnectionDetails(String connectionDetails) {
        this.connectionDetails = connectionDetails;
    }

    public String getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(String adminUserId) {
        this.adminUserId = adminUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

