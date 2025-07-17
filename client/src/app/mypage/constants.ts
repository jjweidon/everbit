// Layout Constants
export const LAYOUT = {
    CONTAINER_MAX_WIDTH: 'max-w-3xl',
    SECTION_PADDING: 'px-4 sm:px-6 lg:px-8',
    SECTION_SPACING: 'mb-6',
    TEXTAREA_HEIGHT: 'h-32',
} as const;

// UI Constants
export const UI = {
    BUTTON: {
        PRIMARY: 'bg-blue-800 text-white px-4 py-2 rounded hover:bg-blue-900 disabled:bg-blue-900 disabled:cursor-not-allowed',
        SECONDARY: 'bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600',
        LINK: 'text-blue-400 hover:text-blue-300 text-sm',
    },
    INPUT: {
        BASE: 'bg-navy-600 text-white rounded px-3 py-2',
    },
} as const;
