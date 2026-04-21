import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import api from '../services/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { motion } from 'framer-motion';
import { PieChart, Pie, Cell, AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Legend } from 'recharts';
import { ChartContainer, ChartTooltip, ChartTooltipContent, ChartLegend, ChartLegendContent } from '@/components/ui/chart';

const statCards = [
  { key: 'totalPatients', label: 'Total Patients', format: (v) => v || 0, accent: 'border-l-terracotta', color: 'text-terracotta' },
  { key: 'todayRevenue', label: "Today's Revenue", format: (v) => `₹${parseFloat(v || 0).toFixed(2)}`, accent: 'border-l-oxford', color: 'text-oxford' },
  { key: 'lowStockMedications', label: 'Low Stock Medications', format: (v) => v || 0, accent: 'border-l-rustic', color: 'text-rustic' },
  { key: 'totalAppointmentsToday', label: "Today's Appointments", format: (v) => v || 0, accent: 'border-l-terracotta', color: 'text-terracotta' },
  { key: 'totalRevenue', label: 'Total Revenue', format: (v) => `₹${parseFloat(v || 0).toFixed(2)}`, accent: 'border-l-oxford', color: 'text-oxford' },
  { key: 'outstandingAmount', label: 'Outstanding Amount', format: (v) => `₹${parseFloat(v || 0).toFixed(2)}`, accent: 'border-l-destructive', color: 'text-destructive' },
];

const container = { hidden: {}, show: { transition: { staggerChildren: 0.06 } } };
const item = { hidden: { opacity: 0, y: 16 }, show: { opacity: 1, y: 0, transition: { duration: 0.35 } } };

