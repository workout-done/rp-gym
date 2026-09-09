package com.workoutdone.rpgym.common.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@MappedSuperclass
public abstract class BaseAuditableDeletedEntity
        extends BaseAuditableEntity {

    private LocalDateTime deletedAt;

    private UUID deletedBy;

    public void delete(UUID deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
}