import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import api from '../services/api';
import { Card, CardContent } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { motion } from 'framer-motion';

const container = { hidden: {}, show: { transition: { staggerChildren: 0.08 } } };
const item = { hidden: { opacity: 0, y: 16 }, show: { opacity: 1, y: 0, transition: { duration: 0.35 } } };

const PatientDashboard = () => {
  const [stats, setStats] = useState({ upcoming: 0, pendingLab: 0, unpaidBills: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [apptRes, billRes, labRes] = await Promise.all([
          api.get('/appointments/patient'),
          api.get('/patients/me/bills'),
          api.get('/patients/me/lab-results')
        ]);

        const now = new Date();
        const upcoming = apptRes.data.filter(a =>
          a.status === 'SCHEDULED' && new Date(a.appointmentDate) >= new Date(now.toDateString())
        ).length;

        const unpaidBills = billRes.data
          .filter(b => b.status !== 'PAID')
          .reduce((sum, b) => sum + (b.totalAmount || 0), 0);

        const pendingLab = labRes.data.filter(l => l.status !== 'COMPLETED').length;

        setStats({ upcoming, pendingLab, unpaidBills });
      } catch (err) {
        console.error('Dashboard fetch error:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  const cards = [
    { label: 'Upcoming Appointments', value: stats.upcoming, accent: 'border-l-terracotta', color: 'text-terracotta' },
    { label: 'Pending Lab Results', value: stats.pendingLab, accent: 'border-l-rustic', color: 'text-rustic' },
    { label: 'Unpaid Bills', value: `₹${stats.unpaidBills.toFixed(2)}`, accent: 'border-l-destructive', color: 'text-destructive' },
  ];

  return (
    <Layout>
      <h1 className="text-2xl font-bold tracking-tight mb-6">Patient Dashboard</h1>

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

export default PatientDashboard;
