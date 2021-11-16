import axios from "axios";

import { authHeader } from "@/helpers";

export const baseURL = process.env.VUE_APP_API_BASE_URL
  ? process.env.VUE_APP_API_BASE_URL + "/api/v2"
  : "/api/v2";

const wsBaseURL = process.env.VUE_APP_WS_BASE_URL
  ? process.env.VUE_APP_WS_BASE_URL + "/api/v2"
  : "/api/v2";

export const wsURL = (shortName: string, floor: number): string => {
  return `${wsBaseURL}/builds/${shortName}/floors/${floor}/ws`;
};

const api = axios.create({
  baseURL,
  responseType: "json",
});

api.defaults.headers.common["X-Auth-Token"] = authHeader();

export default api;
