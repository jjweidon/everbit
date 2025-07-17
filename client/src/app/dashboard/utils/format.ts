export const formatNumber = (num?: number) => {
    if (num === undefined) return '0';
    return num.toLocaleString();
};

export const formatPercent = (num?: number) => {
    if (num === undefined) return '0%';
    return `${num > 0 ? '+' : ''}${num.toFixed(2)}%`;
};
