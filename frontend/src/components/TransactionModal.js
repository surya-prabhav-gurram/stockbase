import React, { useState } from 'react';
import { IconX } from './UI';

const TYPE_LABELS = { STOCK_IN: 'Stock In', STOCK_OUT: 'Stock Out', ADJUSTMENT: 'Manual Adjustment' };

export default function TransactionModal({ products, onSave, onClose, loading, defaultProductId }) {
  const [form, setForm] = useState({
    productId: defaultProductId || '',
    type: 'STOCK_IN',
    quantity: '',
    reason: '',
    notes: '',
  });
  const [errors, setErrors] = useState({});

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const validate = () => {
    const e = {};
    if (!form.productId) e.productId = 'Select a product';
    if (!form.quantity || isNaN(form.quantity) || Number(form.quantity) < 1) e.quantity = 'Enter a valid quantity (min 1)';
    setErrors(e);
    return !Object.keys(e).length;
  };

  const handleSubmit = () => {
    if (!validate()) return;
    onSave({
      productId: Number(form.productId),
      type: form.type,
      quantity: parseInt(form.quantity),
      reason: form.reason.trim(),
      notes: form.notes.trim(),
    });
  };

  const selectedProduct = products.find(p => p.id === Number(form.productId));

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <div className="modal-header">
          <h2>Record Transaction</h2>
          <button className="btn btn-icon" onClick={onClose}><IconX /></button>
        </div>
        <div className="modal-body">
          <div className="form-group">
            <label>Transaction Type *</label>
            <div style={{ display: 'flex', gap: 8 }}>
              {Object.entries(TYPE_LABELS).map(([val, label]) => (
                <button
                  key={val}
                  type="button"
                  onClick={() => set('type', val)}
                  className="btn btn-sm"
                  style={form.type === val ? { background: 'var(--green)', color: '#fff', borderColor: 'var(--green)' } : {}}
                >
                  {label}
                </button>
              ))}
            </div>
          </div>

          <div className="form-group">
            <label>Product *</label>
            <select className="form-control" value={form.productId} onChange={e => set('productId', e.target.value)}>
              <option value="">Select product…</option>
              {products.map(p => <option key={p.id} value={p.id}>{p.name} (SKU: {p.sku}) — Qty: {p.quantity}</option>)}
            </select>
            {errors.productId && <span className="field-error">{errors.productId}</span>}
          </div>

          {selectedProduct && (
            <div style={{ background: 'var(--surface2)', padding: '0.75rem 1rem', borderRadius: 'var(--radius-sm)', marginBottom: '1rem', fontSize: 13 }}>
              <strong>Current stock:</strong> {selectedProduct.quantity} units
              &nbsp;·&nbsp;
              <strong>Reorder threshold:</strong> {selectedProduct.reorderThreshold}
            </div>
          )}

          <div className="form-group">
            <label>{form.type === 'ADJUSTMENT' ? 'New Quantity (absolute)' : 'Quantity'} *</label>
            <input
              className="form-control"
              type="number"
              min="1"
              value={form.quantity}
              onChange={e => set('quantity', e.target.value)}
              placeholder={form.type === 'ADJUSTMENT' ? 'Set stock to this value' : 'Units to add/remove'}
            />
            {errors.quantity && <span className="field-error">{errors.quantity}</span>}
          </div>

          <div className="form-group">
            <label>Reason</label>
            <input className="form-control" value={form.reason} onChange={e => set('reason', e.target.value)} placeholder="e.g. Supplier delivery, Damaged goods, Audit correction…" />
          </div>
          <div className="form-group">
            <label>Notes</label>
            <textarea className="form-control" value={form.notes} onChange={e => set('notes', e.target.value)} placeholder="Additional notes…" />
          </div>
        </div>
        <div className="modal-footer">
          <button className="btn" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={handleSubmit} disabled={loading}>
            {loading ? 'Recording…' : 'Record Transaction'}
          </button>
        </div>
      </div>
    </div>
  );
}
