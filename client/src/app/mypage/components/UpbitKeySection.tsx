import { useEffect, useState } from 'react';
import { UserResponse, UpbitApiKeysResponse } from '@/api/types';
import { userApi } from '@/api/services';
import { LAYOUT, UI } from '../constants';

interface UpbitKeySectionProps {
    user: UserResponse | null;
    onUpdate: (accessKey: string, secretKey: string) => Promise<void>;
}

export function UpbitKeySection({ user, onUpdate }: UpbitKeySectionProps) {
    const [isEditing, setIsEditing] = useState(false);
    const [accessKey, setAccessKey] = useState('');
    const [secretKey, setSecretKey] = useState('');

    useEffect(() => {
        if (isEditing && user) {
            const fetchKeys = async () => {
                try {
                    const keys: UpbitApiKeysResponse = await userApi.getUpbitApiKeys();
                    setAccessKey(keys.accessKey);
                    setSecretKey(keys.secretKey);
                } catch (error) {
                    console.error('API 키를 불러오는데 실패했습니다:', error);
                }
            };
            fetchKeys();
        }
    }, [isEditing, user]);

    const handleSubmit = async () => {
        if (!accessKey || !secretKey) return;
        await onUpdate(accessKey, secretKey);
        setIsEditing(false);
        setAccessKey('');
        setSecretKey('');
    };

    const isUpbitKeyRegistered = user?.isUpbitConnected;
    const connectionStatus = isUpbitKeyRegistered ? '연결됨' : '연결되지 않음';
    const connectionStatusColor = isUpbitKeyRegistered ? 'text-green-400' : 'text-red-400';

    return (
        <div className={LAYOUT.SECTION_SPACING}>
            <h3 className="text-lg font-semibold text-white mb-2">업비트 API 키</h3>
            {isEditing ? (
                <div className="space-y-4">
                    <div>
                        <input
                            type="text"
                            value={accessKey}
                            onChange={(e) => setAccessKey(e.target.value)}
                            className={`w-full ${UI.INPUT.BASE} mb-2`}
                            placeholder="Access Key"
                        />
                        <input
                            type="password"
                            value={secretKey}
                            onChange={(e) => setSecretKey(e.target.value)}
                            className={`w-full ${UI.INPUT.BASE}`}
                            placeholder="Secret Key"
                        />
                    </div>
                    <div className="flex gap-2">
                        <button
                            onClick={() => {
                                setIsEditing(false);
                                setAccessKey('');
                                setSecretKey('');
                            }}
                            className={UI.BUTTON.SECONDARY}
                        >
                            취소
                        </button>
                        <button
                            onClick={handleSubmit}
                            className={UI.BUTTON.PRIMARY}
                            disabled={!accessKey || !secretKey}
                        >
                            저장
                        </button>
                    </div>
                </div>
            ) : (
                <div className="flex justify-between items-center">
                    <p className={connectionStatusColor}>{connectionStatus}</p>
                    <button
                        onClick={() => setIsEditing(true)}
                        className={UI.BUTTON.LINK}
                    >
                        {isUpbitKeyRegistered ? '수정' : '연결하기'}
                    </button>
                </div>
            )}
        </div>
    );
} 