package com.tkd.dictionaryservice.utility;

import com.tkd.models.TranslationModel;
import com.tkd.models.UsageExampleModel;
import com.tkd.models.WordModel;
import jakarta.persistence.Tuple;

import java.math.BigDecimal;
import java.util.Arrays;

public class DictionaryServiceUtility {
    public static final String TOKEN_COOKIE_KEY = "token";

    public static WordModel tupleToWordModel(Tuple tuple) {
        WordModel wordModel = new WordModel();
        wordModel.setUsername(tuple.get("username").toString());
        wordModel.setWordId(BigDecimal.valueOf((Long) tuple.get("wordId")));
        wordModel.setWord(tuple.get("word").toString());
        wordModel.setTranslations(
                Arrays.stream(
                        tuple.get("translations").toString().split(";")
                ).map(value -> {
                    // split the information from the result of the query
                    String stringId = value.split("~")[0];
                    String translationString = value.split("~")[1];
                    BigDecimal translationId = BigDecimal.valueOf(Long.parseLong(stringId));

                    // return the actual object
                    TranslationModel translation = new TranslationModel();
                    translation.setTranslationId(translationId);
                    translation.setTranslation(translationString);
                    return translation;
                }).toList()
        );
        wordModel.setUsageExamples(
                Arrays.stream(
                        tuple.get("usageExamples").toString().split(";")
                ).map(value -> {
                    String[] usageExampleString = value.split("~");
                    String stringId = usageExampleString[0];

                    String[] examples = usageExampleString[1].split("\\|");
                    String kadazanExample = examples[0];
                    String translatedExample = examples[1];
                    BigDecimal exampleId = BigDecimal.valueOf(Long.parseLong(stringId));

                    UsageExampleModel usageExample = new UsageExampleModel();
                    usageExample.setExampleId(exampleId);
                    usageExample.setExample(kadazanExample);
                    usageExample.setExampleTranslation(translatedExample);
                    return usageExample;
                }).toList()
        );

        return wordModel;
    }
}
