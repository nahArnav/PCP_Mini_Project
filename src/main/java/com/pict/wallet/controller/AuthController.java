package com.pict.wallet.controller;

import com.pict.wallet.dto.RegisterRequest;
import com.pict.wallet.exception.WalletException;
import com.pict.wallet.service.UserService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    private final UserService users;

    public AuthController(UserService users) { this.users = users; }

    @GetMapping("/")
    public String home() { return "redirect:/wallet"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registration", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registration") RegisterRequest registration,
                           BindingResult result, Model model) {
        if (result.hasErrors()) { return "register"; }
        try {
            users.register(registration);
            return "redirect:/login?registered";
        } catch (WalletException exception) {
            model.addAttribute("error", exception.getMessage());
        } catch (DataIntegrityViolationException exception) {
            model.addAttribute("error", "An account with this email already exists");
        }
        return "register";
    }
}
