import React, { useState, useEffect } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { getInventoryByCategory, getInventoryBySupplier, getLowStockReport, exportProductsCsv, exportLowStockCsv } from '../api';
import { Loading, IconDownload, StatusBadge } from '../components/UI';

const fmt = n => `$${Number(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

function downloadCsv(blob, filename) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url; a.download = filename; a.click();
  URL.revokeObjectURL(url);
}

export default function Reports({ onToast }) {
  const [catData, setCatData] = useState([]);
  const [supplierData, setSupplierData] = useState([]);
  const [lowStock, setLowStock] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getInventoryByCategory(), getInventoryBySupplier(), getLowStockReport()])
      .then(([c, s, ls]) => {
        setCatData(c.data.map(r => ({ name: r.categoryName, value: Number(r.totalValue) })));
        setSupplierData(s.data.map(r => ({ name: r.supplierName, count: Number(r.productCount), value: Number(r.totalValue) })));
        setLowStock(ls.data);
      })
      .finally(() => setLoading(false));
  }, []);

  const handleExport = async (type) => {
    try {
      const res = type === 'products' ? await exportProductsCsv() : await exportLowStockCsv();
      downloadCsv(res.data, type === 'products' ? 'products.csv' : 'low-stock.csv');
      onToast('✓ CSV downloaded');
    } catch { onToast('✗ Export failed'); }
  };

  if (loading) return <Loading text="Generating reports…" />;

  return (
    <div>
      {/* Export buttons */}
      <div className="card" style={{ overflow: 'visible' }}>
        <div className="card-header">
          <span className="card-title">Export Reports</span>
          <div className="card-header-actions">
            <button className="btn" onClick={() => handleExport('products')}><IconDownload size={14} />All Products CSV</button>
            <button className="btn" onClick={() => handleExport('lowstock')}><IconDownload size={14} />Low Stock CSV</button>
          </div>
        </div>
      </div>

      {/* Charts row */}
      <div className="report-grid">
        <div className="card">
          <div className="card-header"><span className="card-title">Inventory Value by Category</span></div>
          <div style={{ padding: '1rem', height: 260 }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={catData} margin={{ top: 4, right: 16, left: 8, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                <XAxis dataKey="name" tick={{ fontSize: 11, fill: 'var(--text2)' }} />
                <YAxis tick={{ fontSize: 11, fill: 'var(--text2)' }} tickFormatter={v => `$${(v / 1000).toFixed(0)}k`} />
                <Tooltip formatter={v => fmt(v)} contentStyle={{ fontSize: 12, borderRadius: 6 }} />
                <Bar dataKey="value" fill="#1D9E75" radius={[4, 4, 0, 0]} name="Value" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="card">
          <div className="card-header"><span className="card-title">Supplier Inventory Value</span></div>
          <div style={{ padding: '1rem', height: 260 }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={supplierData} margin={{ top: 4, right: 16, left: 8, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                <XAxis dataKey="name" tick={{ fontSize: 11, fill: 'var(--text2)' }} />
                <YAxis tick={{ fontSize: 11, fill: 'var(--text2)' }} tickFormatter={v => `$${(v / 1000).toFixed(0)}k`} />
                <Tooltip formatter={(v, n) => n === 'value' ? fmt(v) : v} contentStyle={{ fontSize: 12, borderRadius: 6 }} />
                <Bar dataKey="value" fill="#185FA5" radius={[4, 4, 0, 0]} name="value" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Supplier table */}
      <div className="card">
        <div className="card-header"><span className="card-title">Supplier-wise Inventory</span></div>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Supplier</th><th>Products</th><th>Total Inventory Value</th></tr></thead>
            <tbody>
              {supplierData.map(s => (
                <tr key={s.name}>
                  <td style={{ fontWeight: 500 }}>{s.name}</td>
                  <td>{s.count}</td>
                  <td>{fmt(s.value)}</td>
                </tr>
              ))}
              {supplierData.length === 0 && <tr><td colSpan={3} style={{ textAlign: 'center', padding: '2rem', color: 'var(--text2)' }}>No supplier data.</td></tr>}
            </tbody>
          </table>
        </div>
      </div>

      {/* Low stock report */}
      <div className="card">
        <div className="card-header">
          <span className="card-title">Low Stock Report ({lowStock.length})</span>
          <button className="btn btn-sm" onClick={() => handleExport('lowstock')}><IconDownload size={12} />Export</button>
        </div>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Product</th><th>SKU</th><th>Supplier</th><th>Qty</th><th>Threshold</th><th>Shortage</th><th>Status</th></tr></thead>
            <tbody>
              {lowStock.map(p => (
                <tr key={p.id}>
                  <td style={{ fontWeight: 500 }}>{p.name}</td>
                  <td><span className="sku-tag">{p.sku}</span></td>
                  <td style={{ color: 'var(--text2)' }}>{p.supplier?.name || '—'}</td>
                  <td className="mono" style={{ color: p.quantity === 0 ? 'var(--red-text)' : 'var(--amber-text)', fontWeight: 600 }}>{p.quantity}</td>
                  <td className="mono">{p.reorderThreshold}</td>
                  <td><span className="badge badge-red">{Math.max(0, p.reorderThreshold - p.quantity)} needed</span></td>
                  <td><StatusBadge quantity={p.quantity} threshold={p.reorderThreshold} /></td>
                </tr>
              ))}
              {lowStock.length === 0 && <tr><td colSpan={7} style={{ textAlign: 'center', padding: '2rem', color: 'var(--text2)' }}>All products are well-stocked.</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
