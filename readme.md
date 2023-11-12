# 环境配置说明：

本项目开发于windows10操作系统，24G内存，代码开发并运行于intelligence idea
2020.3版本；代码语言jdk版本1.8.0_251，spark版本为2.4.8环境，maven版本为3.8.4。其中引用的maven开源依赖包参考pom.xml文件

# 各文件功能说明

## huawei代码说明文档

文档内容包括但不局限于代码编程及运行环境、代码各功能模块关系及接口说明、主要数据结构说明等
### 一．	运行环境
本项目开发于windows11操作系统，24G内存，代码开发并运行于intelligence idea 2020.3版本；代码语言jdk版本1.8.0_251，spark版本为2.4.8环境，maven版本为3.8.4。其中引用的maven开源依赖包参考pom.xml文件
### 二．	各模块功能说明及模块间关系
任务一交付代码中主要分为四个模块，涵盖了重复记录检测的自动特征工程全过程：（1）模块一：特征空间构建模块（2）模块二：描述相似度构建模块（3）模块三：描述-相似度规则挖掘模块（4）模块四：基于规则的特征向量计算模块。

 ![Alt text](image.png)

流程说明：

其中，模块一为重复记录检测的特征工程构建了整体的特征空间，涵盖了大量的相似度函数作为特征值计算方法，为后续模块四中基于规则的特征向量计算的自动化提供输入和功能性函数支撑；模块二计算了实体对属性上的多种统计量信息，实现了属性刻画功能，为模块三中描述-相似度函数的挖掘提供了数据输入；模块三设计了单一前件的探针-剪枝描述-相似度规则挖掘算法以及多前件下基于动态规划算法的挖掘算法，提供了有规则支持度阈值约束保证的挖掘算法，为模块四提供了规则集的输入；模块四基于模块一提供的特征空间以及模块三提供的规则集，构建属性刻画与相似度函数映射关系，并根据模块二中计算的数据集属性刻画，为实体对属性推荐相似度函数集合，从而计算实体对相似度作为特征值，实现重复记录检测的自动特征工程。

#### （1）特征空间构建模块
本模块从实体对属性异构型出发，为数字类型、字符串类型、中文、英文、长文本设计了多种相似度函数，用于整体特征工程的特征空间构建。

该模块主函数为src/sim_func/SimilarityFunction.java,内部提供了20余种相似度函数,支持多种类型实体对属性值间的计算,例如编辑距离,jaccard,dice等;并从hash,ascii编码等多种角度对token进行表征,将字符串转换为编码集合,构造了输入为集合类型的相似度函数如diceForSet,cosineForSet等，函数的输入是成对的字符串，或作为分词结果的成对的list， 输出为double类型变量。

该主函数调用了多种功能类：

**WordBag**类：支持长文本分词功能，该类调用了HANLP，提供了四种分词函数，支持倒排索引分词，nlp分词，快速分词等，接口输入为实体对字符串，输出为词袋集合；以NLP分词为例，输入为字符串对，输出为两个字符串对应的词袋集合

其中，words1与words2是两个词袋集合，用于SimilarityFunction中的函数计算，输入为两个词袋列表list，并用于listSimilarity类进行相似度函数计算

**listSimilarity**:包含了SimilarityFunction中cosineUsingWordBag，diceForSet，jaccardForSet,LevenshteinDisForSet,monge_elkanForSet五种计算集合间相似度的功能函数，为基于ascii码，hash编码等token表征进行相似度计算的功能类提供支撑；

**HashUtils**类：存储了多种用于将token转换为hash编码的hash函数；为hash编码下的相似度函数计算提供功能支撑；

**MinHashSimilarity**类：提供了一种应用多种hash函数计算实体对相似度的函数
calculateSimilarity。该函数输入为两个字符串类型数据，通过调用toSignature函数，输出两个数据对应的hash表征，将hash编码序列输入到listSimilarity函数中，实现实体对属性值相似度的计算；

**Simhash**类：根据字符串的ascii/gbk编码进行解析，并映射为一个BigInteger类型变量；在SimilarityFunction中被simHashDistance调用，最终利用汉明距离（取异或xor）计算输出实体对相似度。

该模块为后续模块四构建属性刻画与相似度函数映射关系时的提供了函数支撑，在基于规则计算实体对特征值时，通过调用模块一中的函数进行计算。该模块的测试用例参考test/Similarity/CalculateSimilarity.java中的calculateSimilarity函数。

