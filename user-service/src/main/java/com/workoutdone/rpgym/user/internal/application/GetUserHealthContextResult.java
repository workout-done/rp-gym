package com.workoutdone.rpgym.user.internal.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetUserHealthContextResult {

    private HealthProfileSummary healthProfile;
    private DailyGoalSummary dailyGoal;
    private List<LongTermGoalSummary> longTermGoals;
}
