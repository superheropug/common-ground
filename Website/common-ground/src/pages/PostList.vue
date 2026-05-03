<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const posts = ref([]);

function formatTime(ts) {
  return new Date(ts).toLocaleString(undefined, {
    year: "numeric",
    month: "short",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

async function loadPosts() {
  const res = await fetch("http://localhost:5000/api/posts");
  const data = await res.json();

  posts.value = data.content ?? data;
}

function openPost(id) {
  router.push(`/post/${id}`);
}

onMounted(loadPosts);
</script>

<template>
  <div class="list">
    <h1>Common Ground</h1>

    <div v-for="post in posts" :key="post.id" class="post" @click="openPost(post.id)">
      <div class="meta">
        {{ formatTime(post.postTime) }}
      </div>

      <p class="text">{{ post.text }}</p>

      <div class="categories">
        <span v-for="c in post.categories" :key="c.name" class="chip">
          {{ c.fancyName ?? c.name }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.list {
  padding: 1rem;
}

.post {
  padding: 1rem;
  margin-bottom: 1rem;
  border-radius: 10px;
  background: #1f1f1f;
  cursor: pointer;
}

.text {
  margin: 0.5rem 0;
}

.meta {
  font-size: 0.85rem;
  opacity: 0.7;
}

.categories {
  margin-top: 0.5rem;
}

.chip {
  display: inline-block;
  padding: 0.2rem 0.5rem;
  margin-right: 0.3rem;
  border-radius: 999px;
  background: #333;
  font-size: 0.75rem;
}
</style>