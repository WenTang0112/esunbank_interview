<script setup>
import { onMounted, reactive, ref } from 'vue'
import { borrowBook, getBooks, getInventoryByIsbn } from '../api/library'

const books = ref([])
const inventoryMap = reactive({})
const loadingBooks = ref(false)
const busyInventoryId = ref(null)
const message = ref('')

function readValue(row, key) {
  const target = key.toLowerCase()
  const hit = Object.keys(row || {}).find((k) => k.toLowerCase() === target)
  return hit ? row[hit] : null
}

function statusText(status) {
  const map = {
    AVAILABLE: '在庫',
    BORROWED: '出借中',
    PROCESSING: '整理中',
    LOST: '遺失',
    DAMAGED: '損毀',
    DISCARDED: '廢棄',
  }
  return map[status] || status
}

async function loadBooks() {
  loadingBooks.value = true
  message.value = ''

  try {
    books.value = await getBooks()
  } catch (error) {
    message.value = error.message
  } finally {
    loadingBooks.value = false
  }
}

async function toggleInventory(isbn) {
  const key = String(isbn)
  if (inventoryMap[key]) {
    delete inventoryMap[key]
    return
  }

  try {
    inventoryMap[key] = await getInventoryByIsbn(isbn)
  } catch (error) {
    message.value = error.message
  }
}

async function borrow(inventoryId) {
  busyInventoryId.value = inventoryId
  message.value = ''

  try {
    await borrowBook(inventoryId)
    message.value = `借書成功，館藏編號：${inventoryId}`
    await loadBooks()

    for (const isbn of Object.keys(inventoryMap)) {
      inventoryMap[isbn] = await getInventoryByIsbn(isbn)
    }
  } catch (error) {
    message.value = error.message
  } finally {
    busyInventoryId.value = null
  }
}

onMounted(loadBooks)
</script>

<template>
  <section class="panel">
    <div class="heading-row">
      <h2>館藏清單</h2>
      <button class="ghost-btn" @click="loadBooks" :disabled="loadingBooks">
        {{ loadingBooks ? '更新中...' : '重新整理' }}
      </button>
    </div>

    <p v-if="message" class="feedback" :class="message.includes('成功') ? 'ok' : 'error'">{{ message }}</p>

    <div v-if="loadingBooks" class="skeleton-list">
      <div class="skeleton" v-for="n in 3" :key="n" />
    </div>

    <div v-else class="book-grid">
      <article class="book-card" v-for="book in books" :key="readValue(book, 'ISBN')">
        <header>
          <h3>{{ readValue(book, 'Name') }}</h3>
          <p class="meta">{{ readValue(book, 'Author') }}</p>
        </header>

        <p class="intro">{{ readValue(book, 'Introduction') }}</p>

        <div class="stats">
          <span>ISBN: {{ readValue(book, 'ISBN') }}</span>
          <span>可借數量：{{ readValue(book, 'AvailableCount') }}</span>
          <span>館藏總數：{{ readValue(book, 'TotalCount') }}</span>
        </div>

        <button class="primary-btn" @click="toggleInventory(readValue(book, 'ISBN'))">查看館藏</button>

        <ul v-if="inventoryMap[String(readValue(book, 'ISBN'))]" class="inventory-list">
          <li v-for="item in inventoryMap[String(readValue(book, 'ISBN'))]" :key="readValue(item, 'InventoryId')">
            <div>
              <strong>#{{ readValue(item, 'InventoryId') }}</strong>
              <small>{{ statusText(readValue(item, 'Status')) }} • {{ readValue(item, 'LocationCode') || '未提供' }}</small>
            </div>

            <button
              class="ghost-btn"
              :disabled="readValue(item, 'Status') !== 'AVAILABLE' || busyInventoryId === readValue(item, 'InventoryId')"
              @click="borrow(readValue(item, 'InventoryId'))"
            >
              {{ busyInventoryId === readValue(item, 'InventoryId') ? '借閱中...' : '借閱' }}
            </button>
          </li>
        </ul>
      </article>
    </div>
  </section>
</template>
