package com.workoutdone.rpgym.common.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;

import java.util.UUID;

@Getter
@MappedSuperclass
public abstract class BaseCreatedByEntity extends BaseCreatedEntity {

    @CreatedBy
    private UUID createdBy;
}