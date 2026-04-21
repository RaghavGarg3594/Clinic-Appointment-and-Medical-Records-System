import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import Layout from '../components/Layout';
import api from '../services/api';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Separator } from '@/components/ui/separator';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { motion } from 'framer-motion';

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

const DoctorConsultation = () => {
  const [searchParams] = useSearchParams();
  const prefilledApptId = searchParams.get('appointmentId') || '';
  const [medications, setMedications] = useState([]);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    appointmentId: prefilledApptId,
    chiefComplaint: '', vitalSigns: '', diagnosis: '', icd10Code: '', severity: 'LOW',
    advice: '', followUpDate: '',
  });
  const [icd10Results, setIcd10Results] = useState([]);
  const [icd10Query, setIcd10Query] = useState('');
  const [icd10Open, setIcd10Open] = useState(false);
  const [prescriptionItems, setPrescriptionItems] = useState([]);
  const [labTests, setLabTests] = useState([]);
  const [orderedLabTests, setOrderedLabTests] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [medsRes, labsRes] = await Promise.all([
          api.get('/consultations/medications'),
          api.get('/lab/types')
        ]);
        setMedications(medsRes.data);
        setLabTests(labsRes.data);
      } catch (err) {
        console.error('Could not load lookup data:', err);
      }
    };
    fetchData();
  }, []);

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

  const removeItem = (idx) => {
    setPrescriptionItems(prescriptionItems.filter((_, i) => i !== idx));
  };

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

  const removeLabTest = (idx) => {
    setOrderedLabTests(orderedLabTests.filter((_, i) => i !== idx));
  };

  const searchIcd10 = async (query) => {
    setIcd10Query(query);
    if (query.length < 1) { setIcd10Results([]); setIcd10Open(false); return; }
    try {
      const res = await api.get(`/consultations/icd10?q=${encodeURIComponent(query)}`);
      setIcd10Results(res.data);
      setIcd10Open(true);
    } catch (err) { console.error(err); }
  };

  const selectIcd10 = (code) => {
    setForm({ ...form, icd10Code: code.code, diagnosis: code.description });
    setIcd10Query(`${code.code} — ${code.description}`);
    setIcd10Open(false);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');
    try {
      const payload = {
        ...form,
        appointmentId: parseInt(form.appointmentId),
        prescriptionItems: prescriptionItems.map(p => ({
          medicationId: parseInt(p.medicationId),
          frequency: p.frequency,
          duration: p.duration,
          route: p.route,
          mealInstruction: p.mealInstruction,
        })),
      };
      await api.post('/consultations', payload);

      for (const lt of orderedLabTests) {
        await api.post('/lab/orders', {
          appointmentId: parseInt(form.appointmentId),
          testTypeId: parseInt(lt.testTypeId),
          priority: lt.priority,
          specialInstructions: lt.specialInstructions
        });
      }

      setSuccess('Consultation recorded successfully! Lab orders and bill generated.');
      setForm({ appointmentId: '', chiefComplaint: '', vitalSigns: '', diagnosis: '', icd10Code: '', severity: 'LOW', advice: '', followUpDate: '' });
      setIcd10Query('');
      setPrescriptionItems([]);
      setOrderedLabTests([]);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to record consultation');
    }
  };

  return (
    <Layout>
      <h1 className="text-2xl font-bold tracking-tight mb-6">Record Consultation</h1>

      {success && (
        <Alert className="mb-4 border-primary/30 bg-primary/10 text-primary-foreground">
          <AlertDescription>{success}</AlertDescription>
        </Alert>
      )}
      {error && (
        <Alert variant="destructive" className="mb-4">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }}>
        <Card>
          <CardContent className="pt-6">
            <form onSubmit={handleSubmit} className="space-y-5">
              <div className="space-y-2">
                <Label>Appointment ID</Label>
                <Input required type="number" value={form.appointmentId}
                  onChange={e => setForm({ ...form, appointmentId: e.target.value })}
                  placeholder="Auto-filled from appointment list" />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Chief Complaint</Label>
                  <Textarea rows={2} value={form.chiefComplaint} required
                    onChange={e => setForm({ ...form, chiefComplaint: e.target.value })} />
                </div>
                <div className="space-y-2">
                  <Label>Vital Signs (BP, Temp, Pulse)</Label>
                  <Textarea rows={2} value={form.vitalSigns}
                    onChange={e => setForm({ ...form, vitalSigns: e.target.value })} />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2 relative">
                  <Label>Diagnosis (ICD-10)</Label>
                  <Input
                    value={icd10Query || form.diagnosis}
                    onChange={e => {
                      searchIcd10(e.target.value);
                      setForm({ ...form, diagnosis: e.target.value, icd10Code: '' });
                    }}
                    onFocus={() => icd10Query.length > 0 && setIcd10Open(true)}
                    placeholder="Type to search ICD-10 codes..."
                    required
                  />
                  {icd10Open && icd10Results.length > 0 && (
                    <div className="absolute z-50 top-full left-0 right-0 mt-1 bg-popover border border-border rounded-md shadow-lg max-h-48 overflow-y-auto">
                      {icd10Results.map(c => (
                        <div key={c.id} onClick={() => selectIcd10(c)}
                          className="px-3 py-2 text-sm cursor-pointer hover:bg-muted flex justify-between">
                          <span className="font-mono text-xs text-muted-foreground mr-2">{c.code}</span>
                          <span className="flex-1 truncate">{c.description}</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
                <div className="space-y-2">
                  <Label>Severity</Label>
                  <select className={selectClasses} value={form.severity}
                    onChange={e => setForm({ ...form, severity: e.target.value })}>
                    <option value="LOW">Low</option>
                    <option value="MODERATE">Moderate</option>
                    <option value="HIGH">High</option>
                    <option value="CRITICAL">Critical</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-4">
                <div className="space-y-2 col-span-2">
                  <Label>Advice</Label>
                  <Textarea rows={2} value={form.advice}
                    onChange={e => setForm({ ...form, advice: e.target.value })} />
                </div>
                <div className="space-y-2">
                  <Label>Follow-up Date</Label>
                  <Input type="date" value={form.followUpDate}
                    onChange={e => setForm({ ...form, followUpDate: e.target.value })} />
                </div>
              </div>

              {/* Prescription Builder */}
              <Separator />
              <div className="flex items-center justify-between">
                <h3 className="text-base font-semibold">Prescription</h3>
                <Button type="button" variant="outline" size="sm" onClick={addPrescriptionItem}>+ Add Medication</Button>
              </div>

              {prescriptionItems.map((item, idx) => (
                <motion.div key={idx} initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} className="bg-muted rounded-lg p-4 border border-border">
                  <div className="grid grid-cols-5 gap-2 items-end">
                    <div className="space-y-1 col-span-1">
                      <Label className="text-xs">Medication</Label>
                      <select className={selectClasses} value={item.medicationId} required
                        onChange={e => updateItem(idx, 'medicationId', e.target.value)}>
                        <option value="">Select</option>
                        {medications.map(m => <option key={m.id} value={m.id}>{m.name} (Stock: {m.stockQuantity})</option>)}
                      </select>
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
                  <div className="mt-2 text-xs text-muted-foreground flex items-center gap-3">
                    <span>📦 Stock to deduct: <strong>{getStockDeduction(item.frequency, item.duration)}</strong> units</span>
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
                </motion.div>
              ))}

              {/* Lab Tests Builder */}
              <Separator />
              <div className="flex items-center justify-between">
                <h3 className="text-base font-semibold">Lab Tests</h3>
                <Button type="button" variant="outline" size="sm" onClick={addLabTest}>+ Add Lab Test</Button>
              </div>

              {orderedLabTests.map((item, idx) => (
                <motion.div key={idx} initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }}
                  className="grid grid-cols-4 gap-2 items-end">
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
                </motion.div>
              ))}

              <Separator />
              <Button type="submit" className="px-8">Complete Consultation</Button>
            </form>
          </CardContent>
        </Card>
      </motion.div>
    </Layout>
  );
};

export default DoctorConsultation;
