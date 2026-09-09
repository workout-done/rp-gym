package com.workoutdone.rpgym.game.ranking.application;

import com.workoutdone.rpgym.game.character.domain.XpClient;
import com.workoutdone.rpgym.game.ranking.FakeRankingStore;
import com.workoutdone.rpgym.game.ranking.domain.CharacterLevelWriter;
import com.workoutdone.rpgym.game.ranking.domain.RankingScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class RankingCommandServiceTest {

    private XpClient xpClient;
    private CharacterLevelWriter characterLevelWriter;
    private FakeRankingStore rankingStore;
    private RankingCommandService sut;

    @BeforeEach
    void setUp(){
        xpClient = mock(XpClient.class);
        characterLevelWriter = mock(CharacterLevelWriter.class);
        rankingStore = new FakeRankingStore();
        sut = new RankingCommandService(xpClient, characterLevelWriter, rankingStore);
    }

    @DisplayName("같은 호출을 반복해도 점수가 같다 - ZADD 멱등성")
    @Test
    void idempotent(){
        UUID userId = UUID.randomUUID();
        given(xpClient.findTotalXp(userId)).willReturn(2450);

        sut.onXpChanged(userId);
        sut.onXpChanged(userId);
        sut.onXpChanged(userId);


        // 2450 XP → level 25 (1 + 2450/100)
        assertThat(rankingStore.findScore(userId))
                .contains(RankingScore.encode(25, 2450));
        assertThat(rankingStore.size()).isEqualTo(1);
        verify(characterLevelWriter, times(3)).upsertLevel(any(), anyInt());
    }

    @DisplayName("XP에서 계산한 레벨로 characters를 upsert한다.")
    @Test
    void upsertCharacterLevel(){
        UUID userId = UUID.randomUUID();
        given(xpClient.findTotalXp(userId)).willReturn(820);

        sut.onXpChanged(userId);

        verify(characterLevelWriter).upsertLevel(userId, 9);    // 1 + 820/100
    }
}
