# Schematch

This is a research prototype for automated schema matching, mainly developed by [Alexander Vielhauer](mailto:avielhauer@informatik.uni-marburg.de)
at the Big Data Analytics group of Thorsten Papenbrock at Marburg University.
Data dependencies as well as code for loading and handling them is developed by [Marcian Seeger](mailto:marcian.seeger@uni-marburg.de) (same research group).

**Please do not publish the code or data elsewhere.** This repository is only for teaching at Marburg University and Hasso-Plattner-Institut (Potsdam).

## General Information

If you encounter any problems or have any questions, do not hesitate to write a mail to [Alexander Vielhauer](mailto:avielhauer@informatik.uni-marburg.de).

When you work on a particular step (e.g., a first-line matcher or boosting first-line similarity matrices), it can help to
tweak the configurations to your use case as this will greatly reduce runtime. (see [Configuration](#configuration))

When reading a lot of CSV files, installing a plugin for that in your IDE can be really helpful. (e.g., [CSV Editor in IntelliJ](https://plugins.jetbrains.com/plugin/10037-csv-editor))

## Setup

This project requires Java JDK 17 and Maven >=3.9.2. We suggest developing and running the project in IntelliJ IDEA.
For setup, IntelliJ should guide you to install the correct Java JDK and download the Maven dependencies for you.
In case of problems with Maven dependencies, try to [reload the project](https://www.jetbrains.com/help/idea/delegate-build-and-run-actions-to-maven.html#maven_reimport).

You can run the project out of the box, as it comes with data and default configurations.
For a successful run, you should see this log line at the end of your console:
```
[INFO ] <timestamp> [main] de.uni_marburg.schematch.Main - Ending Schematch
```

## Project Overview

This project focuses on determining correspondences between attribute pairs of two different database schemata.
To do so, it requires two databases represented as a collection of `.csv` (each file represents a table of its database).
We call this problem a (matching) scenario, and Schematch creates a `MatchTask` for every such scenario.

We require ground truth attribute correspondences to evaluate the matching output of Schematch.
Given two example tables, `students(id,name,subject)` and `studierende(sid,full_name)`, our ground truth attribute
correspondences could be `(students.id,studierende.sid)` and `(students.name,studierende.full_name)`.
In Schematch this information is represented as a *ground truth matrix*:

| **1** | **0** |
|-------|-------|
| **0** | **1** |
| **0** | **0** |

What we get as output of Schematch is a *similarity matrix* trying to approximate the ground truth matrix as good as possible, such as:

| **0.9**  | **0.1**  |
|----------|----------|
| **0.03** | **0.94** |
| **0.02** | **0.04** |


At the moment, Schematch performs five steps sequentially to produce and improve these similarity matrices:
1. **Table Pair Generation:** As a preprocessing step, candidate table pairs are generated. Only for these table pairs,
        Schematch produces similarity matrices.
2. **First-Line Matching:** A selection of different *first-line schema matchers* are applied to the candidate table pairs.
        First-line matchers operate on the input data and do not require any other matcher's output. Each matcher outputs a similarity matrix for every table pair.
3. **Similarity Matrix Boosting:** The output of the first-line matchers is improved using metadata (e.g., data dependencies).
4. **Second-Line Matching:** A selection of different *second-line schema matchers* are applied to the candidate table pairs.
        Second-line matchers use the improved output of the previous step. A common second-line matcher is an *ensemble matcher*,
        i.e., a matcher combining different first-line matchers to produce a new similarity matrix.
5. **Similarity Matrix Boosting:** Finally, the output of second-line matchers is improved using metadata.

For each scenario, Schematch creates an instance of `MatchTask`. This instance holds all information
about the current match process, such as the similarity matrices produced by matchers and similarity matrix boosting.

Data is stored in a hierarchical fashion in classes of the `data` package:
1. A `Dataset` consists of one or more instances of `Scenario`
2. A `Scenario` consists of two instances of `Database` (a source and a target database)
3. A `Database` consists of one or more instances of `Table`
4. A `Table` consists of one or more instances of `Column`

Initially, those data objects only hold what they read from the input files (e.g., table names, column names, schema instance data).
Whenever a matcher requires additional (meta)data, that information is added to the data objects on demand and cached for
later usage by other matchers. Examples are column data types, column value tokens, and multi-column data dependencies.

## Code Overview
```
/src/main/java/de.uni_marburg.schematch/
|- boosting: package for similarity matrix boosting
|- data: package for holding input data and additional information required by matchers
|------/metadata: package for metadata, especially data dependencies
|- evaluation: package for evaluating matchers and similarity matrix boosting
|- matching/
|----------/Matcher: common interface for first- and second-line matchers
|----------/TokenizedMatcher: first-line matchers that require tokenized values
|----------/MatcherFactory: produces only first-line matchers for now
|----------/ensemble: package for second-line ensemble matchers
|----------/metadata: package for first-line metadata matchers
|----------/similarity: package for string- and set-based similarity first-line matchers
|----------/sota: package for state-of-the-art matcher implementations
|- matchtask: package for match steps and processing a matching scenario
|- preprocessing/
|---------------/profiling: package for producing metadata and adding it to data objects
|---------------/tokenization: package for producing tokenized values and adding them to column objects
|- similarity: package for similarity methods, mainly used by similarity matchers
|- utils: package for utility methods

/src/main/resources/
|- datasets.yaml: configuration file for datasets
|- general.yaml: general configuration file
|- log4j2.yaml: configuration file for logger (log4j)
|- first_line_matchers.yaml: configuration file for first-line matchers
|- first_line_tokenizers.yaml: configuration file for tokenizers for first-line matchers
```
Note that second-line matchers and similarity matrix boosting cannot be configured via `.yaml` files yet.
Please see at the beginning of `Main.main()`, there is a block like this specifying those steps:

```
List<Matcher> secondLineMatchers = new ArrayList<>();
secondLineMatchers.add(new RandomEnsembleMatcher(42));
SimMatrixBoosting firstLineSimMatrixBoosting = new IdentitySimMatrixBoosting();
SimMatrixBoosting secondLineSimMatrixBoosting = new IdentitySimMatrixBoosting();
```

## Data Overview

Data can be found in the `data` directory. This directory is specified as `defaultDatasetBasePath` in `src/main/resources/general.yaml`.
For each dataset there is a subdirectory holding all scenarios for that dataset. Within a scenario (e.g., `data/Efes/fdb1-mb2`)
there are four directories: 
- `ground_truth` holding a ground truth matrix for every `<sourceTable>___<targetTable>.csv` table pair.
    The value at position `(i,j)` in a ground truth matrix indicates whether the `i-th` source column matches the `j-th` target column.
- `source` representing the source database, i.e., holding a list of source tables.
- `target` representing the target database, i.e., holding a list of target tables.
- `metadata/source-to-target-inds.txt`: Contains inclusion dependencies where the source attributes are the subsets of the target attributes
- `metadata/target-to-source-inds.txt`: Contains inclusion dependencies where the target attributes are the subsets of the target attributes
- `metadata/<source|target>/inds.txt`: Contains inclusion dependencies from either the source or target.
  With the format: "[<table-name>.csv.<attribute-name1>,..] --> [<table-name>.csv.<attribute-name2>,..]", (attribute-name2,..) ⊆ (attribute-name1,..)
- `metadata/<source|target>/<table-name>/FD_results.txt`: Contains functional dependencies from within <table-name>
  With the format: "[<table-name>.csv.<attribute-name1>,..] --> <table-name>.csv.<attribute-name2>,..", (attribute-name1,..) -> attribute-name2
- `metadata/<source|target>/<table-name>/UCC_results.txt`: Contains unique column combinations from within <table-name>
  With the format: "[<table-name>.csv.<attribute-name>,..]"

### Efes Datasets

These two datasets are introduced in the paper *Estimating Data Integration and Cleaning Effort* by Kruse et al. (2015, [Download](https://hpi.de/naumann/projects/repeatability/data-integration/estimating-data-integration-and-cleaning-effort.html)).

`Efes-bib` consists of four schemata for bibliographic data from [the amalgam dataset](http://dblab.cs.toronto.edu/~miller/amalgam/) which are assembled to three matching scenarios.

`Efes-music` consists of three schemata for music data which are assembled to three matching scenarios. The three schemata are:
- `fdb`: [FreeDB](https://gnudb.org/)
- `mb`: [MusicBrainz](https://musicbrainz.org/)
- `dis`: [Discogs](https://www.discogs.com/)

### Pubs Dataset

The Pubs dataset is a popular SQL sample dataset which mocks a book publishing company. You can access the dataset [here](https://www.codeproject.com/Articles/20987/HowTo-Install-the-Northwind-and-Pubs-Sample-Databa) and explore its structure through this [ER-Diagram](https://relational.fit.cvut.cz/assets/img/datasets-generated/pubs.svg).

The Pubs dataset offers five different variations, each originating from the original dataset as the source:

- pubs1: `Original Dataset (Unchanged)`
- pubs2: `Original Dataset with Modified Headers`
- pubs3: `Original Dataset Joined via Inner Join`
- pubs4: `Original Dataset Joined via Full Outer Join`
- pubs5: `Original Dataset with Encoding Changes`

### Test Dataset

This dataset is mainly used by the test cases. Editing existing test scenarios will break the tests, add new scenarios if needed.

## Results Overview

After running Schematch, it generates a new `logs/<timestamp>.log` log file and a `results/<timestamp>/` directory.

### Similarity Matrices

In the `results/<timestamp>/<dataset>/<scenario>/<match-step>/outputs/<matcher>/` directory, you can find the similarity matrices produced by `<matcher>`
during `<match-step>` for `<scenario>`.

For example, `FirstLineMatchingStep/outputs/RandomMatcher(seed=42)/discs___annotation.csv` holds the similarity matrix that `RandomMatcher` produced
with the configuration `(seed=42)` during the first-line matching step for matching source table `discs` with target table `annotation`.

### Performance Results

Performance results from evaluating the matchers' similarity matrices against ground truth will appear in
different granularity on different levels in the results hierarchy (`results/<timestamp>/`):
```
(1) ./overall_performance.csv
(2) ./<dataset>/<dataset>_performance_summary.csv
(3) ./<dataset>/<scenario>/<scenario>_performance_summary.csv
(4) ./<dataset>/<scenario>/<matchstep>/performances/<matchstep>_performance_summary.csv
(5) ./<dataset>/<scenario>/<matchstep>/performances/<matchstep>_performance_overview.csv
```

The performance overview (5) holds details about the performances of every matcher for every table pair
executed in the respective matching step and scenario. The performance summaries (1-4) show the best matcher for each
matching step: for every table pair (4), as average over all table pairs in a given scenario (3),
as average over all scenarios in a given dataset (2), as average over all datasets (1).

### Performance Metric

At the moment, Schematch uses *non-binary precision (at ground truth)* to evaluate similarity matrices against
ground truth matrices. Given a similarity matrix, it creates a ranked list of attribute correspondence candidates
(i.e., index pairs sorted by their similarity score) and subsequently checks the list until it finds all ground truth
correspondences. It collects the sum of similarity scores for true and false positives and then calculates non-binary
precision as: `sumScoreTP/(sumScoreTP+sumScoreFP)`. Note that the usual precision is calculated by `numTP/(numTP+numFP)`.

Non-binary precision is upper bounded by normal precision and lower bounded by 0, i.e. it ranges from 0 to 1.
The advantage of using this metric is that it accounts for high gaps between similarity scores. See these two examples:

```
Ground Truth:
1 0
0 1

Similarity Matrix 1:
0.1 0.6
0.8 0.2
Precision=2/4=0.5, Non-binary Precision=(0.1+0.2)/(0.1+0.2+0.6+0.8)=0.3/1.7=0.176

Similarity Matrix 2:
0.5 0.6
0.8 0.7
Precision=2/4=0.5, Non-binary Precision=(0.5+0.7)/(0.5+0.7+0.6+0.8)=1.2/2.6=0.462
```

While precision estimates both similarity matrices to be equally good, non-binary precision is much worse for
similarity matrix 1 (low similarity scores for true positives, high similarity scores for false positives) than for
similarity matrix 2 (all similarity scores about on par).

Saving similarity matrices and performances results can be toggled for every matching step (see [Configuration](#configuration)).

## Configuration

Datasets, first-line matchers, logging, and general configuration happens in `src/main/resources/*.yaml`.
The project uses `snakeyaml` to load the configuration into Java objects. You can query a config singleton
via `Configuration.getInstance()`.  Please see `general.yaml` for a list of general configuration parameters and their documentation.

### Matching Steps
To run only specific steps, you can control their execution via these configuration parameters in `general.yaml`:
```
# Step 2: run first line matchers (i.e., matchers that use table data to match)
saveOutputFirstLineMatchers: True
evaluateFirstLineMatchers: True
# Step 3: run similarity matrix boosting on the output of first line matchers
runSimMatrixBoostingOnFirstLineMatchers: True
saveOutputSimMatrixBoostingOnFirstLineMatchers: True
evaluateSimMatrixBoostingOnFirstLineMatchers: True
# Step 4: run second line matchers (ensemble matchers and other matchers using output of first line matchers)
runSecondLineMatchers: True
saveOutputSecondLineMatchers: True
evaluateSecondLineMatchers: True
# Step 5: run similarity matrix boosting on the output of second line matchers
runSimMatrixBoostingOnSecondLineMatchers: True
saveOutputSimMatrixBoostingOnSecondLineMatchers: True
evaluateSimMatrixBoostingOnSecondLineMatchers: True
```
We recommend to only save outputs when they are actually useful for you, as they result in a lot of files.
If you are, for example only working on first-line matchers, you might want to also turn off evaluation for the other steps.

Note that all other steps depend on the first-line matching step, and the similarity matrix boosting steps depend
on their respective line-matching.

### Datasets

For datasets, first-line matchers and tokenizers, it accepts lists of multiple instances, separated by a line of three dashes (`---`).
For example, you can add a new dataset by modifying `datasets.yaml` like this:

```
---
name: "Efes"
path: "Efes"
---
name: "myDataset"
path: "newData"
```

### Matchers

To configure first-line matchers, see `first_line_matchers.yaml`. You can specify `name`, `packageName`, and `params`.
See for example this configuration:
```
name: "RandomMatcher"
packageName: "sota"
params:
  seed: [42, 2023]
```

`packageName.name` gives us the class path, so the matcher class needs to be `matching.sota.RandomMatcher`.
`params` allows us to specify a single value or a list of values for all the matcher's parameters. In this case
we instantiate the `RandomMatcher` twice: once with `seed=42` and another one with `seed=2023`.

If configuring a `TokenizedMatcher` (e.g., `matching.similarity.tokenizedlabel.DiceLabelMatcher`), we need
to configure a list of tokenizers in `first_line_tokenizers.yaml`. Internally, this is just another parameter for the matcher;
yet, by specifying it in a custom file, we only need to configure the list of tokenizers once for all matchers that use tokens.

## Development

### Adding a new matcher

Any matcher need to extend `matching.Matcher` and implement the method `match(TablePair tablePair)`.
If you add a first-line matcher, you can add it to the matching process by extending the list of
matcher configurations in `src/main/resources/first_line_matchers.yaml` (see [Configuration](#Configuration)).
If you add a second-line matcher, you need to configure and add it to `secondLineMatchers` in `Main.main()`, see these lines for reference:
```
List<Matcher> secondLineMatchers = new ArrayList<>();
secondLineMatchers.add(new RandomEnsembleMatcher(42));
```

### Adding a new similarity matrix boosting

Any similarity matrix boosting needs to implement `boosting.SimMatrixBoosting`. At the moment, to
test your similarity matrix boosting, you need to find and adjust these lines in `Main.main()`:

```
SimMatrixBoosting firstLineSimMatrixBoosting = new IdentitySimMatrixBoosting();
SimMatrixBoosting secondLineSimMatrixBoosting = new IdentitySimMatrixBoosting();
```

### Logging

This project uses log4j for logging. You can add a logger to any class like this (make sure to replace `<Your-Class-Name>`):
```
final static Logger log = LogManager.getLogger(<Your-Class-Name>.class);
```

To log a message (to console and to a log file created in `logs/<timestamp>.log`), write:
```
log.info("This is an important message everyone should see");
log.debug("This is a debug message, only necessary for checking details while debugging");
log.trace("This is a very specific message, only necessary for checking step-by-step");
log.warn("This is a warning message");
log.error("This is an error message");
```
You can change the log level in `src/main/resources/log4j2.yaml` in line 19: `level: INFO`.
Set it to `TRACE` to get all log messages produced by Schematch; to `DEBUG` to get all debug, error, warn, and info logs;
or to `INFO` to only get error, warn, and info logs.

### Work in Progress

1. Deduplicate `save()` and `evaluate()` code for match steps.
2. Read dependencies on demand, making config parameter `readDependencies` obsolete.
3. Make step 3 to 5 configurable via `.yaml` file, at the moment you can only toggle run/save/evaluate in `general.yaml` but specific configuration needs to be done near the beginning of `Main.main()`.
4. Streamline evaluation process, also add option for binary precision.
5. Various `FIXME` and `TODO` in the code.

## Teaching

### UMR | StructureMatch: Schema Matching with Data Profiling (Seminar)
#### Topic 1: State-of-the-Art Matchers
Team members: TODO
#### Topic 2: Metadata Matchers
Team members: TODO
#### Topic 3: Similarity Matrix Boosting (Similarity Flooding)
Team members: TODO
#### Topic 4: Similarity Matrix Boosting (Other)
Team members: TODO
#### Topic 5: Ensemble Matchers
Team members: TODO

### HPI | COSMA: Constraint-based Schema Matching (Master Project)

TBA
