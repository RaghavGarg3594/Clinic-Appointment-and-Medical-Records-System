import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import api from '../services/api';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Skeleton } from '@/components/ui/skeleton';
import { Button } from '@/components/ui/button';
import { motion } from 'framer-motion';
import { FileText, Percent, FileOutput, CheckCircle } from 'lucide-react';

const billStatusVariant = (status) => {
  const map = { PAID: 'default', PARTIALLY_PAID: 'secondary', UNPAID: 'destructive' };
  return map[status] || 'outline';
};

const container = { hidden: {}, show: { transition: { staggerChildren: 0.06 } } };
const item = { hidden: { opacity: 0, y: 16 }, show: { opacity: 1, y: 0, transition: { duration: 0.3 } } };

const AdminBilling = () => {
  const [bills, setBills] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchBills(); }, []);

  const fetchBills = async () => {
    try {
      const resp = await api.get('/admin/bills');
      setBills(resp.data);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const markPaid = async (id) => {
    try {
      await api.put(`/admin/bills/${id}/mark-paid`);
      fetchBills();
    } catch (err) {
      alert('Failed to mark as paid');
    }
  };

  const applyDiscount = async (id) => {
    const p = prompt("Enter discount percentage (e.g., 10 for 10%):", "10");
    if (!p) return;
    const discountPercentage = parseFloat(p);
    if (isNaN(discountPercentage) || discountPercentage < 0 || discountPercentage > 100) {
      alert('Invalid percentage');
      return;
    }
    try {
      await api.put(`/admin/bills/${id}/discount`, { discountPercentage });
      fetchBills();
    } catch (err) {
      alert('Failed to apply discount');
    }
  };

  const downloadPdf = async (appointmentId, type) => {
    try {
      const endpoint = type === 'prescription'
        ? `/admin/appointments/${appointmentId}/prescription/pdf`
        : `/admin/appointments/${appointmentId}/lab-report/pdf`;

      const response = await api.get(endpoint, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `${type}-${appointmentId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      alert(`Failed to download ${type}. It may not exist yet.`);
    }
  };

  const totalRevenue = bills.reduce((sum, b) => sum + parseFloat(b.paidAmount || 0), 0);
  const totalUnpaid = bills.reduce((sum, b) => sum + (b.status !== 'PAID' ? parseFloat(b.dueAmount || 0) : 0), 0);

  const stats = [
    { label: 'Total Revenue (Paid)', value: `₹${totalRevenue.toFixed(2)}`, color: 'text-oxford', accent: 'border-l-oxford' },
    { label: 'Outstanding (Unpaid)', value: `₹${totalUnpaid.toFixed(2)}`, color: 'text-destructive', accent: 'border-l-destructive' },
    { label: 'Total Invoices', value: bills.length, color: 'text-foreground', accent: 'border-l-terracotta' },
  ];

  return (
    <Layout>
      <h1 className="text-2xl font-bold tracking-tight mb-6">Billing Overview</h1>

      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-5 mb-6">
          {Array.from({ length: 3 }).map((_, i) => <Skeleton key={i} className="h-28 rounded-xl" />)}
        </div>
      ) : (
        <motion.div className="grid grid-cols-1 sm:grid-cols-3 gap-5 mb-6" variants={container} initial="hidden" animate="show">
          {stats.map((s, i) => (
            <motion.div key={i} variants={item}>
              <Card className={`hover:shadow-lg transition-shadow duration-300 border-l-4 ${s.accent}`}>
                <CardContent className="pt-6">
                  <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">{s.label}</p>
                  <p className={`text-3xl font-extrabold tracking-tight ${s.color}`}>{s.value}</p>
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </motion.div>
      )}

      <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3, delay: 0.15 }}>
        <Card>
          <CardContent className="p-0">
            {loading ? (
              <div className="p-8 text-center text-muted-foreground">Loading billing data...</div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Invoice</TableHead>
                    <TableHead>Patient</TableHead>
                    <TableHead>Date</TableHead>
                    <TableHead>Breakdown</TableHead>
                    <TableHead>Total Amount</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {bills.map(bill => (
                    <TableRow key={bill.id}>
                      <TableCell className="font-mono text-sm">{bill.invoiceNumber}</TableCell>
                      <TableCell className="font-medium">{bill.patientName}</TableCell>
                      <TableCell className="text-muted-foreground">{new Date(bill.issueDate).toLocaleString()}</TableCell>
                      <TableCell className="text-xs">
                        {bill.consultationCharge > 0 && <div>Consult: ₹{bill.consultationCharge}</div>}
                        {bill.medicationCharge > 0 && <div>Meds: ₹{bill.medicationCharge}</div>}
                        {bill.labCharge > 0 && <div>Lab: ₹{bill.labCharge}</div>}
                        {bill.tax > 0 && <div>Tax (12%): ₹{bill.tax}</div>}
                        {bill.discount > 0 && <div className="text-terracotta font-bold">- Discount: ₹{bill.discount}</div>}
                        {bill.paidAmount > 0 && bill.status === 'UNPAID' && (
                          <div className="text-green-600 font-semibold mt-1 border-t border-border pt-1">✓ Already Paid: ₹{bill.paidAmount}</div>
                        )}
                      </TableCell>
                      <TableCell>
                        <div className="font-semibold text-lg">₹{bill.totalAmount}</div>
                        {bill.status === 'UNPAID' && bill.paidAmount > 0 && (
                          <div className="text-destructive font-bold text-sm">Due: ₹{bill.dueAmount}</div>
                        )}
                      </TableCell>
                      <TableCell>
                        <Badge variant={billStatusVariant(bill.status)}>{bill.status}</Badge>
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex items-center justify-end gap-2 flex-wrap">
                          {bill.status === 'UNPAID' && (
                            <>
                              <Button size="sm" onClick={() => markPaid(bill.id)} className="text-xs bg-primary hover:bg-primary/90 text-primary-foreground shadow-sm">
                                <CheckCircle className="w-3 h-3 mr-1" /> Mark Paid
                              </Button>
                              <Button size="sm" variant="outline" onClick={() => applyDiscount(bill.id)} className="text-xs border-dashed border-terracotta text-terracotta hover:bg-terracotta hover:text-white transition-all shadow-sm">
                                <Percent className="w-3 h-3 mr-1" /> Discount
                              </Button>
                            </>
                          )}
                          <Button size="sm" variant="secondary" onClick={() => downloadPdf(bill.appointmentId, 'prescription')} className="text-xs bg-muted hover:bg-oxford hover:text-primary-foreground transition-all shadow-sm">
                            <FileText className="w-3 h-3 mr-1" /> Rx PDF
                          </Button>
                          {bill.hasLabReport && (
                            <Button size="sm" variant="secondary" onClick={() => downloadPdf(bill.appointmentId, 'lab-report')} className="text-xs bg-muted hover:bg-oxford hover:text-primary-foreground transition-all shadow-sm">
                              <FileOutput className="w-3 h-3 mr-1" /> Lab PDF
                            </Button>
                          )}
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                  {bills.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">No billing records found.</TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </motion.div>
    </Layout>
  );
};

export default AdminBilling;
