import axios from "axios";
import { getToken } from "../auth";

export const api = axios.create({
  baseURL: "http://localhost:5000/api",
});

// attach JWT automatically
api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = token; // IMPORTANT: raw token, no "Bearer"
  }
  return config;
});