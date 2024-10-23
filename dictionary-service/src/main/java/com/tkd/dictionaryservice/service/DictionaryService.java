package com.tkd.dictionaryservice.service;


import com.tkd.models.DictionaryItem;
import com.tkd.models.WordRequest;

import java.math.BigDecimal;
import java.util.List;

public interface DictionaryService {
    BigDecimal addNewWord(WordRequest newWord, String tokenCookieString);

    DictionaryItem getWord(BigDecimal wordId);

    List<DictionaryItem> getRandomWords();
}
