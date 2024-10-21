package com.tkd.dictionaryservice.service;

import com.tkd.dictionaryservice.dto.UserViewDto;
import com.tkd.dictionaryservice.entity.TranslationEntity;
import com.tkd.dictionaryservice.entity.UsageExampleEntity;
import com.tkd.dictionaryservice.entity.WordEntity;
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
        UserViewDto userViewDto = iamFeignService.getUserDetails(tokenCookieString);

        WordEntity newWord = WordEntity.builder()
                .word(word.getWord())
                .userId(userViewDto.getId().longValue())
                .build();
        WordEntity savedWord = dictionaryWordDao.save(newWord);

        for(Translation translation : word.getTranslations()) {
            TranslationEntity newTranslation = TranslationEntity.builder()
                    .translation(translation.getTranslation())
                    .wordId(savedWord.getWordId())
                    .build();
            dictionaryTranslationDao.save(newTranslation);
        }

        for(UsageExample usageExample : word.getUsageExamples()) {
            UsageExampleEntity newExample = UsageExampleEntity.builder()
                    .example(usageExample.getExample())
                    .exampleTranslation(usageExample.getExampleTranslation())
                    .wordId(savedWord.getWordId())
                    .build();
            dictionaryExampleDao.save(newExample);
        }

        return "Successfully added new word!";
    }
}
