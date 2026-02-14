package com.theo.casino;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletApiController {

    private static final String KEY = "BANKROLL";

    private int getBankroll(HttpSession session) {
    Integer money = (Integer) session.getAttribute(KEY);
    if (money == null) {
        money = 0;  // start with nothing
        session.setAttribute(KEY, money);
    }
    return money;
}

    @GetMapping
    public Map<String, Object> get(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        response.put("bankroll", getBankroll(session));
        return response;
    }

    @PostMapping("/deposit")
    public Map<String, Object> deposit(@RequestParam int amount, HttpSession session) {
        if (amount <= 0) amount = 0;

        int updated = getBankroll(session) + amount;
        session.setAttribute(KEY, updated);

        Map<String, Object> response = new HashMap<>();
        response.put("bankroll", updated);
        return response;
    }

    @PostMapping("/withdraw")
    public Map<String, Object> withdraw(@RequestParam int amount, HttpSession session) {
        int current = getBankroll(session);
        if (amount <= 0) amount = 0;

        int updated = Math.max(0, current - amount);
        session.setAttribute(KEY, updated);

        Map<String, Object> response = new HashMap<>();
        response.put("bankroll", updated);
        return response;
    }
}

