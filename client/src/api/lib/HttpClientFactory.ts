import axios, { AxiosInstance } from 'axios';
import { API_BASE_URL, SECURE_API_BASE_URL } from '../config';
import { ApiInterceptor } from './ApiInterceptor';

export class HttpClientFactory {
    private static defaultInstance: AxiosInstance;
    private static secureInstance: AxiosInstance;

    static getDefaultInstance(): AxiosInstance {
        if (!this.defaultInstance) {
            const baseUrl = API_BASE_URL || '';
            this.defaultInstance = this.createInstance(baseUrl, false);
        }
        return this.defaultInstance;
    }

    static getSecureInstance(): AxiosInstance {
        if (!this.secureInstance) {
            const baseUrl = SECURE_API_BASE_URL || '';
            this.secureInstance = this.createInstance(baseUrl, true);
        }
        return this.secureInstance;
    }

    private static createInstance(baseURL: string, isSecure: boolean): AxiosInstance {
        const instance = axios.create({
            baseURL,
            timeout: 5000,
            withCredentials: true,
        });

        const interceptor = new ApiInterceptor(isSecure);
        interceptor.setupInterceptors(instance);
        return instance;
    }
} 