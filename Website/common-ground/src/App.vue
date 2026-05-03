<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const isLoggedIn = ref(false);

function syncAuth() {
  isLoggedIn.value = !!localStorage.getItem("token");
}

function logout() {
  localStorage.removeItem("token");
  syncAuth();
  router.push("/");
}

onMounted(() => {
  syncAuth();

  // keeps login state synced across tabs/windows
  window.addEventListener("storage", syncAuth);
});
</script>

<template>
  <div class="app">
    <nav class="nav">
      <div class="left">
        <router-link to="/" class="logo">Common Ground</router-link>
      </div>

      <div class="right">
        <router-link to="/">Posts</router-link>

        <router-link v-if="isLoggedIn" to="/create">
          Create Post
        </router-link>

        <router-link v-if="!isLoggedIn" to="/login">
          Login
        </router-link>

        <button v-if="isLoggedIn" @click="logout">
          Logout
        </button>
      </div>
    </nav>

    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.app {
  font-family: Arial, sans-serif;
  background: #0f0f0f;
  color: #eee;
  min-height: 100vh;
}

.nav {
  display: flex;
  justify-content: space-between;
  padding: 1rem 2rem;
  background: #1a1a1a;
  border-bottom: 1px solid #333;
}

.nav a {
  margin-right: 1rem;
  text-decoration: none;
  color: #ccc;
}

.nav a:hover {
  color: white;
}

.logo {
  font-weight: bold;
  color: #4cafef;
}

button {
  background: #333;
  color: white;
  border: none;
  padding: 0.4rem 0.8rem;
  cursor: pointer;
}

button:hover {
  background: #555;
}

.content {
  padding: 2rem;
}
</style>