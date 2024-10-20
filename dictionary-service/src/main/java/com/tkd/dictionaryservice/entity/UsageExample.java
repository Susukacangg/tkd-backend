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
@Table(name = "dictionary_usage_example")
public class UsageExample {
    @Id
    @GeneratedValue
    private Long usageExampleId;

    @Column(nullable = false)
    private String example;

    @Column(nullable = false)
    private String exampleTranslation;
}
