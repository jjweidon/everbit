import axios from "axios";
import { API_BASE_URL } from "./config";
import { objectToCamel, objectToSnake } from "ts-case-convert";
import { useAuthStore } from "@/store/authStore";

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 5000, // 5초
  withCredentials: true
});

// 요청 인터셉터
axiosInstance.interceptors.request.use(
    (config) => {
      console.log(`[요청] ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);
      
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

// 응답 인터셉터
axiosInstance.interceptors.response.use(
    (response) => {
      // 응답 데이터 디버깅 로그
      console.log('API 응답 데이터:', response.data);
      response.data = objectToCamel(response.data);
      return response;
    },
    (error: any) => {
      // 타입 안전한 에러 로깅
      const errorInfo: Record<string, unknown> = {};
      
      try {
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
        
        console.error('[API 오류]', errorInfo);
      } catch (loggingError) {
        console.error('[API 오류 로깅 실패]', loggingError);
      }

      // 401 Unauthorized 처리
      if (error.response?.status === 401) {
        console.log('인증 오류: 로그아웃 처리');
        const logout = useAuthStore.getState().logout;
        logout();
      }
      
      return Promise.reject(error);
    }
);
  
export default axiosInstance;
