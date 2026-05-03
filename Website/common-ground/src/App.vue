<script setup>
import { useRouter } from "vue-router";
import { isLoggedIn, clearToken, syncAuth } from "./auth";
import { ref, onMounted } from "vue";

const router = useRouter();

// dummy reactive trigger so Vue actually re-renders on auth changes
const authTick = ref(0);

function logout() {
  clearToken();
  authTick.value++;
  router.push("/");
}

onMounted(() => {
  window.addEventListener("storage", () => {
    syncAuth();
    authTick.value++;
  });
});
</script>

<template>
  <div class="app" :key="authTick">
    <nav class="nav">
      <div class="left">
        <router-link to="/" class="logo">
          <img src="/common-ground-wide.svg" alt="Common Ground" />
        </router-link>
      </div>

      <div class="right">
        <router-link to="/">Posts</router-link>

        <router-link v-if="isLoggedIn()" to="/create">
          Create Post
        </router-link>

        <router-link v-if="!isLoggedIn()" to="/login">
          Login
        </router-link>

        <router-link v-if="!isLoggedIn()" to="/register">
          Create User
        </router-link>

        <button v-if="isLoggedIn()" @click="logout">
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
  background: #0f0f0f;
  color: #eee;
  min-height: 100vh;
}

.nav {
  display: flex;
  justify-content: space-between;
  padding: 0.1rem 2rem;
  background: #1a1a1a;
  border-bottom: 1px solid #333;
}

.nav a, nav button {
  margin-right: 1rem;
  text-decoration: none;
  color: #ccc;
  line-height: 1;
}

.nav a:hover {
  color: white;
}

.logo img {
  height: 64px;
  width: auto;
  display: block;
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