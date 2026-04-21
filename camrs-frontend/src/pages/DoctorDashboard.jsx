import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import api from '../services/api';
import { Card, CardContent } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { motion } from 'framer-motion';

const container = { hidden: {}, show: { transition: { staggerChildren: 0.08 } } };
const item = { hidden: { opacity: 0, y: 16 }, show: { opacity: 1, y: 0, transition: { duration: 0.35 } } };

const DoctorDashboard = () => {
  const [stats, setStats] = useState({ todayAppts: 0, completed: 0, inProgress: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const today = new Date().toISOString().split('T')[0];
        const res = await api.get(`/appointments/doctor?date=${today}`);
        const appts = res.data;
        setStats({
          todayAppts: appts.length,
          completed: appts.filter(a => a.status === 'COMPLETED').length,
          inProgress: appts.filter(a => a.status === 'IN_PROGRESS' || a.status === 'CHECKED_IN').length,
        });
      } catch (err) {
        console.error('Dashboard fetch error:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  const cards = [
    { label: "Today's Appointments", value: stats.todayAppts, accent: 'border-l-terracotta', color: 'text-terracotta' },
    { label: 'Completed', value: stats.completed, accent: 'border-l-oxford', color: 'text-oxford' },
    { label: 'In Progress / Checked In', value: stats.inProgress, accent: 'border-l-rustic', color: 'text-rustic' },
  ];

  return (
    <Layout>
      <h1 className="text-2xl font-bold tracking-tight mb-6">Doctor Dashboard</h1>

      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
          {Array.from({ length: 3 }).map((_, i) => <Skeleton key={i} className="h-28 rounded-xl" />)}
        </div>
      ) : (
        <motion.div className="grid grid-cols-1 sm:grid-cols-3 gap-5" variants={container} initial="hidden" animate="show">
          {cards.map((c, i) => (
            <motion.div key={i} variants={item}>
              <Card className={`hover:shadow-lg transition-shadow duration-300 border-l-4 ${c.accent}`}>
                <CardContent className="pt-6">
                  <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">{c.label}</p>
                  <p className={`text-3xl font-extrabold tracking-tight ${c.color}`}>{c.value}</p>
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </motion.div>
      )}
    </Layout>
  );
};

export default DoctorDashboard;