**calculateSimilarity**：作为测试文件，用于测试相似度函数的功能，不代表最终的特征向量。在测试文件中可以指定长文本，数值型属性类型进行针对性的测试。
 
#### （2）描述-相似度构建模块
该模块用于构建描述-相似度中的属性刻画信息，构造描述-相似度规则挖掘问题（DSRM问题）的输入。

该模块功能主函数为test/pre_experiment/StringStatics.java中的calculateStatistic_Spark方法，该方法通过spark读取并计算多种属性刻画，将每个属性的属性刻画用数组类型进行存储，存储于arrayIC/arrayAvg等数组结构中，用于模块四的输入。该函数调用了文件夹src/Statistics中存储的多种属性刻画类，包含熵，IC等。

##### 该函数的参数为：

    Path：输入的数据集路径
    相关数据结构说明：
        array IC：存储计算index of coincidence
    ArrayEntropy:存储字符串的熵
    ArrayLength：存储字符串长度信息
    ArrayAvg：存储字符串中含有的平均数值大小（一种统计量信息）
该函数调用了Spark的RDD数据类型，通过map方法计算多种属性刻画， 然后通过reduce过程为上述的array赋值；

##### 调用使用到的类位于src/Statistics：

IC：计算字符串的重合指数index of coincidence，主要功能函数为calculate函数，输入为字符串s，输出为ic数值；

Entropy：计算字符串的熵，主要功能函数为entropy，输入为字符串str，通过string2ascii函数将字符串转换为ascii编码数组，然后根据熵公式计算，输出为double类型的熵数值；

AvgNumCalc：计算字符串中出现的数字的平均值。这一统计量有助于捕获属性中的数值信息，通过调用cal_avg函数，输入为string类型，输出为double；

AvgWordLenCalc：计算字符串中单词的平均长度。调用avgWordLen函数，输入为string，输出为double；

CharacterWordRatio：计算字符串出现的不同字符的频率，调用characterRatio函数，输入为string，输出为double；

LongestWordCalc/ShortestWordCalc：统计字符串中最长/最短单词长度，这一特征影响了某些相似度函数的偏好程度，例如某些专家知识指出：jaro距离可能更偏好于长字符串，dice则偏向于短字符串。通过调用longestWordLen/shortestWordLen实现，输入为string，输出double类型
 
函数将生成的属性刻画信息存储在二维数组X中，其中X的第一维度表示属性下标，第二维度表示不同的属性刻画；

##### StringStatics中其他函数说明：

calculateStatistic实现了非spark版本的属性刻画计算；

以下函数在主函数calculateStatistic_Spark中被调用：statisticsFromSubset函数：

    用于从历史数据集中计算属性刻画信息，进而构建离线描述-相似度规则库；
    其余函数均在statisticsFromSubset中被调用，是支撑构建规则的功能性函数，如比较字符的compareNumberStrings，根据索引计算文本属性刻画信息的calcSSfromIndexList等。

（3）描述-相似度挖掘模块
本模块功能为生成描述-相似度规则，用于模块四中的自动化特征向量计算，实现自动特征工程。本模块接受了模块2生成的挖掘数据，借助历史数据集经验，通过设计前件挖掘算法DSRM，实现了自动化的相似度函数推荐规则生成。

本模块的主函数位于test/RuleMining/DPmining.java。该文件中的函数maDSRM实现了多前件的规则挖掘，该函数通过指定待挖掘的属性刻画类型，相似度函数类型，通过分析历史数据，获取描述-相似度规则。其中函数的参数如下所示：

    xPath：存储了历史数据集属性刻画信息
    yPath：存储历史数据集y的加权F1 score信息
    yIndex：待挖掘的相似度函数
    ssNum：属性刻画描述前件的个数

该函数按照属性刻画数量，按照前件数量由多到少的顺序，依序挖掘描述-相似度规则。函数调用了mainDP动态规划算法进行高效的规则挖掘，其中调用了加载数据的initTrialMeta数据进行数据预处理，并对属性刻画排序。其中，TrialMeta数据类型表达了描述-相似度规则的语义信息,为数据的预处理和动态规划中的存取提供了大量的赋值接口，并且将可接受的属性刻画和相似度函数用列表方式存储，是广义的描述-相似度规则。动态规划算法过程中，数据点用Point数据类型存储，其成员变量为obs，evi等用于计算sup支持度阈值公式的数据。

