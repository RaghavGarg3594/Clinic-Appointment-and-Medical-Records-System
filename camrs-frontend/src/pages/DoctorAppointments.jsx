import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import api from '../services/api';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Separator } from '@/components/ui/separator';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Skeleton } from '@/components/ui/skeleton';
import { motion, AnimatePresence } from 'framer-motion';

const FREQUENCY_OPTIONS = [
  { label: 'Once daily (1x)', value: '1x daily', times: 1 },
  { label: 'Twice daily (2x)', value: '2x daily', times: 2 },
  { label: 'Thrice daily (3x)', value: '3x daily', times: 3 },
  { label: 'Four times daily (4x)', value: '4x daily', times: 4 },
];
const DURATION_OPTIONS = [
  { label: '3 days', value: '3 days', days: 3 },
  { label: '5 days', value: '5 days', days: 5 },
  { label: '7 days (1 week)', value: '7 days', days: 7 },
  { label: '10 days', value: '10 days', days: 10 },
  { label: '14 days (2 weeks)', value: '14 days', days: 14 },
  { label: '30 days (1 month)', value: '30 days', days: 30 },
];
const selectClasses = "flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-xs transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring";

const statusVariant = (status) => {
  const map = {
    APPROVAL_PENDING: 'secondary', SCHEDULED: 'secondary', CONFIRMED: 'outline', CHECKED_IN: 'outline',
    ONGOING: 'default', IN_PROGRESS: 'secondary', COMPLETED: 'default', CANCELLED: 'destructive', RESCHEDULED: 'outline'
  };
  return map[status] || 'outline';
};

