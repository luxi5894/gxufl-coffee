import http from './http'

export const adminLogin = (data) => http.post('/auth/admin/login', data)
export const getMe = () => http.get('/auth/me')

export const getStats = () => http.get('/admin/stats')

export const getProducts = (params) => http.get('/admin/products', { params })
export const getProduct = (id) => http.get(`/admin/products/${id}`)
export const createProduct = (data) => http.post('/admin/products', data)
export const updateProduct = (id, data) => http.put(`/admin/products/${id}`, data)
export const deleteProduct = (id) => http.delete(`/admin/products/${id}`)
export const updateProductStatus = (id, status) =>
  http.put(`/admin/products/${id}/status`, { status })
export const updateProductRecommended = (id, status) =>
  http.put(`/admin/products/${id}/recommend`, { status })

export const getCategories = () => http.get('/admin/categories')
export const createCategory = (data) => http.post('/admin/categories', data)
export const updateCategory = (id, data) => http.put(`/admin/categories/${id}`, data)
export const deleteCategory = (id) => http.delete(`/admin/categories/${id}`)

export const uploadImage = (file) => {
  const form = new FormData()
  form.append('file', file)
  return http.post('/admin/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const getOrders = (params) => http.get('/admin/orders', { params })
export const getOrder = (id) => http.get(`/admin/orders/${id}`)
export const updateOrderStatus = (id, status) =>
  http.put(`/admin/orders/${id}/status`, { status })

export const getUsers = (params) => http.get('/admin/users', { params })
export const updateUserStatus = (id, status) =>
  http.put(`/admin/users/${id}/status`, { status })

export const getReviews = (params) => http.get('/admin/reviews', { params })
export const updateReviewStatus = (id, status) =>
  http.put(`/admin/reviews/${id}/status`, { status })
export const deleteReview = (id) => http.delete(`/admin/reviews/${id}`)
