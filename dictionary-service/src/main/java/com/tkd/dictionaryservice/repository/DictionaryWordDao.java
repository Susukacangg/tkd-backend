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
                iam.username,
                w.word_id as wordid,
                w.word,
                STRING_AGG(DISTINCT t.translation_id || '~' || t.translation, ';') AS translations,
                STRING_AGG(DISTINCT u.usage_example_id || '~' || u.example || '|' || u.example_translation, ';') AS usageexamples
            FROM
                word w
            LEFT JOIN translation t ON w.word_id = t.word_id
            LEFT JOIN usage_example u ON w.word_id = u.word_id
            LEFT JOIN iam_user iam ON w.user_id = iam.id
            WHERE
                w.word_id = COALESCE(:wordId, w.word_id) AND
                w.word = COALESCE(:wordStr, w.word) AND
                w.user_id = COALESCE(:userId, w.user_id)
            GROUP BY
                wordId, w.word, iam.username
            ORDER BY
                 CASE WHEN :isRandom THEN RANDOM() END,
                 w.word
            LIMIT :limit;
            """)
    List<Tuple> findWord(@Param("wordId") Long wordId,
                         @Param("wordStr") String wordStr,
                         @Param("userId") Long userId,
                         @Param("isRandom") boolean isRandom,
                         @Param("limit") int limit);

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
    List<String> findWordContaining(@Param("wordStr") String wordStr);

    Optional<WordEntity> findByWordId(@Param("wordId") Long wordId);
}
