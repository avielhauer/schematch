package de.uni_marburg.schematch.matchtask.matchstep;

import de.uni_marburg.schematch.matchtask.MatchTask;

public interface MatchStep {
    /**
     * Performs a match step and stores the resulting similarity matrices in the {@code matchTask}'s table pairs
     * @param matchTask The {@link MatchTask} to process
     */
    public void run(MatchTask matchTask);

    /**
     * Saves this match step's results to disk
     * @param matchTask The {@link MatchTask} to process
     */
    public void save(MatchTask matchTask);
    /**
     * Evaluates this match step's results and writes them to disk
     * @param matchTask The {@link MatchTask} to process
     */
    public void evaluate(MatchTask matchTask);
}
