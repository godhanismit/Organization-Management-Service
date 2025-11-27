package BackendInternAssignment.example.Organization.Management.Service.dto;

import BackendInternAssignment.example.Organization.Management.Service.util.ValidationUtils;

public class UpdateOrganizationRequest {

    private String organizationName;
    private String email;
    private String password;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void validate() {
        ValidationUtils.requireNonBlank(organizationName, "organizationName");
        ValidationUtils.requireEmail(email, "email");
        ValidationUtils.requireMinLength(password, 8, "password");
    }
}

