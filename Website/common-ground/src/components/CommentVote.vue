<script setup>
import { ref, onMounted } from "vue";

const props = defineProps({
  commentId: Number,
});

const emit = defineEmits(["updated"]);

const vote = ref("NEUTRAL");

function getToken() {
  return localStorage.getItem("token");
}

async function loadVote() {
  const res = await fetch(
    `/api/votes/comment/${props.commentId}`,
    {
      headers: {
        Authorization: getToken(),
      },
    }
  );

  if (!res.ok) return;

  const data = await res.json();
  vote.value = data.voteType ?? "NEUTRAL";
}

async function sendVote(type) {
  const res = await fetch(
    `/api/votes`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: getToken(),
      },
      body: JSON.stringify({
        commentId: props.commentId,
        voteType: type,
      }),
    }
  );

  if (!res.ok) return;

  const data = await res.json();

  vote.value = data.voteType;

  // 🔥 IMPORTANT: no fake score, just tell parent to refresh
  emit("updated");
}

onMounted(loadVote);
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

    <!-- score removed here: it does NOT belong in this component -->

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
  font-size: 18px;
  cursor: pointer;
  opacity: 0.6;
  color: #aaa;
}

.arrow:hover {
  opacity: 1;
  color: #fff;
}

.arrow.active {
  opacity: 1;
}

.arrow.up.active {
  color: #4ade80;
}

.arrow.down.active {
  color: #f87171;
}
</style>