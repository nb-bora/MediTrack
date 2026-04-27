package cm.pharma.contexts.identite_acces.interfaces.rest;

import cm.pharma.contexts.identite_acces.application.command.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints d’authentification (Module B).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest req,
            @RequestHeader(value = "X-Poste", required = false) String poste,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip
    ) {
        var result = authService.login(req.login(), req.password(), poste, ip);
        return new LoginResponse(result.accessToken(), result.refreshToken(), result.doitChangerMdp());
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(
            @Valid @RequestBody RefreshRequest req,
            @RequestHeader(value = "X-Poste", required = false) String poste,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip
    ) {
        var result = authService.refresh(req.refreshToken(), poste, ip);
        return new RefreshResponse(result.accessToken(), result.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @Valid @RequestBody RefreshRequest req,
            @RequestHeader(value = "X-Poste", required = false) String poste,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip
    ) {
        authService.logout(req.refreshToken(), poste, ip);
    }

    @PostMapping("/changer-mot-de-passe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changerMotDePasse(
            @Valid @RequestBody ChangePasswordRequest req,
            JwtAuthenticationToken auth
    ) {
        UUID userId = UUID.fromString(auth.getToken().getSubject());
        authService.changerMotDePasse(userId, req.ancienMotDePasse(), req.nouveauMotDePasse());
    }

    public record LoginRequest(@NotBlank String login, @NotBlank String password) {
    }

    public record LoginResponse(String accessToken, String refreshToken, boolean doitChangerMdp) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record RefreshResponse(String accessToken, String refreshToken) {
    }

    public record ChangePasswordRequest(@NotBlank String ancienMotDePasse, @NotBlank String nouveauMotDePasse) {
    }
}

