import axiosInstance from './axiosInstance';
import { AxiosRequestConfig } from 'axios';

interface ApiOptions extends Omit<AxiosRequestConfig, 'url' | 'method'> {
  params?: Record<string, string>;
}

class ApiClient {
  async get<T>(url: string, options: ApiOptions = {}): Promise<T> {
    return this.request<T>({
      url,
      method: 'GET',
      ...options
    });
  }

  async post<T>(url: string, data?: any, options: ApiOptions = {}): Promise<T> {
    return this.request<T>({
      url,
      method: 'POST',
      data,
      ...options
    });
  }

  async put<T>(url: string, data?: any, options: ApiOptions = {}): Promise<T> {
    return this.request<T>({
      url,
      method: 'PUT',
      data,
      ...options
    });
  }

  async delete<T>(url: string, options: ApiOptions = {}): Promise<T> {
    return this.request<T>({
      url,
      method: 'DELETE',
      ...options
    });
  }

  private async request<T>(config: AxiosRequestConfig): Promise<T> {
    try {
      const response = await axiosInstance(config);
      return response.data;
    } catch (error: any) {
      if (!error.response) {
        throw new Error('네트워크 오류가 발생했습니다.');
      }
      throw error;
    }
  }
}

export const apiClient = new ApiClient(); 