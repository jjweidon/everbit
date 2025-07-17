import { useState } from 'react';
import { UserResponse } from '@/api/types';
import { LAYOUT, UI } from '../constants';

interface EmailSectionProps {
    user: UserResponse | null;
    onUpdate: (email: string) => Promise<void>;
}

export function EmailSection({ user, onUpdate }: EmailSectionProps) {
    const [isEditing, setIsEditing] = useState(false);
    const [newEmail, setNewEmail] = useState('');

    const handleSubmit = async () => {
        if (!newEmail) return;
        await onUpdate(newEmail);
        setIsEditing(false);
        setNewEmail('');
    };

    return (
        <div className={LAYOUT.SECTION_SPACING}>
            <h3 className="text-lg font-semibold text-white mb-2">이메일</h3>
            {isEditing ? (
                <div className="flex gap-2">
                    <input
                        type="email"
                        value={newEmail}
                        onChange={(e) => setNewEmail(e.target.value)}
                        className={`flex-1 ${UI.INPUT.BASE}`}
                        placeholder="새 이메일 주소"
                    />
                    <button
                        onClick={() => {
                            setIsEditing(false);
                            setNewEmail('');
                        }}
                        className={UI.BUTTON.SECONDARY}
                    >
                        취소
                    </button>
                    <button
                        onClick={handleSubmit}
                        className={UI.BUTTON.PRIMARY}
                        disabled={!newEmail}
                    >
                        저장
                    </button>
                </div>
            ) : (
                <div className="flex justify-between items-center">
                    <p className="text-gray-300">{user?.email}</p>
                    <button
                        onClick={() => {
                            setIsEditing(true);
                            setNewEmail(user?.email || '');
                        }}
                        className={UI.BUTTON.LINK}
                    >
                        수정
                    </button>
                </div>
            )}
        </div>
    );
} 