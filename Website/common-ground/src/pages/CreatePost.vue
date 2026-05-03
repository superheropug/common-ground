<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();

const text = ref("");
const selectedCategories = ref([]);
const availableCategories = ref([]);
const error = ref(null);
const loading = ref(false);

// get token
function getToken() {
  return localStorage.getItem("token");
}

// fetch categories from backend
async function loadCategories() {
  try {
    const res = await axios.get("http://localhost:5000/api/categories");
    availableCategories.value = res.data;
  } catch (err) {
    console.error("Failed to load categories", err);
  }
}

function toggleCategory(name) {
  if (selectedCategories.value.includes(name)) {
    selectedCategories.value = selectedCategories.value.filter(c => c !== name);
  } else {
    selectedCategories.value.push(name);
  }
}

async function submit() {
  error.value = null;

  if (!text.value.trim()) {
    error.value = "Post text is required.";
    return;
  }

  loading.value = true;

  try {
    const token = getToken();

    if (!token) {
      error.value = "You must be logged in.";
      return;
    }

    await axios.post(
      "http://localhost:5000/api/posts",
      {
        text: text.value,
        categories: selectedCategories.value,
      },
      {
        headers: {
          Authorization: token, // raw token (your backend expects this)
        },
      }
    );

    // redirect to home after success
    router.push("/");
  } catch (err) {
    console.error(err);

    if (err.response?.status === 400) {
      error.value = err.response.data;
    } else if (err.response?.status === 401) {
      error.value = "Session expired. Please log in again.";
    } else {
      error.value = "Failed to create post.";
    }
  } finally {
    loading.value = false;
  }
}

onMounted(loadCategories);
</script>

<template>
  <div class="create-post">
    <h1>Create Post</h1>

    <div v-if="error" class="error">
      {{ error }}
    </div>

    <!-- TEXT INPUT -->
    <textarea
      v-model="text"
      placeholder="Ask your question..."
      rows="5"
    />

    <!-- CATEGORY SELECTOR -->
    <div class="categories">
      <h3>Categories</h3>

      <div
        v-for="cat in availableCategories"
        :key="cat.name"
        class="category"
        :class="{ selected: selectedCategories.includes(cat.name) }"
        @click="toggleCategory(cat.name)"
      >
        {{ cat.fancyName || cat.name }}
      </div>
    </div>

    <!-- SUBMIT -->
    <button @click="submit" :disabled="loading">
      {{ loading ? "Posting..." : "Post" }}
    </button>
  </div>
</template>

<style scoped>
.create-post {
  max-width: 600px;
}

textarea {
  width: 100%;
  margin-bottom: 1rem;
  padding: 0.5rem;
  background: #1a1a1a;
  color: white;
  border: 1px solid #333;
}

.categories {
  margin-bottom: 1rem;
}

.category {
  display: inline-block;
  padding: 0.4rem 0.6rem;
  margin: 0.2rem;
  background: #333;
  cursor: pointer;
  border-radius: 5px;
}

.category.selected {
  background: #4cafef;
}

button {
  padding: 0.5rem 1rem;
}

.error {
  color: red;
  margin-bottom: 1rem;
}
</style>