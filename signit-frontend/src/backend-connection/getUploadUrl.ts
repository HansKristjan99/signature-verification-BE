export const getUploadUrl = async (_token: string, filename: string) : Promise<string> => {
    const url = `http://localhost:8080/files/newUploadURL/?filename=${filename}`
    try{ 
        const response = await fetch(url);
        const uploadUrl = await response.text();
        return uploadUrl;
    }
    catch(error){
        console.error("Error fetching upload URL:", error);
        return "";
    }
}