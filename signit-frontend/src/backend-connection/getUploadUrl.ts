import { apiClient } from '../api/client';

export const getUploadUrl = async (filename: string) : Promise<string> => {
    try{
        const response = await apiClient.get(`/files/newUploadURL/?filename=${filename}`);
        const uploadUrl = await response.text();
        return uploadUrl;
    }
    catch(error){
        console.error("Error fetching upload URL:", error);
        return "";
    }
}