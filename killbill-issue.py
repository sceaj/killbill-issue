import base64
import json
import random
import requests
import sys
import uuid

from json.decoder import JSONDecodeError

requests.packages.urllib3.disable_warnings(requests.packages.urllib3.exceptions.InsecureRequestWarning)

iterations = int(sys.argv[1])

encoding = 'utf-8'
credentials = 'admin:password'
api_key = 'testtenant01'
api_secret = '5LyW597R'

endpoint = f"http://localhost:8080/plugins/killbill-test-plugin/tests/v1"

auth_token = base64.b64encode(bytes(credentials, encoding)).decode(encoding)
authorization = f"Basic {auth_token}"

headers = {'content-type': 'application/json', 'accept': 'application/json', 'x-correlation-id': str(uuid.uuid4()), 'authorization': authorization, 'x-killbill-apikey': api_key, 'x-killbill-apisecret': api_secret, 'x-killbill-createdby': 'test-harness', 'x-killbill-reason': 'Killbill payload corruption issue', 'user-agent': 'Python3/3.9.3'}

session = requests.Session()

tenant_response = session.get(f"http://localhost:8080/1.0/kb/tenants?apiKey={api_key}", headers=headers)
if tenant_response.status_code != 200:
    tenant_response = session.post('http://localhost:8080/1.0/kb/tenants', data=f'{ "apiKey": {apiKey}, "apiSecret": {apiSecret} }', headers=headers, verify=False)
    if tenant_response.status_code != 200:
        exit(1)

request = { 'externalId': None, 'someDate': '2023-02-21', "someValue": 3.1415926535 }

for index in range(iterations):
    request['externalId'] = str(uuid.uuid4())
    request_body = json.dumps(request)
    for retry in range(1):
        response = session.post(endpoint, data=request_body, headers=headers, verify=False)
        if response.status_code != 200:
            print("Retry: " + str(retry) + "  Request Failed - status: " + str(response.status_code))
            print("                request: " + request_body)
            try:
                print("               response: " + str(response.json()))
            except JSONDecodeError:
                print("               response: " + response.text)
        else:
            break
    request_count = index + 1
    if request_count % 500 == 0:
        print(f"Completed {request_count} requests...")
