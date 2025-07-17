import { useState } from 'react';
import { LAYOUT, UI } from '../constants';

interface InquirySectionProps {
    onSubmit: (content: string) => Promise<void>;
}

export function InquirySection({ onSubmit }: InquirySectionProps) {
    const [content, setContent] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async () => {
        if (!content.trim() || isSubmitting) return;
        setIsSubmitting(true);
        try {
            await onSubmit(content);
            setContent('');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className={LAYOUT.SECTION_SPACING}>
            <h3 className="text-lg font-semibold text-white mb-2">문의사항</h3>
            <textarea
                value={content}
                onChange={(e) => setContent(e.target.value)}
                className={`w-full ${UI.INPUT.BASE} ${LAYOUT.TEXTAREA_HEIGHT} mb-2`}
                placeholder="관리자에게 문의하실 내용을 작성해주세요."
                disabled={isSubmitting}
            />
            <button
                onClick={handleSubmit}
                className={`w-full ${UI.BUTTON.PRIMARY}`}
                disabled={!content.trim() || isSubmitting}
            >
                {isSubmitting ? '제출 중...' : '문의하기'}
            </button>
        </div>
    );
} 