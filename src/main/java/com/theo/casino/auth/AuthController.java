package com.theo.casino.auth;

import com.theo.casino.AppUser;
import com.theo.casino.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

  private final UserRepository users;
  private final PasswordEncoder encoder;

  public AuthController(UserRepository users, PasswordEncoder encoder) {
    this.users = users;
    this.encoder = encoder;
  }

  @GetMapping("/signup")
  public String signupPage() {
    return "signup";
  }

  @GetMapping("/sign-up")
  public String signUpAlias() {
    return "redirect:/signup";
  }

  @PostMapping("/signup")
  public String signup(@RequestParam String username,
                       @RequestParam String password,
                       @RequestParam String confirmPassword,
                       HttpServletRequest request,
                       Model model) {

    String u = (username == null) ? "" : username.trim();

    if (u.isBlank() || password == null || password.length() < 6) {
      model.addAttribute("error", "Username required and password must be at least 6 chars.");
      return "signup";
    }

    if (!password.equals(confirmPassword)) {
      model.addAttribute("error", "Passwords do not match.");
      return "signup";
    }

    if (users.existsByUsername(u)) {
      model.addAttribute("error", "Username already taken.");
      return "signup";
    }

    users.save(new AppUser(u, encoder.encode(password)));
    try {
      request.login(u, password);
    } catch (Exception ignored) {
      return "redirect:/log-in?signupSuccess=true";
    }

    return "redirect:/";
  }

  @GetMapping("/log-in")
  public String loginPage() {
    return "log-in";
  }
}
