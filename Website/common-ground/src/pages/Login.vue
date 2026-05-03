<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const username = ref("");
const password = ref("");
const error = ref(null);
const loading = ref(false);

const router = useRouter();

function setToken(token) {
  localStorage.setItem("token", token);
}

async function login() {
  error.value = null;

  if (!username.value || !password.value) {
    error.value = "Username and password required.";
    return;
  }

  loading.value = true;

  try {
    const res = await axios.post("/api/api/login", {
      username: username.value,
      password: password.value,
    });

    const token = res.data.token;

    if (!token) {
      error.value = "No token returned.";
      return;
    }

    setToken(token);

    // go back to main page after login
    router.push("/");
  } catch (err) {
    console.error(err);

    if (err.response?.status === 401) {
      error.value = "Invalid credentials.";
    } else {
      error.value = "Login failed.";
    }
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="login">
    <h1>Login</h1>

    <div v-if="error" class="error">
      {{ error }}
    </div>

    <input
      v-model="username"
      placeholder="Username"
      autocomplete="username"
    />

    <input
      v-model="password"
      type="password"
      placeholder="Password"
      autocomplete="current-password"
    />

    <button @click="login" :disabled="loading">
      {{ loading ? "Logging in..." : "Login" }}
    </button>
  </div>
</template>

<style scoped>
.login {
  max-width: 400px;
  margin: auto;
  padding: 2rem;
}

input {
  display: block;
  width: 100%;
  margin: 0.5rem 0;
  padding: 0.5rem;
  background: #1a1a1a;
  border: 1px solid #333;
  color: white;
}

button {
  margin-top: 1rem;
  padding: 0.5rem 1rem;
}

.error {
  color: red;
  margin-bottom: 1rem;
}
input:-webkit-autofill,
input:-webkit-autofill:hover,
input:-webkit-autofill:focus {
  -webkit-box-shadow: 0 0 0px 1000px #2a2a2a inset;
  -webkit-text-fill-color: #fff;
  transition: background-color 5000s ease-in-out 0s;
}
</style>