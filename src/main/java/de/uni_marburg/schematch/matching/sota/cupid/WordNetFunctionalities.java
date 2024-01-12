package de.uni_marburg.schematch.matching.sota.cupid;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordNetFunctionalities {

    Dictionary dict;

    public WordNetFunctionalities() throws IOException {
        String path = "src/main/resources/WordNet/dict";
        URL url = new URL("file", null, path);
        this.dict = new Dictionary(url);
        dict.open();
    }

    List<IIndexWord> getAllPossibleWords(String word) {
        List<IIndexWord> result = new ArrayList<>();

        for (POS pos : POS.values()) {
            IIndexWord indexWord = dict.getIndexWord(word, pos);
            if (indexWord != null) {
                result.add(indexWord);
            }
        }

        return result;
    }

    Set<ISynset> getAllSynonymsets(String word) {
        List<IIndexWord> allPossibleWords = getAllPossibleWords(word);
        Set<ISynset> result = new HashSet<>();
        for (IIndexWord indexWord : allPossibleWords) {
            for (IWordID wordId : indexWord.getWordIDs()) {
                result.add(dict.getWord(wordId).getSynset());
            }
        }
        return result;
    }
}
