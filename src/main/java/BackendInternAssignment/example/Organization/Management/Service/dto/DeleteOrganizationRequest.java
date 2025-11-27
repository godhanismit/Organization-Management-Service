package BackendInternAssignment.example.Organization.Management.Service.dto;

import BackendInternAssignment.example.Organization.Management.Service.util.ValidationUtils;

public class DeleteOrganizationRequest {

    private String organizationName;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public void validate() {
        ValidationUtils.requireNonBlank(organizationName, "organizationName");
    }
}

