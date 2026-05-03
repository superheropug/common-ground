import { ref } from "vue";

const token = ref(localStorage.getItem("token"));

export function setToken(newToken) {
  token.value = newToken;
  localStorage.setItem("token", newToken);
}

export function getToken() {
  return token.value;
}

export function clearToken() {
  token.value = null;
  localStorage.removeItem("token");
}

export function isLoggedIn() {
  return !!token.value;
}

// keeps things in sync if storage changes elsewhere
export function syncAuth() {
  token.value = localStorage.getItem("token");
}