const AdminDashboard = () => {
  const [stats, setStats] = useState({});
  const [diseaseStats, setDiseaseStats] = useState([]);
  const [revenueStats, setRevenueStats] = useState([]);
  const [loading, setLoading] = useState(true);

  // Earthy chart colors
  const COLORS = ['#C06A45', '#4A5568', '#A67C52', '#D4946A', '#7B6E63', '#8B7355'];

  const [diseaseChartConfig, setDiseaseChartConfig] = useState({
    count: { label: "Cases" }
  });

  const revenueChartConfig = {
    revenue: {
      label: "Revenue (₹)",
      color: "#C06A45"
    }
  };

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [res, disRes, billsRes] = await Promise.all([
          api.get('/admin/dashboard'),
          api.get('/admin/reports/disease-stats'),
          api.get('/admin/bills')
        ]);
        setStats(res.data);
        
        const newDiseaseConfig = { count: { label: "Cases" } };
        const formattedDiseaseStats = disRes.data.slice(0, 5).map((d, i) => {
          const safeId = `diag_${i}`;
          
          newDiseaseConfig[safeId] = {
            label: d.diagnosis,
            color: COLORS[i % COLORS.length]
          };

          return {
            ...d,
            id: safeId,
            fill: `var(--color-${safeId})`
          };
        });
        setDiseaseStats(formattedDiseaseStats);
        setDiseaseChartConfig(newDiseaseConfig);

        const revenueMap = {};
        billsRes.data.forEach(b => {
          if (b.status === 'PAID') {
            const date = new Date(b.issueDate).toLocaleDateString('en-GB', { day: '2-digit', month: 'short' });
            revenueMap[date] = (revenueMap[date] || 0) + parseFloat(b.totalAmount || 0);
          }
        });
        
        const revData = Object.keys(revenueMap).map(k => ({ date: k, revenue: revenueMap[k] }))
          .sort((a,b) => new Date(a.date) - new Date(b.date));
        setRevenueStats(revData);

      } catch (err) {
        console.error('Dashboard fetch error:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  return (
    <Layout>
      <h1 className="text-2xl font-bold tracking-tight mb-6">Admin Dashboard</h1>

      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {Array.from({ length: 6 }).map((_, i) => (
            <Skeleton key={i} className="h-28 rounded-xl" />
          ))}
        </div>
      ) : (
        <motion.div
          className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5"
          variants={container}
          initial="hidden"
          animate="show"
        >
          {statCards.map((s) => (
            <motion.div key={s.key} variants={item}>
              <Card className={`hover:shadow-lg transition-shadow duration-300 group border-l-4 ${s.accent}`}>
                <CardContent className="pt-6">
                  <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">{s.label}</p>
                  <p className={`text-3xl font-extrabold tracking-tight ${s.color}`}>
                    {s.format(stats[s.key])}
                  </p>
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </motion.div>
      )}

      {/* Charts Section */}
      {!loading && (
        <motion.div
          className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-8"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.2 }}
        >
          {/* Disease Stats Pie Chart */}
          <Card className="flex flex-col">
            <CardHeader className="items-center pb-0">
              <CardTitle className="text-lg">Top Diagnoses</CardTitle>
            </CardHeader>
            <CardContent className="flex-1 pb-0 mt-4">
              {diseaseStats.length > 0 ? (
                <ChartContainer
                  config={diseaseChartConfig}
                  className="mx-auto aspect-[4/3] max-h-[300px]"
                >
                  <PieChart>
                    <ChartTooltip
                      cursor={false}
                      content={<ChartTooltipContent hideLabel />}
                    />
                    <Pie
                      data={diseaseStats}
                      dataKey="count"
                      nameKey="id"
                      outerRadius={90}
                      labelLine={true}
                      label={({ payload, ...props }) => {
                        return (
                          <text
                            cx={props.cx}
                            cy={props.cy}
                            x={props.x}
                            y={props.y}
                            textAnchor={props.textAnchor}
                            dominantBaseline={props.dominantBaseline}
                            fill="var(--color-foreground)"
                            className="text-xs"
                          >
                            {payload.diagnosis}
                          </text>
                        )
                      }}
                    />
                    <ChartLegend content={<ChartLegendContent nameKey="id" />} className="-translate-y-2 flex-wrap gap-2 [&>*]:justify-center" />
                  </PieChart>
                </ChartContainer>
              ) : (
                <div className="flex h-[300px] items-center justify-center text-muted-foreground">
                  No diagnosis data available
                </div>
              )}
            </CardContent>
          </Card>

          {/* Revenue Area Chart */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Total Revenue</CardTitle>
            </CardHeader>
            <CardContent className="h-80">
              {revenueStats.length > 0 ? (
                <ChartContainer config={revenueChartConfig} className="h-[300px] w-full">
                  <AreaChart data={revenueStats} margin={{ top: 10, right: 10, left: 10, bottom: 0 }}>
                    <defs>
                      <linearGradient id="fillRevenue" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#C06A45" stopOpacity={0.3} />
                        <stop offset="95%" stopColor="#C06A45" stopOpacity={0.02} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid vertical={false} strokeDasharray="3 3" className="stroke-border" />
                    <XAxis 
                      dataKey="date" 
                      tickLine={false}
                      axisLine={false}
                      tickMargin={8}
                    />
                    <YAxis 
                      tickLine={false}
                      axisLine={false}
                      tickMargin={8}
                      tickFormatter={(value) => `₹${value}`}
                    />
                    <ChartTooltip
                      cursor={false}
                      content={<ChartTooltipContent indicator="line" />}
                    />
                    <Area
                      type="natural"
                      dataKey="revenue"
                      fillOpacity={1}
                      fill="url(#fillRevenue)"
                      stroke="#C06A45"
                      strokeWidth={2}
                    />
                  </AreaChart>
                </ChartContainer>
              ) : (
                <div className="flex h-[300px] items-center justify-center text-muted-foreground">
                  No revenue data available
                </div>
              )}
            </CardContent>
          </Card>
        </motion.div>
      )}
    </Layout>
  );
};

export default AdminDashboard;
