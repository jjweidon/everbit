import { GuideStep } from './types';

export const UPBIT_API_URL = 'https://upbit.com/upbit_user/private/signin?pathname=%2Fmypage%2Fopen_api_management';

export const GUIDE_STEPS: GuideStep[] = [
    {
        step: 1,
        title: '업비트 로그인',
        description: '업비트 웹사이트에 로그인합니다.',
        imagePath: '/images/upbit-key/step1-login.png'
    },
    {
        step: 2,
        title: 'QR 코드 인증',
        description: '보안을 위해 QR 코드로 2차 인증을 진행하고 로그인을 완료합니다.',
        imagePath: '/images/upbit-key/step2-qr.png'
    },
    {
        step: 3,
        title: 'API 키 발급 요청',
        description: 'Open API 관리 페이지에서 모든 항목 체크 후, IP 주소를 등록하고 Open API Key 발급을 요청합니다.',
        imagePath: '/images/upbit-key/step3-request-key.png'
    },
    {
        step: 4,
        title: 'API 키 확인',
        description: '발급된 Access Key와 Secret Key를 확인합니다.\n이 키들은 중요한 정보이므로 복사해서 안전하게 보관해주세요.\nSecret Key는 이 페이지를 벗어나면 다시 확인할 수 없습니다.',
        imagePath: '/images/upbit-key/step4-take-key.png'
    },
    {
        step: 5,
        title: '출금허용주소 등록',
        description: 'Open API를 통해 출금하기 위해서 출금허용주소를 반드시 등록해주세요.',
        imagePath: '/images/upbit-key/step5-address.png'
    }
]; 