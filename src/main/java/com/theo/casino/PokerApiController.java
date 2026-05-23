package com.theo.casino;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/poker")
public class PokerApiController {

    private final PokerTable table = new PokerTable();

    @GetMapping
    public synchronized Map<String, Object> state(Principal principal) {
        return render(principalName(principal));
    }

    @PostMapping("/join")
    public synchronized Map<String, Object> join(Principal principal) {
        String username = principalName(principal);
        table.players.putIfAbsent(username, new PokerPlayer(username));
        table.message = username + " joined the poker table.";
        return render(username);
    }

    @PostMapping("/leave")
    public synchronized Map<String, Object> leave(Principal principal) {
        String username = principalName(principal);
        table.players.remove(username);
        if (username.equals(table.currentTurn)) {
            advanceTurn();
        }
        table.message = username + " left the poker table.";
        return render(username);
    }

    @PostMapping("/start")
    public synchronized Map<String, Object> start(Principal principal) {
        String username = principalName(principal);
        table.players.putIfAbsent(username, new PokerPlayer(username));

        if (table.players.size() < 2) {
            table.message = "At least two players are needed to start a poker session.";
            return render(username);
        }

        table.deck = newDeck();
        Collections.shuffle(table.deck);
        table.communityCards.clear();
        table.pot = 0;
        table.highestBet = 0;
        table.playersActed.clear();
        table.handInProgress = true;
        table.message = username + " started a new hand.";

        for (PokerPlayer player : table.players.values()) {
            player.hand.clear();
            player.folded = false;
            player.currentBet = 0;
            player.hand.add(draw());
            player.hand.add(draw());
        }

        table.currentTurn = activePlayers().get(0).username;
        return render(username);
    }

    @PostMapping("/check")
    public synchronized Map<String, Object> check(Principal principal) {
        String username = principalName(principal);
        if (!canAct(username)) return render(username);

        PokerPlayer player = table.players.get(username);
        if (player.currentBet < table.highestBet) {
            table.message = "You need to call $" + (table.highestBet - player.currentBet) + " or fold.";
            return render(username);
        }

        table.message = username + " checked.";
        table.playersActed.add(username);
        advanceRound();
        return render(username);
    }

    @PostMapping("/call")
    public synchronized Map<String, Object> call(Principal principal) {
        String username = principalName(principal);
        if (!canAct(username)) return render(username);

        PokerPlayer player = table.players.get(username);
        int callAmount = Math.max(0, table.highestBet - player.currentBet);
        player.currentBet += callAmount;
        table.pot += callAmount;
        table.message = username + " called $" + callAmount + ".";

        table.playersActed.add(username);
        advanceRound();
        return render(username);
    }

    @PostMapping("/raise")
    public synchronized Map<String, Object> raise(@RequestParam(defaultValue = "10") int amount, Principal principal) {
        String username = principalName(principal);
        if (!canAct(username)) return render(username);

        int raiseAmount = Math.max(1, amount);
        PokerPlayer player = table.players.get(username);
        int newBet = table.highestBet + raiseAmount;
        int added = newBet - player.currentBet;

        player.currentBet = newBet;
        table.highestBet = newBet;
        table.pot += added;
        table.message = username + " raised to $" + newBet + ".";

        table.playersActed.clear();
        table.playersActed.add(username);
        advanceTurn();
        return render(username);
    }

    @PostMapping("/fold")
    public synchronized Map<String, Object> fold(Principal principal) {
        String username = principalName(principal);
        if (!canAct(username)) return render(username);

        table.players.get(username).folded = true;
        table.message = username + " folded.";

        List<PokerPlayer> active = activePlayers();
        if (active.size() == 1) {
            finishHand(active.get(0).username + " wins the $" + table.pot + " pot.");
        } else {
            advanceTurn();
        }

        return render(username);
    }

