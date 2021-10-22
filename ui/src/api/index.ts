import axios from "axios";

import { authHeader } from "@/helpers";
import { logout } from "./auth";

export const baseURL = process.env.VUE_APP_API_BASE_URL
  ? process.env.VUE_APP_API_BASE_URL
  : "http://localhost:8080";

const api = axios.create({
  baseURL,
  responseType: "json",
  headers: {
    ["X-Auth-Token"]: authHeader(),
  },
});

api.interceptors.response.use(
  (resp) => {
    return resp;
  },
  async (err) => {
    if (err.response.status === 401) {
      await logout();
      window.location.replace("/login");
    } else {
      return Promise.reject(err);
    }
  }
);

export default api;
