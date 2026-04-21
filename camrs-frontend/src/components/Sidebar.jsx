import React, { useState, useEffect } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Separator } from '@/components/ui/separator';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { motion } from 'framer-motion';

const Sidebar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [pendingRequestsCount, setPendingRequestsCount] = useState(0);

  useEffect(() => {
    if (user?.role === 'ADMIN_STAFF') {
      api.get('/admin/doctor-requests/pending-count')
        .then(res => setPendingRequestsCount(res.data || 0))
        .catch(() => setPendingRequestsCount(0));
    }
  }, [user]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getLinks = () => {
    switch (user?.role) {
      case 'DOCTOR':
        return {
          menu: [
            { name: 'Dashboard', path: '/doctor/dashboard' },
            { name: 'Appointments', path: '/doctor/appointments' },
            { name: 'Prescriptions', path: '/doctor/prescriptions' },
            { name: 'Prescription History', path: '/doctor/prescription-history' }
          ],
          general: []
        };
      case 'ADMIN_STAFF':
        return {
          menu: [
            { name: 'Dashboard', path: '/admin/dashboard' },
            { name: 'Doctors', path: '/admin/doctors' },
            { name: 'Doctor Requests', path: '/admin/doctor-requests', badge: pendingRequestsCount },
            { name: 'Lab Staff', path: '/admin/lab-staff' },
            { name: 'Inventory', path: '/admin/inventory' },
            { name: 'Billing', path: '/admin/billing' },
          ],
          general: [
            { name: 'Reports', path: '/admin/reports' },
            { name: 'Audit Logs', path: '/admin/audit-logs' },
          ]
        };
      case 'LAB_STAFF':
        return {
          menu: [
            { name: 'Dashboard', path: '/lab/dashboard' },
            { name: 'Pending Tests', path: '/lab/tests' }
          ],
          general: []
        };
      default:
        return {
          menu: [
            { name: 'Dashboard', path: '/patient/dashboard' },
            { name: 'My Appointments', path: '/patient/appointments' },
            { name: 'Medical Records', path: '/patient/records' },
            { name: 'Lab Results', path: '/patient/lab-results' },
          ],
          general: [
            { name: 'My Bills', path: '/patient/bills' },
            { name: 'Profile', path: '/patient/profile' },
          ]
        };
    }
  };

  const { menu, general } = getLinks();
  const fullName = user?.fullName || user?.role?.replace('_', ' ') || 'User';
  const initials = fullName.replace('Dr. ', '').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();

  const renderLink = (link, idx) => (
    <NavLink
      key={idx}
      to={link.path}
      className={({ isActive }) =>
        `group flex items-center justify-between rounded-xl px-4 py-2.5 text-[0.84rem] font-medium transition-all duration-200 ${isActive
          ? 'bg-terracotta/10 text-terracotta border-l-[3px] border-terracotta shadow-sm'
          : 'text-sidebar-foreground/70 hover:text-foreground hover:bg-sidebar-accent border-l-[3px] border-transparent'
        }`
      }
    >
      {({ isActive }) => (
        <>
          <motion.span
            initial={false}
            animate={{ x: isActive ? 2 : 0 }}
            transition={{ type: 'spring', stiffness: 300, damping: 25 }}
          >
            {link.name}
          </motion.span>
          {link.badge > 0 && (
            <Badge className="h-5 min-w-[20px] text-[0.6rem] px-1.5 bg-terracotta text-white border-0">
              {link.badge}
            </Badge>
          )}
        </>
      )}
    </NavLink>
  );

  return (
    <div className="w-[260px] shrink-0 bg-sidebar text-sidebar-foreground flex flex-col border-r border-sidebar-border">
      {/* App Header */}
      <div className="px-6 pt-7 pb-5 border-b border-sidebar-border">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-terracotta/15 flex items-center justify-center">
            <span className="text-terracotta font-bold text-sm">C</span>
          </div>
          <div>
            <h2 className="text-base font-bold tracking-wide text-sidebar-foreground">
              CAMRS
            </h2>
            <p className="text-[0.65rem] text-sidebar-foreground/70 tracking-wider uppercase mt-0.5">
              Healthcare Portal
            </p>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <ScrollArea className="flex-1 px-3 py-4">
        <nav className="flex flex-col gap-0.5">
          {/* MENU section */}
          <p className="px-4 mb-2 text-[0.65rem] font-semibold uppercase tracking-widest text-muted-foreground/60">
            Menu
          </p>
          {menu.map(renderLink)}

          {/* GENERAL section */}
          {general.length > 0 && (
            <>
              <p className="px-4 mt-5 mb-2 text-[0.65rem] font-semibold uppercase tracking-widest text-muted-foreground/60">
                General
              </p>
              {general.map(renderLink)}
            </>
          )}
        </nav>
      </ScrollArea>

      {/* Footer — User info + Logout */}
      <Separator className="bg-sidebar-border" />
      <div className="p-4">
        <div className="flex items-center gap-3 mb-3 px-1">
          <Avatar className="h-8 w-8 text-xs">
            <AvatarFallback className="bg-terracotta/10 text-terracotta font-semibold text-[0.65rem]">
              {initials}
            </AvatarFallback>
          </Avatar>
          <div className="flex-1 min-w-0">
            <p className="text-xs font-semibold text-sidebar-foreground truncate">{fullName}</p>
            <p className="text-[0.6rem] text-sidebar-foreground/70 truncate">Active Session</p>
          </div>
        </div>
        <Button
          variant="ghost"
          onClick={handleLogout}
          className="w-full justify-center text-sidebar-foreground/80 hover:text-white hover:bg-destructive/80 transition-colors text-xs h-9"
        >
          Sign Out
        </Button>
      </div>
    </div>
  );
};

export default Sidebar;