import React, { useState, useEffect, useCallback } from 'react';
import { getTransactions, getProducts, recordTransaction } from '../api';
import { Loading, IconPlus } from '../components/UI';
import TransactionModal from '../components/TransactionModal';

function TxBadge({ type }) {
  const map = { STOCK_IN: ['badge-green', '▲ Stock In'], STOCK_OUT: ['badge-red', '▼ Stock Out'], ADJUSTMENT: ['badge-blue', '⇌ Adjustment'] };
  const [cls, label] = map[type] || ['badge-gray', type];
  return <span className={`badge ${cls}`}>{label}</span>;
}

export default function Transactions({ onToast }) {
  const [txs, setTxs] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [filter, setFilter] = useState('');

  const fetchData = useCallback(async () => {
    setLoading(true);
    const [t, p] = await Promise.all([getTransactions(), getProducts()]);
    setTxs(t.data); setProducts(p.data);
    setLoading(false);
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleSave = async (data) => {
    setSaving(true);
    try {
      const res = await recordTransaction(data);
      setTxs(prev => [res.data, ...prev]);
      onToast('✓ Transaction recorded');
      setShowModal(false);
    } catch (err) {
      onToast('✗ ' + (err.response?.data?.error || 'Error recording transaction'));
    } finally { setSaving(false); }
  };

  const filtered = filter
    ? txs.filter(t => t.type === filter)
    : txs;

  if (loading) return <Loading text="Loading transactions…" />;

  return (
    <div>
      <div className="card">
        <div className="card-header">
          <span className="card-title">Transaction Log ({filtered.length})</span>
          <div className="card-header-actions">
            <select className="filter-select" value={filter} onChange={e => setFilter(e.target.value)}>
              <option value="">All types</option>
              <option value="STOCK_IN">Stock In</option>
              <option value="STOCK_OUT">Stock Out</option>
              <option value="ADJUSTMENT">Adjustment</option>
            </select>
            <button className="btn btn-primary" onClick={() => setShowModal(true)}>
              <IconPlus />Record transaction
            </button>
          </div>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>#</th><th>Product</th><th>SKU</th><th>Type</th>
                <th>Before</th><th>Change</th><th>After</th>
                <th>Reason</th><th>By</th><th>Date</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(tx => (
                <tr key={tx.id}>
                  <td className="mono" style={{ color: 'var(--text3)' }}>{tx.id}</td>
                  <td style={{ fontWeight: 500 }}>{tx.product?.name}</td>
                  <td><span className="sku-tag">{tx.product?.sku}</span></td>
                  <td><TxBadge type={tx.type} /></td>
                  <td className="mono">{tx.quantityBefore}</td>
                  <td className="mono" style={{
                    fontWeight: 600,
                    color: tx.type === 'STOCK_IN' ? 'var(--green)' : tx.type === 'STOCK_OUT' ? 'var(--red-text)' : 'var(--blue-text)'
                  }}>
                    {tx.type === 'STOCK_IN' ? '+' : tx.type === 'STOCK_OUT' ? '-' : '='}{tx.quantity}
                  </td>
                  <td className="mono">{tx.quantityAfter}</td>
                  <td style={{ color: 'var(--text2)', maxWidth: 160, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{tx.reason || '—'}</td>
                  <td style={{ color: 'var(--text2)' }}>{tx.performedBy?.fullName || '—'}</td>
                  <td style={{ color: 'var(--text2)', whiteSpace: 'nowrap', fontSize: 12 }}>
                    {new Date(tx.createdAt).toLocaleString()}
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr><td colSpan={10} style={{ textAlign: 'center', padding: '2.5rem', color: 'var(--text2)' }}>No transactions found.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && (
        <TransactionModal
          products={products}
          onSave={handleSave}
          onClose={() => setShowModal(false)}
          loading={saving}
        />
      )}
    </div>
  );
}
