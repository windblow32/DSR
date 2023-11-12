import csv
import time

import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import f1_score
from sklearn.model_selection import KFold, train_test_split
from tqdm import tqdm

from calMetaVector import cal_mf, read_data, load_history_metaVector
from weight import generateWordList, read_glove_embedding, generate_embeddings, generate_embeddings_list

# 记录程序开始时间
start_time = time.time()
# attention : 计算DSRM问题的Y
all_one_set = pd.DataFrame()
all_zero_set = pd.DataFrame()


def is_all_zeros(lst):
    return all(element == '0' or element == 0 or element == 0.0 or element == '0.0' for element in lst)


def is_all_one(lst):
    return all(element == '1' or element == 1 or element == 1.0 or element == '1.0' for element in lst)


def read_csv_with_blocks(file_path, block_prefix):
    global all_one_set
    global all_zero_set
    subsets = []
    current_subset = []

    with open(file_path, 'r', newline='', encoding='utf-8') as csvfile:
        reader = csv.reader(csvfile)
        for row in reader:
            if row[0].startswith(block_prefix):
                if current_subset:
                    subsets.append(current_subset.copy())
                    current_subset = []
            current_subset.append(row)

        subsets.append(current_subset.copy())

    # 初始化全0和全1集合，平衡正负样例
    zero_max = 0
    same1 = ['1', 1, 1.0, '1.0']
    for current_subset in subsets:
        # 实体识别负样本占据大多数，因此只需要判断是否存在标签全部为0的情况，然后将该子集作为zeroset
        # label=1的样本在遍历过程中保存top100即可
        temp = pd.DataFrame(current_subset[1:])
        if is_all_zeros(temp.iloc[:, -1].values):
            if temp.shape[0] > zero_max:
                zero_max = temp.shape[0]
                last_column = temp.columns[-1]
                # 将最后一列的数据类型指定为整数
                temp[last_column] = pd.to_numeric(temp[last_column], errors='coerce').astype(int)
                all_zero_set = temp
        if all_one_set.shape[0] < 100:
            one_set = temp.loc[temp[colNum].isin(same1)]
            all_one_set = pd.concat([all_one_set, one_set], ignore_index=True)

    return subsets, all_one_set, all_zero_set


def cosine_similarity(vector1, vector2):
    sum1, sum2, sum3 = 0, 0, 0
    for l in range(len(vector1)):
        sum1 += float(vector1[l]) * float(vector2[l])
        sum2 += float(vector1[l]) * float(vector1[l])
        sum3 += float(vector2[l]) * float(vector2[l])
    similarity = sum1 / (sum2 * sum3)
    return similarity


# todo:1.block中一行逗号个数
colNum = 228
times = 0

# todo : 2.数据集名称
ds_name = 'dblp-scholar'

# todo : 4.数据集属性类型（0表示字符串，1表示数字）
col_TypeList = [0, 0, 0, 1]
# 示例用法
# todo : 3.block路径
file_path = r'block/dblp-scholarall,ROU=0.005block10.10.csv'  # 替换为你的CSV文件路径
block_prefix = "model"  # 替换为特定行的前缀
# 新数据集的元向量计算
# 计算新数据集的元向量
result = []
data = read_data(file_path)
temp = []
for i in range(len(data)):
    if 'modelID' in data[i][0] and i != 0:
        result.append(cal_mf(temp))
        temp = []
    else:
        if i != 0:
            temp.append(data[i])
# 补上剩下的block
result.append(cal_mf(temp))

path = 'mf_all.csv'
result_history = read_data(path)
# 计算Yj公式中的分母meta，两两配对
under_meta_sum = 0
for v1 in result_history:
    for v2 in result:
        under_meta_sum += cosine_similarity(v1, v2)

# 执行数据处理
subsets, all_one_set, all_zero_set = read_csv_with_blocks(file_path, block_prefix)
file_path = 'output/trailY_' + ds_name + '10.16.csv'  # 新文件的路径

# fixme : 参数表示simFunc个数，改成X文件代码最终输出的数字
simFunctionNum = 57
path_attr = 'dataset/' + ds_name + '.csv'
attrNameList = []
with open(path_attr, 'r', encoding='utf-8') as f:
    csv_reader = csv.reader(f)
    k = 0
    for row in csv_reader:
        if k == 0:
            for i in range(int(len(row) / 2)):
                attrNameList.append(row[i * 2][7:])
            break

string_col = ['id', 'title', 'authors', 'venue',
              'Song_Name', 'Artist_Name', 'Album_Name', 'Genre', 'CopyRight', 'Released',
              'category', 'brand',
              'modelno',
              'manufacturer',
              'Beer_Name', 'Brew_Factory_Name', 'Style',
              'name', 'addr', 'city', 'phone', 'type'
              ]
numerical_col = ['year', 'Price', 'ABV', 'class', 'time']

