package BackendInternAssignment.example.Organization.Management.Service.controller;

import BackendInternAssignment.example.Organization.Management.Service.dto.ApiResponse;
import BackendInternAssignment.example.Organization.Management.Service.dto.CreateOrganizationRequest;
import BackendInternAssignment.example.Organization.Management.Service.dto.GetOrganizationRequest;
import BackendInternAssignment.example.Organization.Management.Service.dto.DeleteOrganizationRequest;
import BackendInternAssignment.example.Organization.Management.Service.dto.OrganizationResponse;
import BackendInternAssignment.example.Organization.Management.Service.dto.UpdateOrganizationRequest;
import BackendInternAssignment.example.Organization.Management.Service.exception.UnauthorizedException;
import BackendInternAssignment.example.Organization.Management.Service.security.AdminUserDetails;
import BackendInternAssignment.example.Organization.Management.Service.service.OrganizationService;
import BackendInternAssignment.example.Organization.Management.Service.util.ValidationUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/org")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping("/create")
    public ResponseEntity<OrganizationResponse> createOrganization(
            @RequestBody CreateOrganizationRequest request) {
        request.validate();
        return ResponseEntity.ok(organizationService.createOrganization(request));
    }

    @GetMapping("/get")
    public ResponseEntity<OrganizationResponse> getOrganization(
            @RequestBody GetOrganizationRequest request) {

        ValidationUtils.requireNonBlank(request.getOrganizationName(), "organizationName");
        return ResponseEntity.ok(
                organizationService.getOrganizationByName(request.getOrganizationName()));
    }

    @PutMapping("/update")
    public ResponseEntity<OrganizationResponse> updateOrganization(
            @RequestBody UpdateOrganizationRequest request,
            Authentication authentication) {
        request.validate();
        AdminUserDetails adminUserDetails = requirePrincipal(authentication);
        return ResponseEntity.ok(organizationService.updateOrganization(request, adminUserDetails));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteOrganization(
            @RequestBody DeleteOrganizationRequest request,
            Authentication authentication) {
        request.validate();
        AdminUserDetails adminUserDetails = requirePrincipal(authentication);
        organizationService.deleteOrganization(request, adminUserDetails);
        return ResponseEntity.ok(new ApiResponse(true, "Organization deleted successfully"));
    }

    private AdminUserDetails requirePrincipal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AdminUserDetails details)) {
            throw new UnauthorizedException("Invalid authentication context");
        }
        return details;
    }
}

