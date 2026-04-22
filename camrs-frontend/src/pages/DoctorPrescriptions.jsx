import React, { useState, useEffect, useMemo } from 'react';
import Layout from '../components/Layout';
import api from '../services/api';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Separator } from '@/components/ui/separator';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Skeleton } from '@/components/ui/skeleton';
import { motion, AnimatePresence } from 'framer-motion';

const severityVariant = (severity) => {
  const map = { CRITICAL: 'destructive', HIGH: 'secondary', MODERATE: 'outline', LOW: 'default' };
  return map[severity] || 'outline';
};

const priorityVariant = (priority) => {
  const map = { STAT: 'destructive', URGENT: 'secondary', ROUTINE: 'outline' };
  return map[priority] || 'outline';
};

const statusVariant = (status) => {
  const map = { ORDERED: 'secondary', SAMPLE_COLLECTED: 'outline', IN_PROGRESS: 'secondary', COMPLETED: 'default' };
  return map[status] || 'outline';
};

const container = { hidden: {}, show: { transition: { staggerChildren: 0.04 } } };
const item = { hidden: { opacity: 0, y: 12 }, show: { opacity: 1, y: 0, transition: { duration: 0.3 } } };

const DoctorPrescriptions = () => {
  const [records, setRecords] = useState([]);
  const [labResults, setLabResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedId, setExpandedId] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterSeverity, setFilterSeverity] = useState('ALL');
  const [activeTab, setActiveTab] = useState('prescriptions');

  useEffect(() => { fetchRecords(); fetchLabResults(); }, []);

  const fetchRecords = async () => {
    try {
      const resp = await api.get('/doctors/me/patient-records');
      setRecords(resp.data);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const fetchLabResults = async () => {
    try {
      const resp = await api.get('/doctors/me/lab-results');
      setLabResults(resp.data);
    } catch (err) { console.error(err); }
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
      console.error('PDF download error:', err);
      alert('Failed to download prescription PDF');
    }
  };

  const downloadLabReportPdf = async (orderId) => {
    try {
      const res = await api.get(`/doctors/lab-results/${orderId}/pdf`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `lab-report-${orderId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Lab PDF download error:', err);
      alert('Failed to download lab report PDF');
    }
  };

  /* ---- Derived data ---- */
  const filteredRecords = useMemo(() => {
    return records.filter(record => {
      const matchesSearch = searchQuery === '' ||
        record.patientName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        record.diagnosis?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        record.chiefComplaint?.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesSeverity = filterSeverity === 'ALL' || record.severity === filterSeverity;
      return matchesSearch && matchesSeverity;
    });
  }, [records, searchQuery, filterSeverity]);

  const filteredLabResults = useMemo(() => {
    if (searchQuery === '') return labResults;
    return labResults.filter(lr =>
      lr.patientName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      lr.testType?.toLowerCase().includes(searchQuery.toLowerCase())
    );
  }, [labResults, searchQuery]);

  const uniquePatients = useMemo(() => {
    const names = new Set(records.map(r => r.patientName));
    return names.size;
  }, [records]);

  const totalPrescriptions = useMemo(() => {
    return records.filter(r => r.prescriptionItems?.length > 0).length;
  }, [records]);

  const completedLabTests = useMemo(() => {
    return labResults.filter(lr => lr.status === 'COMPLETED').length;
  }, [labResults]);

  const selectClasses = "flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-xs transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring";

  const tabClasses = (tab) =>
    `px-4 py-2 text-sm font-medium rounded-t-lg transition-colors cursor-pointer ${
      activeTab === tab
        ? 'bg-card text-foreground border border-b-0 border-border'
        : 'text-muted-foreground hover:text-foreground'
    }`;

  return (
    <Layout>
      <h1 className="text-2xl font-bold tracking-tight mb-6">Patient Prescriptions & Lab Results</h1>

      {/* Summary stat cards */}
      <motion.div className="grid grid-cols-1 sm:grid-cols-4 gap-4 mb-6" variants={container} initial="hidden" animate="show">
        {[
          { label: 'Total Consultations', value: records.length, accent: 'border-l-terracotta', color: 'text-terracotta' },
          { label: 'Unique Patients', value: uniquePatients, accent: 'border-l-oxford', color: 'text-oxford' },
          { label: 'Prescriptions Issued', value: totalPrescriptions, accent: 'border-l-rustic', color: 'text-rustic' },
          { label: 'Lab Tests Completed', value: completedLabTests, accent: 'border-l-green-600', color: 'text-green-600' },
        ].map((c, i) => (
          <motion.div key={i} variants={item}>
            <Card className={`hover:shadow-lg transition-shadow duration-300 border-l-4 ${c.accent}`}>
              <CardContent className="pt-5 pb-4">
                <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-2">{c.label}</p>
                <p className={`text-3xl font-extrabold tracking-tight ${c.color}`}>{c.value}</p>
              </CardContent>
            </Card>
          </motion.div>
        ))}
      </motion.div>

      {/* Tabs */}
      <div className="flex gap-1 mb-0">
        <button className={tabClasses('prescriptions')} onClick={() => setActiveTab('prescriptions')}>
          Prescriptions ({records.length})
        </button>
        <button className={tabClasses('lab')} onClick={() => setActiveTab('lab')}>
          Lab Results ({labResults.length})
        </button>
      </div>

      {/* Filter bar */}
      <motion.div initial={{ opacity: 0, y: 6 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.15, duration: 0.3 }}>
        <Card className="mb-5 rounded-tl-none">
          <CardContent className="pt-4 pb-4">
            <div className="flex flex-wrap items-center gap-3">
              <div className="flex-1 min-w-[200px]">
                <Input
                  id="doctor-rx-search"
                  placeholder={activeTab === 'prescriptions' ? "Search by patient name, diagnosis, or complaint…" : "Search by patient name or test type…"}
                  value={searchQuery}
                  onChange={e => setSearchQuery(e.target.value)}
                  className="h-9"
                />
              </div>
              {activeTab === 'prescriptions' && (
                <select
                  id="doctor-rx-severity-filter"
                  className={`${selectClasses} w-40`}
                  value={filterSeverity}
                  onChange={e => setFilterSeverity(e.target.value)}
                >
                  <option value="ALL">All Severities</option>
                  <option value="LOW">Low</option>
                  <option value="MODERATE">Moderate</option>
                  <option value="HIGH">High</option>
                  <option value="CRITICAL">Critical</option>
                </select>
              )}
            </div>
          </CardContent>
        </Card>
      </motion.div>

      {/* ====== PRESCRIPTIONS TAB ====== */}
      {activeTab === 'prescriptions' && (
        <>
          {loading ? (
            <div className="space-y-3">
              {Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-20 rounded-xl" />)}
            </div>
          ) : filteredRecords.length === 0 ? (
            <Card className="p-10 text-center">
              <p className="text-muted-foreground">
                {records.length === 0
                  ? 'No consultation records yet. Records will appear here after you complete consultations.'
                  : 'No records match your search or filter criteria.'}
              </p>
            </Card>
          ) : (
            <motion.div className="space-y-3" variants={container} initial="hidden" animate="show">
              {filteredRecords.map(record => (
                <motion.div key={record.id} variants={item} layout>
                  <Card
                    id={`record-${record.id}`}
                    className="cursor-pointer hover:shadow-md transition-shadow"
                    onClick={() => toggleExpand(record.id)}
                  >
                    <CardContent className="pt-5 pb-4">
                      {/* Header row */}
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

                            {/* Clinical summary grid */}
                            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                              {[
                                ['Chief Complaint', record.chiefComplaint],
                                ['Vital Signs', record.vitalSigns],
                                ['Diagnosis', record.diagnosis],
                                ['Follow-Up', record.followUpDate ? new Date(record.followUpDate).toLocaleDateString() : 'None scheduled'],
                              ].map(([label, val]) => (
                                <div key={label}>
                                  <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-1">{label}</p>
                                  <p className="text-sm">{val || '—'}</p>
                                </div>
                              ))}
                            </div>

                            {record.advice && (
                              <div className="mb-4">
                                <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-1">Your Advice</p>
                                <div className="bg-primary/10 border-l-3 border-primary rounded-md p-3 text-sm">
                                  {record.advice}
                                </div>
                              </div>
                            )}

                            {/* Prescription table */}
                            {record.prescriptionItems?.length > 0 ? (
                              <div>
                                <div className="flex items-center justify-between mb-2">
                                  <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">Prescribed Medications</p>
                                  <Button
                                    size="sm"
                                    variant="outline"
                                    className="text-xs"
                                    onClick={(e) => downloadPrescriptionPdf(record.id, e)}
                                  >
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
                                <p className="text-sm text-muted-foreground">No medications were prescribed for this visit.</p>
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
        </>
      )}

      {/* ====== LAB RESULTS TAB ====== */}
      {activeTab === 'lab' && (
        <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }}>
          {filteredLabResults.length === 0 ? (
            <Card className="p-10 text-center">
              <p className="text-muted-foreground">No lab test results found for your patients.</p>
            </Card>
          ) : (
            <Card>
              <CardContent className="p-0">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Order #</TableHead>
                      <TableHead>Patient</TableHead>
                      <TableHead>Test Type</TableHead>
                      <TableHead>Priority</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Result</TableHead>
                      <TableHead>Flag</TableHead>
                      <TableHead>Date</TableHead>
                      <TableHead>Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredLabResults.map(lr => (
                      <TableRow key={lr.id} className={lr.isCritical ? 'bg-destructive/5' : ''}>
                        <TableCell className="font-mono text-sm">#{lr.id}</TableCell>
                        <TableCell className="font-medium">{lr.patientName}</TableCell>
                        <TableCell>{lr.testType}</TableCell>
                        <TableCell>
                          <Badge variant={priorityVariant(lr.priority)}>{lr.priority}</Badge>
                        </TableCell>
                        <TableCell>
                          <Badge variant={statusVariant(lr.status)}>{lr.status?.replace('_', ' ')}</Badge>
                        </TableCell>
                        <TableCell>
                          {lr.resultValue ? (
                            <span className="font-mono text-sm">{lr.resultValue} {lr.unit || ''}</span>
                          ) : (
                            <span className="text-muted-foreground text-xs">Pending</span>
                          )}
                        </TableCell>
                        <TableCell>
                          {lr.isCritical && <Badge variant="destructive">CRITICAL</Badge>}
                          {lr.resultFlag === 'HIGH' && !lr.isCritical && <Badge variant="secondary">HIGH</Badge>}
                          {lr.resultFlag === 'LOW' && !lr.isCritical && <Badge variant="outline">LOW</Badge>}
                          {lr.resultFlag === 'NORMAL' && !lr.isCritical && <Badge variant="default">NORMAL</Badge>}
                        </TableCell>
                        <TableCell className="text-sm text-muted-foreground">
                          {lr.orderDate ? new Date(lr.orderDate).toLocaleDateString() : '—'}
                        </TableCell>
                        <TableCell>
                          {lr.status === 'COMPLETED' && (
                            <Button size="sm" variant="outline" className="text-xs" onClick={() => downloadLabReportPdf(lr.id)}>
                              Lab PDF
                            </Button>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          )}
        </motion.div>
      )}
    </Layout>
  );
};

export default DoctorPrescriptions;
