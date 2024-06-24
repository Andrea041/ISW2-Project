# Machine Learning for Software Engineering

<p align="left">
  <img src="https://pbs.twimg.com/profile_images/545716709311520769/piLLa1iC_400x400.png" alt="logo" style="width: 80px;"/>
  <img src="https://upload.wikimedia.org/wikipedia/commons/6/67/OpenJPA_Logo.png" alt="logo" style="width: 80px;"/>
  <img src="https://upload.wikimedia.org/wikipedia/commons/8/82/Jira_%28Software%29_logo.svg" alt="logo" style="width: 130px;"/>
  <img src="https://upload.wikimedia.org/wikipedia/commons/e/e0/Git-logo.svg" alt="logo" style="width: 80px;"/>
</p>

Evaluate the effectiveness of feature selection, balancing, and sensitivity techniques in improving the accuracy of predictive models for locating bugs in large open-source application codebases. Specifically, the aim is to answer the question: which feature selection, balancing, or sensitivity techniques increase the accuracy of RandomForest, NaiveBayes, and IBK classifiers in the selected projects?

## Selected Open-Source Projects

- Project A: [BookKeeper, repository URL]
- Project B: [OpenJPA, repository URL]

## Evaluation Techniques
Walk Forward: Used as the evaluation technique for temporal validation of predictive models.

## Labeling Techniques
Proportion (Incremental method): Used as the labeling technique for classifying buggy classes.

## Classifiers
RandomForest
NaiveBayes
IBK

## Variables to Validate Empirically
### Feature Selection:
No selection
Best first

### Balancing:
No balancing
Oversampling
Undersampling
SMOTE (Synthetic Minority Over-sampling Technique)

### Cost Sensitivity:
No cost sensitivity
Sensitive Threshold
Sensitive Learning (CFN = 10 * CFP)

## Study Methodology
### Data Collection:
Extract data related to bugs and code changes from the two selected open-source projects.
Apply the Proportion (any) labeling technique to identify buggy classes.

### Data Preparation:
Divide the data into time series using the Walk Forward technique.
Prepare datasets for each combination of feature selection, balancing, and cost sensitivity techniques.

### Experimentation:
Train and test each classifier (RandomForest, NaiveBayes, IBK) using the combinations of feature selection, balancing, and cost sensitivity techniques.
Repeat the experiment for each temporal division obtained from the Walk Forward technique.

### Evaluation Metrics:
Measure the accuracy, precision, recall, and F-measure of each predictive model.
Analyze the impact of different feature selection, balancing, and cost sensitivity techniques on the accuracy of the classifiers.

### Results Analysis:
Compare the performance of classifiers on each project.
Identify which techniques significantly increase the accuracy of the predictive models.
Evaluate whether the effectiveness of the techniques varies depending on the classifier and the dataset used.
