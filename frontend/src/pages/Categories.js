import React, { useState, useEffect } from 'react';
import { getCategories, getProducts, createCategory, updateCategory, deleteCategory } from '../api';
import { Loading, ConfirmModal, IconPlus, IconEdit, IconTrash, IconX } from '../components/UI';

export default function Categories({ onToast }) {
  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ name: '', description: '' });
  const [editing, setEditing] = useState(null);
  const [deleteId, setDeleteId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    Promise.all([getCategories(), getProducts()])
      .then(([c, p]) => { setCategories(c.data); setProducts(p.data); })
      .finally(() => setLoading(false));
  }, []);

  const getCount = id => products.filter(p => p.category?.id === id).length;
  const getValue = id => products.filter(p => p.category?.id === id)
    .reduce((s, p) => s + Number(p.price) * p.quantity, 0);

  const handleSave = async () => {
    if (!form.name.trim()) return;
    setSaving(true);
    try {
      if (editing) {
        const res = await updateCategory(editing.id, form);
        setCategories(prev => prev.map(c => c.id === editing.id ? res.data : c));
        onToast('✓ Category updated');
      } else {
        const res = await createCategory(form);
        setCategories(prev => [...prev, res.data]);
        onToast('✓ Category added');
      }
      setForm({ name: '', description: '' }); setEditing(null); setShowForm(false);
    } catch (err) {
      onToast('✗ ' + (err.response?.data?.error || 'Error'));
    } finally { setSaving(false); }
  };

  const handleDelete = async () => {
    try {
      await deleteCategory(deleteId);
      setCategories(prev => prev.filter(c => c.id !== deleteId));
      onToast('✓ Category deleted');
    } catch (err) {
      onToast('✗ ' + (err.response?.data?.error || 'Error deleting category'));
    } finally { setDeleteId(null); }
  };

  if (loading) return <Loading />;

  return (
    <div>
      <div className="card">
        <div className="card-header">
          <span className="card-title">Categories ({categories.length})</span>
          <button className="btn btn-primary" onClick={() => { setEditing(null); setForm({ name: '', description: '' }); setShowForm(v => !v); }}>
            {showForm ? <IconX /> : <IconPlus />}{showForm ? 'Cancel' : 'Add category'}
          </button>
        </div>

        {showForm && (
          <div style={{ padding: '1rem 1.25rem', borderBottom: '1px solid var(--border)', background: 'var(--surface2)', display: 'flex', gap: 10, alignItems: 'flex-end', flexWrap: 'wrap' }}>
            <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 200 }}>
              <label>Name *</label>
              <input className="form-control" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} placeholder="e.g. Electronics" autoFocus />
            </div>
            <div className="form-group" style={{ marginBottom: 0, flex: 2, minWidth: 240 }}>
              <label>Description</label>
              <input className="form-control" value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} placeholder="Optional description" />
            </div>
            <button className="btn btn-primary" onClick={handleSave} disabled={saving}>{saving ? 'Saving…' : editing ? 'Save' : 'Add'}</button>
          </div>
        )}

        <div className="table-wrap">
          <table>
            <thead><tr><th>Category</th><th>Description</th><th>Products</th><th>Inventory Value</th><th></th></tr></thead>
            <tbody>
              {categories.map(c => (
                <tr key={c.id}>
                  <td style={{ fontWeight: 500 }}><span className="badge badge-blue">{c.name}</span></td>
                  <td style={{ color: 'var(--text2)' }}>{c.description || '—'}</td>
                  <td>{getCount(c.id)}</td>
                  <td>${getValue(c.id).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                  <td>
                    <div style={{ display: 'flex', gap: 2 }}>
                      <button className="btn btn-icon btn-sm" onClick={() => { setEditing(c); setForm({ name: c.name, description: c.description || '' }); setShowForm(true); }}><IconEdit size={14} /></button>
                      <button className="btn btn-icon btn-sm btn-danger" onClick={() => setDeleteId(c.id)}><IconTrash size={14} /></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {deleteId && <ConfirmModal message="Delete this category?" onConfirm={handleDelete} onCancel={() => setDeleteId(null)} />}
    </div>
  );
}
