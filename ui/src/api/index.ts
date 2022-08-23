import axios from "axios";

import { authHeader } from "@/helpers";

export const baseURL = import.meta.env.VUE_APP_API_BASE_URL
  ? import.meta.env.VUE_APP_API_BASE_URL + "/api/v2"
  : "/api/v2";

const api = axios.create({
  baseURL,
  responseType: "json",
});

api.defaults.headers.common["X-Auth-Token"] = authHeader();

export default api;
