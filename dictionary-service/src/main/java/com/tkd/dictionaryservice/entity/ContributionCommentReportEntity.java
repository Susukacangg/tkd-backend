package com.tkd.dictionaryservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contribution_comment_report")
public class ContributionCommentReportEntity {
    @Id
    @GeneratedValue
    private Long reportId;

    @Column(nullable = false)
    private Long commentId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String reportType;

    @Column(nullable = false)
    private LocalDateTime reportDateTime;

    @Column(nullable = false)
    private String status;
}
