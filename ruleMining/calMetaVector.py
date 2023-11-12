import os

import judge_attribute
import csv
import numpy as np


def read_data(filename):
    data = []
    with open(filename, 'r', encoding='utf-8') as f:
        csv_reader = csv.reader(f)
        for row in csv_reader:
            data.append(row)
    data.remove(data[-1])
    return data


def cal_mf(data):
    mf = [0 for _ in range(17)]
    data = np.asarray(data)
    data = data.T
    data = data.tolist()
    # 计算数字属性的数量 1
    # 计算类别属性的数量 2
    for i in range(len(data)):
        if judge_attribute.judge_attribute(data[i][0]) == 9:
            mf[0] += 1
            for j in range(len(data[i])):
                if data[i][j] != '':
                    data[i][j] = float(data[i][j])
        else:
            mf[1] += 1
    # 数字属性的比例 3
    mf[2] = mf[0] / (mf[0] + mf[1])
    # 属性总数 4
    mf[3] = mf[0] + mf[1]
    # 观测值总数 5
    for i in range(len(data)):
        for j in range(len(data[i])):
            if data[i][j] != '':
                mf[4] += 1

    class_all = [{} for _ in range(len(data))]
    for i in range(len(data)):
        if str(type(data[i][0])) == '<class \'str\'>':
            for j in range(len(data[i])):
                temp = data[i][j].split(';')
                for te in temp:
                    if te not in class_all[i]:
                        class_all[i][te] = 1
                    else:
                        class_all[i][te] += 1
    class_all_new = []
    for i in range(len(class_all)):
        if class_all[i] != {}:
            temp = []
            for val in class_all[i].values():
                temp.append(val)
            class_all_new.append(temp)
    max_class, min_class = len(class_all_new[0]), len(class_all_new[0])
    mf[6] = np.max(class_all_new[0])/np.sum(class_all_new[0])
    mf[7] = np.min(class_all_new[0])/np.sum(class_all_new[0])
    mf[9] = np.max(class_all_new[0])/np.sum(class_all_new[0])
    mf[10] = np.min(class_all_new[0])/np.sum(class_all_new[0])
    for i in range(len(class_all_new)):
        if len(class_all_new[i]) < min_class:
            min_class = len(class_all_new[i])
            mf[6] = np.max(class_all_new[i])/np.sum(class_all_new[i])
            mf[7] = np.min(class_all_new[i])/np.sum(class_all_new[i])
        if len(class_all_new[i]) > max_class:
            max_class = len(class_all_new[i])
            mf[9] = np.max(class_all_new[i])/np.sum(class_all_new[i])
            mf[10] = np.min(class_all_new[i])/np.sum(class_all_new[i])


    # 类别属性中类别最少的属性的类别数 6
    mf[5] = min_class
    # 类别属性中类别最少的属性的单个类的最大比例 7
    # 类别属性中类别最少的属性的单个类的最小比例 8
    # 类别属性中类别最多的属性的类别数 9
    mf[8] = max_class
    # 类别属性中类别最多的属性的单个类的最大比例 10
    # 类别属性中类别最多的属性的单个类的最小比例 11

    record_mean, record_var = [], []
    temp_all = []
    for i in range(len(data)):
        if str(type(data[i][0])) == '<class \'float\'>':
            temp = []
            for j in range(len(data[i])):
                if data[i][j] != '':
                    temp.append(data[i][j])
            record_mean.append(np.mean(temp))
            record_var.append(np.var(temp))
            temp_all += temp
    # 数值属性中最小均值 12
    if record_mean != []:
        mf[11] = np.min(record_mean)
    # 数值属性中最大均值 13
    if record_mean != []:
        mf[12] = np.max(record_mean)
    # 数值属性中最小方差 14
    if record_var != []:
        mf[13] = np.min(record_var)
    # 数值属性中最大方差 15
    if record_var != []:
        mf[14] = np.max(record_var)
    # 数值属性中均值的方差 16
    if record_mean != []:
        mf[15] = np.var(record_mean)
    # 数值属性的方差 17
    if temp_all != []:
        mf[16] = np.var(temp_all)
    print("mf:", mf)
    return mf


def load_history_metaVector(path):
    result_load = []
    files = os.listdir(path)
    for file in files:
        data = []
        with open(path + '\\' + file, 'r', encoding='utf-8') as f_load:
            csv_reader = csv.reader(f_load)
            for row in csv_reader:
                data.append(row)
        result_load += data
    f_load.close()
    f_load = open("mf_all.csv", 'w', newline="")
    csv_writer1 = csv.writer(f_load)
    for row1 in range(len(result_load)):
        csv_writer1.writerow(result_load[row1])
    f_load.close()
    return result_load


if __name__ == '__main__':
    # todo : 当前数据集名字
    datasetName = 'structured-Amazon-Google'
    # block path
    data_file_name = r'D:\GitHub\EntityMatching\data\huawei\computer\block\structured-Amazon-Googleall,ROU=0.45block.csv'
    result = []

    data = read_data(data_file_name)
    temp = []
    for i in range(len(data)):
        if 'modelID' in data[i][0] and i != 0:
            result.append(cal_mf(temp))
            temp = []
        else:
            if i != 0:
                temp.append(data[i])
    # 保存所有数据集的result，应该最先算
    print(result)
    f = open("mf/mf-"+datasetName+".csv", 'w', newline="")
    csv_writer = csv.writer(f)
    for row in range(len(result)):
        csv_writer.writerow(result[row])
    f.close()
    # load_history_metaVector('D:\\GitHub\\pythonProject\\ruleMining\\mf')