import axios from 'axios';

export interface MemberInfo {
    isUpbitConnected: boolean;
    // 다른 회원 정보 필드들...
}

export interface MemberResponse {
    success: boolean;
    data?: MemberInfo;
    error?: string;
}

export const getMemberInfo = async (): Promise<MemberResponse> => {
    try {
        const response = await axios.get('/api/members/me', {
            headers: {
                'Authorization': localStorage.getItem('Authorization') || ''
            }
        });
        return { success: true, data: response.data };
    } catch (error) {
        return { 
            success: false, 
            error: error instanceof Error ? error.message : '회원 정보를 가져오는데 실패했습니다.' 
        };
    }
}; 