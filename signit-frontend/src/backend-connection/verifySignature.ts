import { apiClient } from '../api/client';
import type { VerifySignaturesResult } from '../types/signature';

export const verifySignature = async (filename: string): Promise<VerifySignaturesResult> => {
    try {
        const encodedFilename = encodeURIComponent(filename);
        const response = await apiClient.post(`/signatures?filename=${encodedFilename}`);

        if (!response.ok) {
            const errorData = await response.json() as VerifySignaturesResult;
            return errorData;
        }

        const result = await response.json() as VerifySignaturesResult;
        return result;
    } catch (error) {
        console.error("Error verifying signature:", error);

        // Return error result
        return {
            success: false,
            filename,
            valid: false,
            containerFormat: '',
            dataFileCount: 0,
            signatureCount: 0,
            signatures: [],
            containerErrors: [],
            containerWarnings: [],
            error: 'Failed to connect to signature verification service',
            errorType: 'NetworkError',
            timestamp: Date.now()
        };
    }
};
