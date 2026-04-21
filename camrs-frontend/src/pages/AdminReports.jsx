import React, { useState, useEffect, useCallback } from 'react';
import Layout from '../components/Layout';
import api from '../services/api';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Skeleton } from '@/components/ui/skeleton';
import { motion } from 'framer-motion';

const billStatusVariant = (status) => {
  const map = { PAID: 'default', PARTIALLY_PAID: 'secondary', UNPAID: 'destructive' };
  return map[status] || 'outline';
};

const AdminReports = () => {
  const [activeTab, setActiveTab] = useState('consultation');
  const [consultationStats, setConsultationStats] = useState([]);
  const [diseaseStats, setDiseaseStats] = useState([]);
  const [outstandingPayments, setOutstandingPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [refreshing, setRefreshing] = useState(false);

  const fetchAll = useCallback(async (isManual = false) => {
    if (isManual) setRefreshing(true);
    try {
      const [consRes, diseaseRes, paymentRes] = await Promise.all([
        api.get('/admin/reports/consultation-stats'),
        api.get('/admin/reports/disease-stats'),
        api.get('/admin/reports/outstanding-payments'),
      ]);
      setConsultationStats(consRes.data);
      setDiseaseStats(diseaseRes.data);
      setOutstandingPayments(paymentRes.data);
      setLastUpdated(new Date());
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    fetchAll();
    const interval = setInterval(() => fetchAll(), 30000);
    return () => clearInterval(interval);
  }, [fetchAll]);

  const exportCSV = () => {
    let data = [];
    let headers = [];
    let filename = "";

    if (activeTab === 'consultation') {
      headers = ['Doctor Name', 'Specialization', 'Total Appointments', 'Completed', 'Cancelled', 'Revenue'];
      data = consultationStats.map(row => [
        row.doctorName, row.specialization, row.totalAppointments, row.completedAppointments, row.cancelledAppointments, row.revenue
      ]);
      filename = "Consultation_Stats.csv";
    } else if (activeTab === 'disease') {
      headers = ['Diagnosis', 'Count'];
      data = diseaseStats.map(row => [row.diagnosis, row.count]);
      filename = "Disease_Statistics.csv";
    } else if (activeTab === 'payments') {
      headers = ['Invoice Number', 'Patient Name', 'Amount', 'Status', 'Issue Date'];
      data = outstandingPayments.map(row => [
        row.invoiceNumber, row.patientName, row.totalAmount, row.status, row.issueDate ? new Date(row.issueDate).toLocaleString() : ''
      ]);
      filename = "Outstanding_Payments.csv";
    }

    if (data.length === 0) {
      alert("No data to export");
      return;
    }

    const csvContent = [
      headers.join(","),
      ...data.map(row => row.map(cell => `"${String(cell || '').replace(/"/g, '""')}"`).join(","))
    ].join("\n");

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement("a");
    const url = URL.createObjectURL(blob);
    link.setAttribute("href", url);
    link.setAttribute("download", filename);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handlePrint = () => {
    window.print();
  };

  return (
    <Layout>
      <style>{`
        @media print {
          body * {
            visibility: hidden;
          }
          .print-area, .print-area * {
            visibility: visible;
          }
          .print-area {
            position: absolute;
            left: 0;
            top: 0;
            width: 100%;
          }
        }
      `}</style>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold tracking-tight">Reports</h1>
        <div className="flex items-center gap-3">
          {lastUpdated && (
            <span className="text-xs text-muted-foreground">Last updated: {lastUpdated.toLocaleTimeString()}</span>
          )}
          <Button variant="outline" size="sm" onClick={exportCSV}>
            Export CSV
          </Button>
          <Button variant="outline" size="sm" onClick={handlePrint}>
            Print
          </Button>
          <Button variant="outline" size="sm" onClick={() => fetchAll(true)} disabled={refreshing}>
            {refreshing ? 'Refreshing...' : '⟳ Refresh'}
          </Button>
        </div>
      </div>

      {loading ? (
        <Skeleton className="h-64 rounded-xl" />
      ) : (
        <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }} className="print-area">
          <Card>
            <CardContent className="pt-6">
              <Tabs value={activeTab} onValueChange={setActiveTab}>
                <TabsList className="mb-6 mx-auto flex w-fit shadow-sm">
                  <TabsTrigger value="consultation">Consultation Stats</TabsTrigger>
                  <TabsTrigger value="disease">Disease Statistics</TabsTrigger>
                  <TabsTrigger value="payments">Outstanding Payments</TabsTrigger>
                </TabsList>

                <TabsContent value="consultation">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Doctor</TableHead><TableHead>Specialization</TableHead>
                        <TableHead>Total Appts</TableHead><TableHead>Completed</TableHead>
                        <TableHead>Cancelled</TableHead><TableHead>Revenue</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {consultationStats.map((row, i) => (
                        <TableRow key={i}>
                          <TableCell className="font-medium">{row.doctorName}</TableCell>
                          <TableCell className="text-muted-foreground">{row.specialization}</TableCell>
                          <TableCell>{row.totalAppointments}</TableCell>
                          <TableCell>{row.completedAppointments}</TableCell>
                          <TableCell>{row.cancelledAppointments}</TableCell>
                          <TableCell className="font-semibold">₹{parseFloat(row.revenue || 0).toFixed(2)}</TableCell>
                        </TableRow>
                      ))}
                      {consultationStats.length === 0 && (
                        <TableRow><TableCell colSpan={6} className="text-center py-6 text-muted-foreground">No data available</TableCell></TableRow>
                      )}
                    </TableBody>
                  </Table>
                </TabsContent>

                <TabsContent value="disease">
                  <Table>
                    <TableHeader>
                      <TableRow><TableHead>ICD-10 Code</TableHead><TableHead>Diagnosis</TableHead><TableHead>Count</TableHead></TableRow>
                    </TableHeader>
                    <TableBody>
                      {diseaseStats.map((row, i) => (
                        <TableRow key={i}>
                          <TableCell className="font-mono text-sm text-muted-foreground">{row.icd10Code || '—'}</TableCell>
                          <TableCell className="font-medium">{row.diagnosis}</TableCell>
                          <TableCell>
                            <Badge variant="secondary">{row.count}</Badge>
                          </TableCell>
                        </TableRow>
                      ))}
                      {diseaseStats.length === 0 && (
                        <TableRow><TableCell colSpan={3} className="text-center py-6 text-muted-foreground">No data available</TableCell></TableRow>
                      )}
                    </TableBody>
                  </Table>
                </TabsContent>

                <TabsContent value="payments">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Invoice</TableHead><TableHead>Patient</TableHead>
                        <TableHead>Amount</TableHead><TableHead>Status</TableHead><TableHead>Issue Date</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {outstandingPayments.map((row, i) => (
                        <TableRow key={i}>
                          <TableCell className="font-mono text-sm">{row.invoiceNumber}</TableCell>
                          <TableCell className="font-medium">{row.patientName}</TableCell>
                          <TableCell className="font-semibold">₹{parseFloat(row.totalAmount || 0).toFixed(2)}</TableCell>
                          <TableCell>
                            <Badge variant={billStatusVariant(row.status)}>{row.status}</Badge>
                          </TableCell>
                          <TableCell className="text-muted-foreground">{row.issueDate ? new Date(row.issueDate).toLocaleString() : '-'}</TableCell>
                        </TableRow>
                      ))}
                      {outstandingPayments.length === 0 && (
                        <TableRow><TableCell colSpan={5} className="text-center py-6 text-muted-foreground">No outstanding payments</TableCell></TableRow>
                      )}
                    </TableBody>
                  </Table>
                </TabsContent>
              </Tabs>
            </CardContent>
          </Card>
        </motion.div>
      )}
    </Layout>
  );
};

export default AdminReports;
