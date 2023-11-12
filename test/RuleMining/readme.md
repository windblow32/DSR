# Data Preparation for DSRM Problem 
## step1 : run the function AttrStatGenerate in file AttrStatGenerate.java
### parameter to be set:
1. attrLength: set as the attribute num in dataset. Dataset path is at "data/huawei/dataset"
2. dataName: set as the name of dataset in the above path. TO BE CAREFUL, DON'T ADD ".csv"
### attention : check block file in path "data/huawei/block", which is end with 9.26
## after step1, execute step2: run the function generateSS in file AttrStatGenerate.java
## output file is in path "data/huawei/output", which describe the statistics information about entity pairs' attributes

## 实验记录
时间，F1 score