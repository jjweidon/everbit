import { create } from 'zustand';
import { userApi } from '@/api/services';

interface BotState {
    isActive: boolean;
    isLoading: boolean;
    fetchBotStatus: () => Promise<void>;
    toggleBot: () => Promise<void>;
}

export const useBotStore = create<BotState>((set, get) => ({
    isActive: false,
    isLoading: false,

    fetchBotStatus: async () => {
        try {
            const user = await userApi.getCurrentUser();
            set({ isActive: user.isBotActive || false });
        } catch (error) {
            console.error('봇 상태 로드 실패:', error);
        }
    },

    toggleBot: async () => {
        const { isLoading } = get();
        if (isLoading) return;

        try {
            set({ isLoading: true });
            await userApi.toggleBotActive();
            set(state => ({ isActive: !state.isActive }));
        } catch (error) {
            console.error('봇 상태 변경 실패:', error);
        } finally {
            set({ isLoading: false });
        }
    },
})); 