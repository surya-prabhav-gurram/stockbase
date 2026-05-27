import React, { useEffect, useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';
import { getDashboardStats, getInventoryByCategory, getRecentTransactions } from '../api';
import { Loading, IconAlert } from '../components/UI';
import { Link } from 'react-router-dom';

const COLORS = ['#1D9E75','#185FA5','#854F0B','#5b21b6','#A32D2D','#0891b2'];

const fmt = n => `$${Number(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

function TxTypeBadge({ type }) {
  const map = { STOCK_IN: ['badge-green', 'Stock In'], STOCK_OUT: ['badge-red', 'Stock Out'], ADJUSTMENT: ['badge-blue', 'Adjustment'] };
  const [cls, label] = map[type] || ['badge-gray', type];
  return <span className={`badge ${cls}`}><span className="badge-dot" />{label}</span>;
}

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [catData, setCatData] = useState([]);
  const [txs, setTxs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getDashboardStats(), getInventoryByCategory(), getRecentTransactions(8)])
      .then(([s, c, t]) => {
        setStats(s.data);
        setCatData(c.data.map(r => ({ name: r.categoryName, value: Number(r.totalValue) })));
        setTxs(t.data);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loading text="Loading dashboard…" />;

  return (
    <div>
      <div className="metrics-grid">
        {[
          { label: 'Total Products', value: stats.totalProducts, sub: 'in catalog' },
          { label: 'In Stock', value: stats.inStockCount, sub: `${stats.totalProducts ? Math.round((stats.inStockCount / stats.totalProducts) * 100) : 0}% of catalog`, color: 'var(--green)' },
          { label: 'Low / Out of Stock', value: `${stats.lowStockCount} / ${stats.outOfStockCount}`, sub: 'need attention', subClass: stats.lowStockCount > 0 ? 'warn' : '' },
          { label: 'Inventory Value', value: fmt(stats.totalInventoryValue), sub: `${stats.totalTransactions} transactions`, fontSize: 20 },
        ].map(({ label, value, sub, subClass, color, fontSize }) => (
          <div className="metric-card" key={label}>
            <div className="metric-label">{label}</div>
            <div className="metric-value" style={{ color: color || 'inherit', fontSize: fontSize || 28 }}>{value}</div>
            <div className={`metric-sub ${subClass || ''}`}>{sub}</div>
          </div>
        ))}
      </div>

      {stats.lowStockCount > 0 && (
        <div className="alert alert-warn">
          <IconAlert size={14} style={{ flexShrink: 0, marginTop: 1 }} />
          <span><strong>{stats.lowStockCount} product{stats.lowStockCount !== 1 ? 's' : ''}</strong> below reorder threshold.{' '}
          <Link to="/reorder" style={{ color: 'inherit', fontWeight: 600 }}>View reorder alerts →</Link></span>
        </div>
      )}

      <div className="report-grid">
        <div className="card">
          <div className="card-header"><span className="card-title">Inventory Value by Category</span></div>
          <div style={{ padding: '1rem', height: 240 }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={catData} margin={{ top: 4, right: 16, left: 8, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                <XAxis dataKey="name" tick={{ fontSize: 11, fill: 'var(--text2)' }} />
                <YAxis tick={{ fontSize: 11, fill: 'var(--text2)' }} tickFormatter={v => `$${(v/1000).toFixed(0)}k`} />
                <Tooltip formatter={v => fmt(v)} contentStyle={{ fontSize: 12, borderRadius: 6, border: '1px solid var(--border)' }} />
                <Bar dataKey="value" fill="var(--green)" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="card">
          <div className="card-header"><span className="card-title">Category Distribution</span></div>
          <div style={{ padding: '1rem', height: 240 }}>
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={catData} cx="50%" cy="50%" outerRadius={80} dataKey="value" nameKey="name" label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`} labelLine={false} style={{ fontSize: 10 }}>
                  {catData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                </Pie>
                <Tooltip formatter={v => fmt(v)} contentStyle={{ fontSize: 12, borderRadius: 6 }} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <span className="card-title">Recent Transactions</span>
          <Link to="/transactions" className="btn btn-sm">View all →</Link>
        </div>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Product</th><th>Type</th><th>Qty Before</th><th>Change</th><th>Qty After</th><th>Reason</th><th>By</th><th>Date</th></tr></thead>
            <tbody>
              {txs.map(tx => (
                <tr key={tx.id}>
                  <td style={{ fontWeight: 500 }}>{tx.product?.name}</td>
                  <td><TxTypeBadge type={tx.type} /></td>
                  <td className="mono">{tx.quantityBefore}</td>
                  <td className="mono" style={{ color: tx.type === 'STOCK_IN' ? 'var(--green)' : tx.type === 'STOCK_OUT' ? 'var(--red-text)' : 'var(--blue-text)', fontWeight: 500 }}>
                    {tx.type === 'STOCK_IN' ? '+' : tx.type === 'STOCK_OUT' ? '-' : '='}{tx.quantity}
                  </td>
                  <td className="mono">{tx.quantityAfter}</td>
                  <td style={{ color: 'var(--text2)', maxWidth: 140, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{tx.reason || '—'}</td>
                  <td style={{ color: 'var(--text2)' }}>{tx.performedBy?.fullName || '—'}</td>
                  <td style={{ color: 'var(--text2)', whiteSpace: 'nowrap' }}>{new Date(tx.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
              {txs.length === 0 && <tr><td colSpan={8} style={{ textAlign: 'center', padding: '2rem', color: 'var(--text2)' }}>No transactions yet.</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
