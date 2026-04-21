import React, { useState } from 'react';
import Layout from '../components/Layout';
import api from '../services/api';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Separator } from '@/components/ui/separator';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { motion, AnimatePresence } from 'framer-motion';

const severityVariant = (severity) => {
  const map = { CRITICAL: 'destructive', HIGH: 'secondary', MODERATE: 'outline', LOW: 'default' };
  return map[severity] || 'outline';
};

const DoctorPrescriptionHistory = () => {
  const [patientId, setPatientId] = useState('');
  const [records, setRecords] = useState([]);
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [expandedId, setExpandedId] = useState(null);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!patientId) return;
    setLoading(true);
    setError('');
    setSearched(true);
    setExpandedId(null);
    try {
      const res = await api.get(`/consultations/patient/${patientId}/records`);
      setRecords(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'No records found for this Patient ID.');
      setRecords([]);
    } finally {
      setLoading(false);
    }
  };

  const toggleExpand = (id) => {
    setExpandedId(expandedId === id ? null : id);
  };

  const downloadPrescriptionPdf = async (recordId, e) => {
    e.stopPropagation();
    try {
      const res = await api.get(`/doctors/prescriptions/${recordId}/pdf`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `prescription-${recordId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      alert('Failed to download prescription PDF');
    }
  };

  return (
    <Layout>
      <h1 className="text-2xl font-bold tracking-tight mb-6">Prescription History</h1>

      {/* Search Bar */}
      <motion.div initial={{ opacity: 0, y: 6 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }}>
        <Card className="mb-5">
          <CardContent className="pt-5 pb-4">
            <form onSubmit={handleSearch} className="flex items-end gap-3">
              <div className="flex-1 space-y-1">
                <Label className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">Patient ID</Label>
                <Input
                  type="number"
                  value={patientId}
                  onChange={e => setPatientId(e.target.value)}
                  placeholder="Enter Patient ID (e.g. 1, 2, 3...)"
                  className="h-10"
                  required
                />
              </div>
              <Button type="submit" disabled={loading} className="h-10 px-6">
                {loading ? 'Searching...' : 'Search'}
              </Button>
            </form>
          </CardContent>
        </Card>
      </motion.div>

      {error && (
        <Alert variant="destructive" className="mb-4">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* Results */}
      {searched && !loading && records.length === 0 && !error && (
        <Card className="p-10 text-center">
          <p className="text-muted-foreground">No consultation records found for Patient ID: {patientId}</p>
        </Card>
      )}

      {records.length > 0 && (
        <motion.div className="space-y-3" initial="hidden" animate="show"
          variants={{ hidden: {}, show: { transition: { staggerChildren: 0.04 } } }}>
          <p className="text-sm text-muted-foreground mb-2">
            Showing <strong>{records.length}</strong> consultation(s) for Patient ID: <strong>{patientId}</strong>
          </p>

          {records.map(record => (
            <motion.div key={record.id}
              variants={{ hidden: { opacity: 0, y: 12 }, show: { opacity: 1, y: 0, transition: { duration: 0.3 } } }}
              layout>
              <Card
                className="cursor-pointer hover:shadow-md transition-shadow"
                onClick={() => toggleExpand(record.id)}
              >
                <CardContent className="pt-5 pb-4">
                  {/* Header */}
                  <div className="flex justify-between items-start gap-4">
                    <div className="min-w-0 flex-1">
                      <p className="font-semibold text-base truncate">{record.patientName}</p>
                      <p className="text-sm text-muted-foreground mt-0.5">
                        Visit on {new Date(record.visitDate).toLocaleString()} — {record.diagnosis || 'No diagnosis'}
                      </p>
                    </div>
                    <div className="flex items-center gap-2 shrink-0">
                      {record.prescriptionItems?.length > 0 && (
                        <Badge variant="outline" className="border-terracotta/40 text-terracotta text-[0.65rem] px-2">
                          {record.prescriptionItems.length} Rx
                        </Badge>
                      )}
                      <Badge variant={severityVariant(record.severity)}>{record.severity}</Badge>
                      <span className="text-sm text-muted-foreground ml-1">{expandedId === record.id ? '▲' : '▼'}</span>
                    </div>
                  </div>

                  {/* Expanded */}
                  <AnimatePresence>
                    {expandedId === record.id && (
                      <motion.div
                        initial={{ opacity: 0, height: 0 }}
                        animate={{ opacity: 1, height: 'auto' }}
                        exit={{ opacity: 0, height: 0 }}
                        transition={{ duration: 0.25 }}
                        className="overflow-hidden"
                      >
                        <Separator className="my-4" />

                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                          {[
                            ['Chief Complaint', record.chiefComplaint],
                            ['Vital Signs', record.vitalSigns],
                            ['Diagnosis', record.diagnosis],
                            ['Follow-Up', record.followUpDate ? new Date(record.followUpDate).toLocaleDateString() : 'None'],
                          ].map(([label, val]) => (
                            <div key={label}>
                              <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-1">{label}</p>
                              <p className="text-sm">{val || '—'}</p>
                            </div>
                          ))}
                        </div>

                        {record.advice && (
                          <div className="mb-4">
                            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-1">Advice</p>
                            <div className="bg-primary/10 border-l-3 border-primary rounded-md p-3 text-sm">
                              {record.advice}
                            </div>
                          </div>
                        )}

                        {record.prescriptionItems?.length > 0 ? (
                          <div>
                            <div className="flex items-center justify-between mb-2">
                              <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">Prescribed Medications</p>
                              <Button size="sm" variant="outline" className="text-xs"
                                onClick={(e) => downloadPrescriptionPdf(record.id, e)}>
                                Download Rx PDF
                              </Button>
                            </div>
                            <div className="rounded-lg border border-border overflow-hidden">
                              <Table>
                                <TableHeader>
                                  <TableRow className="bg-muted/40">
                                    <TableHead>Medication</TableHead>
                                    <TableHead>Dosage</TableHead>
                                    <TableHead>Frequency</TableHead>
                                    <TableHead>Duration</TableHead>
                                    <TableHead>Route</TableHead>
                                    <TableHead>Instructions</TableHead>
                                  </TableRow>
                                </TableHeader>
                                <TableBody>
                                  {record.prescriptionItems.map(rx => (
                                    <TableRow key={rx.id}>
                                      <TableCell className="font-medium">{rx.medicationName}</TableCell>
                                      <TableCell>{rx.dosage}</TableCell>
                                      <TableCell>{rx.frequency}</TableCell>
                                      <TableCell>{rx.duration}</TableCell>
                                      <TableCell className="text-muted-foreground">{rx.route}</TableCell>
                                      <TableCell className="text-muted-foreground">{rx.mealInstruction}</TableCell>
                                    </TableRow>
                                  ))}
                                </TableBody>
                              </Table>
                            </div>
                          </div>
                        ) : (
                          <div className="rounded-lg border border-dashed border-border p-4 text-center">
                            <p className="text-sm text-muted-foreground">No medications prescribed.</p>
                          </div>
                        )}
                      </motion.div>
                    )}
                  </AnimatePresence>
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </motion.div>
      )}
    </Layout>
  );
};

export default DoctorPrescriptionHistory;
