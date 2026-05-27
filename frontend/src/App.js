import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Sidebar from './components/Sidebar';
import { Toast } from './components/UI';
import AuthPage from './pages/AuthPage';
import Dashboard from './pages/Dashboard';
import Products from './pages/Products';
import Transactions from './pages/Transactions';
import ReorderAlerts from './pages/ReorderAlerts';
import Categories from './pages/Categories';
import Suppliers from './pages/Suppliers';
import Reports from './pages/Reports';

const PAGE_TITLES = {
  '/': 'Dashboard', '/products': 'Products', '/transactions': 'Transaction Log',
  '/reorder': 'Reorder Alerts', '/categories': 'Categories',
  '/suppliers': 'Suppliers', '/reports': 'Reports',
};

function ProtectedLayout() {
  const { user, loading, isAdmin } = useAuth();
  const [toast, setToast] = useState(null);
  const notify = msg => setToast(msg);

  if (loading) return <div className="loading"><div className="spinner" />Loading…</div>;
  if (!user) return <Navigate to="/login" replace />;

  const title = PAGE_TITLES[window.location.pathname] || 'StockBase';

  return (
    <div className="layout">
      <Sidebar />
      <div className="main">
        <div className="topbar">
          <div className="breadcrumb">
            <span>StockBase</span>
            <span className="breadcrumb-sep">›</span>
            <span style={{ color: 'var(--text)' }}>{title}</span>
          </div>
          <h1>{title}</h1>
          <div style={{ width: 100 }} />
        </div>
        <div className="page-content">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/products" element={<Products onToast={notify} />} />
            <Route path="/transactions" element={<Transactions onToast={notify} />} />
            <Route path="/reorder" element={<ReorderAlerts onToast={notify} />} />
            {isAdmin && <Route path="/categories" element={<Categories onToast={notify} />} />}
            {isAdmin && <Route path="/suppliers" element={<Suppliers onToast={notify} />} />}
            {isAdmin && <Route path="/reports" element={<Reports onToast={notify} />} />}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>
      </div>
      {toast && <Toast message={toast} onDone={() => setToast(null)} />}
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<AuthPage />} />
          <Route path="/*" element={<ProtectedLayout />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
