'use client';

import MainHeader from '@/components/MainHeader';
import { userApi, supportApi } from '@/api/services';
import { EmailRequest, UpbitApiKeysRequest, InquiryRequest } from '@/api/types';
import { useAuthStore } from '@/store/authStore';
import { useRouter } from 'next/navigation';
import { LAYOUT } from './constants';
import { EmailSection, UpbitKeySection, InquirySection, AccountManagementSection } from './components';

export default function MyPage() {
    const router = useRouter();
    const { user, logout } = useAuthStore();

    const handleEmailUpdate = async (email: string) => {
        try {
            const request: EmailRequest = { email };
            await userApi.updateEmail(request);
            await useAuthStore.getState().fetchUser();
            alert('이메일 수정에 성공했습니다.');
        } catch (error) {
            console.error('이메일 수정 실패:', error);
            alert('이메일 수정에 실패했습니다.');
            throw error;
        }
    };

    const handleUpbitKeysUpdate = async (accessKey: string, secretKey: string) => {
        try {
            const request: UpbitApiKeysRequest = { accessKey, secretKey };
            await userApi.registerUpbitApiKeys(request);
            await useAuthStore.getState().fetchUser();
            alert('업비트 API 키 수정에 성공했습니다.');
        } catch (error) {
            console.error('업비트 API 키 수정 실패:', error);
            alert('업비트 API 키 수정에 실패했습니다.');
            throw error;
        }
    };

    const handleInquirySubmit = async (content: string) => {
        try {
            const request: InquiryRequest = { content };
            await supportApi.submitInquiry(request);
            alert('문의사항이 성공적으로 제출되었습니다.');
        } catch (error) {
            console.error('문의사항 제출 실패:', error);
            alert('문의사항 제출에 실패했습니다.');
            throw error;
        }
    };

    const handleLogout = () => {
        logout();
        router.push('/');
    };

    const handleDeleteAccount = async () => {
        if (window.confirm('정말로 회원탈퇴를 하시겠습니까?')) {
            try {
                await userApi.deleteUser();
                logout();
                router.push('/');
            } catch (error) {
                console.error('회원탈퇴 실패:', error);
                alert('회원탈퇴에 실패했습니다.');
            }
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-navy-50 to-white dark:from-darkBg dark:to-darkBg">
            <MainHeader title="everbit" />
            <div className={`${LAYOUT.CONTAINER_MAX_WIDTH} mx-auto ${LAYOUT.SECTION_PADDING} py-8`}>
                <div className="bg-navy-700 rounded-lg shadow-lg p-6 mb-8">
                    <h2 className="text-2xl font-bold text-white mb-6">My Page</h2>
                    
                    <EmailSection user={user} onUpdate={handleEmailUpdate} />
                    <UpbitKeySection user={user} onUpdate={handleUpbitKeysUpdate} />
                    <InquirySection onSubmit={handleInquirySubmit} />
                    <AccountManagementSection onLogout={handleLogout} onDelete={handleDeleteAccount} />
                </div>
            </div>
        </div>
    );
}
