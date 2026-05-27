import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  IconDashboard, IconPackage, IconTag, IconTruck,
  IconActivity, IconBarChart, IconAlert, IconLogout, IconShield
} from './UI';

const navItems = [
  { to: '/',            label: 'Dashboard',    icon: IconDashboard },
  { to: '/products',    label: 'Products',     icon: IconPackage },
  { to: '/transactions',label: 'Transactions', icon: IconActivity },
  { to: '/reorder',     label: 'Reorder Alerts',icon: IconAlert },
];

const adminItems = [
  { to: '/categories',  label: 'Categories',   icon: IconTag },
  { to: '/suppliers',   label: 'Suppliers',    icon: IconTruck },
  { to: '/reports',     label: 'Reports',      icon: IconBarChart },
];

export default function Sidebar() {
  const { user, logout, isAdmin } = useAuth();
  const initials = user?.fullName?.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) || '?';

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <div className="logo-mark">
          <div className="logo-icon">
            <IconPackage size={16} style={{ color: '#fff' }} />
          </div>
          <div>
            <div className="logo-name">StockBase</div>
            <div className="logo-sub">Inventory System</div>
          </div>
        </div>
      </div>

      <nav className="sidebar-nav">
        <div className="nav-label">Main</div>
        {navItems.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}
          >
            <Icon size={15} />{label}
          </NavLink>
        ))}

        {isAdmin && (
          <>
            <hr className="nav-divider" />
            <div className="nav-label">Admin</div>
            {adminItems.map(({ to, label, icon: Icon }) => (
              <NavLink
                key={to}
                to={to}
                className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}
              >
                <Icon size={15} />{label}
              </NavLink>
            ))}
          </>
        )}
      </nav>

      <div className="sidebar-footer">
        <div className="user-info">
          <div className="user-avatar">{initials}</div>
          <div>
            <div className="user-name">{user?.fullName}</div>
            <div className="user-role" style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
              {isAdmin && <IconShield size={10} />}
              {user?.role}
            </div>
          </div>
          <button className="logout-btn" onClick={logout} title="Logout"><IconLogout size={15} /></button>
        </div>
      </div>
    </aside>
  );
}
