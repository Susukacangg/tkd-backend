package com.tkd.dictionaryservice.service;


import com.tkd.models.WordModel;
import feign.FeignException;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface DictionaryService {
    BigDecimal addNewWord(WordModel newWord, String tokenCookieString);

    WordModel getWord(BigDecimal wordId);

    List<WordModel> getRandomWords();

    Page<WordModel> findWord(String word, int pageNum);

    List<String> suggestWord(String searchStr);

    Page<WordModel> getAllUserWords(String tokenCookieString, int pageNum) throws FeignException.Forbidden;

    Integer editWord(WordModel editedWord);
}
