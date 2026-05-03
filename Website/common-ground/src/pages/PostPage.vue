<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import CommentVote from "../components/CommentVote.vue";

const route = useRoute();
const router = useRouter();

const postId = route.params.id;

const post = ref(null);
const comments = ref([]);

const newComment = ref("");
const selectedCategories = ref([]);
const availableCategories = ref([]);

function getToken() {
  return localStorage.getItem("token");
}

function formatTime(ts) {
  return new Date(ts).toLocaleString(undefined, {
    year: "numeric",
    month: "short",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function handle401(res) {
  if (res.status === 401) {
    localStorage.removeItem("token");
    router.push("/login");
    return true;
  }
  return false;
}

async function loadPost() {
  const res = await fetch(`http://localhost:5000/api/posts/${postId}`);
  post.value = await res.json();
}

async function loadComments() {
  const res = await fetch(
    `http://localhost:5000/api/post/${postId}/comments`
  );
  comments.value = await res.json();
}

async function loadCategories() {
  const res = await fetch("http://localhost:5000/api/categories");
  availableCategories.value = await res.json();
}

async function submitComment() {
  if (!newComment.value.trim()) return;

  const res = await fetch("http://localhost:5000/api/comments", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: getToken(),
    },
    body: JSON.stringify({
      text: newComment.value,
      postId: postId,
      categories: selectedCategories.value,
    }),
  });

  if (handle401(res)) return;

  newComment.value = "";
  selectedCategories.value = [];
  await loadComments();
}

onMounted(async () => {
  await loadPost();
  await loadComments();
  await loadCategories();
});
</script>

<template>
  <div class="post-page" v-if="post">

    <!-- POST -->
    <h1>{{ post.text }}</h1>

    <div class="meta">
      {{ formatTime(post.postTime) }}
    </div>

    <div class="categories">
      <span v-for="c in post.categories" :key="c.name" class="chip">
        {{ c.fancyName || c.name }}
      </span>
    </div>

    <hr />

    <!-- COMMENTS -->
    <h2>Comments</h2>

    <div v-for="c in comments" :key="c.id" class="comment">

      <!-- VOTE COLUMN -->
      <CommentVote
        :commentId="c.id"
        :modelValue="c"
        @refresh="loadComments"
      />

      <!-- CONTENT -->
      <div class="comment-body">
        <div class="meta">
          {{ formatTime(c.postTime) }}
        </div>

        <p class="text">{{ c.content }}</p>

        <div class="categories">
          <span v-for="cat in c.categories" :key="cat.name" class="chip">
            {{ cat.fancyName || cat.name }}
          </span>
        </div>
      </div>
    </div>

    <!-- COMMENT FORM -->
    <div v-if="getToken()" class="form">
      <textarea
        v-model="newComment"
        placeholder="Write a comment..."
      />

      <div class="category-select">
        <label v-for="c in availableCategories" :key="c.name">
          <input
            type="checkbox"
            :value="c.name"
            v-model="selectedCategories"
          />
          {{ c.fancyName || c.name }}
        </label>
      </div>

      <button
        :disabled="!newComment.trim()"
        @click="submitComment"
      >
        Post Comment
      </button>
    </div>

    <div v-else class="login-hint">
      Login to comment.
    </div>

  </div>
</template>

<style scoped>
.post-page {
  padding: 1rem;
}

.meta {
  opacity: 0.7;
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
}

.categories {
  margin: 0.5rem 0;
}

.chip {
  display: inline-block;
  padding: 0.2rem 0.5rem;
  margin-right: 0.3rem;
  border-radius: 999px;
  background: #333;
  font-size: 0.75rem;
}

.comment {
  display: flex;
  gap: 0.75rem;
  padding: 0.75rem;
  margin: 0.5rem 0;
  background: #1f1f1f;
  border-radius: 10px;
}

.comment-body {
  flex: 1;
}

.text {
  margin: 0.3rem 0;
}

.form {
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

textarea {
  min-height: 90px;
  padding: 0.5rem;
  border-radius: 8px;
  border: 1px solid #333;
  background: #111;
  color: white;
  resize: vertical;
}

button {
  background: #333;
  color: white;
  border: none;
  padding: 0.5rem;
  cursor: pointer;
  border-radius: 6px;
}

button:hover {
  background: #555;
}

button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.category-select {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.login-hint {
  margin-top: 1rem;
  opacity: 0.7;
}
</style>