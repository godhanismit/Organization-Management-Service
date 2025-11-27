package BackendInternAssignment.example.Organization.Management.Service.repository;

import BackendInternAssignment.example.Organization.Management.Service.model.Organization;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OrganizationRepository extends MongoRepository<Organization, String> {

    boolean existsByOrganizationNameIgnoreCase(String organizationName);

    Optional<Organization> findByOrganizationNameIgnoreCase(String organizationName);

    boolean existsByCollectionName(String collectionName);
}

