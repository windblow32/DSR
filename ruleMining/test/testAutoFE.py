import time

import numpy as np
import pandas as pd
from sklearn.feature_selection import SelectFromModel
from sklearn.model_selection import train_test_split, KFold, RandomizedSearchCV
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import f1_score, accuracy_score, precision_score, recall_score


# 1. 读取CSV文件，假设文件名为data.csv
data = pd.read_csv(
    r'/Users/ovoniko/Documents/GitHub/EntityMatching/data/huawei/autoFE/data/labeled'
    r'/labeledFV_AutoFE_CompareWithAutoEM_Test_'
    r'Amazon-Google.csv',
    header=None)
start = time.perf_counter()
# 2. 将数据分为特征（X）和标签（y）
X = data.iloc[:, :-1]  # 所有列除了最后一列

y = data.iloc[:, -1]  # 最后一列


# 3. 将数据分为训练集和测试集
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

param_grid_simple = {"criterion": ['entropy', 'gini', 'log_loss']
    , 'n_estimators': [*range(20, 100, 5)]
    , 'max_depth': [*range(10, 25, 2)]
    , "min_impurity_decrease": [*np.arange(0, 5, 10)]
                     }

rf = RandomForestClassifier(random_state=24)
cv = KFold(n_splits=5, shuffle=True, random_state=24)

# 定义随机搜索
search1 = RandomizedSearchCV(estimator=rf
                             , param_distributions=param_grid_simple
                             , scoring="neg_mean_squared_error"
                             , verbose=True
                             , cv=cv
                             , random_state=24
                             , n_jobs=-1
                             )

# threshold参数设置根据特征重要性筛选特征的阈值
search1.fit(X_train, y_train)

# 5. 使用模型进行预测
y_pred = search1.predict(X_test)

# 6. 计算F1分数
f1 = f1_score(y_test, y_pred, average='weighted')  # 使用weighted平均
accuracy = accuracy_score(y_test, y_pred)
precision = precision_score(y_test, y_pred, average='weighted')
recall = recall_score(y_test, y_pred, average='weighted')
# 7. 输出F1分数
print("F1 Score:", f1)
print("accuracy Score:", accuracy)
print("precision Score:", precision)
print("recall Score:", recall)

end = time.perf_counter()
runTime = end - start
print("time:", runTime/50)

