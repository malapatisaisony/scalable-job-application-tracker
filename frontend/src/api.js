export const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL ||
    'http://localhost:8080';

async function refreshAccessToken() {
    const refreshToken =
        localStorage.getItem('refreshToken');

    if (!refreshToken) {
        return null;
    }

    try {
        const response = await fetch(
            `${API_BASE_URL}/api/users/refresh`,
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    refreshToken,
                }),
            }
        );

        if (!response.ok) {
            return null;
        }

        const data = await response.json();

        localStorage.setItem(
            'accessToken',
            data.accessToken
        );

        if (data.refreshToken) {
            localStorage.setItem(
                'refreshToken',
                data.refreshToken
            );
        }

        return data.accessToken;

    } catch (error) {
        return null;
    }
}

export async function authenticatedFetch(
    path,
    options = {}
) {
    let accessToken =
        localStorage.getItem('accessToken');

    const makeRequest = (token) =>
        fetch(
            `${API_BASE_URL}${path}`,
            {
                ...options,
                headers: {
                    ...options.headers,

                    ...(token
                        ? {
                            Authorization:
                                `Bearer ${token}`,
                        }
                        : {}),
                },
            }
        );

    let response =
        await makeRequest(accessToken);

    if (response.status !== 401) {
        return response;
    }

    accessToken =
        await refreshAccessToken();

    if (!accessToken) {
        localStorage.removeItem(
            'accessToken'
        );

        localStorage.removeItem(
            'refreshToken'
        );

        return response;
    }

    response =
        await makeRequest(accessToken);

    return response;
}