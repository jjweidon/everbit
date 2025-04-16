'use client';

import React, { useState } from 'react';

interface UpbitApiKeyFormProps {
    onSubmit: (accessKey: string, secretKey: string) => void;
}

const UpbitApiKeyForm: React.FC<UpbitApiKeyFormProps> = ({ onSubmit }) => {
    const [accessKey, setAccessKey] = useState('');
    const [secretKey, setSecretKey] = useState('');

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        onSubmit(accessKey, secretKey);
    };

    return (
        <div className="p-6 max-w-[500px] mx-auto mt-8 border border-gray-200 rounded-lg shadow-lg">
            <h2 className="text-xl font-bold mb-4">
                업비트 API 키 등록
            </h2>
            <p className="text-gray-600 mb-6">
                업비트에서 발급받은 API 키를 입력해주세요.
            </p>
            <form onSubmit={handleSubmit}>
                <div className="space-y-4">
                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-gray-700">
                            Access Key
                        </label>
                        <input
                            type="text"
                            value={accessKey}
                            onChange={(e) => setAccessKey(e.target.value)}
                            placeholder="Access Key를 입력하세요"
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-navy-500"
                            required
                        />
                    </div>
                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-gray-700">
                            Secret Key
                        </label>
                        <input
                            type="password"
                            value={secretKey}
                            onChange={(e) => setSecretKey(e.target.value)}
                            placeholder="Secret Key를 입력하세요"
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-navy-500"
                            required
                        />
                    </div>
                    <button
                        type="submit"
                        className="w-full mt-4 px-4 py-2 bg-navy-500 text-white rounded-md hover:bg-navy-600 focus:outline-none focus:ring-2 focus:ring-navy-500 focus:ring-offset-2"
                    >
                        API 키 등록
                    </button>
                </div>
            </form>
        </div>
    );
};

export default UpbitApiKeyForm; 