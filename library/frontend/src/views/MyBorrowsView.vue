<script setup>
import { onMounted, ref } from 'vue'
import { getMyBorrowRecords, returnBook } from '../api/library'

const records = ref([])
const message = ref('')
const loading = ref(false)
const busyInventoryId = ref(null)

function readValue(row, key) {
  const target = key.toLowerCase()
  const hit = Object.keys(row || {}).find((k) => k.toLowerCase() === target)
  return hit ? row[hit] : null
}

function formatTaiwanTime(value) {
  if (!value) {
    return '尚未歸還'
  }

  const text = String(value)
  return text.replace('T', ' ')
}

async function loadRecords() {
  loading.value = true
  message.value = ''

  try {
    records.value = await getMyBorrowRecords()
  } catch (error) {
    message.value = error.message
  } finally {
    loading.value = false
  }
}

async function returnInventory(inventoryId) {
  busyInventoryId.value = inventoryId
  message.value = ''

  try {
    await returnBook(inventoryId)
    message.value = `還書成功，館藏編號：${inventoryId}`
    await loadRecords()
  } catch (error) {
    message.value = error.message
  } finally {
    busyInventoryId.value = null
  }
}

onMounted(loadRecords)
</script>

<template>
  <section class="panel">
    <div class="heading-row">
      <h2>我的借閱紀錄</h2>
      <button class="ghost-btn" @click="loadRecords" :disabled="loading">
        {{ loading ? '更新中...' : '重新整理' }}
      </button>
    </div>

    <p v-if="message" class="feedback" :class="message.includes('成功') ? 'ok' : 'error'">{{ message }}</p>

    <div class="record-list">
      <article class="record-card" v-for="row in records" :key="readValue(row, 'BorrowingRecordId')">
        <div>
          <h3>{{ readValue(row, 'BookName') }}</h3>
          <p class="meta">ISBN {{ readValue(row, 'ISBN') }} • 館藏編號 #{{ readValue(row, 'InventoryId') }}</p>
          <p class="sub">借閱時間：{{ formatTaiwanTime(readValue(row, 'BorrowingTime')) }}</p>
          <p class="sub">歸還時間：{{ formatTaiwanTime(readValue(row, 'ReturnTime')) }}</p>
        </div>

        <button
          class="primary-btn"
          :disabled="Boolean(readValue(row, 'ReturnTime')) || busyInventoryId === readValue(row, 'InventoryId')"
          @click="returnInventory(readValue(row, 'InventoryId'))"
        >
          {{ busyInventoryId === readValue(row, 'InventoryId') ? '處理中...' : '還書' }}
        </button>
      </article>
    </div>
  </section>
</template>
