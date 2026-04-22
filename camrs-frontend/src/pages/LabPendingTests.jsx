import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import api from '../services/api';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { motion } from 'framer-motion';

const priorityVariant = (priority) => {
  const map = { STAT: 'destructive', URGENT: 'secondary', ROUTINE: 'outline' };
  return map[priority] || 'outline';
};

const statusVariant = (status) => {
  const map = { ORDERED: 'secondary', SAMPLE_COLLECTED: 'outline', IN_PROGRESS: 'secondary', COMPLETED: 'default' };
  return map[status] || 'outline';
};

const REFERENCE_RANGES = [
  { test: 'Complete Blood Count (RBC)', male: '4.32 - 5.72 trillion cells/L', female: '3.90 - 5.03 trillion cells/L', unit: 'cells/L' },
  { test: 'Hemoglobin (Hb)', male: '13.5 - 17.5 g/dL', female: '12.0 - 15.5 g/dL', unit: 'g/dL' },
  { test: 'Fasting Blood Sugar (FBS)', male: '70 - 100 mg/dL', female: '70 - 100 mg/dL', unit: 'mg/dL' },
  { test: 'Lipid Panel (Total Cholesterol)', male: '< 200 mg/dL', female: '< 200 mg/dL', unit: 'mg/dL' },
  { test: 'Serum Creatinine', male: '0.74 - 1.35 mg/dL', female: '0.59 - 1.04 mg/dL', unit: 'mg/dL' },
  { test: 'TSH (Thyroid)', male: '0.4 - 4.0 mIU/L', female: '0.4 - 4.0 mIU/L', unit: 'mIU/L' }
];

