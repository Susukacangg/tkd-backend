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
@Table(name = "word")
public class WordEntity {
    @Id
    @GeneratedValue
    private Long wordId;

    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private Long userId;
}
