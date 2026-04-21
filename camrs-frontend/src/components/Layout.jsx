import React from 'react';
import Sidebar from './Sidebar';
import { useAuth } from '../context/AuthContext';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { motion } from 'framer-motion';

const Layout = ({ children }) => {
  const { user } = useAuth();
  const fullName = user?.fullName || user?.role?.replace('_', ' ') || 'User';
  const initials = fullName.replace('Dr. ', '').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();

  return (
    <div className="flex h-screen overflow-hidden bg-background">
      <Sidebar />
      <div className="flex flex-1 flex-col overflow-hidden">
        {/* Top Header Bar */}
        <header className="h-[60px] shrink-0 border-b border-border bg-card flex items-center justify-between px-8 sticky top-0 z-30">
          <div className="flex-1">
            <p className="text-sm font-medium text-muted-foreground">
              Welcome, <span className="text-foreground font-semibold">{fullName}</span>
            </p>
          </div>

          {/* Right side — User Info */}
          <div className="flex items-center gap-4">
            <div className="text-right">
              <p className="text-sm font-semibold text-foreground leading-tight">{fullName}</p>
              <p className="text-[0.65rem] text-muted-foreground">CAMRS Portal</p>
            </div>
            <Avatar className="h-9 w-9 text-xs border-2 border-terracotta/20">
              <AvatarFallback className="bg-terracotta/10 text-terracotta font-semibold text-xs">
                {initials}
              </AvatarFallback>
            </Avatar>
          </div>
        </header>

        {/* Main Content */}
        <motion.main
          className="flex-1 overflow-y-auto p-6 lg:p-8"
          initial={{ opacity: 0, y: 6 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.25, ease: 'easeOut' }}
        >
          {children}
        </motion.main>
      </div>
    </div>
  );
};

export default Layout;
