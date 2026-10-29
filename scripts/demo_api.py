#!/usr/bin/env python3
"""Run against the local app: python3 scripts/demo_api.py [http://localhost:8080]."""
import http.cookiejar
import json
import secrets
import sys
import urllib.error
import urllib.parse
import urllib.request
from decimal import Decimal


BASE = sys.argv[1] if len(sys.argv) > 1 else 'http://localhost:8080'


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


class Session:
    def __init__(self):
        self.client = urllib.request.build_opener(
            urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()), NoRedirect())

    def request(self, method, path, body=None, form=False):
        headers = {}
        if method == 'POST':
            _, token = self.request('GET', '/api/csrf')
            headers[token['headerName']] = token['token']
            headers['Content-Type'] = 'application/x-www-form-urlencoded' if form else 'application/json'
            data = urllib.parse.urlencode(body).encode() if form else json.dumps(body).encode()
        else:
            data = None
        req = urllib.request.Request(BASE + path, data=data, headers=headers, method=method)
        try:
            response = self.client.open(req, timeout=15)
        except urllib.error.HTTPError as error:
            response = error
        with response:
            text = response.read().decode()
            result = json.loads(text) if 'application/json' in response.headers.get('Content-Type', '') else text
            return response.code, result


def main():
    suffix = secrets.token_hex(4)
    password = secrets.token_urlsafe(16)
    sender, receiver = Session(), Session()
    emails = [f'aadi-{suffix}@example.com', f'arnav-{suffix}@example.com']
    for client, name, email in zip([sender, receiver], ['Aadi Demo', 'Arnav Demo'], emails):
        code, result = client.request('POST', '/api/register', {'name': name, 'email': email, 'password': password})
        assert code == 201, (code, result)
        code, _ = client.request('POST', '/login', {'email': email, 'password': password}, form=True)
        assert code == 302, code
        assert client.request('GET', '/api/wallet')[0] == 200
    code, result = sender.request('POST', '/api/wallet/add-money', {'amount': 1000})
    assert code == 200 and result['status'] == 'SUCCESS', (code, result)
    code, result = sender.request('POST', '/api/wallet/transfer', {'recipientEmail': emails[1], 'amount': 250.50})
    assert code == 200 and result['status'] == 'SUCCESS', (code, result)
    code, result = sender.request('POST', '/api/wallet/transfer', {'recipientEmail': emails[1], 'amount': 2000})
    assert code == 422 and result['status'] == 'FAILED', (code, result)
    for client, expected in [(sender, '749.50'), (receiver, '250.50')]:
        code, result = client.request('GET', '/api/wallet')
        assert code == 200 and Decimal(str(result['balance'])) == Decimal(expected), (code, result)
    history = sender.request('GET', '/api/wallet/transactions')[1]
    assert len(history) == 3 and history[0]['status'] == 'FAILED', history
    print('PASS: registration, session login, top-up, transfer, failed overspend, history and both balances')
    print('Sender: INR 749.50 | Receiver: INR 250.50 | Total: INR 1000.00')
    print('Demo accounts:', ', '.join(emails))


if __name__ == '__main__':
    main()
