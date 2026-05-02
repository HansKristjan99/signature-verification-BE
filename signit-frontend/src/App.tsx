import { useState } from 'react'
import { getAllFiles } from './backend-connection/getAllfiles'
import { verifySignature } from './backend-connection/verifySignature'
import { signFile } from './backend-connection/sign_file'
import { createTheme, MantineProvider, AppShell, Container, Title, Button, Stack, Group } from '@mantine/core';
import FileTable from './components/FileTable';
import SingleFileUploader from './components/SingleFileUploader';
import SignaturesList from './components/SignaturesList';
import LoginScreen from './components/LoginScreen';
import { BrowserRouter, Routes, Route, Navigate, useNavigate } from "react-router";
import { useAuth } from './hooks/useAuth';
import type { VerifySignaturesResult } from './types/signature';

const theme = createTheme({
  primaryColor: 'blue',
  fontFamily: 'system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", sans-serif',
  headings: {
    fontFamily: 'system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", sans-serif',
    fontWeight: '700',
  },
  colors: {
    blue: ['#e7f5ff', '#d0ebff', '#a5d8ff', '#74c0fc', '#4dabf7', '#339af0', '#228be6', '#1c7ed6', '#1971c2', '#1864ab'],
  },
});

function App() {
  return (
    <MantineProvider theme={theme}>
      <BrowserRouter>
      <Routes>
        <Route path="/" element={<LoginScreen />} />
        <Route path="/main" element={<Main />} />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </BrowserRouter>
    </MantineProvider>
  );
}

function Main() {
  const [fileList, setfileList] = useState<string[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [signatureData, setSignatureData] = useState<VerifySignaturesResult | null>(null);
  const [verifyingFile, setVerifyingFile] = useState<string | null>(null);
  const [signingFile, setSigningFile] = useState<string | null>(null);
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleGetFiles = async () => {
    setLoading(true);
    try {
      const files = await getAllFiles();
      setfileList(files);
    } finally {
      setLoading(false);
    }
  };

  const handleVerifySignature = async (filename: string) => {
    setVerifyingFile(filename);
    try {
      const result = await verifySignature(filename);
      setSignatureData(result);
    } catch (error) {
      console.error('Error verifying signature:', error);
    } finally {
      setVerifyingFile(null);
    }
  };

  const handleSignFile = async (filename: string) => {
    setSigningFile(filename);
    try {
      await signFile(filename);
      await handleGetFiles();
    } catch (error) {
      console.error('Error signing file:', error);
    } finally {
      setSigningFile(null);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <AppShell
      header={{ height: 60 }}
      padding="md"
    >
      <AppShell.Header>
        <Container size="xl" h="100%" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Title order={2}>SignIT - Digital Signature Verification</Title>
          <Button variant="subtle" onClick={handleLogout}>
            Logout
          </Button>
        </Container>
      </AppShell.Header>

      <AppShell.Main>
        <Container size="xl">
          <Stack gap="xl">
            <SingleFileUploader />

            <div>
              <Group justify="space-between" mb="md">
                <Title order={3}>Your Files</Title>
                <Button
                  onClick={handleGetFiles}
                  loading={loading}
                  variant="filled"
                >
                  Refresh Files
                </Button>
              </Group>
              <FileTable
                elements={fileList ? fileList : []}
                onVerifySignature={handleVerifySignature}
                verifyingFile={verifyingFile}
                onSignFile={handleSignFile}
                signingFile={signingFile}
              />
            </div>

            <SignaturesList
              signatureData={signatureData}
              loading={verifyingFile !== null}
            />
          </Stack>
        </Container>
      </AppShell.Main>
    </AppShell>
  )
}

export default App
