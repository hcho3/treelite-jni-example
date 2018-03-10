Deploying compiled tree model using JNI
=======================================

This is a short example of how you can deploy decision tree models to a
Java-based serving system using Treelite and JNI. Some assumptions:
* The client will send single-part POST requests. Each request carries a
  plain-text JSON string of the following form:
  `{ [feature_name] : [feature_value], ... } `
* A simple HTTP server will handle incoming POST requests.
* The server converts incoming requests into a dense numeric feature vector.
* All features are numerical.
* `PredictWrapper.java` maintains a one-to-one mapping between feature names
  and feature indices.

How-to
------
1. Install Treelite package from the Python Package Index:
```console
pip3 install --user treelite
```

2. Run `genlib.py` to generate compiled library from your decision tree model:
```console
python3 genlib.py your_model.model xgboost
```
Only XGBoost and LightGBM models are supported.

3. Specify the installation directory of Java Development Kit (JDK) in 
`config.mk`:
```bash
# Modify the following line to locate JDK
JAVA_HOME := /path/to/JDK
```

4. Run `make`.

5. Start the HTTP server by typing
```console
java SimpleServer
```

6. You can now send POST requests to `localhost`. To test, open a new terminal
window and run
```console
python3 send_request.py
```
