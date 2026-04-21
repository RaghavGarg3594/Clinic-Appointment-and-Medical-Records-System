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

const selectClasses = "flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-xs transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring";

const statusVariant = (status) => {
  const map = {
    APPROVAL_PENDING: 'secondary', SCHEDULED: 'secondary', CONFIRMED: 'outline', CHECKED_IN: 'outline',
    ONGOING: 'default', IN_PROGRESS: 'secondary', COMPLETED: 'default', CANCELLED: 'destructive', RESCHEDULED: 'outline'
  };
  return map[status] || 'outline';
};

const PatientAppointments = () => {
  const [appointments, setAppointments] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showBooking, setShowBooking] = useState(false);
  const [availableSlots, setAvailableSlots] = useState([]);
  const [cancelModal, setCancelModal] = useState({ show: false, apptId: null, reason: '' });
  const [rescheduleModal, setRescheduleModal] = useState({ show: false, appt: null, newDate: '', newTimeSlot: '', doctorId: null });
  const [form, setForm] = useState({ doctorId: '', appointmentDate: '', timeSlot: '', appointmentType: 'ROUTINE' });

  const fetchAppointments = async () => {
    try {
      const res = await api.get('/appointments/patient');
      setAppointments(res.data);
    } catch (err) { console.error(err); }
    setLoading(false);
  };

  const fetchDoctors = async () => {
    try {
      const res = await api.get('/doctors');
      setDoctors(res.data);
    } catch (err) { console.error(err); }
  };

  useEffect(() => { fetchAppointments(); fetchDoctors(); }, []);

  const fetchSlots = async (doctorId, date) => {
    if (!doctorId || !date) return;
    try {
      const res = await api.get(`/appointments/slots?doctorId=${doctorId}&date=${date}`);
      setAvailableSlots(res.data);
    } catch (err) { console.error(err); setAvailableSlots([]); }
  };

  const handleBooking = async (e) => {
    e.preventDefault();
    try {
      const payload = { ...form, doctorId: parseInt(form.doctorId, 10) };
      await api.post('/appointments/book', payload);
      setShowBooking(false);
      setForm({ doctorId: '', appointmentDate: '', timeSlot: '', appointmentType: 'ROUTINE' });
      setAvailableSlots([]);
      fetchAppointments();
    } catch (err) { alert(err.response?.data?.message || 'Booking failed'); }
  };

  const handleCancel = async () => {
    try {
      await api.put(`/appointments/${cancelModal.apptId}/cancel`, { reason: cancelModal.reason });
      setCancelModal({ show: false, apptId: null, reason: '' });
      fetchAppointments();
    } catch (err) { alert('Cancellation failed'); }
  };

  const handleReschedule = async () => {
    if (!rescheduleModal.newDate || !rescheduleModal.newTimeSlot) return;
    try {
      await api.put(`/appointments/${rescheduleModal.appt.id}/reschedule`, { 
        appointmentDate: rescheduleModal.newDate, 
        timeSlot: rescheduleModal.newTimeSlot 
      });
      setRescheduleModal({ show: false, appt: null, newDate: '', newTimeSlot: '', doctorId: null });
      fetchAppointments();
    } catch (err) { alert(err.response?.data?.message || 'Rescheduling failed'); }
  };

  return (
    <Layout>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold tracking-tight">My Appointments</h1>
        <Button variant={showBooking ? 'outline' : 'default'} onClick={() => {
          setShowBooking(!showBooking);
          setAvailableSlots([]);
          setForm({ doctorId: '', appointmentDate: '', timeSlot: '', appointmentType: 'ROUTINE' });
        }}>
          {showBooking ? 'Cancel' : '+ Book Appointment'}
        </Button>
      </div>

      {showBooking && (
        <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} transition={{ duration: 0.25 }}>
          <Card className="mb-5">
            <CardContent className="pt-6">
              <h3 className="font-semibold mb-4">Book New Appointment</h3>
              <form onSubmit={handleBooking} className="space-y-4">
                <div className="space-y-2">
                  <Label>Doctor</Label>
                  <select className={selectClasses} value={form.doctorId} required
                    onChange={e => {
                      const id = e.target.value;
                      setForm({ ...form, doctorId: id, timeSlot: '' });
                      setAvailableSlots([]);
                      fetchSlots(id, form.appointmentDate);
                    }}>
                    <option value="">Select Doctor</option>
                    {doctors.map(d => (
                      <option key={d.id} value={d.id}>Dr. {d.firstName} {d.lastName} — {d.specialization}</option>
                    ))}
                  </select>
                </div>
                <div className="space-y-2">
                  <Label>Date</Label>
                  <Input type="date" value={form.appointmentDate} required
                    min={new Date().toISOString().split('T')[0]}
                    onChange={e => {
                      const date = e.target.value;
                      setForm({ ...form, appointmentDate: date, timeSlot: '' });
                      if (form.appointmentType !== 'EMERGENCY') {
                        setAvailableSlots([]);
                        fetchSlots(form.doctorId, date);
                      }
                    }} />
                </div>
                <div className="space-y-2">
                  <Label>Appointment Type</Label>
                  <select className={selectClasses} value={form.appointmentType}
                    onChange={e => setForm({ ...form, appointmentType: e.target.value, timeSlot: '' })}>
                    <option value="ROUTINE">Routine</option>
                    <option value="FIRST_VISIT">First Visit</option>
                    <option value="FOLLOW_UP">Follow-up</option>
                    <option value="EMERGENCY">Emergency</option>
                  </select>
                </div>
                <div className="space-y-2">
                  <Label>Time Slot</Label>
                  {form.appointmentType === 'EMERGENCY' ? (
                    <div className="text-sm font-medium text-destructive mt-1">
                      Immediate Action. Assigned automatically.
                    </div>
                  ) : (
                    <select className={selectClasses} value={form.timeSlot} required
                      onChange={e => setForm({ ...form, timeSlot: e.target.value })}>
                      <option value="">
                        {form.doctorId && form.appointmentDate
                          ? availableSlots.length === 0 ? 'No slots available' : 'Select Time Slot'
                          : 'Select doctor & date first'}
                      </option>
                      {availableSlots.map(s => <option key={s} value={s}>{s}</option>)}
                    </select>
                  )}
                </div>
                <Button type="submit">Confirm Booking</Button>
              </form>
            </CardContent>
          </Card>
        </motion.div>
      )}

      <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }}>
        <Card>
          <CardContent className="p-0">
            {loading ? (
              <div className="p-8 text-center text-muted-foreground">Loading...</div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Date</TableHead><TableHead>Time</TableHead><TableHead>Doctor</TableHead>
                    <TableHead>Type</TableHead><TableHead>Token</TableHead><TableHead>Status</TableHead><TableHead>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {appointments.map(appt => (
                    <TableRow key={appt.id}>
                      <TableCell>{appt.appointmentDate}</TableCell>
                      <TableCell className="font-mono text-sm">{appt.timeSlot}</TableCell>
                      <TableCell className="font-medium">{appt.doctorName}</TableCell>
                      <TableCell className="text-muted-foreground">{appt.type}</TableCell>
                      <TableCell><Badge variant="outline">{appt.tokenNumber}</Badge></TableCell>
                      <TableCell><Badge variant={statusVariant(appt.status)}>{appt.status === 'APPROVAL_PENDING' ? 'Pending Payment' : appt.status}</Badge></TableCell>
                      <TableCell>
                        {(appt.status === 'SCHEDULED' || appt.status === 'RESCHEDULED' || appt.status === 'APPROVAL_PENDING') && (
                          <div className="flex gap-2">
                            <Button size="sm" variant="outline"
                              onClick={() => {
                                setRescheduleModal({ show: true, appt, newDate: '', newTimeSlot: '', doctorId: appt.doctorId });
                                setAvailableSlots([]);
                              }}>
                              Reschedule
                            </Button>
                            <Button size="sm" variant="destructive"
                              onClick={() => setCancelModal({ show: true, apptId: appt.id, reason: '' })}>
                              Cancel
                            </Button>
                          </div>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                  {appointments.length === 0 && (
                    <TableRow><TableCell colSpan={7} className="text-center py-8 text-muted-foreground">No appointments found</TableCell></TableRow>
                  )}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </motion.div>

      <Dialog open={cancelModal.show} onOpenChange={(open) => !open && setCancelModal({ show: false, apptId: null, reason: '' })}>
        <DialogContent className="sm:max-w-[400px]">
          <DialogHeader>
            <DialogTitle>Cancel Appointment</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 mt-2">
            <div className="space-y-2">
              <Label>Reason for cancellation</Label>
              <Textarea rows={3} value={cancelModal.reason}
                onChange={e => setCancelModal({ ...cancelModal, reason: e.target.value })}
                placeholder="Enter reason..." />
            </div>
            <div className="flex gap-2">
              <Button onClick={handleCancel}>Confirm Cancel</Button>
              <Button variant="outline" onClick={() => setCancelModal({ show: false, apptId: null, reason: '' })}>Go Back</Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      <Dialog open={rescheduleModal.show} onOpenChange={(open) => !open && setRescheduleModal({ show: false, appt: null, newDate: '', newTimeSlot: '', doctorId: null })}>
        <DialogContent className="sm:max-w-[400px]">
          <DialogHeader>
            <DialogTitle>Reschedule Appointment</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 mt-2">
            <p className="text-sm text-muted-foreground">Select a new date and time for Dr. {rescheduleModal.appt?.doctorName}.</p>
            <div className="space-y-2">
              <Label>New Date</Label>
              <Input type="date" value={rescheduleModal.newDate} required
                min={new Date().toISOString().split('T')[0]}
                onChange={e => {
                  const date = e.target.value;
                  setRescheduleModal({ ...rescheduleModal, newDate: date, newTimeSlot: '' });
                  if (rescheduleModal.appt?.appointmentType !== 'EMERGENCY') {
                    setAvailableSlots([]);
                    fetchSlots(rescheduleModal.doctorId, date);
                  }
                }} />
            </div>
            <div className="space-y-2">
              <Label>New Time Slot</Label>
              {rescheduleModal.appt?.appointmentType === 'EMERGENCY' ? (
                <Input type="time" value={rescheduleModal.newTimeSlot} required
                  onChange={e => setRescheduleModal({ ...rescheduleModal, newTimeSlot: e.target.value })} 
                  step="1800" />
              ) : (
                <select className={selectClasses} value={rescheduleModal.newTimeSlot} required
                  onChange={e => setRescheduleModal({ ...rescheduleModal, newTimeSlot: e.target.value })}>
                  <option value="">
                    {rescheduleModal.newDate
                      ? availableSlots.length === 0 ? 'No slots available' : 'Select Time Slot'
                      : 'Select date first'}
                  </option>
                  {availableSlots.map(s => <option key={s} value={s}>{s}</option>)}
                </select>
              )}
            </div>
            <div className="flex gap-2 mt-4">
              <Button onClick={handleReschedule}>Confirm Reschedule</Button>
              <Button variant="outline" onClick={() => setRescheduleModal({ show: false, appt: null, newDate: '', newTimeSlot: '', doctorId: null })}>Cancel</Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </Layout>
  );
};

export default PatientAppointments;