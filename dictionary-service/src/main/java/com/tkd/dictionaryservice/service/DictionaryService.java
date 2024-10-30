package com.tkd.dictionaryservice.service;


import com.tkd.models.DictionaryItem;
import com.tkd.models.WordRequest;
import feign.FeignException;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface DictionaryService {
    BigDecimal addNewWord(WordRequest newWord, String tokenCookieString);

    DictionaryItem getWord(BigDecimal wordId);

    Integer editWord(BigDecimal wordId, WordRequest editedWord);

    List<DictionaryItem> getRandomWords();

    Page<DictionaryItem> findWord(String word, int pageNum);

    List<String> suggestWord(String searchStr);

    Page<DictionaryItem> getAllUserWords(String tokenCookieString, int pageNum) throws FeignException.Forbidden;
}
