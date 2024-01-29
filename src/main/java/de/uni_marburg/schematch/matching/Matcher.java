package de.uni_marburg.schematch.matching;

import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import de.uni_marburg.schematch.matchtask.matchstep.MatchingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.Configuration;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@NoArgsConstructor
public abstract class Matcher {
    // this is required to make logs from abstract methods show the concrete class
    protected Logger getLogger() {
        return LogManager.getLogger(this.getClass());
    }

    /**
     * @param matchTask MatchTask to match
     * @param matchStep Current MatchStep (MatchingStep)
     * @return Similarity matrix for the given match task. Position (i,j) represents the similarity score for
     * the column pair (i-th source column, j-th target column)
     */
    public abstract float[][] match(MatchTask matchTask, MatchingStep matchStep);

    /**
     * Sets all matcher fields according to the configuration (see first_line_matchers.yaml)
     * @param matcherConfiguration Configuration for this matcher
     */
    public void configure(Configuration.MatcherConfiguration matcherConfiguration) {
        Map<String, Object> params = matcherConfiguration.getParams();
        for (String key : params.keySet()) {
            Object value = params.get(key);
            Method setter;
            Class<?> fieldType;
            try {
                fieldType = this.getClass().getDeclaredField(key).getType();
            } catch (NoSuchFieldException e) {
                getLogger().error("Configuration error: Could not find field " + key + " for " + this);
                throw new RuntimeException(e);
            }
            try {
                String setterName = "set" + key.substring(0,1).toUpperCase() + key.substring(1);
                setter = this.getClass().getDeclaredMethod(setterName, fieldType);
            } catch (NoSuchMethodException e) {
                getLogger().error("Configuration error: Could not find setter for field " + key + " for " + this);
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
                getLogger().error("Configuration error: Failed to invoke setter for field " + key + " for " + this);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(getClass().getSimpleName());
        result.append("(");
        for (Field field : getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                result.append(field.getName()).append("=").append(field.get(this)).append(": ");
                field.setAccessible(false);
            } catch (IllegalAccessException ignored) {} // Cannot happen, we have set the field to be accessible
        }
        String res = result.toString();
        if (getClass().getDeclaredFields().length > 0) {
            res = res.substring(0, res.length() - 2);
        }
        return res + ")";
    }
}
