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
                wordId, w.word;
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
                wordId, w.word
            ORDER BY RANDOM()
            LIMIT 20;
            """)
    List<Tuple> getRandomWords();

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
            WHERE
                w.word = :wordStr
            GROUP BY
                wordId, w.word;
            """)
    List<Tuple> findWord(@Param("wordStr") String wordStr);

    @Query(nativeQuery = true,
            value = """
            SELECT
                word
            FROM
                word
            WHERE
                word LIKE '%' || :wordStr || '%'
            GROUP BY
                word
            ORDER BY
                word
            LIMIT
                10;
            """)
    List<Tuple> findWordContaining(@Param("wordStr") String wordStr);
}
