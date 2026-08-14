import axios, { AxiosResponse } from 'axios';
import {
  Product, Category, Supplier, Transaction,
  DashboardStats, CategoryValueRow, SupplierValueRow,
  ProductRequest, CategoryRequest, SupplierRequest, TransactionRequest,
} from '../types';

const api = axios.create({ baseURL: '' });

// Response interceptor — auto-logout on 401
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

// Products
export const getProducts = (): Promise<AxiosResponse<Product[]>> => api.get('/api/products');
export const getProduct = (id: number): Promise<AxiosResponse<Product>> => api.get(`/api/products/${id}`);
export const getLowStockProducts = (): Promise<AxiosResponse<Product[]>> => api.get('/api/products/low-stock');
export const searchProducts = (q: string): Promise<AxiosResponse<Product[]>> =>
  api.get(`/api/products/search?q=${encodeURIComponent(q)}`);
export const createProduct = (data: ProductRequest): Promise<AxiosResponse<Product>> => api.post('/api/products', data);
export const updateProduct = (id: number, data: ProductRequest): Promise<AxiosResponse<Product>> =>
  api.put(`/api/products/${id}`, data);
export const deleteProduct = (id: number): Promise<AxiosResponse<void>> => api.delete(`/api/products/${id}`);

// Categories
export const getCategories = (): Promise<AxiosResponse<Category[]>> => api.get('/api/categories');
export const createCategory = (data: CategoryRequest): Promise<AxiosResponse<Category>> => api.post('/api/categories', data);
export const updateCategory = (id: number, data: CategoryRequest): Promise<AxiosResponse<Category>> =>
  api.put(`/api/categories/${id}`, data);
export const deleteCategory = (id: number): Promise<AxiosResponse<void>> => api.delete(`/api/categories/${id}`);

// Suppliers
export const getSuppliers = (): Promise<AxiosResponse<Supplier[]>> => api.get('/api/suppliers');
export const createSupplier = (data: SupplierRequest): Promise<AxiosResponse<Supplier>> => api.post('/api/suppliers', data);
export const updateSupplier = (id: number, data: SupplierRequest): Promise<AxiosResponse<Supplier>> =>
  api.put(`/api/suppliers/${id}`, data);
export const deleteSupplier = (id: number): Promise<AxiosResponse<void>> => api.delete(`/api/suppliers/${id}`);

// Transactions
export const getTransactions = (): Promise<AxiosResponse<Transaction[]>> => api.get('/api/transactions');
export const getRecentTransactions = (limit = 20): Promise<AxiosResponse<Transaction[]>> =>
  api.get(`/api/transactions/recent?limit=${limit}`);
export const getProductTransactions = (id: number): Promise<AxiosResponse<Transaction[]>> =>
  api.get(`/api/transactions/product/${id}`);
export const recordTransaction = (data: TransactionRequest): Promise<AxiosResponse<Transaction>> =>
  api.post('/api/transactions', data);

// Reports
export const getDashboardStats = (): Promise<AxiosResponse<DashboardStats>> => api.get('/api/reports/dashboard');
export const getInventoryByCategory = (): Promise<AxiosResponse<CategoryValueRow[]>> =>
  api.get('/api/reports/inventory-by-category');
export const getInventoryBySupplier = (): Promise<AxiosResponse<SupplierValueRow[]>> =>
  api.get('/api/reports/inventory-by-supplier');
export const getLowStockReport = (): Promise<AxiosResponse<Product[]>> => api.get('/api/reports/low-stock');
export const exportProductsCsv = (): Promise<AxiosResponse<Blob>> =>
  api.get('/api/reports/export/products.csv', { responseType: 'blob' });
export const exportLowStockCsv = (): Promise<AxiosResponse<Blob>> =>
  api.get('/api/reports/export/low-stock.csv', { responseType: 'blob' });

export default api;
