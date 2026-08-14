import React, { useState, useEffect } from 'react';
import { getLowStockReport, getProducts, recordTransaction } from '../api';
import { Loading, IconAlert, StatusBadge, IconPlus } from '../components/UI';
import TransactionModal from '../components/TransactionModal';
import { Product, TransactionRequest, PageProps } from '../types';

export default function ReorderAlerts({ onToast }: PageProps) {
  const [lowStock, setLowStock] = useState<Product[]>([]);
  const [allProducts, setAllProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [selectedProductId, setSelectedProductId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    Promise.all([getLowStockReport(), getProducts()])
      .then(([ls, all]) => { setLowStock(ls.data); setAllProducts(all.data); })
      .finally(() => setLoading(false));
  }, []);

  const handleReorder = (productId: number) => {
    setSelectedProductId(productId);
    setShowModal(true);
  };

  const handleSave = async (data: TransactionRequest) => {
    setSaving(true);
    try {
      await recordTransaction(data);
      const res = await getLowStockReport();
      setLowStock(res.data);
      onToast('✓ Stock updated');
      setShowModal(false);
    } catch (err: any) {
      onToast('✗ ' + (err.response?.data?.error || 'Error'));
    } finally { setSaving(false); }
  };

  if (loading) return <Loading text="Checking stock levels…" />;

  return (
    <div>
      {lowStock.length > 0 && (
        <div className="alert alert-warn" style={{ marginBottom: '1.5rem' }}>
          <IconAlert size={14} style={{ flexShrink: 0, marginTop: 1 }} />
          <span><strong>{lowStock.length} product{lowStock.length !== 1 ? 's' : ''}</strong> at or below reorder threshold. Review and restock below.</span>
        </div>
      )}

      <div className="card">
        <div className="card-header">
          <span className="card-title">Reorder Alerts ({lowStock.length})</span>
        </div>

        {lowStock.length === 0 ? (
          <div className="empty">
            <div style={{ fontSize: 32, marginBottom: 8 }}>✅</div>
            <p>All products are above their reorder thresholds. No action needed.</p>
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Product</th><th>SKU</th><th>Supplier</th>
                  <th>Current Stock</th><th>Reorder Threshold</th><th>Shortage</th>
                  <th>Status</th><th>Action</th>
                </tr>
              </thead>
              <tbody>
                {lowStock.map(p => {
                  const shortage = Math.max(0, p.reorderThreshold - p.quantity);
                  const suggested = Math.max(p.reorderThreshold * 2 - p.quantity, 0);
                  return (
                    <tr key={p.id}>
                      <td style={{ fontWeight: 500 }}>{p.name}</td>
                      <td><span className="sku-tag">{p.sku}</span></td>
                      <td style={{ color: 'var(--text2)' }}>{p.supplier?.name || '—'}</td>
                      <td>
                        <span className="mono" style={{ fontWeight: 600, color: p.quantity === 0 ? 'var(--red-text)' : 'var(--amber-text)' }}>
                          {p.quantity}
                        </span>
                      </td>
                      <td className="mono">{p.reorderThreshold}</td>
                      <td>
                        <span className="badge badge-red">{shortage} needed</span>
                      </td>
                      <td><StatusBadge quantity={p.quantity} threshold={p.reorderThreshold} /></td>
                      <td>
                        <button
                          className="btn btn-sm btn-primary"
                          onClick={() => handleReorder(p.id)}
                        >
                          <IconPlus size={12} />Restock ({suggested} suggested)
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {showModal && (
        <TransactionModal
          products={allProducts}
          defaultProductId={selectedProductId}
          onSave={handleSave}
          onClose={() => setShowModal(false)}
          loading={saving}
        />
      )}
    </div>
  );
}
