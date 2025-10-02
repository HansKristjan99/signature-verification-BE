export const loginUser = async (email: string, password: string): Promise<string| null> => {
  const response = await fetch("http://localhost:8080/users/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded", // matches @RequestParam style
    },
    body: new URLSearchParams({
      email: email,
      password: password
    })
  });

  const text = await response.text();
  return response.ok ? text : null;
}
