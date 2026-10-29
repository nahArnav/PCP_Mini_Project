package com.pict.wallet.service;

import com.pict.wallet.dto.*;
import com.pict.wallet.exception.WalletException;
import com.pict.wallet.model.WalletUser;
import com.pict.wallet.repository.UserRepository;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder, Validator validator) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public WalletResponse register(RegisterRequest request) {
        request.setEmail(normalizeEmail(request.getEmail()));
        request.setName(request.getName() == null ? "" : request.getName().trim());
        var errors = validator.validate(request);
        if (!errors.isEmpty()) {
            throw new WalletException(HttpStatus.BAD_REQUEST, errors.iterator().next().getMessage());
        }
        // BCrypt has a 72-byte input limit, including multi-byte characters.
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new WalletException(HttpStatus.BAD_REQUEST, "Password must be at most 72 UTF-8 bytes");
        }
        if (users.existsByEmail(request.getEmail())) {
            throw new WalletException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        WalletUser user = new WalletUser(request.getName(), request.getEmail(),
                passwordEncoder.encode(request.getPassword()));
        return new WalletResponse(users.saveAndFlush(user));
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(String email) {
        return new WalletResponse(findUser(email));
    }

    public WalletUser findUser(String email) {
        return users.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new WalletException(HttpStatus.NOT_FOUND, "Account not found"));
    }
}
