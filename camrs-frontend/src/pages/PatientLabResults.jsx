import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import api from '../services/api';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { motion } from 'framer-motion';

const statusVariant = (status) => {
  const map = { COMPLETED: 'default', ORDERED: 'secondary', SAMPLE_COLLECTED: 'outline', IN_PROGRESS: 'secondary' };
  return map[status] || 'outline';
};

const PatientLabResults = () => {
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const res = await api.get('/patients/me/lab-results');
        setResults(res.data);
      } catch (err) { console.error(err); }
      finally { setLoading(false); }
    };
    fetch();
  }, []);

  const downloadPdf = async (orderId) => {
    try {
      const res = await api.get(`/patients/me/lab-results/${orderId}/pdf`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `lab-report-${orderId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) { alert('Failed to download report'); }
  };

  return (
    <Layout>
      <h1 className="text-2xl font-bold tracking-tight mb-6">Lab Results</h1>

      <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }}>
        <Card>
          <CardContent className="p-0">
            {loading ? (
              <div className="p-8 text-center text-muted-foreground">Loading...</div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Test</TableHead><TableHead>Doctor</TableHead><TableHead>Priority</TableHead>
                    <TableHead>Status</TableHead><TableHead>Result</TableHead><TableHead>Flag</TableHead><TableHead>Reference</TableHead>
                    <TableHead>Critical</TableHead><TableHead>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {results.map(r => (
                    <TableRow key={r.id}>
                      <TableCell className="font-medium">{r.testType}</TableCell>
                      <TableCell className="text-muted-foreground">{r.doctorName}</TableCell>
                      <TableCell>
                        <Badge variant={r.priority === 'STAT' ? 'destructive' : r.priority === 'URGENT' ? 'secondary' : 'outline'}>
                          {r.priority}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <Badge variant={statusVariant(r.status)}>{r.status}</Badge>
                      </TableCell>
                      <TableCell className="font-mono text-sm">{r.resultValue ? `${r.resultValue} ${r.unit || ''}` : 'Pending'}</TableCell>
                      <TableCell>
                        {r.resultFlag === 'HIGH' && <Badge variant="destructive">HIGH ↑</Badge>}
                        {r.resultFlag === 'LOW' && <Badge className="bg-orange-500 hover:bg-orange-600 text-white">LOW ↓</Badge>}
                        {r.resultFlag === 'NORMAL' && <Badge className="bg-green-600 hover:bg-green-700 text-white">Normal</Badge>}
                        {!r.resultFlag && r.resultValue && <span className="text-muted-foreground text-xs">—</span>}
                      </TableCell>
                      <TableCell className="text-muted-foreground text-sm">{r.referenceRange || '-'}</TableCell>
                      <TableCell>
                        {r.isCritical ? <Badge variant="destructive">Yes</Badge> : <span className="text-muted-foreground">-</span>}
                      </TableCell>
                      <TableCell>
                        {r.status === 'COMPLETED' && (
                          <Button size="sm" variant="outline" onClick={() => downloadPdf(r.id)}>PDF</Button>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                  {results.length === 0 && (
                    <TableRow><TableCell colSpan={9} className="text-center py-8 text-muted-foreground">No lab results found</TableCell></TableRow>
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

export default PatientLabResults;
