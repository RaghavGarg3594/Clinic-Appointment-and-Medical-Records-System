import React, { createContext, useState, useContext, useEffect, useRef, useCallback } from 'react';

const AuthContext = createContext(null);

const INACTIVITY_TIMEOUT_MS = 30 * 60 * 1000; // SE-4: 30 minutes

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const timeoutRef = useRef(null);

  const doLogout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('userId');
    localStorage.removeItem('fullName');
    setUser(null);
  }, []);

  // SE-4: Reset the inactivity timer
  const resetTimer = useCallback(() => {
    if (timeoutRef.current) clearTimeout(timeoutRef.current);
    if (user) {
      timeoutRef.current = setTimeout(() => {
        alert('Session expired due to 30 minutes of inactivity. Please log in again.');
        doLogout();
        window.location.href = '/login';
      }, INACTIVITY_TIMEOUT_MS);
    }
  }, [user, doLogout]);

  useEffect(() => {
    // Check local storage for token on mount
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    const userId = localStorage.getItem('userId');
    const fullName = localStorage.getItem('fullName');
    
    if (token && role && userId) {
      setUser({ token, role, userId, fullName });
    }
    setLoading(false);
  }, []);

  // SE-4: Attach activity listeners when user is logged in
  useEffect(() => {
    if (!user) return;
    const events = ['mousedown', 'mousemove', 'keydown', 'scroll', 'touchstart', 'click'];
    events.forEach(e => window.addEventListener(e, resetTimer));
    resetTimer(); // start the timer
    return () => {
      events.forEach(e => window.removeEventListener(e, resetTimer));
      if (timeoutRef.current) clearTimeout(timeoutRef.current);
    };
  }, [user, resetTimer]);

  const login = (userData) => {
    localStorage.setItem('token', userData.token);
    localStorage.setItem('role', userData.role);
    localStorage.setItem('userId', userData.userId);
    localStorage.setItem('fullName', userData.fullName || '');
    setUser(userData);
  };

  const logout = () => {
    if (timeoutRef.current) clearTimeout(timeoutRef.current);
    doLogout();
  };

  if (loading) return <div>Loading...</div>;

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);

