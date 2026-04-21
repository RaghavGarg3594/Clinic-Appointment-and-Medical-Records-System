import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import api from '../services/api';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { motion, AnimatePresence } from 'framer-motion';

const severityVariant = (severity) => {
  const map = { CRITICAL: 'destructive', HIGH: 'secondary', MODERATE: 'outline', LOW: 'default' };
  return map[severity] || 'outline';
};

const PatientMedicalRecords = () => {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedId, setExpandedId] = useState(null);

  useEffect(() => { fetchRecords(); }, []);

  const fetchRecords = async () => {
    try {
      const resp = await api.get('/patients/me/medical-records');
      setRecords(resp.data);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const toggleExpand = (id) => {
    setExpandedId(expandedId === id ? null : id);
  };

  const downloadPrescriptionPdf = async (recordId, e) => {
    e.stopPropagation();
    try {
      const res = await api.get(`/patients/me/prescriptions/${recordId}/pdf`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `prescription-${recordId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error('PDF download error:', err);
      alert('Failed to download prescription PDF');
    }
  };

  return (
    <Layout>
      <h1 className="text-2xl font-bold tracking-tight mb-6">My Medical Records</h1>

      {loading ? (
        <div className="text-center py-12 text-muted-foreground">Loading records...</div>
      ) : records.length === 0 ? (
        <Card className="p-10 text-center">
          <p className="text-muted-foreground">No medical records found. Records will appear here after your first consultation.</p>
        </Card>
      ) : (
        <div className="space-y-3">
          {records.map(record => (
            <motion.div key={record.id} layout initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }}>
              <Card className="cursor-pointer hover:shadow-md transition-shadow" onClick={() => toggleExpand(record.id)}>
                <CardContent className="pt-5 pb-4">
                  {/* Header */}
                  <div className="flex justify-between items-center">
                    <div>
                      <p className="font-semibold text-base">Visit on {new Date(record.visitDate).toLocaleString()}</p>
                      <p className="text-sm text-muted-foreground mt-0.5">
                        Dr. {record.doctorName} — {record.specialization}
                      </p>
                    </div>
                    <div className="flex items-center gap-2">
                      {record.prescriptionItems?.length > 0 && (
                        <Button size="sm" variant="outline" onClick={(e) => downloadPrescriptionPdf(record.id, e)}>
                          Prescription PDF
                        </Button>
                      )}
                      <Badge variant={severityVariant(record.severity)}>{record.severity}</Badge>
                      <span className="text-sm text-muted-foreground">{expandedId === record.id ? '▲' : '▼'}</span>
                    </div>
                  </div>

                  {/* Expanded details */}
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
                        <div className="grid grid-cols-2 gap-4 mb-4">
                          {[
                            ['Chief Complaint', record.chiefComplaint],
                            ['Vital Signs', record.vitalSigns],
                            ['Diagnosis', record.diagnosis],
                            ['Follow-Up', record.followUpDate ? new Date(record.followUpDate).toLocaleString() : 'None scheduled'],
                          ].map(([label, val]) => (
                            <div key={label}>
                              <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-1">{label}</p>
                              <p className="text-sm">{val || '—'}</p>
                            </div>
                          ))}
                        </div>

                        {record.advice && (
                          <div className="mb-4">
                            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-1">Doctor's Advice</p>
                            <div className="bg-primary/10 border-l-3 border-primary rounded-md p-3 text-sm">
                              {record.advice}
                            </div>
                          </div>
                        )}

                        {record.prescriptionItems?.length > 0 && (
                          <div>
                            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-2">Prescribed Medications</p>
                            <Table>
                              <TableHeader>
                                <TableRow>
                                  <TableHead>Medication</TableHead><TableHead>Dosage</TableHead>
                                  <TableHead>Frequency</TableHead><TableHead>Duration</TableHead><TableHead>Instructions</TableHead>
                                </TableRow>
                              </TableHeader>
                              <TableBody>
                                {record.prescriptionItems.map(item => (
                                  <TableRow key={item.id}>
                                    <TableCell className="font-medium">{item.medicationName}</TableCell>
                                    <TableCell>{item.dosage}</TableCell>
                                    <TableCell>{item.frequency}</TableCell>
                                    <TableCell>{item.duration}</TableCell>
                                    <TableCell className="text-muted-foreground">{item.mealInstruction}</TableCell>
                                  </TableRow>
                                ))}
                              </TableBody>
                            </Table>
                          </div>
                        )}
                      </motion.div>
                    )}
                  </AnimatePresence>
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </div>
      )}
    </Layout>
  );
};

export default PatientMedicalRecords;
