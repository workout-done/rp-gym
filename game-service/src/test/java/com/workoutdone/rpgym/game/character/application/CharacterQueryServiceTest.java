package com.workoutdone.rpgym.game.character.application;

import com.workoutdone.rpgym.game.character.domain.XpClient;
import com.workoutdone.rpgym.game.character.domain.CharacterReader;
import com.workoutdone.rpgym.game.character.domain.Character;
import com.workoutdone.rpgym.game.character.domain.CharacterTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CharacterQueryServiceTest {

    @Mock
    private CharacterReader characterReader;


    @Mock
    private XpClient xpClient;

    @InjectMocks
    private CharacterQueryService sut;

    @DisplayName("캐릭터가 있으면 저장된 레벨과 지갑 XP 로 조립한다")
    @Test
    void found() {
        UUID userId = UUID.randomUUID();
        given(characterReader.findByUserId(userId))
                .willReturn(Optional.of(Character.restore(userId, 12)));
        given(xpClient.findTotalXp(userId)).willReturn(2450);

        CharacterView view = sut.getCharacter(userId);

        assertThat(view.level()).isEqualTo(12);
        assertThat(view.tier()).isEqualTo(CharacterTier.SILVER);
        assertThat(view.totalXp()).isEqualTo(2450);
        assertThat(view.currentLevelXp()).isEqualTo(50);
        assertThat(view.progressPercent()).isEqualTo(50.0);
    }

    @DisplayName("캐릭터가 없어도 예외 없이 기본값을 조립한다 — 404 가 아니다")
    @Test
    void notFoundReturnsDefault() {
        UUID userId = UUID.randomUUID();
        given(characterReader.findByUserId(userId)).willReturn(Optional.empty());

        CharacterView view = sut.getCharacter(userId);

        assertThat(view.userId()).isEqualTo(userId);
        assertThat(view.level()).isEqualTo(1);
        assertThat(view.totalXp()).isZero();
        assertThat(view.tier()).isEqualTo(CharacterTier.BRONZE);
        assertThat(view.createdAt()).isNull();
    }

    @DisplayName("캐릭터가 없으면 지갑을 조회하지 않는다")
    @Test
    void skipsXpLookupWhenNoCharacter() {
        UUID userId = UUID.randomUUID();
        given(characterReader.findByUserId(userId)).willReturn(Optional.empty());

        sut.getCharacter(userId);

        verify(xpClient, never()).findTotalXp(userId);
    }
}