算法最终挖掘出的规则用数据结构DSR表示，DSR将规则逐条表示，与TrialMeta不同，仅针对具体的属性刻画和相似度函数。

函数输出了挖掘的规则DSR的列表ruleList，并提供了最优规则bestRule。通过saveRules和loadRules函数实现了规则的本地化存取。
 
（4）自动化计算特征向量模块
本模块利用模块二计算的属性刻画信息和模块三提供的描述-相似度规则库，通过属性刻画，建立属性与相似度函数的映射，进一步通过映射调用相似度函数，结合Spark并行化计算实体对属性的相似度数值。
该模块的主函数位于src/tools/GenerateFeatureWithSim.java，该文件中的，src/tools/generateFeatureRules_Spark函数实现了spark版本的功能，该函数的输入如下：

    ruleList：列表类型，列表中的元素为DSR类的描述-相似度规则
    X：属性刻画信息，以二维数组的形式存储。
 
生成的特征向量存储在成员变量outputPath中，通过addLabel添加模型训练数据的label，最终通过调用weka函数库训练实体识别模型，进行最终的F1 score度量。
 
最终模型测试集的F1 score结果打印输出在控制台中。
作为对比，非自动特征工程方面，我们首先参考了magellan给出的专家知识库作为对比，并给出了齐Spark实现的代码，详见类runMegallan。

    runMegallan：调用模块二中构建属性刻画的方法runMegallan来计算length，进而借助magellan中提供的经由长度推荐相似度函数的专家知识，进行特征向量的计算，我们设计了generateMagellanFeatureUseSpark函数，通过分析字符串平均长度，建立了分组推荐机制，进而调用随机森林模型完成F1 score验证计算。

## 额外说明

### SelfRF.testModel:利用weka读取csv文件训练模型

### StringStatics : 计算属性刻画值，在SSRM——test文件中被调用

StringStatics中的statisticsFromSubset函数，读取blockData和all.csv，以及unlabeled文件，计算分块的属性刻画.生成SS文件
生成的SS文件，通过RegTreeTrial文件中generateX函数，输入SS文件生成trialX
trialX继续在后面加上五元组，生成trail_tuple,作为最终的输入X
Y在python的computerF1文件输出，注意需要调用file.flush()函数刷新缓冲区才能输出到文件

### AttrStatGenerate文件说明

该代码用于生成DSRM问题所需的属性刻画文件
代码输入为原数据集，其中attrLength（line18）表示数据集属性数（注意，包含了left和right，是最终输入数据集的属性数）

    simName表示相似度计算文件名字，需要在文件名字中体现出是哪个数据集，无重名和歧义即可
    line23 ：unlabelPath是输入数据路径
    line33 ： labelPath是标签路径（就一列）
    line50：输出路径，注意一定要修改，要不生成的文件都被覆盖了呜呜
运行代码，然后查看line45路径位置的block文件，搜索modelID，查看数目
如果数目》100，则调大line41的参数，如果太小（不到10）就调小，理想数值大概是40-90都行
最终line50输出文件保存即可
<<<<<<< HEAD

### 子集划分模块说明

该模块位于"src/crr",其中，ModelShare为主功能类，Model、Predict、Rule为子集划分算法中维护的数据结构，分别表示模型类、谓词类、规则类

### ModelShare文件说明：

#### 函数说明：

crr：运行子集划分算法主函数
addPred：函数输入为规则rule、新添加的谓词pred，输出为新的规则集合。该函数对应了子集划分算法中构建规则部分的谓词构建部分，将新谓词添加到原有的规则中
producePred：利用随机森林训练生成的树，分析并提取树中结点信息，形成谓词
readData：加载csv中的数据，转换为Instances类型，便于java的weka库处理
selectByRule：函数输入参数为readData读取后的数据Instances，以及用于划分数据的规则Rule，输出为符合该规则的数据子集

#### 参数说明：

    classIndex：数据中label的下标
    outputPath：block文件输出路径
    RouMax：模型表现阈值

### ModelShareTest测试说明：

测试文件位于test/ModelShareTest.java文件，运行test方法即可。算法需要指定outputPath作为block输出路径，指定存储了特征向量的dataPath作为分块的输入数据（属性对的相似度数值文件），以及模型超参数RouMax，classIndex