embedding_dim = 300
# 读取预训练词向量文件
embedding_file = 'saved_model/glove.840B.300d.txt'
embedding_dict = read_glove_embedding(embedding_file)
# 生成嵌入
string_embdi, numerical_embdi = generate_embeddings_list(string_col, numerical_col, embedding_dict)
# 计算输入数据集所有属性与历史属性间embedding的余弦相似度
current_dataset_string_attr = []
current_dataset_numerical_attr = []
for enum, i in enumerate(col_TypeList):
    # 枚举col_TypeList，enum是索引，i是list中的数值
    # 如果list中数值为0，则添加到string list中
    if i == 0:
        current_dataset_string_attr.append(attrNameList[enum])
    else:
        current_dataset_numerical_attr.append(attrNameList[enum])
current_string_embdi, current_numerical_embdi = generate_embeddings_list(current_dataset_string_attr,
                                                                         current_dataset_numerical_attr, embedding_dict)
under_embed_string_sum = 0
under_embed_numerical_sum = 0
for e1 in string_embdi:
    for e2 in current_string_embdi:
        under_embed_string_sum += cosine_similarity(string_embdi[e1], current_string_embdi[e2])

for e1 in numerical_embdi:
    for e2 in current_numerical_embdi:
        under_embed_numerical_sum += cosine_similarity(numerical_embdi[e1], current_numerical_embdi[e2])

# 打开文件并写入内容
with open(file_path, mode='w') as file:
    t = 0
    temp = pd.DataFrame()
    for subset in tqdm(subsets):
        data = pd.DataFrame(subset[1:])
        # fixme：如果block中实体数太少，则判断data大小，如果小于阈值，则自动拓展到下一个数据集
        if data.shape[0] < 100:
            exit(-200)
            data = pd.concat([data, temp], ignore_index=True)
            temp = data
            continue

        # 成功达到指定大小后，清空temp，为下次作准备
        temp = pd.DataFrame()
        last_column = data.columns[-1]
        data[last_column] = pd.to_numeric(data[last_column], errors='coerce').astype(int)
        y_true = data.iloc[:, -1].values
        y = y_true
        # 避免都是负样本，F1 score为0
        value_counts = data[last_column].value_counts()
        # 统计data中含有的0、1个数，平衡正负样本
        count_ones = value_counts.get(1, 0)
        count_zeros = value_counts.get(0, 0)
        if count_ones > count_zeros:
            # add zeros
            zero_size = all_zero_set.shape[0]
            data = pd.concat([data, all_zero_set.head(min(zero_size, count_ones - count_zeros))], ignore_index=True)

        elif count_zeros > count_ones:
            # add one
            one_size = all_one_set.shape[0]
            data = pd.concat([data, all_one_set.head(min(one_size, count_zeros - count_ones))], ignore_index=True)
        # 类型转换
        data[last_column] = pd.to_numeric(data[last_column], errors='coerce').astype(int)
        y_true = data.iloc[:, -1].values
        y = y_true
        # 数据集划分结束，开始计算embedding和元向量
        meta_sum = 0
        # 历史元向量文件中每个向量和目前的这个向量计算cosine
        for i in range(len(result_history)):
            meta_sum += cosine_similarity(result_history[i], result[t])

        # embedding
        embedEqualSum = 0
        numerical_flag = 0
        for id1 in range(0, colNum):
            # 每simFunctionNum（57）个属性,计算一次，节约代价
            if id1 % simFunctionNum == 0:
                str_index: int = int(id1 / simFunctionNum)
                str1 = attrNameList[str_index]
                vector_now = generate_embeddings(str1, embedding_dict)
                embedEqualSum = 0
                if col_TypeList[str_index] == 0:
                    # use curent string embdi
                    for vector in string_embdi.values():
                        embedEqualSum += cosine_similarity(vector, vector_now)
                else:
                    # use current numerical embdi
                    numerical_flag = 1
                    for vector in numerical_embdi.values():
                        embedEqualSum += cosine_similarity(vector, vector_now)

            file.flush()
            X = data.iloc[:, id1:id1 + 1].values
            # 输入数据如果包含NaN则替换为0
            X[X == 'NaN'] = '0'
            X[X == '-Infinity'] = '0'
            # k = 2  # 设置k值
            # kf = KFold(n_splits=k, shuffle=True)
            f1 = 0
            X_train, X_test, Y_train, Y_test = train_test_split(X, y, random_state=42)
            rf = RandomForestClassifier()
            try:
                rf.fit(X_train, Y_train)
            except Exception:
                exit(-100)

            y_pred = rf.predict(X_test)
            f1 += f1_score(Y_test, y_pred, pos_label=1)
            if numerical_flag == 0:
                res = (meta_sum * embedEqualSum * f1) / (under_embed_string_sum * under_meta_sum)
            else:
                res = (meta_sum * embedEqualSum * f1) / (under_embed_numerical_sum * under_meta_sum)

            file.write(str(f1))
            times = times + 1
            if times % simFunctionNum == 0:
                file.write('\n')
            else:
                file.write(',')
        t = t + 1

# 记录程序结束时间
end_time = time.time()
# 计算程序执行时间
execution_time = end_time - start_time
print(f"程序执行时间：{execution_time} 秒")
