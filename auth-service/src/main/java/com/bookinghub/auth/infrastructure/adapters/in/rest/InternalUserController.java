package com.bookinghub.auth.infrastructure.adapters.in.rest;

import com.bookinghub.auth.core.usecases.GetUserEmailUseCase;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final GetUserEmailUseCase getUserEmailUseCase;

    @GetMapping("/{id}/email")
    public ResponseEntity<Map<String, String>> getUserEmail(
            @PathVariable("id") UUID id) {
        String email = getUserEmailUseCase.execute(id);
        if (email == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("email", email));
    }
}
