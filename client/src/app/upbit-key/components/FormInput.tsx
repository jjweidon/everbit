import { useState } from 'react';
import { FaEye, FaEyeSlash } from 'react-icons/fa';

interface FormInputProps {
    id: string;
    type: string;
    value: string;
    onChange: (value: string) => void;
    error: string;
    label: string;
    placeholder: string;
}

export const FormInput = ({
    id,
    type,
    value,
    onChange,
    error,
    label,
    placeholder,
}: FormInputProps) => {
    const [showPassword, setShowPassword] = useState(false);
    const isPassword = type === 'password';
    const inputType = isPassword ? (showPassword ? 'text' : 'password') : type;

    return (
        <div>
            <label
                htmlFor={id}
                className="block text-sm font-medium text-navy-700 dark:text-gray-300 mb-1"
            >
                {label}
            </label>
            <div className="relative">
                <input
                    id={id}
                    type={inputType}
                    value={value}
                    onChange={(e) => onChange(e.target.value)}
                    className={`w-full px-4 py-2 bg-white dark:bg-navy-700 text-navy-900 dark:text-white border dark:border-navy-600 rounded-lg focus:ring-2 focus:ring-navy-500 focus:border-navy-500 outline-none transition-colors ${
                        error ? 'border-red-500' : 'border-navy-300 dark:border-navy-600'
                    } ${isPassword ? 'pr-10' : ''}`}
                    placeholder={placeholder}
                />
                {isPassword && (
                    <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute right-2 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-300"
                    >
                        {showPassword ? (
                            <FaEyeSlash className="h-5 w-5" />
                        ) : (
                            <FaEye className="h-5 w-5" />
                        )}
                    </button>
                )}
            </div>
            {error && <p className="mt-1 text-xs text-red-500">{error}</p>}
        </div>
    );
};
