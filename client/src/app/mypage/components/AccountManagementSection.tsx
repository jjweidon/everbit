interface AccountManagementSectionProps {
    onLogout: () => void;
    onDelete: () => Promise<void>;
}

export function AccountManagementSection({ onLogout, onDelete }: AccountManagementSectionProps) {
    return (
        <div className="border-t border-navy-600 pt-6">
            <div className="flex justify-end gap-4 text-sm">
                <button
                    onClick={onLogout}
                    className="text-gray-400 hover:text-gray-300 transition-colors"
                >
                    로그아웃
                </button>
                <span className="text-gray-600">|</span>
                <button
                    onClick={onDelete}
                    className="text-red-400 hover:text-red-300 transition-colors"
                >
                    회원탈퇴
                </button>
            </div>
        </div>
    );
} 