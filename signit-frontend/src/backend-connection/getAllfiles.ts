export const getAllFiles = async (_token: string) : Promise<string[]> => {
    const url = "http://localhost:8080/files/"
    try{ 
        const response = await fetch(url);
        console.log("Response status:", response.status);

        const fileNames = await response.json() as  string[];
        return fileNames;
    }
    catch(error){
        console.error("Error fetching files:", error);
        return [];
    }
}