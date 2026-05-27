import React, { useState, useEffect, useCallback } from 'react';
import { getProducts, getCategories, getSuppliers, createProduct, updateProduct, deleteProduct } from '../api';
import { Loading, StatusBadge, ConfirmModal, IconPlus, IconEdit, IconTrash, IconSearch } from '../components/UI';
import ProductModal from '../components/ProductModal';
import { useAuth } from '../context/AuthContext';

export default function Products({ onToast }) {
  const { isAdmin } = useAuth();
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [search, setSearch] = useState('');
  const [catFilter, setCatFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editProduct, setEditProduct] = useState(null);
  const [deleteId, setDeleteId] = useState(null);

  const fetchData = useCallback(async () => {
    setLoading(true);
    const [pr, ca, su] = await Promise.all([getProducts(), getCategories(), getSuppliers()]);
    setProducts(pr.data); setCategories(ca.data); setSuppliers(su.data);
    setLoading(false);
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const getStatus = p => p.quantity === 0 ? 'Out of stock' : p.quantity <= p.reorderThreshold ? 'Low stock' : 'In stock';

  const filtered = products.filter(p => {
    const matchSearch = !search || p.name.toLowerCase().includes(search.toLowerCase()) || p.sku.toLowerCase().includes(search.toLowerCase());
    const matchCat = !catFilter || p.category?.id === Number(catFilter);
    const matchStatus = !statusFilter || getStatus(p) === statusFilter;
    return matchSearch && matchCat && matchStatus;
  });

  const handleSave = async (data) => {
    setSaving(true);
    try {
      if (editProduct) {
        const res = await updateProduct(editProduct.id, data);
        setProducts(prev => prev.map(p => p.id === editProduct.id ? res.data : p));
        onToast('✓ Product updated');
      } else {
        const res = await createProduct(data);
        setProducts(prev => [...prev, res.data]);
        onToast('✓ Product added');
      }
      setShowModal(false); setEditProduct(null);
    } catch (err) {
      onToast('✗ ' + (err.response?.data?.error || 'Error saving product'));
    } finally { setSaving(false); }
  };

  const handleDelete = async () => {
    try {
      await deleteProduct(deleteId);
      setProducts(prev => prev.filter(p => p.id !== deleteId));
      onToast('✓ Product deleted');
    } catch (err) {
      onToast('✗ ' + (err.response?.data?.error || 'Error deleting product'));
    } finally { setDeleteId(null); }
  };

  if (loading) return <Loading text="Loading products…" />;

  return (
    <div>
      <div className="card">
        <div className="card-header">
          <span className="card-title">Products ({filtered.length})</span>
          <div className="card-header-actions">
            <div className="search-wrap">
              <IconSearch />
              <input className="search-input" placeholder="Search name or SKU…" value={search} onChange={e => setSearch(e.target.value)} />
            </div>
            <select className="filter-select" value={catFilter} onChange={e => setCatFilter(e.target.value)}>
              <option value="">All categories</option>
              {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
            <select className="filter-select" value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
              <option value="">All statuses</option>
              <option>In stock</option><option>Low stock</option><option>Out of stock</option>
            </select>
            {isAdmin && (
              <button className="btn btn-primary" onClick={() => { setEditProduct(null); setShowModal(true); }}>
                <IconPlus />Add product
              </button>
            )}
          </div>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Name</th><th>SKU</th><th>Category</th><th>Supplier</th>
                <th>Price</th><th>Qty</th><th>Reorder</th><th>Status</th>
                {isAdmin && <th></th>}
              </tr>
            </thead>
            <tbody>
              {filtered.map(p => (
                <tr key={p.id}>
                  <td style={{ fontWeight: 500 }}>{p.name}</td>
                  <td><span className="sku-tag">{p.sku}</span></td>
                  <td>{p.category ? <span className="badge badge-blue">{p.category.name}</span> : '—'}</td>
                  <td style={{ color: 'var(--text2)' }}>{p.supplier?.name || '—'}</td>
                  <td>${Number(p.price).toFixed(2)}</td>
                  <td className="mono">{p.quantity}</td>
                  <td className="mono" style={{ color: 'var(--text2)' }}>{p.reorderThreshold}</td>
                  <td><StatusBadge quantity={p.quantity} threshold={p.reorderThreshold} /></td>
                  {isAdmin && (
                    <td>
                      <div style={{ display: 'flex', gap: 2 }}>
                        <button className="btn btn-icon btn-sm" onClick={() => { setEditProduct(p); setShowModal(true); }} title="Edit"><IconEdit size={14} /></button>
                        <button className="btn btn-icon btn-sm btn-danger" onClick={() => setDeleteId(p.id)} title="Delete"><IconTrash size={14} /></button>
                      </div>
                    </td>
                  )}
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr><td colSpan={isAdmin ? 9 : 8} style={{ textAlign: 'center', padding: '2.5rem', color: 'var(--text2)' }}>
                  {products.length === 0 ? 'No products yet.' : 'No products match your filters.'}
                </td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && (
        <ProductModal
          product={editProduct}
          categories={categories}
          suppliers={suppliers}
          onSave={handleSave}
          onClose={() => { setShowModal(false); setEditProduct(null); }}
          loading={saving}
        />
      )}

      {deleteId && (
        <ConfirmModal
          message="Are you sure you want to delete this product? This cannot be undone."
          onConfirm={handleDelete}
          onCancel={() => setDeleteId(null)}
        />
      )}
    </div>
  );
}
