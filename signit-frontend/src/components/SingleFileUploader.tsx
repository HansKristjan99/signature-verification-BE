import { useState } from "react";
import { getUploadUrl } from "../backend-connection/getUploadUrl";
import {
  Paper,
  Title,
  Text,
  Button,
  Group,
  Stack,
  FileButton,
  Badge,
  Progress,
} from "@mantine/core";
import { IconUpload, IconFile, IconCheck } from "@tabler/icons-react";

function SingleFileUploader() {
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [uploadSuccess, setUploadSuccess] = useState(false);

  const handleFileChange = (selectedFile: File | null) => {
    setFile(selectedFile);
    setUploadSuccess(false);
  };

  const handleUpload = async () => {
    if (!file) return;

    setUploading(true);
    try {
      const uploadUrl = await getUploadUrl(file.name);
      await fetch(uploadUrl, {
        method: "PUT",
        body: file,
        headers: {
          "Content-Type": file.type || "application/octet-stream",
        },
      });

      setUploadSuccess(true);
      setTimeout(() => {
        setFile(null);
        setUploadSuccess(false);
      }, 2000);
    } catch (error) {
      console.error("Upload failed:", error);
    } finally {
      setUploading(false);
    }
  };

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + " " + sizes[i];
  };

  return (
    <Paper p="lg" radius="md" withBorder>
      <Stack gap="md">
        <div>
          <Title order={3} mb="xs">
            Upload Document
          </Title>
          <Text size="sm" c="dimmed">
            Upload documents for digital signature verification
          </Text>
        </div>

        <FileButton onChange={handleFileChange} accept="*">
          {(props) => (
            <Button
              {...props}
              leftSection={<IconUpload size={16} />}
              variant="light"
              fullWidth
              size="md"
            >
              {file ? "Change File" : "Select File"}
            </Button>
          )}
        </FileButton>

        {file && (
          <Paper p="md" withBorder bg="gray.0">
            <Group justify="space-between" align="flex-start">
              <Group>
                <IconFile size={32} color="var(--mantine-color-blue-6)" />
                <div>
                  <Text fw={500}>{file.name}</Text>
                  <Group gap="xs" mt={4}>
                    <Badge size="sm" variant="light">
                      {file.type || "Unknown type"}
                    </Badge>
                    <Badge size="sm" variant="light" color="gray">
                      {formatFileSize(file.size)}
                    </Badge>
                  </Group>
                </div>
              </Group>
            </Group>
          </Paper>
        )}

        {file && (
          <>
            {uploading && <Progress value={100} animated />}
            <Button
              onClick={handleUpload}
              loading={uploading}
              fullWidth
              size="md"
              leftSection={uploadSuccess ? <IconCheck size={16} /> : <IconUpload size={16} />}
              color={uploadSuccess ? "green" : "blue"}
            >
              {uploadSuccess ? "Upload Successful!" : "Upload File"}
            </Button>
          </>
        )}
      </Stack>
    </Paper>
  );
}

export default SingleFileUploader;