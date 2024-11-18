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
@Table(name = "contribution_report")
public class ContributionReportEntity {
    @Id
    @GeneratedValue
    private Long reportId;

    @Column(nullable = false)
    private Long wordId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String reportType;

    @Column
    private String reportDescription;

    @Column(nullable = false)
    private Instant reportDateTime;

    @Column(nullable = false)
    private String status;
}
