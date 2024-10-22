package com.tkd.dictionaryservice.repository;

import com.tkd.dictionaryservice.entity.WordEntity;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DictionaryWordDao extends JpaRepository<WordEntity, Long> {

    @Query(nativeQuery = true,
            value = """
            SELECT
                w.word_id AS wordId,
                w.word,
                STRING_AGG(DISTINCT t.translation, ';') AS translations,
                STRING_AGG(DISTINCT u.example || '|' || u.example_translation, ';') AS usageexamples
            FROM
                word w
                    LEFT JOIN
                translation t ON w.word_id = t.word_id
                    LEFT JOIN
                usage_example u ON w.word_id = u.word_id
            WHERE w.word_id = :wordId
            GROUP BY
                w.word_id, w.word, w.user_id;
            """)
    Optional<Tuple> findWordByWordId(@Param("wordId") Long wordId);

    @Query(nativeQuery = true,
            value = """
            SELECT
                w.word_id AS wordId,
                w.word,
                STRING_AGG(DISTINCT t.translation, ';') AS translations,
                STRING_AGG(DISTINCT u.example || '|' || u.example_translation, ';') AS usageexamples
            FROM
                word w
                    LEFT JOIN
                translation t ON w.word_id = t.word_id
                    LEFT JOIN
                usage_example u ON w.word_id = u.word_id
            GROUP BY
                w.word_id, w.word, w.user_id
            ORDER BY RANDOM()
            LIMIT 20;
            """)
    List<Tuple> getRandomWords();
}
