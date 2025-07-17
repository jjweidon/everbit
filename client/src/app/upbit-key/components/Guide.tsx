import { GuideStep } from '../types';
import { GUIDE_STEPS } from '../constants';

const GuideStepCard = ({ step }: { step: GuideStep }) => (
    <div className="bg-white dark:bg-navy-800 rounded-lg overflow-hidden shadow-sm border border-navy-200 dark:border-navy-700 mb-8">
        <div className="p-6">
            <div className="flex items-center mb-4">
                <div className="w-10 h-10 flex items-center justify-center bg-navy-500 text-white rounded-lg font-bold text-lg font-kimm">
                    {step.step}
                </div>
                <h3 className="text-xl font-medium text-navy-900 dark:text-white ml-4">
                    {step.title}
                </h3>
            </div>
            <p className="text-navy-600 dark:text-navy-200 mb-6 whitespace-pre-line">
                {step.description}
            </p>
            <div className="rounded-lg overflow-hidden border border-navy-200 dark:border-navy-700">
                <img src={step.imagePath} alt={step.title} className="w-full h-auto" />
            </div>
        </div>
    </div>
);

export const UpbitKeyGuide = () => (
    <div className="mt-12">
        <h2 className="text-xl font-bold text-navy-900 dark:text-white mb-8">
            업비트 API 키 발급 가이드
        </h2>
        <div className="max-w-4xl mx-auto">
            {GUIDE_STEPS.map((step) => (
                <GuideStepCard key={step.step} step={step} />
            ))}
        </div>
    </div>
);
