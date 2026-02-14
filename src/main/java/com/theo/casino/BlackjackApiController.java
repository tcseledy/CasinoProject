package com.theo.casino;

import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/api/blackjack")
public class BlackjackApiController {

    private static final String KEY_GAME = "BJ_GAME";

    // ---------- DTO-like state ----------
    static class GameState {
        List<String> deck = new ArrayList<>();
        List<String> player = new ArrayList<>();
        List<String> dealer = new ArrayList<>();
        boolean finished = false;
        String status = "Not started";
        int bet = 0;
    }

    // ---------- Endpoints ----------
    @PostMapping("/start")
    public Map<String, Object> start(@RequestParam int bet, HttpSession session) {
        GameState g = new GameState();
        g.bet = bet;

        g.deck = newDeck();
        shuffle(g.deck);

        // Deal: player, dealer, player, dealer
        g.player.add(draw(g));
        g.dealer.add(draw(g));
        g.player.add(draw(g));
        g.dealer.add(draw(g));

        int pTotal = handValue(g.player);
        if (pTotal == 21) {
            g.finished = true;
            g.status = "Blackjack! Player wins";
        } else {
            g.status = "In progress";
        }

        session.setAttribute(KEY_GAME, g);
        return render(g, true);
    }

    @PostMapping("/hit")
    public Map<String, Object> hit(HttpSession session) {
        GameState g = getGame(session);

        if (g.finished) return render(g, true);

        g.player.add(draw(g));
        int pTotal = handValue(g.player);

        if (pTotal > 21) {
            g.finished = true;
            g.status = "Player busts";
        } else {
            g.status = "In progress";
        }

        session.setAttribute(KEY_GAME, g);
        return render(g, true);
    }

    @PostMapping("/stand")
    public Map<String, Object> stand(HttpSession session) {
        GameState g = getGame(session);

        if (g.finished) return render(g, true);

        // Dealer draws until 17+
        while (handValue(g.dealer) < 17) {
            g.dealer.add(draw(g));
        }

        int p = handValue(g.player);
        int d = handValue(g.dealer);

        g.finished = true;

        if (d > 21) g.status = "Dealer busts — Player wins";
        else if (p > d) g.status = "Player wins";
        else if (d > p) g.status = "Dealer wins";
        else g.status = "Push";

        session.setAttribute(KEY_GAME, g);
        return render(g, true);
    }

    // ---------- Helpers ----------
    private GameState getGame(HttpSession session) {
        GameState g = (GameState) session.getAttribute(KEY_GAME);
        if (g == null) {
            // If user hits buttons without starting
            g = new GameState();
            g.status = "Not started";
            session.setAttribute(KEY_GAME, g);
        }
        return g;
    }

    private Map<String, Object> render(GameState g, boolean hideDealerHoleCardIfInProgress) {
        List<String> dealerView = new ArrayList<>(g.dealer);
        int dealerTotalView = handValue(g.dealer);

        // Hide dealer's 2nd card while game is in progress (like real blackjack)
        if (!g.finished && hideDealerHoleCardIfInProgress && dealerView.size() >= 2) {
            dealerView.set(1, "🂠"); // hidden card
            dealerTotalView = cardValue(g.dealer.get(0)); // only show first card's value
        }

        return Map.of(
                "status", g.status,
                "bet", g.bet,
                "playerHand", g.player,
                "dealerHand", dealerView,
                "playerTotal", handValue(g.player),
                "dealerTotal", dealerTotalView,
                "finished", g.finished
        );
    }

    private List<String> newDeck() {
        String[] ranks = {"A","2","3","4","5","6","7","8","9","10","J","Q","K"};
        String[] suits = {"♠","♥","♦","♣"};
        List<String> deck = new ArrayList<>();
        for (String s : suits) {
            for (String r : ranks) {
                deck.add(r + s);
            }
        }
        return deck;
    }

    private void shuffle(List<String> deck) {
        Collections.shuffle(deck);
    }

    private String draw(GameState g) {
        if (g.deck.isEmpty()) {
            g.deck = newDeck();
            shuffle(g.deck);
        }
        return g.deck.remove(g.deck.size() - 1);
    }

    private int handValue(List<String> hand) {
        int total = 0;
        int aces = 0;

        for (String c : hand) {
            String rank = rankOf(c);
            if (rank.equals("A")) {
                aces++;
                total += 11; // start A as 11
            } else if (rank.equals("K") || rank.equals("Q") || rank.equals("J")) {
                total += 10;
            } else {
                total += Integer.parseInt(rank);
            }
        }

        // Convert Aces from 11 to 1 as needed
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }

        return total;
    }

    private int cardValue(String card) {
        String rank = rankOf(card);
        if (rank.equals("A")) return 11;
        if (rank.equals("K") || rank.equals("Q") || rank.equals("J")) return 10;
        return Integer.parseInt(rank);
    }

    private String rankOf(String card) {
        // card is like "10♠" or "A♦"
        // suit is last char, rank is the rest
        return card.substring(0, card.length() - 1);
    }
}