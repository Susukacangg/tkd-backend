package com.tkd.dictionaryservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contribution_comment")
public class ContributionCommentEntity {
    @Id
    @GeneratedValue
    private Long commentId;

    @Column(nullable = false)
    private Long wordId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String comment;

    @Column(nullable = false)
    private Instant commentDateTime;

    @Column
    private Instant editedDateTime;

    @Column(nullable = false)
    private Boolean isEdited;

    @Column(nullable = false)
    private Boolean isDeleted;
}
