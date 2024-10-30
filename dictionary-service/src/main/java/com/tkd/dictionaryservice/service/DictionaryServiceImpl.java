package com.tkd.dictionaryservice.service;

import com.tkd.dictionaryservice.dto.UserViewDto;
import com.tkd.dictionaryservice.entity.TranslationEntity;
import com.tkd.dictionaryservice.entity.UsageExampleEntity;
import com.tkd.dictionaryservice.entity.WordEntity;
import com.tkd.dictionaryservice.feign.IamFeignService;
import com.tkd.dictionaryservice.repository.DictionaryExampleDao;
import com.tkd.dictionaryservice.repository.DictionaryTranslationDao;
import com.tkd.dictionaryservice.repository.DictionaryWordDao;
import com.tkd.models.DictionaryItem;
import com.tkd.models.TranslationRequest;
import com.tkd.models.UsageExampleRequest;
import com.tkd.models.WordRequest;
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
    public BigDecimal addNewWord(WordRequest word, String tokenCookieString) {
        UserViewDto userViewDto = iamFeignService.getUserDetails(tokenCookieString);

        WordEntity newWord = WordEntity.builder()
                .word(word.getWord())
                .userId(userViewDto.getId().longValue())
                .build();
        WordEntity savedWord = dictionaryWordDao.save(newWord);

        for(TranslationRequest translation : word.getTranslations()) {
            TranslationEntity newTranslation = TranslationEntity.builder()
                    .translation(translation.getTranslation())
                    .wordId(savedWord.getWordId())
                    .build();
            dictionaryTranslationDao.save(newTranslation);
        }

        for(UsageExampleRequest usageExample : word.getUsageExamples()) {
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
    public DictionaryItem getWord(BigDecimal wordId) {
        DictionaryItem dictionaryItem = null;

        Tuple queryResult = dictionaryWordDao.findWordByWordId(wordId.longValue()).orElse(null);
        if (queryResult != null) {
            dictionaryItem = new DictionaryItem();
            dictionaryItem.setUsername(queryResult.get("username").toString());
            dictionaryItem.setWordId(BigDecimal.valueOf((Long) queryResult.get("wordId")));
            dictionaryItem.setWord(queryResult.get("word").toString());
            dictionaryItem.setTranslations(queryResult.get("translations").toString());
            dictionaryItem.setUsageExamples(queryResult.get("usageExamples").toString());
        }

        return dictionaryItem;
    }

    @Override
    public Integer editWord(BigDecimal wordId, WordRequest editedWord) {
        return 0;
    }

    @Override
    public List<DictionaryItem> getRandomWords() {
        List<Tuple> queryResults = dictionaryWordDao.getRandomWords();
        List<DictionaryItem> dictionaryItems = new ArrayList<>();

        if(!queryResults.isEmpty()) {
            dictionaryItems = queryResults.stream().map(
                    tuple -> {
                        DictionaryItem dictionaryItem = new DictionaryItem();
                        dictionaryItem.setUsername(tuple.get("username").toString());
                        dictionaryItem.setWordId(BigDecimal.valueOf((Long) tuple.get("wordId")));
                        dictionaryItem.setWord(tuple.get("word").toString());
                        dictionaryItem.setTranslations(tuple.get("translations").toString());
                        dictionaryItem.setUsageExamples(tuple.get("usageExamples").toString());
                        return dictionaryItem;
                    }
            ).toList();
        }

        return dictionaryItems;
    }

    @Override
    public Page<DictionaryItem> findWord(String word, int pageNum) {
        List<Tuple> queryResults = dictionaryWordDao.findWord(word);

        if(!queryResults.isEmpty()) {
            List<DictionaryItem> dictionaryItems = queryResults.stream().map(
                    tuple -> {
                        DictionaryItem dictionaryItem = new DictionaryItem();
                        dictionaryItem.setUsername(tuple.get("username").toString());
                        dictionaryItem.setWordId(BigDecimal.valueOf((Long) tuple.get("wordId")));
                        dictionaryItem.setWord(tuple.get("word").toString());
                        dictionaryItem.setTranslations(tuple.get("translations").toString());
                        dictionaryItem.setUsageExamples(tuple.get("usageExamples").toString());
                        return dictionaryItem;
                    }
            ).toList();

            Pageable pageable = PageRequest.of(pageNum - 1, PAGE_SIZE);
            int sublistStart = (int) pageable.getOffset();
            int subListEnd = Math.min((sublistStart + PAGE_SIZE), dictionaryItems.size());
            List<DictionaryItem> pagedDictionaryItems = dictionaryItems.subList(sublistStart, subListEnd);

            return new PageImpl<>(pagedDictionaryItems, pageable, dictionaryItems.size());
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
    public Page<DictionaryItem> getAllUserWords(String tokenCookieString, int pageNum) throws FeignException.Forbidden {
        UserViewDto userViewDto = iamFeignService.getUserDetails(tokenCookieString);

        List<Tuple> queryResults = dictionaryWordDao.getAllWordsForUser(userViewDto.getId().longValue());
        List<DictionaryItem> dictionaryItems;
        if(!queryResults.isEmpty()) {
            dictionaryItems = queryResults.stream().map(item -> {
                DictionaryItem dictionaryItem = new DictionaryItem();
                dictionaryItem.setWordId(BigDecimal.valueOf((Long) item.get("wordId")));
                dictionaryItem.setWord(item.get("word").toString());
                dictionaryItem.setTranslations(item.get("translations").toString());
                dictionaryItem.setUsageExamples(item.get("usageExamples").toString());
                return dictionaryItem;
            }).toList();

            Pageable pageable = PageRequest.of(pageNum - 1, PAGE_SIZE);
            int sublistStart = (int) pageable.getOffset();
            int subListEnd = Math.min((sublistStart + PAGE_SIZE), dictionaryItems.size());
            List<DictionaryItem> pagedDictionaryItems = dictionaryItems.subList(sublistStart, subListEnd);

            return new PageImpl<>(pagedDictionaryItems, pageable, dictionaryItems.size());
        }
        return null;
    }
}
