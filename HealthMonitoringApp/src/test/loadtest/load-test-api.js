import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 10 },   // ramp-up
        { duration: '1m', target: 10 },    // steady
        { duration: '30s', target: 0 },    // ramp-down
    ],
};

export default function () {
    const res = http.get('http://localhost:8080/api-summary'); // replace with your actual endpoint

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 1000ms': (r) => r.timings.duration < 1000,
    });

    sleep(1);
}
