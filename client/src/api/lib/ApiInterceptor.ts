import { AxiosInstance } from 'axios';
import { objectToCamel, objectToSnake } from 'ts-case-convert';
import { useAuthStore } from '@/store/authStore';
import { ApiErrorInfo } from './types';

export class ApiInterceptor {
    constructor(private readonly isSecure: boolean = false) {}

    setupInterceptors(instance: AxiosInstance): void {
        this.setupRequestInterceptor(instance);
        this.setupResponseInterceptor(instance);
    }

    private setupRequestInterceptor(instance: AxiosInstance): void {
        instance.interceptors.request.use(
            (config) => {
                const prefix = this.isSecure ? '[보안 API]' : '[API]';
                console.log(`${prefix} ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);

                // 필요 시 공통 헤더
                config.headers['Content-Type'] = 'application/json';

                // 요청 데이터가 있고 JSON 형식인 경우 snake_case로 변환
                if (config.data && typeof config.data === 'object') {
                    config.data = objectToSnake(config.data);
                }

                return config;
            },
            (error) => Promise.reject(error)
        );
    }

    private setupResponseInterceptor(instance: AxiosInstance): void {
        instance.interceptors.response.use(
            (response) => {
                // 응답 데이터 디버깅 로그
                const prefix = this.isSecure ? '보안 API' : 'API';
                console.log(`${prefix} 응답 데이터:`, response.data);
                response.data = objectToCamel(response.data);
                return response;
            },
            (error: any) => {
                const errorInfo = this.createErrorInfo(error);
                this.handleError(errorInfo, error);
                return Promise.reject(error);
            }
        );
    }

    private createErrorInfo(error: any): ApiErrorInfo {
        const errorInfo: ApiErrorInfo = {};

        if (error.response) {
            errorInfo.url = error.config?.url;
            errorInfo.status = error.response.status;
            errorInfo.data = error.response.data;
            errorInfo.message = error.message;
        } else if (error.request) {
            errorInfo.url = error.config?.url;
            errorInfo.message = '응답 없음';
            errorInfo.request = '요청은 전송되었으나 응답을 받지 못함';
        } else {
            errorInfo.message = error.message || '알 수 없는 오류';
        }

        return errorInfo;
    }

    private handleError(errorInfo: ApiErrorInfo, error: any): void {
        const prefix = this.isSecure ? '[보안 API 오류]' : '[API 오류]';
        console.error(prefix, errorInfo);

        // 401 Unauthorized 처리
        if (error.response?.status === 401) {
            console.log('인증 오류: 로그아웃 처리');
            const logout = useAuthStore.getState().logout;
            logout();
        }
    }
} 