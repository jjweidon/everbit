'use client';

import React, { useState } from 'react';
import {
    Box,
    Button,
    FormControl,
    FormLabel,
    Input,
    VStack,
    Text,
    useToast,
} from '@chakra-ui/react';

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
        <Box
            p={6}
            maxW="500px"
            mx="auto"
            mt={8}
            borderWidth="1px"
            borderRadius="lg"
            boxShadow="lg"
        >
            <Text fontSize="xl" fontWeight="bold" mb={4}>
                업비트 API 키 등록
            </Text>
            <Text color="gray.600" mb={6}>
                업비트에서 발급받은 API 키를 입력해주세요.
            </Text>
            <form onSubmit={handleSubmit}>
                <VStack spacing={4}>
                    <FormControl isRequired>
                        <FormLabel>Access Key</FormLabel>
                        <Input
                            value={accessKey}
                            onChange={(e) => setAccessKey(e.target.value)}
                            placeholder="Access Key를 입력하세요"
                        />
                    </FormControl>
                    <FormControl isRequired>
                        <FormLabel>Secret Key</FormLabel>
                        <Input
                            type="password"
                            value={secretKey}
                            onChange={(e) => setSecretKey(e.target.value)}
                            placeholder="Secret Key를 입력하세요"
                        />
                    </FormControl>
                    <Button
                        type="submit"
                        colorScheme="blue"
                        width="full"
                        mt={4}
                    >
                        API 키 등록
                    </Button>
                </VStack>
            </form>
        </Box>
    );
};

export default UpbitApiKeyForm; 