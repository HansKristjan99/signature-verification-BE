export const registerUser = async (email: string, password: string): Promise<string| null> => {
  const response = await fetch("http://localhost:8080/users/register", {
  method: "POST",
  headers: {
    "Content-Type": "application/x-www-form-urlencoded",
  },
  body: new URLSearchParams({
    email,
    password
  })
});

  const text = await response.text();
  return response.ok ? text : null;
}