    private void advanceRound() {
        List<PokerPlayer> active = activePlayers();
        boolean betsMatched = active.stream().allMatch(player -> player.currentBet == table.highestBet);
        boolean everyoneActed = active.stream()
                .allMatch(player -> table.playersActed.contains(player.username));

        if (!betsMatched || !everyoneActed) {
            advanceTurn();
            return;
        }

        for (PokerPlayer player : table.players.values()) {
            player.currentBet = 0;
        }
        table.highestBet = 0;
        table.playersActed.clear();

        if (table.communityCards.isEmpty()) {
            table.communityCards.add(draw());
            table.communityCards.add(draw());
            table.communityCards.add(draw());
            table.message += " Flop dealt.";
            table.currentTurn = active.get(0).username;
        } else if (table.communityCards.size() < 5) {
            table.communityCards.add(draw());
            table.message += " Next card dealt.";
            table.currentTurn = active.get(0).username;
        } else {
            finishHand("Showdown! Best hand wins the $" + table.pot + " pot.");
        }
    }

    private void advanceTurn() {
        List<PokerPlayer> active = activePlayers();
        if (active.isEmpty()) {
            table.currentTurn = null;
            return;
        }

        int currentIndex = 0;
        for (int i = 0; i < active.size(); i++) {
            if (active.get(i).username.equals(table.currentTurn)) {
                currentIndex = i;
                break;
            }
        }
        table.currentTurn = active.get((currentIndex + 1) % active.size()).username;
    }

    private void finishHand(String message) {
        table.message = message;
        table.handInProgress = false;
        table.currentTurn = null;
        table.playersActed.clear();
    }

    private boolean canAct(String username) {
        if (!table.handInProgress) {
            table.message = "Start a hand before taking an action.";
            return false;
        }
        if (!username.equals(table.currentTurn)) {
            table.message = "It is " + table.currentTurn + "'s turn.";
            return false;
        }
        return table.players.containsKey(username) && !table.players.get(username).folded;
    }

    private List<PokerPlayer> activePlayers() {
        return table.players.values().stream()
                .filter(player -> !player.folded)
                .toList();
    }

    private Map<String, Object> render(String currentUsername) {
        List<Map<String, Object>> players = table.players.values().stream()
                .map(player -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("username", player.username);
                    row.put("currentBet", player.currentBet);
                    row.put("folded", player.folded);
                    row.put("cards", player.username.equals(currentUsername) ? player.hand : List.of("Hidden", "Hidden"));
                    return row;
                })
                .toList();

        return Map.of(
                "players", players,
                "communityCards", table.communityCards,
                "pot", table.pot,
                "highestBet", table.highestBet,
                "currentTurn", table.currentTurn == null ? "" : table.currentTurn,
                "handInProgress", table.handInProgress,
                "message", table.message
        );
    }

    private String principalName(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("You must be logged in to play poker.");
        }
        return principal.getName();
    }

    private String draw() {
        if (table.deck.isEmpty()) {
            table.deck = newDeck();
            Collections.shuffle(table.deck);
        }
        return table.deck.remove(table.deck.size() - 1);
    }

    private List<String> newDeck() {
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] suits = {"S", "H", "D", "C"};
        List<String> deck = new ArrayList<>();

        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(rank + suit);
            }
        }

        return deck;
    }

    private static class PokerTable {
        Map<String, PokerPlayer> players = new LinkedHashMap<>();
        List<String> deck = new ArrayList<>();
        List<String> communityCards = new ArrayList<>();
        int pot = 0;
        int highestBet = 0;
        Set<String> playersActed = new HashSet<>();
        String currentTurn = null;
        boolean handInProgress = false;
        String message = "Join the table to start a live poker session.";
    }

    private static class PokerPlayer {
        String username;
        List<String> hand = new ArrayList<>();
        int currentBet = 0;
        boolean folded = false;

        PokerPlayer(String username) {
            this.username = username;
        }
    }
}
