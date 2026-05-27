import axios from 'axios';

const api = axios.create({ baseURL: '' });

// Response interceptor — auto-logout on 401
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

// Products
export const getProducts = () => api.get('/api/products');
export const getProduct = id => api.get(`/api/products/${id}`);
export const getLowStockProducts = () => api.get('/api/products/low-stock');
export const searchProducts = q => api.get(`/api/products/search?q=${encodeURIComponent(q)}`);
export const createProduct = data => api.post('/api/products', data);
export const updateProduct = (id, data) => api.put(`/api/products/${id}`, data);
export const deleteProduct = id => api.delete(`/api/products/${id}`);

// Categories
export const getCategories = () => api.get('/api/categories');
export const createCategory = data => api.post('/api/categories', data);
export const updateCategory = (id, data) => api.put(`/api/categories/${id}`, data);
export const deleteCategory = id => api.delete(`/api/categories/${id}`);

// Suppliers
export const getSuppliers = () => api.get('/api/suppliers');
export const createSupplier = data => api.post('/api/suppliers', data);
export const updateSupplier = (id, data) => api.put(`/api/suppliers/${id}`, data);
export const deleteSupplier = id => api.delete(`/api/suppliers/${id}`);

// Transactions
export const getTransactions = () => api.get('/api/transactions');
export const getRecentTransactions = (limit = 20) => api.get(`/api/transactions/recent?limit=${limit}`);
export const getProductTransactions = id => api.get(`/api/transactions/product/${id}`);
export const recordTransaction = data => api.post('/api/transactions', data);

// Reports
export const getDashboardStats = () => api.get('/api/reports/dashboard');
export const getInventoryByCategory = () => api.get('/api/reports/inventory-by-category');
export const getInventoryBySupplier = () => api.get('/api/reports/inventory-by-supplier');
export const getLowStockReport = () => api.get('/api/reports/low-stock');
export const exportProductsCsv = () => api.get('/api/reports/export/products.csv', { responseType: 'blob' });
export const exportLowStockCsv = () => api.get('/api/reports/export/low-stock.csv', { responseType: 'blob' });

export default api;
