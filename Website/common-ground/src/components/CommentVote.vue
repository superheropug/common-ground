<script setup>
import { ref, watch } from "vue";

const props = defineProps({
  commentId: Number,
  modelValue: Object, // { voteScore, userVote }
});

const emit = defineEmits(["refresh"]);

const vote = ref("NEUTRAL");

function getToken() {
  return localStorage.getItem("token");
}

// sync from parent
watch(
  () => props.modelValue,
  (val) => {
    if (val?.userVote) {
      vote.value = val.userVote;
    }
  },
  { immediate: true }
);

async function sendVote(type) {
  const res = await fetch(
    `http://localhost:5000/api/comments/${props.commentId}/vote`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: getToken(),
      },
      body: JSON.stringify({
        vote: type,
      }),
    }
  );

  if (res.status === 401) return;

  const data = await res.json();

  // simplest correct approach: refresh parent comments
  emit("refresh");
}
</script>

<template>
  <div class="vote">
    <button
      class="arrow up"
      :class="{ active: vote === 'POSITIVE' }"
      @click="sendVote('POSITIVE')"
    >
      ▲
    </button>

    <div class="score">
      {{ modelValue?.voteScore ?? 0 }}
    </div>

    <button
      class="arrow down"
      :class="{ active: vote === 'NEGATIVE' }"
      @click="sendVote('NEGATIVE')"
    >
      ▼
    </button>
  </div>
</template>

<style scoped>
.vote {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 0.5rem;
}

.arrow {
  background: transparent;
  border: none;
  color: black;
  font-size: 18px;
  cursor: pointer;
  opacity: 0.8;
}

.arrow:hover {
  opacity: 1;
}

.arrow.active {
  color: white;
}

.score {
  font-size: 0.85rem;
  margin: 0.2rem 0;
}
</style>