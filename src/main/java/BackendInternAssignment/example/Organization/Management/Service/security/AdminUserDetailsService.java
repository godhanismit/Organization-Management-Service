package BackendInternAssignment.example.Organization.Management.Service.security;

import BackendInternAssignment.example.Organization.Management.Service.model.AdminUser;
import BackendInternAssignment.example.Organization.Management.Service.repository.AdminUserRepository;
import BackendInternAssignment.example.Organization.Management.Service.exception.NotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    public AdminUserDetailsService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser adminUser = adminUserRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("Admin user not found for email: " + username));
        return new AdminUserDetails(adminUser);
    }
}

