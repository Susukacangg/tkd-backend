package com.tkd.dictionaryservice.service;

import com.tkd.dictionaryservice.dto.ContributionCommentDto;
import com.tkd.dictionaryservice.dto.ContributionCommentReportDao;
import com.tkd.dictionaryservice.dto.UserViewDto;
import com.tkd.dictionaryservice.entity.*;
import com.tkd.dictionaryservice.feign.IamFeignService;
import com.tkd.dictionaryservice.repository.*;
import com.tkd.dictionaryservice.utility.DictionaryServiceUtility;
import com.tkd.models.*;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {
    private final IamFeignService iamFeignService;
    private final DictionaryWordDao dictionaryWordDao;
    private final DictionaryTranslationDao dictionaryTranslationDao;
    private final DictionaryExampleDao dictionaryExampleDao;
    private final ContributionReportDao contributionReportDao;
    private final ContributionCommentDao contributionCommentDao;
    private final ContributionCommentReportDao contributionCommentReportDao;

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
            wordModel = DictionaryServiceUtility.tupleToWordModel(queryResult);

        return wordModel;
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
            wordModels = queryResults.stream().map(DictionaryServiceUtility::tupleToWordModel).toList();

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
            List<WordModel> wordModels = queryResults.stream().map(DictionaryServiceUtility::tupleToWordModel).toList();

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
            wordModels = queryResults.stream().map(DictionaryServiceUtility::tupleToWordModel).toList();

            int sublistStart = (int) pageable.getOffset();
            int subListEnd = Math.min((sublistStart + PAGE_SIZE), wordModels.size());
            List<WordModel> pagedWordModels = wordModels.subList(sublistStart, subListEnd);

            return new PageImpl<>(pagedWordModels, pageable, wordModels.size());
        }
        return null;
    }

    @Override
    public Integer editWord(WordModel editedWord) {
        dictionaryWordDao.findByWordId(editedWord.getWordId().longValue()).ifPresent(wordEntity -> {
            // update word
            wordEntity.setWord(editedWord.getWord());
            dictionaryWordDao.save(wordEntity);

            // TRANSLATIONS
            // find the existing translations for the current word
            List<TranslationEntity> existingTranslations = dictionaryTranslationDao.findByWordId(editedWord.getWordId().longValue());
            // getting the current words' translations' IDs that have been edited
            // don't count the translations with no IDs; these are new translations added
            List<BigDecimal> editedTranslationIds = editedWord.getTranslations().stream()
                    .map(TranslationModel::getTranslationId)
                    .filter(Objects::nonNull)
                    .toList();
            // filter out the existing translations that have been removed from the edit request
            existingTranslations.stream()
                    .filter(translationEntity ->
                                    !editedTranslationIds.contains(BigDecimal.valueOf(translationEntity.getTranslationId())))
                    .forEach(dictionaryTranslationDao::delete);
            // edit the translations that got edited by user, and add new ones if dun have ID
            editedWord.getTranslations().forEach(translation -> {
                if(translation.getTranslationId() != null) {
                    dictionaryTranslationDao.findByTranslationId(translation.getTranslationId().longValue()).ifPresent(
                            translationEntity -> {
                                translationEntity.setTranslation(translation.getTranslation());
                                dictionaryTranslationDao.save(translationEntity);
                            });
                } else {
                    TranslationEntity translationEntity = TranslationEntity.builder()
                            .translation(translation.getTranslation())
                            .wordId(editedWord.getWordId().longValue())
                            .build();
                    dictionaryTranslationDao.save(translationEntity);
                }
            });

            // USAGE EXAMPLES
            List<UsageExampleEntity> existingExamples = dictionaryExampleDao.findByWordId(editedWord.getWordId().longValue());

            List<BigDecimal> editedExampleIds = editedWord.getUsageExamples().stream()
                    .map(UsageExampleModel::getExampleId)
                    .filter(Objects::nonNull)
                    .toList();

            existingExamples.stream()
                    .filter(exampleEntity ->
                            !editedExampleIds.contains(BigDecimal.valueOf(exampleEntity.getUsageExampleId())))
                    .forEach(dictionaryExampleDao::delete);

            editedWord.getUsageExamples().forEach(example -> {
                if(example.getExampleId() != null) {
                    dictionaryExampleDao.findByUsageExampleId(example.getExampleId().longValue()).ifPresent(exampleEntity -> {
                        exampleEntity.setExample(example.getExample());
                        exampleEntity.setExampleTranslation(example.getExampleTranslation());
                        dictionaryExampleDao.save(exampleEntity);
                    });
                } else {
                    UsageExampleEntity usageExampleEntity = UsageExampleEntity.builder()
                            .example(example.getExample())
                            .exampleTranslation(example.getExampleTranslation())
                            .wordId(editedWord.getWordId().longValue())
                            .build();
                    dictionaryExampleDao.save(usageExampleEntity);
                }
            });
        });

        return editedWord.getWordId().intValue();
    }

    @Override
    public Boolean deleteWord(BigDecimal wordId) {
        boolean isDeleteSuccess;

        try {
            dictionaryWordDao.findByWordId(wordId.longValue()).ifPresent(dictionaryWordDao::delete);

            List<TranslationEntity> translationEntities = dictionaryTranslationDao.findByWordId(wordId.longValue());
            if (!translationEntities.isEmpty())
                dictionaryTranslationDao.deleteAll(translationEntities);

            List<UsageExampleEntity> usageExampleEntities = dictionaryExampleDao.findByWordId(wordId.longValue());
            if (!usageExampleEntities.isEmpty())
                dictionaryExampleDao.deleteAll(usageExampleEntities);

            List<ContributionReportEntity> reports = contributionReportDao.findReportEntitiesByWordId(wordId.longValue());
            log.info(reports.toString());
            if (!reports.isEmpty())
                contributionReportDao.deleteAll(reports);

            isDeleteSuccess = true;
        } catch (Exception e) {
            isDeleteSuccess = false;
        }

        return isDeleteSuccess;
    }

    @Override
    public void reportContribution(ReportRequest reportRequest, String tokenCookieString) {
        UserViewDto userViewDto = iamFeignService.getUserDetails(tokenCookieString);

        ContributionReportEntity reportEntity = ContributionReportEntity.builder()
                .wordId(reportRequest.getWordId().longValue())
                .userId(userViewDto.getId().longValue())
                .reportType(reportRequest.getReportType())
                .reportDescription(reportRequest.getReportDescription())
                .reportDate(LocalDateTime.now())
                .status("PENDING")
                .build();

        contributionReportDao.save(reportEntity);
    }

    @Override
    public String addContributionComment(ContributionCommentRequest commentRequest, String tokenCookieString) {
        UserViewDto userViewDto = iamFeignService.getUserDetails(tokenCookieString);

        ContributionCommentEntity commentEntity = ContributionCommentEntity.builder()
                .wordId(commentRequest.getWordId().longValue())
                .userId(userViewDto.getId().longValue())
                .comment(commentRequest.getComment())
                .commentDateTime(LocalDateTime.now())
                .editedDateTime(null)
                .isEdited(false)
                .isDeleted(false)
                .build();

        contributionCommentDao.save(commentEntity);

        return "Successfully added comment";
    }

    @Override
    public Page<ContributionCommentDto> getContributionComments(Long wordId, int pageNum) {
        Page<ContributionCommentEntity> comments =
                contributionCommentDao
                        .findAllByWordIdOrderByCommentDateTimeDescEditedDateTimeDesc(
                                wordId, PageRequest.of(pageNum - 1, 5)
                        );

        AtomicReference<String> fetchedUsername = new AtomicReference<>();
        return comments.map(comment -> {
            fetchedUsername.set(iamFeignService.getUser(comment.getUserId()).getUsername());
            return new ContributionCommentDto(comment, fetchedUsername.get());
        });
    }

    @Override
    public void reportContributionComment(ReportContributionCommentRequest reportRequest, String tokenCookieString) {
        UserViewDto userViewDto = iamFeignService.getUserDetails(tokenCookieString);

        ContributionCommentReportEntity reportEntity = ContributionCommentReportEntity.builder()
                .commentId(reportRequest.getCommentId().longValue())
                .userId(userViewDto.getId().longValue())
                .reportType(reportRequest.getReportType())
                .reportDateTime(LocalDateTime.now())
                .status("PENDING")
                .build();

        contributionCommentReportDao.save(reportEntity);
    }

    @Override
    public Boolean editContributionComment(ContributionCommentRequest commentRequest) {
        ContributionCommentEntity commentEntity = contributionCommentDao
                .findByCommentIdEquals(commentRequest.getCommentId().longValue()).orElse(null);

        if (commentEntity == null)
            return false;

        commentEntity.setComment(commentRequest.getComment());
        commentEntity.setEditedDateTime(LocalDateTime.now());
        commentEntity.setIsEdited(true);

        contributionCommentDao.save(commentEntity);

        return true;
    }

    @Override
    public Boolean softDeleteContributionComment(Long commentId) {
        ContributionCommentEntity commentEntity = contributionCommentDao
                .findByCommentIdEquals(commentId).orElse(null);

        if (commentEntity == null)
            return false;

        commentEntity.setIsDeleted(true);
        contributionCommentDao.save(commentEntity);

        return true;
    }
}
