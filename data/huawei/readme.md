## 实验计划
### 完善数据集（wyc）10.9完成
自动化实现数据集的抽取：
从train、test、valid中获取tableA与tableB中的id与label，形成笛卡尔积形式的标注数据
### 形成DSR规则库：10.10-10.11
test/RuleMining/AttrStatGenerate.java计算block，属性刻画output
pythonProject文件计算数据集的mf，并计算F1 score，形成DSRM问题的输入集合
### 挖掘DSR规则：10.12
DPmining.java文件生成MADSRM，存储为规则库ruleDB.txt
### 利用规则库验证数据集效果：10.13
Magellan（10.9）
DSRM-AutoFE：10.13，实现在线验证
