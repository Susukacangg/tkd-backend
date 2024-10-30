package com.tkd.dictionaryservice.service;

import com.tkd.dictionaryservice.dto.UserViewDto;
import com.tkd.dictionaryservice.entity.TranslationEntity;
import com.tkd.dictionaryservice.entity.UsageExampleEntity;
import com.tkd.dictionaryservice.entity.WordEntity;
import com.tkd.dictionaryservice.feign.IamFeignService;
import com.tkd.dictionaryservice.repository.DictionaryExampleDao;
import com.tkd.dictionaryservice.repository.DictionaryTranslationDao;
import com.tkd.dictionaryservice.repository.DictionaryWordDao;
import com.tkd.models.WordModel;
import com.tkd.models.TranslationModel;
import com.tkd.models.UsageExampleModel;
import feign.FeignException;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {
    private final IamFeignService iamFeignService;
    private final DictionaryWordDao dictionaryWordDao;
    private final DictionaryTranslationDao dictionaryTranslationDao;
    private final DictionaryExampleDao dictionaryExampleDao;

    private static final int PAGE_SIZE = 10;

    @Override
    public BigDecimal addNewWord(WordModel word, String tokenCookieString) {
        UserViewDto userViewDto = iamFeignService.getUserDetails(tokenCookieString);

        WordEntity newWord = WordEntity.builder()
                .word(word.getWord())
                .userId(userViewDto.getId().longValue())
                .build();
        WordEntity savedWord = dictionaryWordDao.save(newWord);

        for(TranslationModel translation : word.getTranslations()) {
            TranslationEntity newTranslation = TranslationEntity.builder()
                    .translation(translation.getTranslation())
                    .wordId(savedWord.getWordId())
                    .build();
            dictionaryTranslationDao.save(newTranslation);
        }

        for(UsageExampleModel usageExample : word.getUsageExamples()) {
            UsageExampleEntity newExample = UsageExampleEntity.builder()
                    .example(usageExample.getExample())
                    .exampleTranslation(usageExample.getExampleTranslation())
                    .wordId(savedWord.getWordId())
                    .build();
            dictionaryExampleDao.save(newExample);
        }

        return BigDecimal.valueOf(savedWord.getWordId());
    }

    @Override
    public WordModel getWord(BigDecimal wordId) {
        WordModel wordModel = null;

        Tuple queryResult = dictionaryWordDao
                .findWord(
                    wordId.longValue(),
                    null,
                    null,
                    false,
                    1
                ).stream().findFirst()
                .orElse(null);

        if (queryResult != null)
            wordModel = tupleToWordModel(queryResult);

        return wordModel;
    }

    @Override
    public Integer editWord(BigDecimal wordId, WordModel editedWord) {
        return 0;
    }

    @Override
    public List<WordModel> getRandomWords() {
        List<Tuple> queryResults = dictionaryWordDao
                .findWord(
                        null,
                        null,
                        null,
                        true,
                        20
                );
        List<WordModel> wordModels = new ArrayList<>();

        if(!queryResults.isEmpty())
            wordModels = queryResults.stream().map(this::tupleToWordModel).toList();

        return wordModels;
    }

    @Override
    public Page<WordModel> findWord(String word, int pageNum) {
        Pageable pageable = PageRequest.of(pageNum - 1, PAGE_SIZE);

        List<Tuple> queryResults = dictionaryWordDao
                .findWord(
                        null,
                        word,
                        null,
                        false,
                        Integer.MAX_VALUE
                );

        if(!queryResults.isEmpty()) {
            List<WordModel> wordModels = queryResults.stream().map(this::tupleToWordModel).toList();

            int sublistStart = (int) pageable.getOffset();
            int subListEnd = Math.min((sublistStart + PAGE_SIZE), wordModels.size());
            List<WordModel> pagedWordModels = wordModels.subList(sublistStart, subListEnd);

            return new PageImpl<>(pagedWordModels, pageable, wordModels.size());
        }

        return null;
    }

    @Override
    public List<String> suggestWord(String searchStr) {
        List<String> queryResults = dictionaryWordDao.findWordContaining(searchStr);

        if(!queryResults.isEmpty())
            return queryResults;

        return null;
    }

    @Override
    public Page<WordModel> getAllUserWords(String tokenCookieString, int pageNum) throws FeignException.Forbidden {
        UserViewDto userViewDto = iamFeignService.getUserDetails(tokenCookieString);

        Pageable pageable = PageRequest.of(pageNum - 1, PAGE_SIZE);
        List<Tuple> queryResults = dictionaryWordDao
                .findWord(
                        null,
                        null,
                        userViewDto.getId().longValue(),
                        false,
                        Integer.MAX_VALUE
                );

        List<WordModel> wordModels;
        if(!queryResults.isEmpty()) {
            wordModels = queryResults.stream().map(this::tupleToWordModel).toList();

            int sublistStart = (int) pageable.getOffset();
            int subListEnd = Math.min((sublistStart + PAGE_SIZE), wordModels.size());
            List<WordModel> pagedWordModels = wordModels.subList(sublistStart, subListEnd);

            return new PageImpl<>(pagedWordModels, pageable, wordModels.size());
        }
        return null;
    }

    private WordModel tupleToWordModel(Tuple tuple) {
        WordModel wordModel = new WordModel();
        wordModel.setUsername(tuple.get("username").toString());
        wordModel.setWordId(BigDecimal.valueOf((Long) tuple.get("wordId")));
        wordModel.setWord(tuple.get("word").toString());
        wordModel.setTranslations(
                Arrays.stream(
                        tuple.get("translations").toString().split(";")
                ).map(value -> {
                    // split the information from the result of the query
                    String stringId = value.split("~")[0];
                    String translationString = value.split("~")[1];
                    BigDecimal translationId = BigDecimal.valueOf(Long.parseLong(stringId));

                    // return the actual object
                    TranslationModel translation = new TranslationModel();
                    translation.setTranslationId(translationId);
                    translation.setTranslation(translationString);
                    return translation;
                }).toList()
        );
        wordModel.setUsageExamples(
                Arrays.stream(
                        tuple.get("usageExamples").toString().split(";")
                ).map(value -> {
                    String[] usageExampleString = value.split("~");
                    String stringId = usageExampleString[0];

                    String[] examples = usageExampleString[1].split("\\|");
                    String kadazanExample = examples[0];
                    String translatedExample = examples[1];
                    BigDecimal exampleId = BigDecimal.valueOf(Long.parseLong(stringId));

                    UsageExampleModel usageExample = new UsageExampleModel();
                    usageExample.setExampleId(exampleId);
                    usageExample.setExample(kadazanExample);
                    usageExample.setExampleTranslation(translatedExample);
                    return usageExample;
                }).toList()
        );

        return wordModel;
    }
}
