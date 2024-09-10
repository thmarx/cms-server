import { check } from 'k6';
import http from 'k6/http';

export const options = {
    thresholds: {
        http_req_failed: ['rate<0.01'], // http errors should be less than 1%
        http_req_duration: ['p(95)<250'], // 95% of requests should be below 200ms
    },
};

export default function () {

    let res = http.get("http://localhost2:1010/example/route");
    check(res, {
        'is status 200': (r) => r.status === 200,
    });

    res = http.get("http://localhost2:1010/module/example-module/world");
    check(res, {
        'is status 200': (r) => r.status === 200,
        'verify content': (r) =>
            r.body.includes('Hello world!'),
    });

    res = http.get("http://localhost2:1010/example/route");
    check(res, {
        'is status 200': (r) => r.status === 200,
        'verify content': (r) =>
            r.body.includes('example route\nHELlO: NO-NAME'),
    });
    res = http.get("http://localhost2:1010/example/route?name=test-suite");
    check(res, {
        'is status 200': (r) => r.status === 200,
        'verify content': (r) =>
            r.body.includes('example route\nHELlO: test-suite'),
    });
}