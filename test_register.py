import json
import urllib.request

data = json.dumps({"username": "测试用户", "password": "123456"}).encode("utf-8")
req = urllib.request.Request(
    "http://localhost:8080/api/auth/register",
    data=data,
    headers={"Content-Type": "application/json; charset=UTF-8"}
)
try:
    resp = urllib.request.urlopen(req)
    print("Status:", resp.status)
    print("Response:", resp.read().decode("utf-8"))
except urllib.error.HTTPError as e:
    print("Error Status:", e.status)
    print("Error Body:", e.read().decode("utf-8"))
