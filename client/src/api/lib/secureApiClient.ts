import { ApiClient } from './apiClient';
import { HttpClientFactory } from './HttpClientFactory';

export class SecureApiClient extends ApiClient {
    constructor() {
        super(HttpClientFactory.getSecureInstance());
    }
}

export const secureApiClient = new SecureApiClient(); 