package com.workoutdone.rpgym.user.dailyhealthgoal.adapter.out.persistence;

import com.workoutdone.rpgym.user.dailyhealthgoal.domain.DailyHealthGoal;
import com.workoutdone.rpgym.user.dailyhealthgoal.domain.DailyHealthGoalRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DailyHealthGoalJpaRepository extends JpaRepository<DailyHealthGoal, UUID>, DailyHealthGoalRepository {
}
