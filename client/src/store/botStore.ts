import { create } from 'zustand';
import { userApi } from '@/api/services';
import { useAuthStore } from './authStore';

interface BotState {
    isActive: boolean;
    isLoading: boolean;
    fetchBotStatus: () => void;
    toggleBot: () => Promise<void>;
}

export const useBotStore = create<BotState>((set, get) => ({
    isActive: false,
    isLoading: false,

    fetchBotStatus: () => {
        const user = useAuthStore.getState().user;
        if (user) {
            set({ isActive: user.isBotActive || false });
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
            throw error;
        } finally {
            set({ isLoading: false });
        }
    },
})); 