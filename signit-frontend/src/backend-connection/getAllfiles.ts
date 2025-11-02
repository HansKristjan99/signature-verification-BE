import { apiClient } from '../api/client';

export const getAllFiles = async () : Promise<string[]> => {
    try{
        const response = await apiClient.get('/files/');
        console.log("Response status:", response.status);

        const fileNames = await response.json() as  string[];
        return fileNames;
    }
    catch(error){
        console.error("Error fetching files:", error);
        return [];
    }
}