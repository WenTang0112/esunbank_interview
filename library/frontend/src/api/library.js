import http from './http'

export async function getBooks() {
  const { data } = await http.get('/books')
  return data
}

export async function getInventoryByIsbn(isbn) {
  const { data } = await http.get(`/books/${isbn}/inventory`)
  return data
}

export async function borrowBook(inventoryId) {
  const { data } = await http.post('/borrow', { inventoryId })
  return data
}

export async function returnBook(inventoryId) {
  const { data } = await http.post('/return', { inventoryId })
  return data
}

export async function getMyBorrowRecords() {
  const { data } = await http.get('/borrow/records')
  return data
}
