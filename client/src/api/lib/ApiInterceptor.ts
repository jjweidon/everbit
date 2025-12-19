import { AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { objectToCamel, objectToSnake } from 'ts-case-convert';
import { ApiErrorInfo } from './types';
import { SECURE_API_BASE_URL } from '../config';
import { tokenStorage } from '@/utils/tokenStorage';

export type LogoutHandler = () => void;

export class ApiInterceptor {
    private isRefreshing = false;
    private failedQueue: Array<{
        resolve: (value?: any) => void;
        reject: (reason?: any) => void;
    }> = [];

    constructor(
        private readonly isSecure: boolean = false,
        private readonly onUnauthorized?: LogoutHandler
    ) {}

    setupInterceptors(instance: AxiosInstance): void {
        this.setupRequestInterceptor(instance);
        this.setupResponseInterceptor(instance);
    }

    private setupRequestInterceptor(instance: AxiosInstance): void {
        instance.interceptors.request.use(
            (config) => {
                // 필요 시 공통 헤더
                config.headers['Content-Type'] = 'application/json';

                // Access 토큰을 로컬 스토리지에서 가져와 Authorization 헤더에 추가
                if (this.isSecure) {
                    const accessToken = tokenStorage.getAccessToken();
                    if (accessToken) {
                        config.headers['Authorization'] = `Bearer ${accessToken}`;
                    }
                }

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
                response.data = objectToCamel(response.data);
                return response;
            },
            async (error: any) => {
                const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

                // 401 에러이고 refresh API가 아닌 경우에만 토큰 갱신 시도
                if (
                    error.response?.status === 401 &&
                    !originalRequest._retry &&
                    !originalRequest.url?.includes('/auth/refresh') &&
                    this.isSecure
                ) {
                    if (this.isRefreshing) {
                        // 이미 토큰 갱신 중이면 대기
                        return new Promise((resolve, reject) => {
                            this.failedQueue.push({ resolve, reject });
                        })
                            .then(() => {
                                return instance(originalRequest);
                            })
                            .catch((err) => {
                                return Promise.reject(err);
                            });
                    }

                    originalRequest._retry = true;
                    this.isRefreshing = true;

                    try {
                        // Refresh 토큰으로 새 토큰 발급
                        await this.refreshToken();
                        
                        // 대기 중인 요청들 재시도
                        this.processQueue(null);
                        
                        // 원래 요청 재시도
                        return instance(originalRequest);
                    } catch (refreshError) {
                        // Refresh 실패 시 대기 중인 요청들 모두 실패 처리
                        this.processQueue(refreshError);
                        
                        // 로그아웃 처리
                        if (this.onUnauthorized) {
                            this.onUnauthorized();
                        }
                        
                        return Promise.reject(refreshError);
                    } finally {
                        this.isRefreshing = false;
                    }
                }

                const errorInfo = this.createErrorInfo(error);
                this.handleError(errorInfo, error);
                return Promise.reject(error);
            }
        );
    }

    private async refreshToken(): Promise<void> {
        const response = await fetch(`${SECURE_API_BASE_URL}/auth/refresh`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
            },
        });

        if (!response.ok) {
            throw new Error('토큰 갱신 실패');
        }

        // 응답에서 새로운 Access 토큰 추출하여 로컬 스토리지에 저장
        const data = await response.json();
        if (data?.data?.accessToken) {
            tokenStorage.setAccessToken(data.data.accessToken);
        }
    }

    private processQueue(error: any): void {
        this.failedQueue.forEach((promise) => {
            if (error) {
                promise.reject(error);
            } else {
                promise.resolve();
            }
        });
        this.failedQueue = [];
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
        // 401 Unauthorized 처리 (refresh API 호출 후에도 실패한 경우)
        if (error.response?.status === 401 && this.onUnauthorized && !this.isRefreshing) {
            this.onUnauthorized();
        }
    }
} 