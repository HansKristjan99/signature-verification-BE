import { useNavigate } from "react-router";
import { useState } from "react";
import { registerUser } from "../backend-connection/registerUser";
import { loginUser } from "../backend-connection/loginUser";

function LoginScreen() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
    const [registerUsername, setRegisterUsername] = useState("");
  const [registerPassword, setRegisterPassword] = useState("");
  const navigate = useNavigate();


  const handleRegister= async (e: React.FormEvent) => {
    e.preventDefault();
    const result = await registerUser(registerUsername, registerPassword);
    console.log("Registration result:", result);
    if (result){
      // navigate("/main");
    } else {
      alert("Bad registration");
    }
    };
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    const result = await loginUser(username, password);
    console.log("Login result:", result);

    if (result){
      navigate("/main");
    } else {
      alert("Bad login");
    }
  };

  return (
    <><div style={{ display: "flex", justifyContent: "center", marginTop: "100px" }}>
      <form onSubmit={handleLogin} style={{ display: "flex", flexDirection: "column", width: "250px", gap: "10px" }}>
        <h2>Login</h2>
        <input
          type="text"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required />
        <button type="submit">Login</button>
      </form>
    </div><div>

        <form onSubmit={handleRegister} style={{ display: "flex", flexDirection: "column", width: "250px", gap: "10px" }}>
          <h2>Register</h2>
          <input
            type="text"
            placeholder="Username"
            value={registerUsername}
            onChange={(e) => setRegisterUsername(e.target.value)}
            required />
          <input
            type="password"
            placeholder="Password"
            value={registerPassword}
            onChange={(e) => setRegisterPassword(e.target.value)}
            required />
          <button type="submit">Login</button>
        </form>
      </div></>
  );
}



export default LoginScreen;