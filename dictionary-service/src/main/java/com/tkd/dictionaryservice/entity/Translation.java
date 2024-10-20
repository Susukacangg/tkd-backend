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
@Table(name = "dictionary_translation")
public class Translation {
    @Id
    @GeneratedValue
    private Long translationId;

    @Column(nullable = false)
    private String translation;

    @Column(nullable = false)
    private Long wordId;
}
