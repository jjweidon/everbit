import { AxiosInstance, AxiosRequestConfig } from 'axios';
import { ApiOptions, ApiResponse } from './types';
import { HttpClientFactory } from './HttpClientFactory';

export class ApiClient {
    protected axiosInstance: AxiosInstance;

    constructor(instance?: AxiosInstance) {
        this.axiosInstance = instance || HttpClientFactory.getDefaultInstance();
    }

    async get<T>(url: string, options: ApiOptions = {}): Promise<T> {
        return this.request<T>({
            url,
            method: 'GET',
            ...options,
        });
    }

    async post<T>(url: string, data?: any, options: ApiOptions = {}): Promise<T> {
        return this.request<T>({
            url,
            method: 'POST',
            data,
            ...options,
        });
    }

    async put<T>(url: string, data?: any, options: ApiOptions = {}): Promise<T> {
        return this.request<T>({
            url,
            method: 'PUT',
            data,
            ...options,
        });
    }

    async patch<T>(url: string, data?: any, options: ApiOptions = {}): Promise<T> {
        return this.request<T>({
            url,
            method: 'PATCH',
            data,
            ...options,
        });
    }

    async delete<T>(url: string, options: ApiOptions = {}): Promise<T> {
        return this.request<T>({
            url,
            method: 'DELETE',
            ...options,
        });
    }

    protected async request<T>(config: AxiosRequestConfig): Promise<T> {
        try {
            const response = await this.axiosInstance(config);
            const apiResponse = response.data as ApiResponse<T>;
            
            if (!apiResponse.success) {
                throw new Error(apiResponse.message || '서버 응답 실패');
            }
            return apiResponse.data;
        } catch (error: any) {
            if (!error.response) {
                throw new Error('네트워크 오류가 발생했습니다.');
            }
            throw error;
        }
    }
}

export const apiClient = new ApiClient(); 