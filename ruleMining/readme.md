# DSR规则数据准备：相似度函数部分
## 功能说明：
本模块依据子集划分生成的历史经验数据集，结合词向量模型与元学习，生成加权的相似度函数F1 score
## 数据说明：
block中的数据对应了entity matching项目输出的文件，存储在huawei/block中，大家自己复制过来就行
saved_model文件夹存储了glove预训练模型，我给链接大家自己下载后放进去就行
模型链接：https://apache-mxnet.s3.cn-north-1.amazonaws.com.cn/gluon/embeddings/glove/glove.840B.300d.zip
## 代码说明：
输入参数：参考generateY中的三个todo，注释均有详细说明
生成的数据存储于output文件夹中
每个数据集要求记录运行时间！！！我输出在命令行了。需要大家记录运行时间：）
最后写个txt和数据一起打包发群里就可以
