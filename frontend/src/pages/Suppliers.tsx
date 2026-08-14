import React, { useState, useEffect } from 'react';
import { getSuppliers, getProducts, createSupplier, updateSupplier, deleteSupplier } from '../api';
import { Loading, ConfirmModal, IconPlus, IconEdit, IconTrash, IconX } from '../components/UI';
import { Supplier, Product, PageProps } from '../types';

interface SupplierForm {
  name: string;
  contactEmail: string;
  phone: string;
  address: string;
  notes: string;
}

interface FieldConfig {
  k: keyof SupplierForm;
  label: string;
  placeholder: string;
  min: number;
}

const empty: SupplierForm = { name: '', contactEmail: '', phone: '', address: '', notes: '' };

const fields: FieldConfig[] = [
  { k: 'name', label: 'Name *', placeholder: 'Acme Wholesale', min: 200 },
  { k: 'contactEmail', label: 'Email', placeholder: 'orders@supplier.com', min: 200 },
  { k: 'phone', label: 'Phone', placeholder: '405-555-0100', min: 140 },
  { k: 'address', label: 'Address', placeholder: 'City, State', min: 180 },
];

export default function Suppliers({ onToast }: PageProps) {
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState<SupplierForm>(empty);
  const [editing, setEditing] = useState<Supplier | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);

  const set = <K extends keyof SupplierForm>(k: K, v: string) => setForm(f => ({ ...f, [k]: v }));

  useEffect(() => {
    Promise.all([getSuppliers(), getProducts()])
      .then(([s, p]) => { setSuppliers(s.data); setProducts(p.data); })
      .finally(() => setLoading(false));
  }, []);

  const getCount = (id: number) => products.filter(p => p.supplier?.id === id).length;

  const handleSave = async () => {
    if (!form.name.trim()) return;
    setSaving(true);
    try {
      if (editing) {
        const res = await updateSupplier(editing.id, form);
        setSuppliers(prev => prev.map(s => s.id === editing.id ? res.data : s));
        onToast('✓ Supplier updated');
      } else {
        const res = await createSupplier(form);
        setSuppliers(prev => [...prev, res.data]);
        onToast('✓ Supplier added');
      }
      setForm(empty); setEditing(null); setShowForm(false);
    } catch (err: any) {
      onToast('✗ ' + (err.response?.data?.error || 'Error'));
    } finally { setSaving(false); }
  };

  const handleDelete = async () => {
    if (deleteId == null) return;
    try {
      await deleteSupplier(deleteId);
      setSuppliers(prev => prev.filter(s => s.id !== deleteId));
      onToast('✓ Supplier deleted');
    } catch (err: any) {
      onToast('✗ ' + (err.response?.data?.error || 'Error deleting supplier'));
    } finally { setDeleteId(null); }
  };

  if (loading) return <Loading />;

  return (
    <div>
      <div className="card">
        <div className="card-header">
          <span className="card-title">Suppliers ({suppliers.length})</span>
          <button className="btn btn-primary" onClick={() => { setEditing(null); setForm(empty); setShowForm(v => !v); }}>
            {showForm ? <IconX /> : <IconPlus />}{showForm ? 'Cancel' : 'Add supplier'}
          </button>
        </div>

        {showForm && (
          <div style={{ padding: '1rem 1.25rem', borderBottom: '1px solid var(--border)', background: 'var(--surface2)' }}>
            <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', alignItems: 'flex-end' }}>
              {fields.map(({ k, label, placeholder, min }) => (
                <div key={k} className="form-group" style={{ marginBottom: 0, minWidth: min }}>
                  <label>{label}</label>
                  <input className="form-control" value={form[k]} onChange={e => set(k, e.target.value)} placeholder={placeholder} />
                </div>
              ))}
              <button className="btn btn-primary" onClick={handleSave} disabled={saving}>{saving ? 'Saving…' : editing ? 'Save' : 'Add'}</button>
            </div>
          </div>
        )}

        <div className="table-wrap">
          <table>
            <thead><tr><th>Supplier</th><th>Email</th><th>Phone</th><th>Address</th><th>Products</th><th></th></tr></thead>
            <tbody>
              {suppliers.map(s => (
                <tr key={s.id}>
                  <td style={{ fontWeight: 500 }}>{s.name}</td>
                  <td style={{ color: 'var(--text2)' }}>{s.contactEmail || '—'}</td>
                  <td className="mono">{s.phone || '—'}</td>
                  <td style={{ color: 'var(--text2)' }}>{s.address || '—'}</td>
                  <td>{getCount(s.id)} products</td>
                  <td>
                    <div style={{ display: 'flex', gap: 2 }}>
                      <button className="btn btn-icon btn-sm" onClick={() => { setEditing(s); setForm({ name: s.name, contactEmail: s.contactEmail || '', phone: s.phone || '', address: s.address || '', notes: s.notes || '' }); setShowForm(true); }}><IconEdit size={14} /></button>
                      <button className="btn btn-icon btn-sm btn-danger" onClick={() => setDeleteId(s.id)}><IconTrash size={14} /></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {deleteId != null && <ConfirmModal message="Delete this supplier?" onConfirm={handleDelete} onCancel={() => setDeleteId(null)} />}
    </div>
  );
}
