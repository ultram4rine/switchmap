import axios from "axios";

import { authHeader } from "@/helpers";
import { logout } from "./auth";

export const baseURL = process.env.VUE_APP_API_BASE_URL
  ? process.env.VUE_APP_API_BASE_URL + "/api/v2"
  : "http://localhost:8080/api/v2";

const api = axios.create({
  baseURL,
  responseType: "json",
});

api.defaults.headers.common["X-Auth-Token"] = authHeader();

export default api;
