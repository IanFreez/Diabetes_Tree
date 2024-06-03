from flask import Flask, request, jsonify
from flask_cors import CORS
import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
from sklearn.preprocessing import LabelEncoder
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, confusion_matrix, classification_report
from sklearn.tree import DecisionTreeClassifier
from sklearn.tree import plot_tree

app = Flask(__name__)
CORS(app)

dataset = pd.read_csv("path.csv", sep=';')
enc = LabelEncoder()
print(dataset)

#0 = Baik, 1 = Buruk, 2 = Sedang.
dataset['Glukosa'] = enc.fit_transform(dataset['Glukosa'].values)
#0 = Hipertensi1, 1 = Hipertensi2, 2 = Krisis, 3 = Normal, 4 = Prahipertensi
dataset['BloodPressure'] = enc.fit_transform(dataset['BloodPressure'].values)
#0 = Kurang, 1 = Normal, 2 = Obesitas
dataset['BMI'] = enc.fit_transform(dataset['BMI'].values)
#0 = Dewasa, 1 = Lansia
dataset['Age'] = enc.fit_transform(dataset['Age'].values)
#0 = Tidak, 1 = Ya
dataset['Diabetes'] = enc.fit_transform(dataset['Diabetes'].values)

print(dataset)
atr_dataset = dataset.drop(columns = 'Diabetes')
cls_dataset = dataset['Diabetes']

X_train, X_test, y_train, y_test = train_test_split(atr_dataset, cls_dataset, test_size=0.2, random_state=50)
dtree = DecisionTreeClassifier(criterion='entropy', random_state=50)
dtree.fit(X_train, y_train)

y_pred = dtree.predict(X_test)
accuracy = accuracy_score(y_test, y_pred)
confusion_mat = confusion_matrix(y_test, y_pred)

print(f"Accuracy: {accuracy}")
print(f"Confusion Matrix:\n{confusion_mat}")

plt.figure(figsize=(12, 9))
plot_tree(dtree, feature_names=atr_dataset.columns, class_names=['Tidak', 'Ya'], filled=True, rounded=True)
plt.show()

@app.route('/predict_diabetes', methods=['POST'])
def predict_diabetes():
    try:
        # Get data
        data = request.get_json()['new_data']
        new_data = pd.DataFrame([data])
        
        prediction = dtree.predict(new_data)
        # Inverse transform the prediction to get the original class labels
        predicted_class = enc.inverse_transform(prediction)[0]

        return jsonify({'prediction': predicted_class})
    except Exception as e:
        return jsonify({'error': str(e)})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)