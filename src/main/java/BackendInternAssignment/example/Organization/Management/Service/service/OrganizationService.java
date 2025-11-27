package BackendInternAssignment.example.Organization.Management.Service.service;

import BackendInternAssignment.example.Organization.Management.Service.dto.CreateOrganizationRequest;
import BackendInternAssignment.example.Organization.Management.Service.dto.DeleteOrganizationRequest;
import BackendInternAssignment.example.Organization.Management.Service.dto.OrganizationResponse;
import BackendInternAssignment.example.Organization.Management.Service.dto.UpdateOrganizationRequest;
import BackendInternAssignment.example.Organization.Management.Service.exception.BadRequestException;
import BackendInternAssignment.example.Organization.Management.Service.exception.ConflictException;
import BackendInternAssignment.example.Organization.Management.Service.exception.NotFoundException;
import BackendInternAssignment.example.Organization.Management.Service.model.AdminUser;
import BackendInternAssignment.example.Organization.Management.Service.model.Organization;
import BackendInternAssignment.example.Organization.Management.Service.repository.AdminUserRepository;
import BackendInternAssignment.example.Organization.Management.Service.repository.OrganizationRepository;
import BackendInternAssignment.example.Organization.Management.Service.security.AdminUserDetails;
import BackendInternAssignment.example.Organization.Management.Service.util.ValidationUtils;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;

    public OrganizationService(OrganizationRepository organizationRepository,
                               AdminUserRepository adminUserRepository,
                               PasswordEncoder passwordEncoder,
                               MongoTemplate mongoTemplate) {
        this.organizationRepository = organizationRepository;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.mongoTemplate = mongoTemplate;
    }

    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        ensureOrganizationNameAvailable(request.getOrganizationName());
        ensureAdminEmailAvailable(request.getEmail());

        String collectionName = buildCollectionName(request.getOrganizationName());
        ensureCollectionAvailable(collectionName);

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        Instant now = Instant.now();

        AdminUser adminUser = new AdminUser(request.getEmail(), hashedPassword, null, now, now);
        adminUser = adminUserRepository.save(adminUser);

        Organization organization = new Organization(
                request.getOrganizationName().trim(),
                collectionName,
                buildConnectionDetails(collectionName),
                adminUser.getId(),
                now,
                now
        );
        organization = organizationRepository.save(organization);
        adminUser.setOrganizationId(organization.getId());
        adminUserRepository.save(adminUser);

        if (!mongoTemplate.collectionExists(collectionName)) {
            mongoTemplate.createCollection(collectionName);
        }

        // Also persist the organization document inside the organization's own collection
        // so related entries and metadata are visible inside that per-org collection.
        mongoTemplate.save(organization, collectionName);

        return mapToResponse(organization, adminUser);
    }

    public OrganizationResponse getOrganizationByName(String organizationName) {
        ValidationUtils.requireNonBlank(organizationName, "organizationName");
        Organization organization = organizationRepository.findByOrganizationNameIgnoreCase(organizationName.trim())
                .orElseThrow(() -> new NotFoundException("Organization not found: " + organizationName));
        AdminUser adminUser = adminUserRepository.findById(organization.getAdminUserId())
                .orElseThrow(() -> new NotFoundException("Admin not found for organization"));
        return mapToResponse(organization, adminUser);
    }

    public OrganizationResponse updateOrganization(UpdateOrganizationRequest request, AdminUserDetails principal) {
        AdminUser adminUser = principal.getAdminUser();
        Organization organization = organizationRepository.findById(adminUser.getOrganizationId())
                .orElseThrow(() -> new NotFoundException("Organization not found for the current admin"));

        String trimmedOrgName = request.getOrganizationName().trim();
        if (!organization.getOrganizationName().equalsIgnoreCase(trimmedOrgName)) {
            ensureOrganizationNameAvailable(trimmedOrgName);
            String newCollectionName = buildCollectionName(trimmedOrgName);
            ensureCollectionAvailable(newCollectionName);
            copyCollection(organization.getCollectionName(), newCollectionName);
            organization.setCollectionName(newCollectionName);
            organization.setConnectionDetails(buildConnectionDetails(newCollectionName));
            organization.setOrganizationName(trimmedOrgName);
        }

        if (!adminUser.getEmail().equalsIgnoreCase(request.getEmail())) {
            ensureAdminEmailAvailable(request.getEmail(), adminUser.getId());
            adminUser.setEmail(request.getEmail());
        }

        if (StringUtils.hasText(request.getPassword())) {
            adminUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        Instant now = Instant.now();
        adminUser.setUpdatedAt(now);
        organization.setUpdatedAt(now);

    adminUserRepository.save(adminUser);
    organizationRepository.save(organization);

    // Ensure the updated organization document is present in the target per-org collection.
    // If the collection was just created/copied above, this will upsert the latest org metadata.
    mongoTemplate.save(organization, organization.getCollectionName());

    return mapToResponse(organization, adminUser);
    }

    public void deleteOrganization(DeleteOrganizationRequest request, AdminUserDetails principal) {
        AdminUser adminUser = principal.getAdminUser();
        Organization organization = organizationRepository.findById(adminUser.getOrganizationId())
                .orElseThrow(() -> new NotFoundException("Organization not found for the current admin"));

        if (!organization.getOrganizationName().equalsIgnoreCase(request.getOrganizationName().trim())) {
            throw new BadRequestException("Organization name mismatch. Provide the exact organization name to delete.");
        }

        if (mongoTemplate.collectionExists(organization.getCollectionName())) {
            mongoTemplate.dropCollection(organization.getCollectionName());
        }

        adminUserRepository.deleteById(adminUser.getId());
        organizationRepository.deleteById(organization.getId());
    }

    private void ensureOrganizationNameAvailable(String organizationName) {
        boolean exists = organizationRepository.existsByOrganizationNameIgnoreCase(organizationName.trim());
        if (exists) {
            throw new ConflictException("Organization already exists with name: " + organizationName);
        }
    }

    private void ensureAdminEmailAvailable(String email) {
        if (adminUserRepository.existsByEmailIgnoreCase(email.trim())) {
            throw new ConflictException("Admin already registered with email: " + email);
        }
    }

    private void ensureAdminEmailAvailable(String email, String currentAdminId) {
        Optional<AdminUser> existing = adminUserRepository.findByEmailIgnoreCase(email.trim());
        if (existing.isPresent() && !existing.get().getId().equals(currentAdminId)) {
            throw new ConflictException("Admin already registered with email: " + email);
        }
    }

    private void ensureCollectionAvailable(String collectionName) {
        if (organizationRepository.existsByCollectionName(collectionName) || mongoTemplate.collectionExists(collectionName)) {
            throw new ConflictException("Collection already exists for: " + collectionName);
        }
    }

    private String buildCollectionName(String organizationName) {
        String sanitized = organizationName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "_");
        sanitized = sanitized.replaceAll("_+", "_");
        sanitized = sanitized.replaceAll("^_|_$", "");
        if (!StringUtils.hasText(sanitized)) {
            throw new BadRequestException("Organization name must contain alphanumeric characters.");
        }
        return "org_" + sanitized;
    }

    private String buildConnectionDetails(String collectionName) {
        return "mongodb://localhost:27017/organization_service/" + collectionName;
    }

    private void copyCollection(String currentCollection, String nextCollection) {
        if (!mongoTemplate.collectionExists(currentCollection)) {
            mongoTemplate.createCollection(nextCollection);
            return;
        }
        mongoTemplate.createCollection(nextCollection);
        MongoCollection<Document> source = mongoTemplate.getCollection(currentCollection);
        MongoCollection<Document> destination = mongoTemplate.getCollection(nextCollection);
        for (Document document : source.find()) {
            destination.insertOne(document);
        }
        source.drop();
    }

    private OrganizationResponse mapToResponse(Organization organization, AdminUser adminUser) {
        return new OrganizationResponse(
                organization.getId(),
                organization.getOrganizationName(),
                organization.getCollectionName(),
                organization.getConnectionDetails(),
                adminUser.getEmail(),
                organization.getCreatedAt(),
                organization.getUpdatedAt()
        );
    }
}

