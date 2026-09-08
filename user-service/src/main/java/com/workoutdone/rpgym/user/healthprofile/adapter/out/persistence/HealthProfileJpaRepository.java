package com.workoutdone.rpgym.user.healthprofile.adapter.out.persistence;

import com.workoutdone.rpgym.user.healthprofile.domain.HealthProfile;
import com.workoutdone.rpgym.user.healthprofile.domain.HealthProfileRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HealthProfileJpaRepository extends JpaRepository<HealthProfile, UUID>, HealthProfileRepository {
}
