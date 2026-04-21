import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import api from '../services/api';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { motion } from 'framer-motion';

const billStatusVariant = (status) => {
  const map = { PAID: 'default', PARTIALLY_PAID: 'secondary', UNPAID: 'destructive' };
  return map[status] || 'outline';
};

const container = { hidden: {}, show: { transition: { staggerChildren: 0.06 } } };
const item = { hidden: { opacity: 0, y: 12 }, show: { opacity: 1, y: 0, transition: { duration: 0.3 } } };

const PatientBills = () => {
  const [bills, setBills] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchBills(); }, []);

  const fetchBills = async () => {
    try {
      const resp = await api.get('/patients/me/bills');
      setBills(resp.data);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  return (
    <Layout>
      <h1 className="text-2xl font-bold tracking-tight mb-6">My Bills</h1>

      {loading ? (
        <div className="text-center py-12 text-muted-foreground">Loading bills...</div>
      ) : bills.length === 0 ? (
        <Card className="p-10 text-center">
          <p className="text-muted-foreground">No bills found.</p>
        </Card>
      ) : (
        <motion.div className="space-y-3" variants={container} initial="hidden" animate="show">
          {bills.map(bill => (
            <motion.div key={bill.id} variants={item}>
              <Card className="hover:shadow-md transition-shadow">
                <CardContent className="pt-5 pb-4">
                  {/* Header */}
                  <div className="flex justify-between items-center">
                    <div>
                      <p className="font-semibold text-base">Invoice #{bill.invoiceNumber}</p>
                      <p className="text-sm text-muted-foreground mt-0.5">
                        Issued: {new Date(bill.issueDate).toLocaleString()}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="text-xl font-bold tracking-tight">₹{bill.totalAmount}</p>
                      <Badge variant={billStatusVariant(bill.status)} className="mt-1">{bill.status}</Badge>
                    </div>
                  </div>

                  {/* Charge breakdown */}
                  <Separator className="my-3" />
                  <div className="grid grid-cols-4 gap-3">
                    {[
                      ['Consultation', bill.consultationCharge || 0],
                      ['Lab', bill.labCharge || 0],
                      ['Medication', bill.medicationCharge || 0],
                      ['Tax', bill.tax || 0],
                    ].map(([label, val]) => (
                      <div key={label}>
                        <p className="text-xs text-muted-foreground">{label}</p>
                        <p className="font-semibold text-sm mt-0.5">₹{val}</p>
                      </div>
                    ))}
                  </div>

                  {/* View-only: no Pay button. Only admin can pay bills. */}
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </motion.div>
      )}
    </Layout>
  );
};

export default PatientBills;
