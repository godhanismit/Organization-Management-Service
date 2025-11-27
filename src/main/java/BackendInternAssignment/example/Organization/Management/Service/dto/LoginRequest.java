package BackendInternAssignment.example.Organization.Management.Service.dto;

import BackendInternAssignment.example.Organization.Management.Service.util.ValidationUtils;

public class LoginRequest {

    private String email;
    private String password;

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
        ValidationUtils.requireEmail(email, "email");
        ValidationUtils.requireNonBlank(password, "password");
    }
}

