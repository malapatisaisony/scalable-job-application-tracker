import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const jobGetDuration = new Trend(
    'job_get_duration',
    true
);

export const options = {
    vus: 4,
    duration: '20s',
};

export function setup() {

    const loginResponse = http.post(
        'http://host.docker.internal:8080/api/users/login',
        JSON.stringify({
            email: 'test2@example.com',
            password: 'Password123'
        }),
        {
            headers: {
                'Content-Type': 'application/json'
            }
        }
    );

    check(loginResponse, {
        'login succeeded': (res) => res.status === 200
    });

    const token = loginResponse.json('accessToken');

    // Warm the Redis cache before measuring.
    http.get(
        'http://host.docker.internal:8080/api/job-applications/1',
        {
            headers: {
                Authorization: `Bearer ${token}`
            }
        }
    );

    return {
        token: token
    };
}

export default function (data) {

    const response = http.get(
        'http://host.docker.internal:8080/api/job-applications/1',
        {
            headers: {
                Authorization: `Bearer ${data.token}`
            }
        }
    );

    jobGetDuration.add(
        response.timings.duration
    );

    check(response, {
        'status is 200': (res) => res.status === 200
    });

    sleep(1);
}