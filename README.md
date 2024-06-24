# Machine Learning for Software Engineering

<p align="left">
  <img src="https://pbs.twimg.com/profile_images/545716709311520769/piLLa1iC_400x400.png" alt="logo" style="width: 80px;"/>
  <img src="https://upload.wikimedia.org/wikipedia/commons/6/67/OpenJPA_Logo.png" alt="logo" style="width: 80px;"/>
  <img src="https://upload.wikimedia.org/wikipedia/commons/8/82/Jira_%28Software%29_logo.svg" alt="logo" style="width: 130px;"/>
  <img src="https://upload.wikimedia.org/wikipedia/commons/e/e0/Git-logo.svg" alt="logo" style="width: 80px;"/>
</p>

Evaluate the effectiveness of feature selection, balancing, and sensitivity techniques in improving the accuracy of predictive models for locating bugs in large open-source application codebases. Specifically, the aim is to answer the question: which feature selection, balancing, or sensitivity techniques increase the accuracy of RandomForest, NaiveBayes, and IBK classifiers in the selected projects?

## Selected Open-Source Projects

- Project A: [BookKeeper, https://github.com/Andrea041/bookkeeper]
- Project B: [OpenJPA, https://github.com/Andrea041/openjpa]

## Evaluation Techniques
**Walk Forward**: Used as the evaluation technique for temporal validation of predictive models.

## Labeling Techniques
Proportion (Incremental method): Used as the labeling technique for classifying buggy classes.

## Classifiers
- RandomForest
- NaiveBayes
- IBK

## Variables to Validate Empirically
### Feature Selection:
No selection
Best first

### Balancing:
No balancing
SMOTE (Synthetic Minority Over-sampling Technique)

### Cost Sensitivity:
No cost sensitivity
Sensitive Learning (CFN = 10 * CFP)
