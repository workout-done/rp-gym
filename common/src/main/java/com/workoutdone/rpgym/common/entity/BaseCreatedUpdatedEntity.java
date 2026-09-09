package com.workoutdone.rpgym.common.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class BaseCreatedUpdatedEntity extends BaseCreatedEntity {

    @LastModifiedDate
    private LocalDateTime updatedAt;
}