package com.theo.casino;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletApiController {

    private final UserRepository users;

    public WalletApiController(UserRepository users) {
        this.users = users;
    }

    @GetMapping
    public Map<String, Object> get(Principal principal) {
        AppUser user = currentUser(principal);

        Map<String, Object> response = new HashMap<>();
        response.put("bankroll", user.getBankroll());
        return response;
    }

    @PostMapping("/deposit")
    public Map<String, Object> deposit(@RequestParam int amount, Principal principal) {
        AppUser user = currentUser(principal);
        if (amount <= 0) amount = 0;

        int updated = user.getBankroll() + amount;
        user.setBankroll(updated);
        users.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("bankroll", updated);
        return response;
    }

    @PostMapping("/withdraw")
    public Map<String, Object> withdraw(@RequestParam int amount, Principal principal) {
        AppUser user = currentUser(principal);
        int current = user.getBankroll();
        if (amount <= 0) amount = 0;

        int updated = Math.max(0, current - amount);
        user.setBankroll(updated);
        users.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("bankroll", updated);
        return response;
    }

    private AppUser currentUser(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("You must be logged in to use the wallet.");
        }

        return users.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user was not found."));
    }
}
