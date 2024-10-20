package com.tkd.dictionaryservice.service;


import com.tkd.models.Word;

public interface DictionaryService {
    String addNewWord(Word newWord, String tokenCookieString);
}
