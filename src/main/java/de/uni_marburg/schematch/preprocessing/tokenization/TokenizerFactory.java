package de.uni_marburg.schematch.preprocessing.tokenization;

import de.uni_marburg.schematch.utils.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class TokenizerFactory {
    private static final Logger log = LogManager.getLogger(TokenizerFactory.class);

    public List<Tokenizer> createTokenizerInstances(String tokenizerName) throws Exception {
        Configuration config = Configuration.getInstance();

        List<Configuration.TokenizerConfiguration> tokenizerConfigurations = config.getTokenizerConfigurations().get(tokenizerName);
        log.info("Instantiating tokenizer " + tokenizerName + " with " + tokenizerConfigurations.size() + " different configurations");

        String name = tokenizerConfigurations.get(0).getName();
        Class<?> tokenizerClass = Class.forName(Configuration.TOKENIZATION_PACKAGE_NAME + "." + name);

        List<Tokenizer> tokenizerInstances = new ArrayList<>();
        for (Configuration.TokenizerConfiguration tokenizerConfiguration : tokenizerConfigurations) {
            Tokenizer tokenizer = (Tokenizer) tokenizerClass.getConstructor().newInstance();
            tokenizer.configure(tokenizerConfiguration);
            tokenizerInstances.add(tokenizer);
        }

        return tokenizerInstances;
    }
}
