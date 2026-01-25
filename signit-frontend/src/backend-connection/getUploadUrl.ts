import { apiClient } from '../api/client';

interface UploadUrlResponse {
    uploadUrl: string;
    uploadSessionId: string;
    s3Key: string;
    expiresInSeconds: number;
}

export const getUploadUrl = async (filename: string): Promise<string> => {
    try {
        const encodedFilename = encodeURIComponent(filename);
        const response = await apiClient.get(`/files/newUploadURL/?filename=${encodedFilename}`);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data: UploadUrlResponse = await response.json();
        return data.uploadUrl;
    } catch (error) {
        console.error("Error fetching upload URL:", error);
        return "";
    }
}