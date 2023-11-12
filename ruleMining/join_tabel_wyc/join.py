import pandas as pd


def merge_tables(table_a, table_b, train, test, valid, output_file):
    # 读取 CSV 文件为 DataFrame
    df_a = pd.read_csv(table_a)
    df_b = pd.read_csv(table_b)
    df_train = pd.read_csv(train)
    df_test = pd.read_csv(test)
    df_valid = pd.read_csv(valid)

    # 根据 train、test 和 valid 的值查找对应的元组
    df_merged_train = pd.merge(df_train, df_a, left_on='ltable_id', right_on='id')
    df_merged_train = pd.merge(df_merged_train, df_b, left_on='rtable_id', right_on='id',
                               suffixes=('_ltable', '_rtable'))
    df_merged_train.columns = [f'ltable_{col}' if col.endswith('_ltable') else f'rtable_{col}' for col in
                               df_merged_train.columns]
    df_merged_train.columns = [col.replace('_ltable', '').replace('_rtable', '') for col in df_merged_train.columns]
    df_merged_train = df_merged_train.iloc[:, 2:]
    columns = df_merged_train.columns.tolist()
    columns = columns[1:] + columns[:1]
    df_merged_train = df_merged_train[columns]
    # 获取列数
    num_cols = df_merged_train.shape[1]
    n = (num_cols - 1) // 2
    # 调整列顺序
    new_columns = []
    for i in range(n):
        new_columns.append(df_merged_train.columns[i])
        new_columns.append(df_merged_train.columns[i + n])
    if num_cols % 2 != 0:
        new_columns.append(df_merged_train.columns[-1])
    # 将新列名插入到原 DataFrame 中
    df_merged_train = df_merged_train[new_columns]
    df_merged_train.rename(columns={df_merged_train.columns[-1]: "label"}, inplace=True)
    # df_merged_train['label'] = 'train'

    df_merged_test = pd.merge(df_test, df_a, left_on='ltable_id', right_on='id')
    df_merged_test = pd.merge(df_merged_test, df_b, left_on='rtable_id', right_on='id', suffixes=('_ltable', '_rtable'))
    df_merged_test.columns = [f'ltable_{col}' if col.endswith('_ltable') else f'rtable_{col}' for col in
                              df_merged_test.columns]
    df_merged_test.columns = [col.replace('_ltable', '').replace('_rtable', '') for col in df_merged_test.columns]
    df_merged_test = df_merged_test.iloc[:, 2:]
    columns = df_merged_test.columns.tolist()
    columns = columns[1:] + columns[:1]
    df_merged_test = df_merged_test[columns]
    # 获取列数
    num_cols = df_merged_test.shape[1]
    n = (num_cols - 1) // 2
    # 调整列顺序
    new_columns = []
    for i in range(n):
        new_columns.append(df_merged_test.columns[i])
        new_columns.append(df_merged_test.columns[i + n])
    if num_cols % 2 != 0:
        new_columns.append(df_merged_test.columns[-1])
    # 将新列名插入到原 DataFrame 中
    df_merged_test = df_merged_test[new_columns]
    df_merged_test.rename(columns={df_merged_test.columns[-1]: "label"}, inplace=True)

    df_merged_valid = pd.merge(df_valid, df_a, left_on='ltable_id', right_on='id')
    df_merged_valid = pd.merge(df_merged_valid, df_b, left_on='rtable_id', right_on='id',
                               suffixes=('_ltable', '_rtable'))
    df_merged_valid.columns = [f'ltable_{col}' if col.endswith('_ltable') else f'rtable_{col}' for col in
                               df_merged_valid.columns]
    df_merged_valid.columns = [col.replace('_ltable', '').replace('_rtable', '') for col in df_merged_valid.columns]
    df_merged_valid = df_merged_valid.iloc[:, 2:]
    columns = df_merged_valid.columns.tolist()
    columns = columns[1:] + columns[:1]
    df_merged_valid = df_merged_valid[columns]
    # 获取列数
    num_cols = df_merged_valid.shape[1]
    n = (num_cols - 1) // 2
    # 调整列顺序
    new_columns = []
    for i in range(n):
        new_columns.append(df_merged_valid.columns[i])
        new_columns.append(df_merged_valid.columns[i + n])
    if num_cols % 2 != 0:
        new_columns.append(df_merged_valid.columns[-1])
    # 将新列名插入到原 DataFrame 中
    df_merged_valid = df_merged_valid[new_columns]
    df_merged_valid.rename(columns={df_merged_valid.columns[-1]: "label"}, inplace=True)

    # 重新命名列名
    # df_merged_train.rename(columns=lambda x: 'ltable_' + x if x != 'label' else x, inplace=True)
    # df_merged_train.rename(columns=lambda x: 'rtable_' + x if x not in ['label', 'ltable_id', 'rtable_id'] else x, inplace=True)
    # df_merged_test.rename(columns=lambda x: 'ltable_' + x if x != 'label' else x, inplace=True)
    # df_merged_test.rename(columns=lambda x: 'rtable_' + x if x not in ['label', 'ltable_id', 'rtable_id'] else x, inplace=True)
    # df_merged_valid.rename(columns=lambda x: 'ltable_' + x if x != 'label' else x, inplace=True)
    # df_merged_valid.rename(columns=lambda x: 'rtable_' + x if x not in ['label', 'ltable_id', 'rtable_id'] else x, inplace=True)

    # 合并 train、test 和 valid 的结果
    df_merged = pd.concat([df_merged_train, df_merged_test, df_merged_valid], ignore_index=True)

    # 保存为 CSV 文件
    df_merged.to_csv(output_file, index=False)
    print("输出文件已保存为:", output_file)


# 示例用法

if __name__ == '__main__':
    datasetName = 'Walmart-Amazon'
    table_a = 'dataset/entity_matching/structured/'+datasetName+'/tableA.csv'
    table_b = 'dataset/entity_matching/structured/'+datasetName+'/tableB.csv'
    train = 'dataset/entity_matching/structured/'+datasetName+'/train.csv'
    test = 'dataset/entity_matching/structured/'+datasetName+'/test.csv'
    valid = 'dataset/entity_matching/structured/'+datasetName+'/valid.csv'
    output = 'dataset/entity_matching/structured/'+datasetName+'/'+datasetName+'.csv'
    merge_tables(table_a, table_b, train, test, valid, output)
