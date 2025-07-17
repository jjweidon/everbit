import { UPBIT_API_URL } from '../constants';

export const UpbitApiKeyDescription = () => (
    <div className="flex-1">
        <h2 className="text-lg sm:text-xl font-medium text-navy-900 dark:text-white mb-3 sm:mb-4">
            업비트 API 키 등록
        </h2>
        <p className="text-sm sm:text-base text-navy-600 dark:text-navy-200 mb-4 sm:mb-6">
            업비트에서 발급받은 API 키를 등록해야 서비스를 이용할 수 있습니다.
            API 키는 암호화되어 보관되며, 읽기 전용 권한만 사용합니다.
        </p>
    </div>
);

export const UpbitApiKeyLink = () => (
    <div className="flex lg:items-center">
        <a
            href={UPBIT_API_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center px-6 py-3 bg-navy-500 text-white font-medium rounded-lg hover:bg-navy-600 focus:outline-none focus:ring-2 focus:ring-navy-500 focus:ring-offset-2 transition-colors whitespace-nowrap"
        >
            업비트 API 키 발급받기
            <svg className="ml-2 -mr-1 w-4 h-4" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
                <path d="M11 3a1 1 0 100 2h2.586l-6.293 6.293a1 1 0 101.414 1.414L15 6.414V9a1 1 0 102 0V4a1 1 0 00-1-1h-5z" />
                <path d="M5 5a2 2 0 00-2 2v8a2 2 0 002 2h8a2 2 0 002-2v-3a1 1 0 10-2 0v3H5V7h3a1 1 0 000-2H5z" />
            </svg>
        </a>
    </div>
); 