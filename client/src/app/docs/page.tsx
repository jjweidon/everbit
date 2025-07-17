'use client';

import Link from 'next/link';

export default function Docs() {
    return (
        <div className="min-h-screen bg-white">
            {/* Header */}
            <div className="bg-navy-500 text-white">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
                    <div className="flex justify-between items-center">
                        <h1 className="text-2xl font-bold">문서</h1>
                        <Link
                            href="/dashboard"
                            className="px-4 py-2 bg-white text-navy-700 rounded-md hover:bg-navy-50"
                        >
                            대시보드로 돌아가기
                        </Link>
                    </div>
                </div>
            </div>

            {/* Main Content */}
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="grid grid-cols-1 gap-8 lg:grid-cols-3">
                    {/* Sidebar */}
                    <div className="lg:col-span-1">
                        <nav className="space-y-1">
                            <Link
                                href="#getting-started"
                                className="block px-4 py-2 text-sm font-medium text-navy-700 hover:bg-navy-50 rounded-md"
                            >
                                시작하기
                            </Link>
                            <Link
                                href="#api-keys"
                                className="block px-4 py-2 text-sm font-medium text-navy-700 hover:bg-navy-50 rounded-md"
                            >
                                API 키 설정
                            </Link>
                            <Link
                                href="#trading-strategies"
                                className="block px-4 py-2 text-sm font-medium text-navy-700 hover:bg-navy-50 rounded-md"
                            >
                                트레이딩 전략
                            </Link>
                            <Link
                                href="#faq"
                                className="block px-4 py-2 text-sm font-medium text-navy-700 hover:bg-navy-50 rounded-md"
                            >
                                자주 묻는 질문
                            </Link>
                        </nav>
                    </div>

                    {/* Content */}
                    <div className="lg:col-span-2">
                        <div className="prose prose-navy max-w-none">
                            <h2 id="getting-started" className="text-2xl font-bold text-navy-900">
                                시작하기
                            </h2>
                            <p className="text-navy-600">
                                에버비트는 업비트 API를 활용한 비트코인 자동 트레이딩 시스템입니다.
                                이 문서를 통해 서비스 사용 방법을 알아보세요.
                            </p>

                            <h2 id="api-keys" className="text-2xl font-bold text-navy-900 mt-8">
                                API 키 설정
                            </h2>
                            <p className="text-navy-600">
                                업비트에서 발급받은 API 키를 등록하여 서비스를 이용할 수 있습니다.
                                API 키는 안전하게 보관되며, 읽기 전용 권한만 사용합니다.
                            </p>

                            <h2
                                id="trading-strategies"
                                className="text-2xl font-bold text-navy-900 mt-8"
                            >
                                트레이딩 전략
                            </h2>
                            <p className="text-navy-600">
                                다양한 트레이딩 전략을 제공하며, 사용자의 필요에 맞게 커스터마이징할
                                수 있습니다. RSI, MACD 등 다양한 기술적 지표를 활용합니다.
                            </p>

                            <h2 id="faq" className="text-2xl font-bold text-navy-900 mt-8">
                                자주 묻는 질문
                            </h2>
                            <div className="space-y-4">
                                <div>
                                    <h3 className="text-lg font-medium text-navy-900">
                                        수수료는 어떻게 되나요?
                                    </h3>
                                    <p className="text-navy-600">
                                        현재 베타 서비스 기간 동안 수수료가 면제됩니다.
                                    </p>
                                </div>
                                <div>
                                    <h3 className="text-lg font-medium text-navy-900">
                                        최소 투자 금액은 얼마인가요?
                                    </h3>
                                    <p className="text-navy-600">
                                        최소 10,000원부터 투자 가능합니다.
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
