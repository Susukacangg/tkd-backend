package com.tkd.dictionaryservice.service;

import com.tkd.dictionaryservice.dto.UserAccount;
import com.tkd.dictionaryservice.entity.DictionaryTranslation;
import com.tkd.dictionaryservice.entity.DictionaryUsageExample;
import com.tkd.dictionaryservice.entity.DictionaryWord;
import com.tkd.dictionaryservice.feign.IamFeignService;
import com.tkd.dictionaryservice.repository.DictionaryExampleDao;
import com.tkd.dictionaryservice.repository.DictionaryTranslationDao;
import com.tkd.dictionaryservice.repository.DictionaryWordDao;
import com.tkd.models.Translation;
import com.tkd.models.UsageExample;
import com.tkd.models.Word;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {
    private final IamFeignService iamFeignService;
    private final DictionaryWordDao dictionaryWordDao;
    private final DictionaryTranslationDao dictionaryTranslationDao;
    private final DictionaryExampleDao dictionaryExampleDao;

    @Override
    public String addNewWord(Word word, String tokenCookieString) {
        UserAccount userAccount = iamFeignService.getUserDetails(tokenCookieString);

        DictionaryWord newWord = DictionaryWord.builder()
                .word(word.getWord())
                .userId(userAccount.getId().longValue())
                .build();
        DictionaryWord savedWord = dictionaryWordDao.save(newWord);

        for(Translation translation : word.getTranslations()) {
            DictionaryTranslation newTranslation = DictionaryTranslation.builder()
                    .translation(translation.getTranslation())
                    .wordId(savedWord.getWordId())
                    .build();
            dictionaryTranslationDao.save(newTranslation);
        }

        for(UsageExample usageExample : word.getUsageExamples()) {
            DictionaryUsageExample newExample = DictionaryUsageExample.builder()
                    .example(usageExample.getExample())
                    .exampleTranslation(usageExample.getExampleTranslation())
                    .wordId(savedWord.getWordId())
                    .build();
            dictionaryExampleDao.save(newExample);
        }

        return "Successfully added new word!";
    }
}
