package com.theo.casino;

import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PokerApiControllerTests {

    @Test
    void checksWaitForEveryPlayerBeforeDealingFlop() {
        PokerApiController controller = startedTwoPlayerTable();

        Map<String, Object> afterFirstCheck = controller.check(player("alice"));
        assertThat(afterFirstCheck.get("currentTurn")).isEqualTo("bob");
        assertThat((List<?>) afterFirstCheck.get("communityCards")).isEmpty();

        Map<String, Object> afterSecondCheck = controller.check(player("bob"));
        assertThat(afterSecondCheck.get("currentTurn")).isEqualTo("alice");
        assertThat((List<?>) afterSecondCheck.get("communityCards")).hasSize(3);
    }

    @Test
    void raiseAndCallCompletesBettingRound() {
        PokerApiController controller = startedTwoPlayerTable();

        Map<String, Object> afterRaise = controller.raise(10, player("alice"));
        assertThat(afterRaise.get("currentTurn")).isEqualTo("bob");
        assertThat(afterRaise.get("pot")).isEqualTo(10);

        Map<String, Object> afterCall = controller.call(player("bob"));
        assertThat(afterCall.get("pot")).isEqualTo(20);
        assertThat((List<?>) afterCall.get("communityCards")).hasSize(3);
    }

    private PokerApiController startedTwoPlayerTable() {
        PokerApiController controller = new PokerApiController();
        controller.join(player("alice"));
        controller.join(player("bob"));
        controller.start(player("alice"));
        return controller;
    }

    private Principal player(String username) {
        return () -> username;
    }
}
