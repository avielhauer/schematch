package de.uni_marburg.schematch.matchtask.matchstep;

import de.uni_marburg.schematch.matchtask.MatchTask;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Central class for global matching steps used across the entire matching run.
 * Do not store scenario-specific data here as it is only initialized once for the entire run!
 */
@Data
@RequiredArgsConstructor
public abstract class MatchStep {
    private final boolean doSave;
    private final boolean doEvaluate;

    /**
     * Performs a match step and stores the resulting similarity matrices in the {@code matchTask}'s table pairs
     * @param matchTask The {@link MatchTask} to process
     */
    public abstract void run(MatchTask matchTask);

    /**
     * Saves this match step's results to disk
     * @param matchTask The {@link MatchTask} to process
     */
    public abstract void save(MatchTask matchTask);
    /**
     * Evaluates this match step's results and writes them to disk
     * @param matchTask The {@link MatchTask} to process
     */
    public abstract void evaluate(MatchTask matchTask);

    public String toString() {
        return this.getClass().getSimpleName();
    }
}
