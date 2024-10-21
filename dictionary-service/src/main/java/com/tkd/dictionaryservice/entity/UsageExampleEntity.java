package com.tkd.dictionaryservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usage_example")
public class UsageExampleEntity {
    @Id
    @GeneratedValue
    private Long usageExampleId;

    @Column(nullable = false)
    private String example;

    @Column(nullable = false)
    private String exampleTranslation;

    @Column(nullable = false)
    private Long wordId;
}
