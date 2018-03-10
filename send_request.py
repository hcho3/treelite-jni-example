import requests
import json

payload = {"foo": -0.323, "bar": 2, 3: -0.23, 7: 1.00}
r = requests.post("http://localhost:8080/func1", json.dumps(payload))
print(json.loads(r.text))
