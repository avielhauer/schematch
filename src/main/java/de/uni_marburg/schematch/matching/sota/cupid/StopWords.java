package de.uni_marburg.schematch.matching.sota.cupid;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * to get the nltk-list of stop words:
 *
 * import nltk as nltk
 * from nltk.corpus import stopwords
 *
 * nltk.download('stopwords')
 *
 * string_set = set(stopwords.words("english"))
 *
 * file_path = "nltk-stopwords.txt"
 * with open(file_path, "w") as file:
 *     for string in string_set:
 *         file.write(f"{string}\n")
 */

public class StopWords {
    private final List<String> stopWords = new ArrayList<>();

    public StopWords() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("src/main/resources/cupid/nltk-stopwords.txt"));
            String line = br.readLine();
            while (line != null) {
                stopWords.add(line);
                line = br.readLine();
            }
        } catch (Exception ignored) {

        }
    }


    boolean isStopWord(String element) {
        return stopWords.contains(element);
    }

}
