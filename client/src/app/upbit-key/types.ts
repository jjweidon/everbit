export type ValidationErrors = {
    accessKey: string;
    secretKey: string;
};

export type GuideStep = {
    step: number;
    title: string;
    description: string;
    imagePath: string;
};
