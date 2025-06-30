

import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '30s', target: 20 },  // ramp-up
        { duration: '1m', target: 20 },   // sustained load
        { duration: '30s', target: 0 },   // ramp-down
    ],
};

export default function () {
    const res = http.get('http://localhost:8080/status-summary'); // replace with your actual endpoint

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 1000ms': (r) => r.timings.duration < 1000,
    });

    sleep(1);
}
