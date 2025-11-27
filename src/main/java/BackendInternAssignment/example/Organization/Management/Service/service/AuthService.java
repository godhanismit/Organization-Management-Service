package BackendInternAssignment.example.Organization.Management.Service.service;

import BackendInternAssignment.example.Organization.Management.Service.dto.AuthResponse;
import BackendInternAssignment.example.Organization.Management.Service.dto.LoginRequest;
import BackendInternAssignment.example.Organization.Management.Service.exception.NotFoundException;
import BackendInternAssignment.example.Organization.Management.Service.exception.UnauthorizedException;
import BackendInternAssignment.example.Organization.Management.Service.model.AdminUser;
import BackendInternAssignment.example.Organization.Management.Service.model.Organization;
import BackendInternAssignment.example.Organization.Management.Service.repository.AdminUserRepository;
import BackendInternAssignment.example.Organization.Management.Service.repository.OrganizationRepository;
import BackendInternAssignment.example.Organization.Management.Service.security.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(AdminUserRepository adminUserRepository,
                       OrganizationRepository organizationRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService jwtTokenService) {
        this.adminUserRepository = adminUserRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public AuthResponse login(LoginRequest request) {
        AdminUser adminUser = adminUserRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), adminUser.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        Organization organization = organizationRepository.findById(adminUser.getOrganizationId())
                .orElseThrow(() -> new NotFoundException("Organization not found for admin"));

        String token = jwtTokenService.generateToken(adminUser.getEmail(), Map.of(
                "adminId", adminUser.getId(),
                "organizationId", organization.getId()
        ));
        Instant expiresAt = jwtTokenService.extractExpiry(token);

        return new AuthResponse(token, expiresAt, organization.getId(), organization.getOrganizationName());
    }
}

