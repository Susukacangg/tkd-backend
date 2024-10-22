package com.tkd.dictionaryservice.service;


import com.tkd.models.DictionaryItem;
import com.tkd.models.WordRequest;

import java.math.BigDecimal;

public interface DictionaryService {
    BigDecimal addNewWord(WordRequest newWord, String tokenCookieString);

    DictionaryItem findWord(BigDecimal wordId);
}
