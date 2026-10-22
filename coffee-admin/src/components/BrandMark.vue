<template>
  <div class="brand-mark" :class="{ compact }">
    <div class="seal">
      <span>GX</span>
    </div>
    <div v-if="!compact" class="word">
      <strong>GXUFL</strong>
      <small>Coffee Atelier</small>
    </div>
  </div>
</template>

<script setup>
defineProps({
  compact: { type: Boolean, default: false }
})
</script>

<style scoped>
.brand-mark {
  display: flex;
  align-items: center;
  gap: 12px;
}

.seal {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: 1px solid rgba(200, 169, 126, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  flex-shrink: 0;
}

.seal::after {
  content: '';
  position: absolute;
  inset: 4px;
  border-radius: 50%;
  border: 1px solid rgba(200, 169, 126, 0.18);
}

.seal span {
  font-family: var(--serif, Georgia, serif);
  font-size: 15px;
  color: #c8a97e;
  letter-spacing: 0.04em;
}

.word {
  display: flex;
  flex-direction: column;
  line-height: 1;
}

.word strong {
  font-family: var(--serif, Georgia, serif);
  font-weight: 500;
  font-size: 20px;
  letter-spacing: 0.16em;
  color: #f3ede4;
}

.word small {
  font-size: 8px;
  letter-spacing: 0.3em;
  text-transform: uppercase;
  color: #9c9287;
  margin-top: 5px;
}

.compact .seal {
  width: 38px;
  height: 38px;
}
</style>
