// Shared domain types mirroring the backend API contract.

export type Role = 'ADMIN' | 'USER';
export type TransactionType = 'STOCK_IN' | 'STOCK_OUT' | 'ADJUSTMENT';

export interface Category {
  id: number;
  name: string;
  description?: string | null;
}

export interface Supplier {
  id: number;
  name: string;
  contactEmail?: string | null;
  phone?: string | null;
  address?: string | null;
  notes?: string | null;
}

export interface Product {
  id: number;
  name: string;
  sku: string;
  description?: string | null;
  price: number;
  quantity: number;
  reorderThreshold: number;
  category?: Category | null;
  supplier?: Supplier | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface TransactionProductSummary {
  id: number;
  name: string;
  sku: string;
}

export interface TransactionUserSummary {
  id: number;
  fullName: string;
}

export interface Transaction {
  id: number;
  type: TransactionType;
  quantity: number;
  quantityBefore: number;
  quantityAfter: number;
  reason?: string | null;
  notes?: string | null;
  createdAt: string;
  product?: TransactionProductSummary | null;
  performedBy?: TransactionUserSummary | null;
}

export interface DashboardStats {
  totalProducts: number;
  inStockCount: number;
  lowStockCount: number;
  outOfStockCount: number;
  totalInventoryValue: number;
  totalTransactions: number;
}

export interface CategoryValueRow {
  categoryName: string;
  totalValue: number;
}

export interface SupplierValueRow {
  supplierName: string;
  productCount: number;
  totalValue: number;
}

export interface AuthUser {
  id: number;
  fullName: string;
  email: string;
  role: Role;
}

// ── Request payloads ────────────────────────────────────────────────────────

export interface ProductRequest {
  name: string;
  sku: string;
  description?: string;
  price: number;
  quantity: number;
  reorderThreshold: number;
  categoryId: number | null;
  supplierId: number | null;
}

export interface CategoryRequest {
  name: string;
  description?: string;
}

export interface SupplierRequest {
  name: string;
  contactEmail?: string;
  phone?: string;
  address?: string;
  notes?: string;
}

export interface TransactionRequest {
  productId: number;
  type: TransactionType;
  quantity: number;
  reason?: string;
  notes?: string;
}

// Props shared by pages that can raise a toast notification.
export interface PageProps {
  onToast: (message: string) => void;
}
