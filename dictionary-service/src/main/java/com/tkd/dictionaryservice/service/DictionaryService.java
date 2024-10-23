package com.tkd.dictionaryservice.service;


import com.tkd.models.DictionaryItem;
import com.tkd.models.WordRequest;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public interface DictionaryService {
    BigDecimal addNewWord(WordRequest newWord, String tokenCookieString);

    DictionaryItem getWord(BigDecimal wordId);

    Page<DictionaryItem> getRandomWords(int pageNumber);
}
