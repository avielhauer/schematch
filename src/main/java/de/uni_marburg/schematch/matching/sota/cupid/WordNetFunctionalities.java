package de.uni_marburg.schematch.matching.sota.cupid;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.item.*;
import edu.uniba.di.lacam.kdde.lexical_db.ILexicalDatabase;
import edu.uniba.di.lacam.kdde.lexical_db.MITWordNet;
import edu.uniba.di.lacam.kdde.ws4j.similarity.WuPalmer;
import edu.uniba.di.lacam.kdde.ws4j.util.WS4JConfiguration;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class WordNetFunctionalities {

    Dictionary dict;

    public WordNetFunctionalities() throws IOException {
        String path = "src/main/resources/cupid/dict/";
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

    public static void main(String[] args) throws IOException {
        WS4JConfiguration.getInstance().setMemoryDB(false);
        WS4JConfiguration.getInstance().setMFS(true);
        WordNetFunctionalities wordNetFunctionalities = new WordNetFunctionalities();
        LinguisticMatching linguisticMatching = new LinguisticMatching(wordNetFunctionalities);
        ILexicalDatabase db = new MITWordNet(new RAMDictionary(wordNetFunctionalities.dict, 3));
        WuPalmer wu = new WuPalmer(db);

        System.out.println(wu.calcRelatednessOfWords("dog","cat"));
    }
}
