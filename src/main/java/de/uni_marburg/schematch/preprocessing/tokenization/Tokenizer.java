package de.uni_marburg.schematch.preprocessing.tokenization;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.utils.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class Tokenizer {
    // this is required to make logs from abstract methods show the concrete class
    private Logger getConcreteLogger() {
        return LogManager.getLogger(this.getClass());
    }

    public abstract Set<String> tokenize(String str);

    public Set<String> tokenize(List<String> values) {
        Set<String> tokens = new HashSet<>();
        for (String value : values) {
            tokens.addAll(tokenize(value));
        }
        return tokens;
    }

    private void tokenize(Column column) {
        column.addLabelTokens(this, tokenize(column.getLabel()));
        column.addValuesTokens(this, tokenize(column.getValues()));
    }

    private void tokenize(Table table) {
        List<Column> columns = table.getColumns();
        for (Column column : columns) {
            tokenize(column);
        }
    }

    public void tokenize(Database database) {
        Map<String, Table> tables = database.getTables();
        for (String tableName : tables.keySet()) {
            tokenize(tables.get(tableName));
        }
    }

    public void configure(Configuration.TokenizerConfiguration tokenizerConfiguration) {
        Map<String, Object> params = tokenizerConfiguration.getParams();
        for (String key : params.keySet()) {
            Object value = params.get(key);
            Method setter;
            Class<?> fieldType;
            try {
                fieldType = this.getClass().getDeclaredField(key).getType();
            } catch (NoSuchFieldException e) {
                getConcreteLogger().error("Configuration error: Could not find field " + key + " for " + this);
                throw new RuntimeException(e);
            }
            try {
                String setterName = "set" + key.substring(0,1).toUpperCase() + key.substring(1);
                setter = this.getClass().getDeclaredMethod(setterName, fieldType);
            } catch (NoSuchMethodException e) {
                getConcreteLogger().error("Configuration error: Could not find setter for field " + key + " for " + this);
                throw new RuntimeException(e);
            }
            try {
                if (value instanceof Integer) {
                    setter.invoke(this, value);
                } else if (value instanceof Double) {
                    setter.invoke(this, ((Double) value).floatValue());
                } else {
                    setter.invoke(this, fieldType.cast(value));
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                getConcreteLogger().error("Configuration error: Failed to invoke setter for field " + key + " for " + this);
                throw new RuntimeException(e);
            }
        }
    }
}
