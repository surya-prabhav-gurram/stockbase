import React, { useState, useEffect } from 'react';
import { IconX } from './UI';
import { Product, Category, Supplier, ProductRequest } from '../types';

interface ProductForm {
  name: string;
  sku: string;
  description: string;
  price: string;
  quantity: string;
  reorderThreshold: string;
  categoryId: string;
  supplierId: string;
}

interface ProductModalProps {
  product: Product | null;
  categories: Category[];
  suppliers: Supplier[];
  onSave: (data: ProductRequest) => void;
  onClose: () => void;
  loading: boolean;
}

const empty: ProductForm = {
  name: '', sku: '', description: '', price: '', quantity: '',
  reorderThreshold: '10', categoryId: '', supplierId: '',
};

export default function ProductModal({ product, categories, suppliers, onSave, onClose, loading }: ProductModalProps) {
  const [form, setForm] = useState<ProductForm>(empty);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (product) {
      setForm({
        name: product.name || '',
        sku: product.sku || '',
        description: product.description || '',
        price: product.price != null ? String(product.price) : '',
        quantity: product.quantity != null ? String(product.quantity) : '',
        reorderThreshold: String(product.reorderThreshold ?? 10),
        categoryId: product.category?.id != null ? String(product.category.id) : '',
        supplierId: product.supplier?.id != null ? String(product.supplier.id) : '',
      });
    } else {
      setForm(empty);
    }
    setErrors({});
  }, [product]);

  const set = (k: keyof ProductForm, v: string) => setForm(f => ({ ...f, [k]: v }));

  const validate = (): boolean => {
    const e: Record<string, string> = {};
    if (!form.name.trim()) e.name = 'Required';
    if (!form.sku.trim()) e.sku = 'Required';
    if (form.price === '' || isNaN(Number(form.price)) || Number(form.price) < 0) e.price = 'Valid price required';
    if (form.quantity === '' || isNaN(Number(form.quantity)) || Number(form.quantity) < 0) e.quantity = 'Valid quantity required';
    setErrors(e);
    return !Object.keys(e).length;
  };

  const handleSubmit = () => {
    if (!validate()) return;
    onSave({
      name: form.name.trim(),
      sku: form.sku.trim().toUpperCase(),
      description: form.description.trim(),
      price: parseFloat(form.price),
      quantity: parseInt(form.quantity, 10),
      reorderThreshold: parseInt(form.reorderThreshold, 10) || 10,
      categoryId: form.categoryId ? Number(form.categoryId) : null,
      supplierId: form.supplierId ? Number(form.supplierId) : null,
    });
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <div className="modal-header">
          <h2>{product ? 'Edit Product' : 'Add New Product'}</h2>
          <button className="btn btn-icon" onClick={onClose}><IconX /></button>
        </div>
        <div className="modal-body">
          <div className="form-grid">
            <div className="form-group full">
              <label>Product Name *</label>
              <input className="form-control" value={form.name} onChange={e => set('name', e.target.value)} placeholder="e.g. Blue Pen Pack" />
              {errors.name && <span className="field-error">{errors.name}</span>}
            </div>
            <div className="form-group">
              <label>SKU *</label>
              <input className="form-control" value={form.sku} onChange={e => set('sku', e.target.value)} placeholder="e.g. OFF-001" />
              {errors.sku && <span className="field-error">{errors.sku}</span>}
            </div>
            <div className="form-group">
              <label>Category</label>
              <select className="form-control" value={form.categoryId} onChange={e => set('categoryId', e.target.value)}>
                <option value="">Select category…</option>
                {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Price ($) *</label>
              <input className="form-control" type="number" step="0.01" min="0" value={form.price} onChange={e => set('price', e.target.value)} placeholder="0.00" />
              {errors.price && <span className="field-error">{errors.price}</span>}
            </div>
            <div className="form-group">
              <label>Quantity *</label>
              <input className="form-control" type="number" min="0" value={form.quantity} onChange={e => set('quantity', e.target.value)} placeholder="0" />
              {errors.quantity && <span className="field-error">{errors.quantity}</span>}
            </div>
            <div className="form-group">
              <label>Reorder Threshold</label>
              <input className="form-control" type="number" min="0" value={form.reorderThreshold} onChange={e => set('reorderThreshold', e.target.value)} placeholder="10" />
            </div>
            <div className="form-group">
              <label>Supplier</label>
              <select className="form-control" value={form.supplierId} onChange={e => set('supplierId', e.target.value)}>
                <option value="">Select supplier…</option>
                {suppliers.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
            </div>
            <div className="form-group full">
              <label>Description</label>
              <textarea className="form-control" value={form.description} onChange={e => set('description', e.target.value)} placeholder="Brief product description…" />
            </div>
          </div>
        </div>
        <div className="modal-footer">
          <button className="btn" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={handleSubmit} disabled={loading}>
            {loading ? 'Saving…' : (product ? 'Save Changes' : 'Add Product')}
          </button>
        </div>
      </div>
    </div>
  );
}