const LabPendingTests = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showResultModal, setShowResultModal] = useState(false);
  const [showReferenceModal, setShowReferenceModal] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [resultForm, setResultForm] = useState({
    resultValue: '', unit: '', referenceRange: '', isCritical: false, notes: ''
  });

  useEffect(() => { fetchOrders(); }, []);

  const fetchOrders = async () => {
    try {
      const resp = await api.get('/lab/orders/pending');
      setOrders(resp.data);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handleCollect = async (id) => {
    try {
      await api.put(`/lab/orders/${id}/collect`);
      fetchOrders();
    } catch (err) {
      alert(err.response?.data?.message || err.response?.data || 'Failed to update status. Bill may not be paid yet.');
    }
  };

  const openResultModal = (order) => {
    setSelectedOrder(order);
    setResultForm({ resultValue: '', unit: '', referenceRange: '', isCritical: false, notes: '' });
    setShowResultModal(true);
  };

  const handleResultSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post(`/lab/orders/${selectedOrder.id}/results`, resultForm);
      setShowResultModal(false);
      fetchOrders();
    } catch (err) { alert('Failed to submit results'); }
  };

  return (
    <Layout>
      <h1 className="text-2xl font-bold tracking-tight mb-6">Pending Lab Tests</h1>

      <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }}>
        <Card>
          <CardContent className="p-0">
            {loading ? (
              <div className="p-8 text-center text-muted-foreground">Loading orders...</div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Order ID</TableHead><TableHead>Patient</TableHead><TableHead>Test Type</TableHead>
                    <TableHead>Priority</TableHead><TableHead>Ordered By</TableHead><TableHead>Status</TableHead><TableHead>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {orders.map(order => (
                    <TableRow key={order.id}>
                      <TableCell className="font-mono text-sm">#{order.id}</TableCell>
                      <TableCell className="font-medium">{order.patientName}</TableCell>
                      <TableCell>{order.testType}</TableCell>
                      <TableCell>
                        <Badge variant={priorityVariant(order.priority)}>{order.priority}</Badge>
                      </TableCell>
                      <TableCell className="text-muted-foreground">Dr. {order.doctorName}</TableCell>
                      <TableCell>
                        <Badge variant={statusVariant(order.status)}>{order.status?.replace('_', ' ')}</Badge>
                      </TableCell>
                      <TableCell>
                        <div className="flex gap-1.5 items-center">
                          {order.status === 'ORDERED' && (
                            order.billPaid ? (
                              <Button size="sm" variant="outline" onClick={() => handleCollect(order.id)}>Collect Sample</Button>
                            ) : (
                              <span className="text-xs text-destructive font-medium">⏳ Awaiting Payment</span>
                            )
                          )}
                          {(order.status === 'SAMPLE_COLLECTED' || order.status === 'IN_PROGRESS') && (
                            <Button size="sm" className="bg-primary hover:bg-primary/90 text-primary-foreground" onClick={() => openResultModal(order)}>Enter Results</Button>
                          )}
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                  {orders.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">No pending lab tests at this time.</TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </motion.div>

      <Dialog open={showResultModal} onOpenChange={setShowResultModal}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Enter Lab Results — {selectedOrder?.testType}</DialogTitle>
          </DialogHeader>
          <p className="text-sm text-muted-foreground mb-3">Patient: {selectedOrder?.patientName}</p>
          <form onSubmit={handleResultSubmit} className="space-y-4">
            <div className="grid grid-cols-3 gap-3">
              <div className="space-y-2 col-span-2">
                <Label>Result Value</Label>
                <Input required value={resultForm.resultValue}
                  onChange={e => setResultForm({ ...resultForm, resultValue: e.target.value })} placeholder="e.g. 120" />
              </div>
              <div className="space-y-2">
                <Label>Unit</Label>
                <Input value={resultForm.unit}
                  onChange={e => setResultForm({ ...resultForm, unit: e.target.value })} placeholder="mg/dL" />
              </div>
            </div>
            <div className="space-y-2">
              <div className="flex justify-between items-center mb-1">
                <Label>Reference Range</Label>
                <Button type="button" variant="link" size="sm" onClick={() => setShowReferenceModal(true)} className="h-auto p-0 text-terracotta">
                  View Reference Ranges
                </Button>
              </div>
              <Input value={resultForm.referenceRange}
                onChange={e => setResultForm({ ...resultForm, referenceRange: e.target.value })} placeholder="70 - 100 mg/dL" />
            </div>
            <div className="flex items-center gap-2">
              <input type="checkbox" id="critical" checked={resultForm.isCritical}
                onChange={e => setResultForm({ ...resultForm, isCritical: e.target.checked })}
                className="rounded border-input" />
              <Label htmlFor="critical" className="text-destructive font-semibold cursor-pointer">Flag as Critical</Label>
            </div>
            <div className="space-y-2">
              <Label>Notes</Label>
              <Textarea rows={2} value={resultForm.notes}
                onChange={e => setResultForm({ ...resultForm, notes: e.target.value })}
                placeholder="Additional observations..." />
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <Button type="button" variant="outline" onClick={() => setShowResultModal(false)}>Cancel</Button>
              <Button type="submit">Submit Results</Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>

      <Dialog open={showReferenceModal} onOpenChange={setShowReferenceModal}>
        <DialogContent className="sm:max-w-[700px]">
          <DialogHeader>
            <DialogTitle>Standard Medical Reference Ranges</DialogTitle>
          </DialogHeader>
          <div className="overflow-x-auto mt-2">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Test Category</TableHead>
                  <TableHead>Normal Range (Male)</TableHead>
                  <TableHead>Normal Range (Female)</TableHead>
                  <TableHead>Unit</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {REFERENCE_RANGES.map((ref, idx) => (
                  <TableRow key={idx}>
                    <TableCell className="font-medium">{ref.test}</TableCell>
                    <TableCell>{ref.male}</TableCell>
                    <TableCell>{ref.female}</TableCell>
                    <TableCell className="text-muted-foreground">{ref.unit}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
          <div className="flex justify-end pt-2">
            <Button variant="outline" onClick={() => setShowReferenceModal(false)}>Close</Button>
          </div>
        </DialogContent>
      </Dialog>
    </Layout>
  );
};

export default LabPendingTests;
