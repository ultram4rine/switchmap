import api from ".";

import { authHeader } from "~/helpers";

export const login = async (
  username: string,
  password: string,
  rememberMe: boolean
): Promise<void> => {
  const resp = await api.post<{ token: string }>("/auth/login", {
    username,
    password,
    rememberMe,
  });
  if (resp.data.token) {
    api.defaults.headers.common["X-Auth-Token"] = resp.data.token;
    localStorage.setItem("token", resp.data.token);
  }
};

export const logout = async (): Promise<void> => {
  localStorage.removeItem("token");
  api.defaults.headers.common["X-Auth-Token"] = authHeader();
};
