<template>
  <div class="create-user">
    <h2>Create Account</h2>

    <form @submit.prevent="register">
      <input v-model="username" placeholder="username" />
      <input v-model="password" type="password" placeholder="password" />

      <button type="submit">Create Account</button>
    </form>

    <p v-if="message">{{ message }}</p>
  </div>
</template>

<script setup>
import { ref } from "vue";

const username = ref("");
const password = ref("");
const message = ref("");

async function register() {
  message.value = "";

  try {
    const res = await fetch("http://localhost:5000/api/users", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        username: username.value,
        password: password.value,
      }),
    });

    const data = await res.json();

    if (!res.ok) {
      message.value = data.error || "Failed to create user";
      return;
    }

    message.value = `User created: ${data.username}`;
    username.value = "";
    password.value = "";
  } catch (err) {
    message.value = "Server error";
  }
}
</script>

<style scoped>
.create-user {
  max-width: 400px;
  margin: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
input {
  padding: 8px;
}
button {
  padding: 8px;
  cursor: pointer;
  
}
input:-webkit-autofill,
input:-webkit-autofill:hover,
input:-webkit-autofill:focus {
  -webkit-box-shadow: 0 0 0px 1000px #2a2a2a inset;
  -webkit-text-fill-color: #fff;
  transition: background-color 5000s ease-in-out 0s;
}
</style>