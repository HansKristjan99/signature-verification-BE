import { useState } from 'react'
import './App.css'
import { getAllFiles } from './backend-connection/getAllfiles'
import { createTheme, MantineProvider } from '@mantine/core';
import FileTable from './components/FileTable';
import SingleFileUploader from './components/SingleFileUploader';
import LoginScreen from './components/LoginScreen';
import {BrowserRouter, Routes, Route, Navigate } from "react-router";

const theme = createTheme({
  /** Put your mantine theme override here */
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
  return (
    <>
      <FileTable elements={fileList ? fileList : []}/>
      <button onClick={async () => {
            const files = await getAllFiles("token");
            setfileList(files);         
          }
        }>
          Get all files
      </button>
      
      <SingleFileUploader/>

    </>
  )
}

export default App
