package BackendInternAssignment.example.Organization.Management.Service.controller;

import BackendInternAssignment.example.Organization.Management.Service.dto.AuthResponse;
import BackendInternAssignment.example.Organization.Management.Service.dto.LoginRequest;
import BackendInternAssignment.example.Organization.Management.Service.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/org/admin")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        request.validate();
        return ResponseEntity.ok(authService.login(request));
    }
}

