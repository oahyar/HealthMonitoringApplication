import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '30s', target: 50 },
        { duration: '30s', target: 100 },
        { duration: '1m', target: 100 },
        { duration: '30s', target: 0 },
    ]
};

export default function () {
    // Make both calls simultaneously (like frontend)
    const responses = http.batch([
        ['GET', 'http://localhost:8080/aggregated-tablespace'],
        ['GET', 'http://localhost:8080/aggregated'],
    ]);

    // Validate both responses
    check(responses[0], {
        'db status is 200': (r) => r.status === 200,
        'db response time < 1000ms': (r) => r.timings.duration < 1000,
    });

    check(responses[1], {
        'server status is 200': (r) => r.status === 200,
        'server response time < 1000ms': (r) => r.timings.duration < 1000,
    });

    sleep(1);
}
