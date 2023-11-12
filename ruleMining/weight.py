import numpy as np
import pandas as pd


# 读取GloVe预训练词向量文件
def read_glove_embedding(embedding_file):
    embedding_dict = {}
    with open(embedding_file, 'r', encoding='utf-8') as f:
        for line in f:
            values = line.split(' ')
            word = values[0]
            vector = np.array(values[1:], dtype='float32')
            embedding_dict[word] = vector
    return embedding_dict


# 使用预训练词向量生成嵌入
def generate_embeddings_list(string_col, numerical_col, embedding_dict):
    string_embeddings = {}
    numerical_embeddings = {}
    for text in string_col:
        tokens = text.split()  # 分割文本为单词
        for token in tokens:
            if token in embedding_dict:
                string_embeddings[token] = embedding_dict[token][:embedding_dim]
            else:
                # 如果单词不在预训练词向量中，可以采用随机向量或其他处理方式
                string_embeddings[token] = np.random.rand(embedding_dim)

    for text in numerical_col:
        tokens = text.split()  # 分割文本为单词
        for token in tokens:
            if token in embedding_dict:
                numerical_embeddings[token] = embedding_dict[token][:embedding_dim]
            else:
                # 如果单词不在预训练词向量中，可以采用随机向量或其他处理方式
                numerical_embeddings[token] = np.random.rand(embedding_dim)

    return string_embeddings, numerical_embeddings


def generate_embeddings(word, embedding_dict):
    embeddings = []
    if type(word) == str:
        tokens = word.split()  # 分割文本为单词
        for token in tokens:
            if token in embedding_dict:
                return embedding_dict[token][:embedding_dim]
            else:
                # 如果单词不在预训练词向量中，可以采用随机向量或其他处理方式
                return np.random.rand(embedding_dim)
    for text in word:
        tokens = text.split()  # 分割文本为单词
        for token in tokens:
            if token in embedding_dict:
                embeddings.append(embedding_dict[token][:embedding_dim])
            else:
                # 如果单词不在预训练词向量中，可以采用随机向量或其他处理方式
                embeddings.append(np.random.rand(embedding_dim))
    return embeddings


def process_csv_row(row):
    processed_row = []
    for cell in row:
        cell_str = str(cell)
        processed_cell = cell_str[7:]  # 截取下标为2后面的部分
        processed_row.append(processed_cell)
    return processed_row


def generateWordList(path):
    # 读取csv文件
    data = pd.read_csv(path, nrows=1,
                       header=None, encoding='utf-8')

    # 将第一行的数据转化为字符串列表
    processed_row = process_csv_row(data.iloc[0])
    return list(set(processed_row))


# # 读取数据
# dataPath = 'E:\GitHub\pythonProject\data\computer\large\label data.csv'
# input_text = generateWordList(dataPath)

embedding_dim = 50

# # 读取预训练词向量文件
# embedding_file = 'E:\GitHub\pythonProject\saved_model\glove.42B.300d.txt'
# embedding_dict = read_glove_embedding(embedding_file)
#
# # 生成嵌入
# embedList = generate_embeddings(input_text, embedding_dict)
# print(embedList)


