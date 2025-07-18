import { AxiosRequestConfig } from 'axios';

export interface ApiOptions extends Omit<AxiosRequestConfig, 'url' | 'method'> {
    params?: Record<string, string>;
}

export interface ApiErrorInfo {
    url?: string;
    status?: number;
    data?: unknown;
    message?: string;
    request?: string;
}

export interface ApiResponse<T = unknown> {
    success: boolean;
    message?: string;
    data: T;
} 