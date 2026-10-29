package com.pict.wallet.controller;

import com.pict.wallet.dto.*;
import com.pict.wallet.exception.WalletException;
import com.pict.wallet.model.TransactionStatus;
import com.pict.wallet.service.*;
import jakarta.validation.Valid;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.time.ZoneId;

@Controller
public class WalletController {
    private final UserService users;
    private final WalletService wallets;

    public WalletController(UserService users, WalletService wallets) {
        this.users = users;
        this.wallets = wallets;
    }

    @GetMapping("/wallet")
    public String dashboard(Principal principal, Model model) {
        model.addAttribute("wallet", users.getWallet(principal.getName()));
        model.addAttribute("transactions", wallets.history(principal.getName()));
        model.addAttribute("displayZone", ZoneId.of("Asia/Kolkata"));
        return "wallet";
    }

    @PostMapping("/wallet/add-money")
    public String addMoney(Principal principal, @Valid @ModelAttribute AddMoneyRequest request,
                           BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("error", "Enter an amount from INR 0.01 to INR 100000, using at most two decimals");
            return "redirect:/wallet";
        }
        try {
            showResult(wallets.addMoney(principal.getName(), request.getAmount()), flash);
        } catch (WalletException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
        } catch (PessimisticLockingFailureException exception) {
            flash.addFlashAttribute("error", "Wallet is busy. Check history, then try again");
        }
        return "redirect:/wallet";
    }

    @PostMapping("/wallet/transfer")
    public String transfer(Principal principal, @Valid @ModelAttribute TransferRequest request,
                           BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("error", "Enter a valid recipient email and an amount from INR 0.01 to INR 100000 with at most two decimals");
            return "redirect:/wallet";
        }
        try {
            showResult(wallets.transfer(principal.getName(), request.getRecipientEmail(), request.getAmount()), flash);
        } catch (WalletException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
        } catch (PessimisticLockingFailureException exception) {
            flash.addFlashAttribute("error", "Wallet is busy. Check history, then try again");
        }
        return "redirect:/wallet";
    }

    private void showResult(TransactionResponse transaction, RedirectAttributes flash) {
        String key = transaction.getStatus() == TransactionStatus.SUCCESS ? "success" : "error";
        flash.addFlashAttribute(key, transaction.getMessage());
    }
}