const DoctorAppointments = () => {
  const [activeTab, setActiveTab] = useState('daily');
  const [appointments, setAppointments] = useState([]);
  const [upcomingAppointments, setUpcomingAppointments] = useState([]);
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [loading, setLoading] = useState(true);

  // Consultation popup state
  const [consultOpen, setConsultOpen] = useState(false);
  const [consultAppt, setConsultAppt] = useState(null);
  const [medications, setMedications] = useState([]);
  const [labTests, setLabTests] = useState([]);
  const [lastPrescription, setLastPrescription] = useState(null);
  const [showLastRx, setShowLastRx] = useState(false);
  const [consultForm, setConsultForm] = useState({
    chiefComplaint: '', vitalSigns: '', diagnosis: '', icd10Code: '', severity: 'LOW', advice: '', followUpDate: '',
  });
  const [prescriptionItems, setPrescriptionItems] = useState([]);
  const [orderedLabTests, setOrderedLabTests] = useState([]);
  const [consultSuccess, setConsultSuccess] = useState('');
  const [consultError, setConsultError] = useState('');

  // ICD-10 preloaded list
  const [allIcd10Codes, setAllIcd10Codes] = useState([]);
  const [icd10Query, setIcd10Query] = useState('');
  const [icd10Open, setIcd10Open] = useState(false);

  // Medication search
  const [medSearches, setMedSearches] = useState({});
  const [medOpenIdx, setMedOpenIdx] = useState(null);

  // Allergy check
  const [allergyWarnings, setAllergyWarnings] = useState([]);
  const [showAllergyDialog, setShowAllergyDialog] = useState(false);
  const [pendingSubmit, setPendingSubmit] = useState(null);

  useEffect(() => {
    if (activeTab === 'daily') {
      const fetchDaily = async () => {
        setLoading(true);
        try {
          const res = await api.get(`/appointments/doctor?date=${date}`);
          setAppointments(res.data);
        } catch (err) { console.error(err); }
        finally { setLoading(false); }
      };
      fetchDaily();
    }
  }, [date, activeTab]);

  useEffect(() => {
    if (activeTab === 'upcoming') {
      const fetchUpcoming = async () => {
        setLoading(true);
        try {
          const res = await api.get('/appointments/doctor/upcoming');
          setUpcomingAppointments(res.data);
        } catch (err) { console.error(err); }
        finally { setLoading(false); }
      };
      fetchUpcoming();
    }
  }, [activeTab]);

  const refreshAppointments = async () => {
    if (activeTab === 'daily') {
      const res = await api.get(`/appointments/doctor?date=${date}`);
      setAppointments(res.data);
    } else {
      const res = await api.get('/appointments/doctor/upcoming');
      setUpcomingAppointments(res.data);
    }
  };

  const updateStatus = async (id, status) => {
    try {
      await api.put(`/appointments/${id}/status`, { status });
      refreshAppointments();
    } catch (err) { alert('Failed to update status'); }
  };

  const openConsultation = async (appt) => {
    setConsultAppt(appt);
    setConsultForm({ chiefComplaint: '', vitalSigns: '', diagnosis: '', icd10Code: '', severity: 'LOW', advice: '', followUpDate: '' });
    setPrescriptionItems([]);
    setOrderedLabTests([]);
    setConsultSuccess('');
    setConsultError('');
    setLastPrescription(null);
    setShowLastRx(false);
    setIcd10Query('');
    setMedSearches({});
    setMedOpenIdx(null);
    setAllergyWarnings([]);
    setShowAllergyDialog(false);
    setPendingSubmit(null);

    try {
      const [medsRes, labsRes, icd10Res] = await Promise.all([
        api.get('/consultations/medications'),
        api.get('/lab/types'),
        api.get('/consultations/icd10'),
      ]);
      setMedications(medsRes.data);
      setLabTests(labsRes.data);
      setAllIcd10Codes(icd10Res.data);
    } catch (err) { console.error('Could not load lookup data:', err); }

    // Fetch last prescription for this patient
    if (appt.patientId) {
      try {
        const lastRxRes = await api.get(`/consultations/patient/${appt.patientId}/last-prescription`);
        if (lastRxRes.status === 200 && lastRxRes.data) {
          setLastPrescription(lastRxRes.data);
        }
      } catch (err) { /* no previous prescription */ }
    }

    setConsultOpen(true);
  };

  const addPrescriptionItem = () => {
    setPrescriptionItems([...prescriptionItems, {
      medicationId: '', frequency: '1x daily', duration: '5 days', route: 'Oral', mealInstruction: 'After meals'
    }]);
  };
  const updateItem = (idx, field, value) => {
    const updated = [...prescriptionItems];
    updated[idx][field] = value;
    setPrescriptionItems(updated);
  };
  const removeItem = (idx) => setPrescriptionItems(prescriptionItems.filter((_, i) => i !== idx));
  const getStockDeduction = (frequency, duration) => {
    const freqObj = FREQUENCY_OPTIONS.find(f => f.value === frequency);
    const durObj = DURATION_OPTIONS.find(d => d.value === duration);
    if (!freqObj || !durObj) return '?';
    return freqObj.times * durObj.days;
  };
  const addLabTest = () => {
    setOrderedLabTests([...orderedLabTests, { testTypeId: '', priority: 'ROUTINE', specialInstructions: '' }]);
  };
  const updateLabTest = (idx, field, value) => {
    const updated = [...orderedLabTests];
    updated[idx][field] = value;
    setOrderedLabTests(updated);
  };
  const removeLabTest = (idx) => setOrderedLabTests(orderedLabTests.filter((_, i) => i !== idx));

  // ICD-10: local filter on preloaded list
  const filteredIcd10 = icd10Query.length > 0
    ? allIcd10Codes.filter(c =>
        c.code.toLowerCase().includes(icd10Query.toLowerCase()) ||
        c.description.toLowerCase().includes(icd10Query.toLowerCase())
      )
    : allIcd10Codes;

  const selectIcd10 = (code) => {
    setConsultForm({ ...consultForm, icd10Code: code.code, diagnosis: code.description });
    setIcd10Query(`${code.code} — ${code.description}`);
    setIcd10Open(false);
  };

  // Medication search helpers
  const getMedSearch = (idx) => medSearches[idx] || '';
  const setMedSearch = (idx, val) => setMedSearches({ ...medSearches, [idx]: val });
  const filteredMeds = (idx) => {
    const q = getMedSearch(idx).toLowerCase();
    if (!q) return medications;
    return medications.filter(m => m.name.toLowerCase().includes(q));
  };
  const selectMedication = (idx, med) => {
    updateItem(idx, 'medicationId', String(med.id));
    setMedSearch(idx, med.name);
    setMedOpenIdx(null);
  };

  const handleConsultSubmit = async (e) => {
    e.preventDefault();
    setConsultError(''); setConsultSuccess('');

    // Build payload
    const payload = {
      ...consultForm,
      appointmentId: consultAppt.id,
      prescriptionItems: prescriptionItems.map(p => ({
        medicationId: parseInt(p.medicationId),
        frequency: p.frequency,
        duration: p.duration,
        route: p.route,
        mealInstruction: p.mealInstruction,
      })),
    };

    // Allergy check before submit (if medications are present)
    if (prescriptionItems.length > 0 && consultAppt?.patientId) {
      try {
        const medIds = prescriptionItems.map(p => parseInt(p.medicationId)).filter(id => !isNaN(id));
        const checkRes = await api.post('/consultations/check-allergies', {
          patientId: consultAppt.patientId,
          medicationIds: medIds
        });
        if (checkRes.data && checkRes.data.length > 0) {
          setAllergyWarnings(checkRes.data);
          setPendingSubmit(payload);
          setShowAllergyDialog(true);
          return;
        }
      } catch (err) { console.error('Allergy check failed:', err); }
    }

    await doSubmitConsultation(payload);
  };

  const doSubmitConsultation = async (payload) => {
    setConsultError(''); setConsultSuccess('');
    try {
      await api.post('/consultations', payload);

      for (const lt of orderedLabTests) {
        await api.post('/lab/orders', {
          appointmentId: consultAppt.id,
          testTypeId: parseInt(lt.testTypeId),
          priority: lt.priority,
          specialInstructions: lt.specialInstructions
        });
      }

      setConsultSuccess('Consultation recorded successfully!');
      setTimeout(() => {
        setConsultOpen(false);
        refreshAppointments();
      }, 1500);
    } catch (err) {
      setConsultError(err.response?.data?.message || 'Failed to record consultation');
    }
  };

  const handleAllergyProceed = () => {
    setShowAllergyDialog(false);
    if (pendingSubmit) doSubmitConsultation(pendingSubmit);
  };

  const downloadPdf = async (appointmentId, type) => {
    try {
      const endpoint = type === 'prescription'
        ? `/doctors/appointments/${appointmentId}/prescription/pdf`
        : `/doctors/appointments/${appointmentId}/lab-report/pdf`;

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

  const renderTable = (list) => (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Date</TableHead><TableHead>Time</TableHead><TableHead>Patient</TableHead>
          <TableHead>Type</TableHead><TableHead>Token</TableHead><TableHead>Status</TableHead><TableHead>Actions</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {list.map(appt => (
          <TableRow key={appt.id}>
            <TableCell>{appt.appointmentDate}</TableCell>
            <TableCell className="font-mono text-sm">{appt.timeSlot}</TableCell>
            <TableCell className="font-medium">{appt.patientName}</TableCell>
            <TableCell className="text-muted-foreground">{appt.type}</TableCell>
            <TableCell>
              <Badge variant="outline">{appt.tokenNumber}</Badge>
            </TableCell>
            <TableCell>
              <Badge variant={statusVariant(appt.status)}>{appt.status === 'APPROVAL_PENDING' ? 'Pending Payment' : appt.status}</Badge>
            </TableCell>
            <TableCell>
              <div className="flex gap-1.5">
                {appt.status === 'APPROVAL_PENDING' && (
                  <span className="text-xs text-muted-foreground italic">Awaiting admin payment</span>
                )}
                {appt.status === 'ONGOING' && (
                  <Button size="sm" className="bg-primary hover:bg-primary/90 text-primary-foreground"
                    onClick={() => openConsultation(appt)}>
                    Start Consultation
                  </Button>
                )}
                {(appt.status === 'SCHEDULED' || appt.status === 'RESCHEDULED') && (
                  <Button size="sm" variant="outline" onClick={() => updateStatus(appt.id, 'CHECKED_IN')}>Check In</Button>
                )}
                {appt.status === 'CHECKED_IN' && (
                  <Button size="sm" onClick={() => updateStatus(appt.id, 'IN_PROGRESS')}>Start</Button>
                )}
                {(appt.status === 'CHECKED_IN' || appt.status === 'IN_PROGRESS') && (
                  <Button size="sm" variant="default"
                    className="bg-primary hover:bg-primary/90 text-primary-foreground"
                    onClick={() => openConsultation(appt)}>
                    Consult
                  </Button>
                )}
                {appt.status === 'COMPLETED' && (
                  <>
                    <Button size="sm" variant="outline" onClick={() => downloadPdf(appt.id, 'prescription')}>Rx PDF</Button>
                    {appt.hasLabReport && (
                      <Button size="sm" variant="outline" onClick={() => downloadPdf(appt.id, 'lab-report')}>Lab PDF</Button>
                    )}
                  </>
                )}
              </div>
            </TableCell>
          </TableRow>
        ))}
        {list.length === 0 && (
          <TableRow><TableCell colSpan={7} className="text-center py-8 text-muted-foreground">No appointments found</TableCell></TableRow>
        )}
      </TableBody>
    </Table>
  );

  return (
    <Layout>
      <h1 className="text-2xl font-bold tracking-tight mb-6">Appointments</h1>

      <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }}>
        <Card>
          <CardContent className="pt-6">
            <Tabs value={activeTab} onValueChange={setActiveTab}>
              <TabsList className="mb-4">
                <TabsTrigger value="daily">Daily Schedule</TabsTrigger>
                <TabsTrigger value="upcoming">Upcoming</TabsTrigger>
              </TabsList>

              <TabsContent value="daily">
                <div className="flex items-center gap-3 mb-4">
                  <Label>Date:</Label>
                  <Input type="date" className="w-48" value={date} onChange={e => setDate(e.target.value)} />
                </div>
                {loading ? <Skeleton className="h-32" /> : renderTable(appointments)}
              </TabsContent>

              <TabsContent value="upcoming">
                <p className="text-sm text-muted-foreground mb-4">Showing all upcoming appointments from today onwards.</p>
                {loading ? <Skeleton className="h-32" /> : renderTable(upcomingAppointments)}
              </TabsContent>
            </Tabs>
          </CardContent>
        </Card>
      </motion.div>

      {/* Consultation Dialog */}
      <Dialog open={consultOpen} onOpenChange={(open) => !open && setConsultOpen(false)}>
        <DialogContent className="sm:max-w-[800px] max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>
              Consultation — {consultAppt?.patientName}
              <span className="text-sm text-muted-foreground ml-2">(Appt #{consultAppt?.id})</span>
            </DialogTitle>
          </DialogHeader>

          {consultSuccess && (
            <Alert className="border-primary/30 bg-primary/10 text-primary-foreground">
              <AlertDescription>{consultSuccess}</AlertDescription>
            </Alert>
          )}
          {consultError && (
            <Alert variant="destructive">
              <AlertDescription>{consultError}</AlertDescription>
            </Alert>
          )}

          {/* Previous Prescription Section */}
          {lastPrescription && (
            <div className="mb-2">
              <Button type="button" variant="outline" size="sm" onClick={() => setShowLastRx(!showLastRx)} className="text-xs w-full justify-between">
                <span>📋 Previous Prescription ({new Date(lastPrescription.visitDate).toLocaleDateString()})</span>
                <span>{showLastRx ? '▲' : '▼'}</span>
              </Button>
              <AnimatePresence>
                {showLastRx && (
                  <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }}
                    className="mt-2 bg-muted/50 rounded-lg p-3 border border-border overflow-hidden">
                    <div className="grid grid-cols-2 gap-2 text-sm mb-2">
                      <div><span className="text-xs text-muted-foreground">Diagnosis:</span> {lastPrescription.diagnosis || '—'}</div>
                      <div><span className="text-xs text-muted-foreground">Severity:</span> {lastPrescription.severity}</div>
                      <div><span className="text-xs text-muted-foreground">Complaint:</span> {lastPrescription.chiefComplaint || '—'}</div>
                      <div><span className="text-xs text-muted-foreground">Advice:</span> {lastPrescription.advice || '—'}</div>
                    </div>
                    {lastPrescription.prescriptionItems?.length > 0 && (
                      <Table>
                        <TableHeader>
                          <TableRow className="bg-muted/40">
                            <TableHead className="text-xs">Medication</TableHead>
                            <TableHead className="text-xs">Frequency</TableHead>
                            <TableHead className="text-xs">Duration</TableHead>
                            <TableHead className="text-xs">Instructions</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {lastPrescription.prescriptionItems.map(rx => (
                            <TableRow key={rx.id}>
                              <TableCell className="text-xs font-medium">{rx.medicationName}</TableCell>
                              <TableCell className="text-xs">{rx.frequency}</TableCell>
                              <TableCell className="text-xs">{rx.duration}</TableCell>
                              <TableCell className="text-xs">{rx.mealInstruction}</TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    )}
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          )}

          <form onSubmit={handleConsultSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <Label className="text-xs">Chief Complaint</Label>
                <Textarea rows={2} value={consultForm.chiefComplaint} required
                  onChange={e => setConsultForm({ ...consultForm, chiefComplaint: e.target.value })} />
              </div>
              <div className="space-y-1">
                <Label className="text-xs">Vital Signs (BP, Temp, Pulse)</Label>
                <Textarea rows={2} value={consultForm.vitalSigns}
                  onChange={e => setConsultForm({ ...consultForm, vitalSigns: e.target.value })} />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1 relative">
                <Label className="text-xs">Diagnosis (ICD-10)</Label>
                <Input
                  value={icd10Query || consultForm.diagnosis}
                  onChange={e => {
                    setIcd10Query(e.target.value);
                    setConsultForm({ ...consultForm, diagnosis: e.target.value, icd10Code: '' });
                    setIcd10Open(true);
                  }}
                  onFocus={() => setIcd10Open(true)}
                  placeholder="Type to search or browse ICD-10 codes..."
                  required
                />
                {icd10Open && filteredIcd10.length > 0 && (
                  <div className="absolute z-50 top-full left-0 right-0 mt-1 bg-popover border border-border rounded-md shadow-lg max-h-48 overflow-y-auto">
                    {filteredIcd10.map(c => (
                      <div key={c.id} onClick={() => selectIcd10(c)}
                        className="px-3 py-2 text-sm cursor-pointer hover:bg-muted flex justify-between">
                        <span className="font-mono text-xs text-muted-foreground mr-2">{c.code}</span>
                        <span className="flex-1 truncate">{c.description}</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>
              <div className="space-y-1">
                <Label className="text-xs">Severity</Label>
                <select className={selectClasses} value={consultForm.severity}
                  onChange={e => setConsultForm({ ...consultForm, severity: e.target.value })}>
                  <option value="LOW">Low</option>
                  <option value="MODERATE">Moderate</option>
                  <option value="HIGH">High</option>
                  <option value="CRITICAL">Critical</option>
                </select>
              </div>
            </div>

            <div className="grid grid-cols-3 gap-3">
              <div className="space-y-1 col-span-2">
                <Label className="text-xs">Advice</Label>
                <Textarea rows={2} value={consultForm.advice}
                  onChange={e => setConsultForm({ ...consultForm, advice: e.target.value })} />
              </div>
              <div className="space-y-1">
                <Label className="text-xs">Follow-up Date</Label>
                <Input type="date" value={consultForm.followUpDate}
                  min={new Date().toISOString().split('T')[0]}
                  onChange={e => setConsultForm({ ...consultForm, followUpDate: e.target.value })} />
              </div>
            </div>

            {/* Prescription Builder */}
            <Separator />
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-semibold">Prescription</h3>
              <Button type="button" variant="outline" size="sm" onClick={addPrescriptionItem}>+ Add Medication</Button>
            </div>

            {prescriptionItems.map((item, idx) => (
              <div key={idx} className="bg-muted rounded-lg p-3 border border-border">
                <div className="grid grid-cols-6 gap-2 items-end">
                  <div className="space-y-1 relative col-span-2">
                    <Label className="text-xs">Medication</Label>
                    <Input
                      value={getMedSearch(idx) || (item.medicationId ? medications.find(m => String(m.id) === String(item.medicationId))?.name || '' : '')}
                      onChange={e => { setMedSearch(idx, e.target.value); setMedOpenIdx(idx); updateItem(idx, 'medicationId', ''); }}
                      onFocus={() => setMedOpenIdx(idx)}
                      placeholder="Type medicine name..."
                      required={!item.medicationId}
                    />
                    {medOpenIdx === idx && (
                      <div className="absolute z-50 top-full left-0 right-0 mt-1 bg-popover border border-border rounded-md shadow-lg max-h-48 overflow-y-auto">
                        {filteredMeds(idx).map(m => (
                          <div key={m.id} onClick={() => selectMedication(idx, m)}
                            className={`px-3 py-2 text-sm cursor-pointer hover:bg-muted flex justify-between ${m.stockQuantity <= 0 ? 'opacity-50' : ''}`}>
                            <span className="flex-1 truncate">{m.name}</span>
                            <span className={`text-xs font-mono ml-2 ${m.stockQuantity <= 0 ? 'text-destructive' : 'text-muted-foreground'}`}>
                              Stock: {m.stockQuantity}
                            </span>
                          </div>
                        ))}
                        {filteredMeds(idx).length === 0 && (
                          <div className="px-3 py-2 text-sm text-muted-foreground">No matching medication found</div>
                        )}
                      </div>
                    )}
                  </div>
                  <div className="space-y-1">
                    <Label className="text-xs">Frequency</Label>
                    <select className={selectClasses} value={item.frequency}
                      onChange={e => updateItem(idx, 'frequency', e.target.value)}>
                      {FREQUENCY_OPTIONS.map(f => <option key={f.value} value={f.value}>{f.label}</option>)}
                    </select>
                  </div>
                  <div className="space-y-1">
                    <Label className="text-xs">Duration</Label>
                    <select className={selectClasses} value={item.duration}
                      onChange={e => updateItem(idx, 'duration', e.target.value)}>
                      {DURATION_OPTIONS.map(d => <option key={d.value} value={d.value}>{d.label}</option>)}
                    </select>
                  </div>
                  <div className="space-y-1">
                    <Label className="text-xs">Meal</Label>
                    <select className={selectClasses} value={item.mealInstruction}
                      onChange={e => updateItem(idx, 'mealInstruction', e.target.value)}>
                      <option value="Before meals">Before meals</option>
                      <option value="After meals">After meals</option>
                      <option value="With meals">With meals</option>
                    </select>
                  </div>
                  <Button type="button" variant="destructive" size="sm" onClick={() => removeItem(idx)}>✕</Button>
                </div>
                <div className="mt-1 text-xs text-muted-foreground flex items-center gap-3">
                  <span>📦 Stock: <strong>{getStockDeduction(item.frequency, item.duration)}</strong> units</span>
                  <span>Route:
                    <select className="ml-1 text-xs border border-input rounded px-1 py-0.5 bg-transparent"
                      value={item.route} onChange={e => updateItem(idx, 'route', e.target.value)}>
                      <option value="Oral">Oral</option>
                      <option value="Topical">Topical</option>
                      <option value="Intravenous">Intravenous</option>
                      <option value="Intramuscular">Intramuscular</option>
                      <option value="Subcutaneous">Subcutaneous</option>
                      <option value="Inhaled">Inhaled</option>
                    </select>
                  </span>
                </div>
              </div>
            ))}

            {/* Lab Tests */}
            <Separator />
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-semibold">Lab Tests</h3>
              <Button type="button" variant="outline" size="sm" onClick={addLabTest}>+ Add Lab Test</Button>
            </div>

            {orderedLabTests.map((item, idx) => (
              <div key={idx} className="grid grid-cols-4 gap-2 items-end">
                <div className="space-y-1">
                  <Label className="text-xs">Test</Label>
                  <select className={selectClasses} value={item.testTypeId} required
                    onChange={e => updateLabTest(idx, 'testTypeId', e.target.value)}>
                    <option value="">Select Test</option>
                    {labTests.map(t => <option key={t.id} value={t.id}>{t.testName} ({t.testCode})</option>)}
                  </select>
                </div>
                <div className="space-y-1">
                  <Label className="text-xs">Priority</Label>
                  <select className={selectClasses} value={item.priority}
                    onChange={e => updateLabTest(idx, 'priority', e.target.value)}>
                    <option value="ROUTINE">Routine</option>
                    <option value="URGENT">Urgent</option>
                    <option value="STAT">STAT</option>
                  </select>
                </div>
                <div className="space-y-1">
                  <Label className="text-xs">Instructions for Lab</Label>
                  <Input value={item.specialInstructions}
                    onChange={e => updateLabTest(idx, 'specialInstructions', e.target.value)}
                    placeholder="e.g. Fasting required" />
                </div>
                <Button type="button" variant="destructive" size="sm" onClick={() => removeLabTest(idx)}>✕</Button>
              </div>
            ))}

            <Separator />
            <Button type="submit" className="px-8 w-full">Complete Consultation</Button>
          </form>
        </DialogContent>
      </Dialog>

      {/* Allergy Warning Dialog */}
      <Dialog open={showAllergyDialog} onOpenChange={(open) => !open && setShowAllergyDialog(false)}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle className="text-destructive">⚠ Allergy Warning</DialogTitle>
          </DialogHeader>
          <div className="space-y-2 text-sm">
            <p className="text-muted-foreground">The following potential allergy conflicts were detected:</p>
            {allergyWarnings.map((w, i) => (
              <Alert key={i} variant="destructive" className="py-2">
                <AlertDescription>{w}</AlertDescription>
              </Alert>
            ))}
            <p className="text-muted-foreground mt-3">Do you want to proceed with this prescription anyway?</p>
          </div>
          <div className="flex gap-2 mt-4">
            <Button variant="destructive" className="flex-1" onClick={handleAllergyProceed}>Proceed Anyway</Button>
            <Button variant="outline" className="flex-1" onClick={() => setShowAllergyDialog(false)}>Cancel & Edit</Button>
          </div>
        </DialogContent>
      </Dialog>
    </Layout>
  );
};

export default DoctorAppointments;
