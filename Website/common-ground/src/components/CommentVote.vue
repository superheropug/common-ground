<script setup>
import { ref, onMounted } from "vue";

const props = defineProps({
  commentId: Number,
});

const vote = ref("NEUTRAL");

function getToken() {
  return localStorage.getItem("token");
}

async function loadVote() {
  const res = await fetch(
    `http://localhost:5000/api/votes/comment/${props.commentId}`,
    {
      headers: {
        Authorization: getToken(),
      },
    }
  );

  const data = await res.json();
  vote.value = data.voteType;
}

async function sendVote(type) {
  const res = await fetch("http://localhost:5000/api/votes", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: getToken(),
    },
    body: JSON.stringify({
      commentId: props.commentId,
      voteType: type,
    }),
  });

  const data = await res.json();
  vote.value = data.voteType;
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
    <div>{{ c.voteScore }}</div>
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
</style>