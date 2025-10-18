import { useNavigate } from "react-router";
import { useState } from "react";
import { registerUser } from "../backend-connection/registerUser";
import { loginUser } from "../backend-connection/loginUser";
import {
  Container,
  Paper,
  Title,
  Text,
  TextInput,
  PasswordInput,
  Button,
  Stack,
  Tabs,
  Box,
  Notification,
} from "@mantine/core";

function LoginScreen() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [registerUsername, setRegisterUsername] = useState("");
  const [registerPassword, setRegisterPassword] = useState("");
  const [loginLoading, setLoginLoading] = useState(false);
  const [registerLoading, setRegisterLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setRegisterLoading(true);
    try {
      const result = await registerUser(registerUsername, registerPassword);
      console.log("Registration result:", result);
      if (result) {
        setError(null);
        // Optionally auto-login or show success message
      } else {
        setError("Registration failed. Username may already exist.");
      }
    } finally {
      setRegisterLoading(false);
    }
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoginLoading(true);
    try {
      const result = await loginUser(username, password);
      console.log("Login result:", result);

      if (result) {
        navigate("/main");
      } else {
        setError("Invalid username or password.");
      }
    } finally {
      setLoginLoading(false);
    }
  };

  return (
    <Box
      style={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
      }}
    >
      <Container size={420} my={40}>
        <Paper radius="md" p="xl" withBorder shadow="xl">
          <Title order={1} ta="center" mb="xs">
            SignIT
          </Title>
          <Text c="dimmed" size="sm" ta="center" mb="lg">
            Digital Signature Verification Platform
          </Text>

          {error && (
            <Notification color="red" onClose={() => setError(null)} mb="md">
              {error}
            </Notification>
          )}

          <Tabs defaultValue="login">
            <Tabs.List grow>
              <Tabs.Tab value="login">Login</Tabs.Tab>
              <Tabs.Tab value="register">Register</Tabs.Tab>
            </Tabs.List>

            <Tabs.Panel value="login" pt="lg">
              <form onSubmit={handleLogin}>
                <Stack gap="md">
                  <TextInput
                    label="Username"
                    placeholder="Enter your username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                    size="md"
                  />
                  <PasswordInput
                    label="Password"
                    placeholder="Enter your password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    size="md"
                  />
                  <Button
                    type="submit"
                    fullWidth
                    size="md"
                    loading={loginLoading}
                    mt="md"
                  >
                    Login
                  </Button>
                </Stack>
              </form>
            </Tabs.Panel>

            <Tabs.Panel value="register" pt="lg">
              <form onSubmit={handleRegister}>
                <Stack gap="md">
                  <TextInput
                    label="Username"
                    placeholder="Choose a username"
                    value={registerUsername}
                    onChange={(e) => setRegisterUsername(e.target.value)}
                    required
                    size="md"
                  />
                  <PasswordInput
                    label="Password"
                    placeholder="Choose a password"
                    value={registerPassword}
                    onChange={(e) => setRegisterPassword(e.target.value)}
                    required
                    size="md"
                  />
                  <Button
                    type="submit"
                    fullWidth
                    size="md"
                    loading={registerLoading}
                    mt="md"
                  >
                    Register
                  </Button>
                </Stack>
              </form>
            </Tabs.Panel>
          </Tabs>
        </Paper>
      </Container>
    </Box>
  );
}

export default LoginScreen;