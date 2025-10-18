import { useState } from 'react'
import { getAllFiles } from './backend-connection/getAllfiles'
import { createTheme, MantineProvider, AppShell, Container, Title, Button, Stack, Group, ActionIcon } from '@mantine/core';
import FileTable from './components/FileTable';
import SingleFileUploader from './components/SingleFileUploader';
import LoginScreen from './components/LoginScreen';
import {BrowserRouter, Routes, Route, Navigate } from "react-router";

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

  const handleGetFiles = async () => {
    setLoading(true);
    try {
      const files = await getAllFiles("token");
      setfileList(files);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AppShell
      header={{ height: 60 }}
      padding="md"
    >
      <AppShell.Header>
        <Container size="xl" h="100%" style={{ display: 'flex', alignItems: 'center' }}>
          <Title order={2}>SignIT - Digital Signature Verification</Title>
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
              <FileTable elements={fileList ? fileList : []} />
            </div>
          </Stack>
        </Container>
      </AppShell.Main>
    </AppShell>
  )
}

export default App
