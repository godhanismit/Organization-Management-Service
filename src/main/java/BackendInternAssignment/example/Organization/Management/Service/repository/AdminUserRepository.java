package BackendInternAssignment.example.Organization.Management.Service.repository;

import BackendInternAssignment.example.Organization.Management.Service.model.AdminUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AdminUserRepository extends MongoRepository<AdminUser, String> {

    Optional<AdminUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}

