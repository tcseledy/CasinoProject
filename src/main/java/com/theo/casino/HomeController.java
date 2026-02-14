package com.theo.casino;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/deposit")
    public String deposit() {
        return "deposit";
    }

    @GetMapping("/blackjack")
    public String blackjack() {
        return "blackjack";
    }

    @GetMapping("/slots")
    public String slots() {
        return "slots";
    }

    @GetMapping("/roulette")
    public String roulette() {
        return "roulette";
    }
}
