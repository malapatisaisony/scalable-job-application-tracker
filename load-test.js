import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const jobGetDuration = new Trend(
    'job_get_duration',
    true
);

const baseUrl =
    __ENV.BASE_URL || 'http://host.docker.internal:8080';

const testEmail = __ENV.TEST_EMAIL;
const testPassword = __ENV.TEST_PASSWORD;

export const options = {
    vus: 4,
    duration: '20s',
};

export function setup() {

    if (!testEmail || !testPassword) {
        throw new Error(
            'TEST_EMAIL and TEST_PASSWORD environment variables are required'
        );
    }

    const loginResponse = http.post(
        `${baseUrl}/api/users/login`,
        JSON.stringify({
            email: testEmail,
            password: testPassword
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

    // Warm Redis cache before measuring.
    http.get(
        `${baseUrl}/api/job-applications/1`,
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
        `${baseUrl}/api/job-applications/1`,
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