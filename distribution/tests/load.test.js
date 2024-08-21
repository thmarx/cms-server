import http from 'k6/http';
import { check, group } from 'k6';

export let options = {
    stages: [
        { duration: '0.5m', target: 10 }, // simulate ramp-up of traffic from 1 to 3 virtual users over 0.5 minutes.
        { duration: '1.5m', target: 100 }, // stay at 4 virtual users for 0.5 minutes
        { duration: '0.5m', target: 0 }, // ramp-down to 0 users
    ],
};

export default function () {
    group('check base page', () => {
        const response = http.get('http://localhost2:1010');
        check(response, {
            "status code should be 200": res => res.status === 200,
        });
    });

    group('check routes', () => {
        const response = http.get('http://localhost2:1010/example/route');
        check(response, {
            "status code should be 200": res => res.status === 200,
        });
    });
    
    group('check extensions', () => {
        const response = http.get('http://localhost2:1010/extension/test');
        check(response, {
            "status code should be 200": res => res.status === 200,
        });
    });
    group('check modules', () => {

        const check_urls = [
            'http://localhost2:1010/module/example-module/world',
            'http://localhost2:1010/module/example-module/hook',
            'http://localhost2:1010/hello-extension'
        ]
        check_urls.forEach(url => {
            let res = http.get(url);
            check(res, {
                'status code should be 200': (r) => r.status === 200,
            });
        })
    });
};
