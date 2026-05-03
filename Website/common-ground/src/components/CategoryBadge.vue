<template>
  <span class="badge">
    {{ label }}
  </span>
</template>

<script setup>
defineProps({
  label: String
});
</script>

<style scoped>
.badge {
  display: inline-block;
  padding: 0.2rem 0.5rem;
  margin-right: 0.3rem;
  margin-top: 0.2rem;

  background: #2c2c2c;
  border: 1px solid #444;
  border-radius: 999px;

  font-size: 0.75rem;
  color: #ddd;
}
</style>