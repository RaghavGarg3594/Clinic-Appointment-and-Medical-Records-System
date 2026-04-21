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
import { motion } from 'framer-motion';

const statusVariant = (status) => {
  const map = { PENDING: 'secondary', APPROVED: 'default', REJECTED: 'destructive' };
  return map[status] || 'outline';
};

const AdminDoctorRequests = () => {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);
  const [statusUpdate, setStatusUpdate] = useState('');
  const [adminNotes, setAdminNotes] = useState('');
  const [saving, setSaving] = useState(false);
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [successMsg, setSuccessMsg] = useState('');

  // Credentials popup for approval
  const [credentialsPopup, setCredentialsPopup] = useState(false);
  const [credUsername, setCredUsername] = useState('');
  const [credPassword, setCredPassword] = useState('');
  const [credError, setCredError] = useState('');

  useEffect(() => { fetchRequests(); }, []);

  const fetchRequests = async () => {
    try {
      const res = await api.get('/admin/doctor-requests');
      setRequests(res.data);
    } catch (err) {
      console.error('Failed to fetch doctor requests:', err);
    } finally {
      setLoading(false);
    }
  };

  const openRequest = (req) => {
    setSelected(req);
    setStatusUpdate(req.status);
    setAdminNotes(req.adminNotes || '');
    setSuccessMsg('');
  };

  const handleSave = async () => {
    if (!selected) return;

    // If status is APPROVED, show credentials popup first
    if (statusUpdate === 'APPROVED' && selected.status !== 'APPROVED') {
      setCredUsername(selected.email || '');
      setCredPassword('');
      setCredError('');
      setCredentialsPopup(true);
      return;
    }

    await doSave({});
  };

  const doSave = async (extraFields) => {
    setSaving(true);
    try {
      const res = await api.put(`/admin/doctor-requests/${selected.id}`, {
        status: statusUpdate,
        adminNotes: adminNotes,
        ...extraFields,
      });
      setRequests(prev => prev.map(r => r.id === selected.id ? res.data : r));
      setSelected(res.data);
      setSuccessMsg('Updated successfully!');
      setCredentialsPopup(false);
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data || 'Update failed';
      if (credentialsPopup) {
        setCredError(typeof msg === 'string' ? msg : 'Update failed');
      } else {
        console.error('Update failed:', err);
      }
    } finally {
      setSaving(false);
    }
  };

  const handleCredentialsSave = async () => {
    if (!credUsername || !credPassword) {
      setCredError('Username and password are required');
      return;
    }
    if (credPassword.length < 8) {
      setCredError('Password must be at least 8 characters');
      return;
    }
    await doSave({ username: credUsername, password: credPassword });
  };

  const filtered = filterStatus === 'ALL' ? requests : requests.filter(r => r.status === filterStatus);
  const pendingCount = requests.filter(r => r.status === 'PENDING').length;

  return (
    <Layout>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold tracking-tight mb-1">Doctor Join Requests</h1>
          {pendingCount > 0 && (
            <Badge variant="destructive" className="text-xs">{pendingCount} pending</Badge>
          )}
        </div>
        <select
          value={filterStatus}
          onChange={e => setFilterStatus(e.target.value)}
          className="flex h-9 rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-xs focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
        >
          <option value="ALL">All Statuses</option>
          <option value="PENDING">Pending</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>

      {loading ? (
        <div className="text-center py-12 text-muted-foreground">Loading requests...</div>
      ) : (
        <div className={`grid gap-5 ${selected ? 'grid-cols-2' : 'grid-cols-1'}`}>
          {/* LIST */}
          <div className="space-y-3">
            {filtered.length === 0 ? (
              <Card className="p-10 text-center text-muted-foreground">No requests found.</Card>
            ) : filtered.map(req => (
              <motion.div key={req.id} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.25 }}>
                <Card
                  onClick={() => openRequest(req)}
                  className={`cursor-pointer p-4 transition-all duration-200 hover:shadow-md ${selected?.id === req.id ? 'ring-2 ring-primary' : ''}`}
                >
                  <CardContent className="p-0">
                    <div className="flex justify-between items-start">
                      <div>
                        <p className="font-semibold">{req.firstName} {req.lastName}</p>
                        <p className="text-sm text-muted-foreground mt-0.5">
                          {req.specialization} • {req.experienceYears ? `${req.experienceYears} yrs exp` : 'Exp N/A'}
                        </p>
                        <p className="text-xs text-muted-foreground mt-1">{req.email}</p>
                      </div>
                      <Badge variant={statusVariant(req.status)}>{req.status}</Badge>
                    </div>
                    <p className="text-xs text-muted-foreground mt-2">
                      Submitted: {new Date(req.submittedAt).toLocaleString()}
                    </p>
                  </CardContent>
                </Card>
              </motion.div>
            ))}
          </div>

          {/* DETAIL PANEL */}
          {selected && (
            <motion.div initial={{ opacity: 0, x: 16 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.3 }}>
              <Card className="p-6 sticky top-5">
                <div className="flex justify-between items-center mb-4">
                  <h3 className="font-semibold text-lg">Request Details</h3>
                  <Button variant="ghost" size="sm" onClick={() => setSelected(null)}>✕</Button>
                </div>

                {successMsg && (
                  <Alert className="mb-4 border-primary/30 bg-primary/10 text-primary-foreground">
                    <AlertDescription>{successMsg}</AlertDescription>
                  </Alert>
                )}

                <div className="space-y-2 text-sm">
                  {[
                    ['Name', `${selected.firstName} ${selected.lastName}`],
                    ['Email', selected.email],
                    ['Phone', selected.phone || '—'],
                    ['Specialization', selected.specialization || '—'],
                    ['Qualification', selected.qualification || '—'],
                    ['License No.', selected.licenseNumber || '—'],
                    ['Experience', selected.experienceYears ? `${selected.experienceYears} years` : '—'],
                    ['Submitted', new Date(selected.submittedAt).toLocaleString()],
                  ].map(([label, val]) => (
                    <div key={label} className="flex">
                      <span className="w-2/5 text-muted-foreground font-medium">{label}</span>
                      <span className="w-3/5">{val}</span>
                    </div>
                  ))}
                </div>

                {selected.message && (
                  <div className="mt-4">
                    <p className="text-xs font-medium text-muted-foreground mb-1">Message</p>
                    <div className="bg-muted rounded-md p-3 text-sm leading-relaxed">{selected.message}</div>
                  </div>
                )}

                <Separator className="my-5" />

                <div className="space-y-4">
                  <div className="space-y-2">
                    <Label>Update Status</Label>
                    <select
                      value={statusUpdate}
                      onChange={e => setStatusUpdate(e.target.value)}
                      className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-xs focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                    >
                      <option value="PENDING">Pending</option>
                      <option value="APPROVED">Approved</option>
                      <option value="REJECTED">Rejected</option>
                    </select>
                  </div>

                  <div className="space-y-2">
                    <Label>Admin Notes</Label>
                    <Textarea rows={3} value={adminNotes} onChange={e => setAdminNotes(e.target.value)}
                      placeholder="Add internal notes (e.g. called on 12 Jan, scheduled interview...)" />
                  </div>

                  <div className="flex gap-2">
                    <Button onClick={handleSave} disabled={saving} className="flex-1">
                      {saving ? 'Saving...' : 'Save Changes'}
                    </Button>
                    <Button variant="outline" className="flex-1" asChild>
                      <a href={`mailto:${selected.email}`}>Email Doctor</a>
                    </Button>
                  </div>
                </div>
              </Card>
            </motion.div>
          )}
        </div>
      )}

      {/* Credentials Popup for Doctor Approval */}
      <Dialog open={credentialsPopup} onOpenChange={(open) => !open && setCredentialsPopup(false)}>
        <DialogContent className="sm:max-w-[420px]">
          <DialogHeader>
            <DialogTitle>Set Doctor Login Credentials</DialogTitle>
          </DialogHeader>
          <p className="text-sm text-muted-foreground mb-2">
            Create login credentials for <strong>{selected?.firstName} {selected?.lastName}</strong>.
            They will use these to access the CAMRS portal as a Doctor.
          </p>

          {credError && (
            <Alert variant="destructive" className="mb-3">
              <AlertDescription>{credError}</AlertDescription>
            </Alert>
          )}

          <div className="space-y-3">
            <div className="space-y-1">
              <Label>Username (Email)</Label>
              <Input type="email" value={credUsername} onChange={e => setCredUsername(e.target.value)}
                placeholder="doctor@camrs.com" required />
            </div>
            <div className="space-y-1">
              <Label>Password</Label>
              <Input type="password" value={credPassword} onChange={e => setCredPassword(e.target.value)}
                placeholder="Min. 8 characters" required />
            </div>
          </div>

          <div className="flex gap-2 mt-4">
            <Button onClick={handleCredentialsSave} disabled={saving} className="flex-1">
              {saving ? 'Creating...' : 'Approve & Create Account'}
            </Button>
            <Button variant="outline" onClick={() => setCredentialsPopup(false)}>Cancel</Button>
          </div>
        </DialogContent>
      </Dialog>
    </Layout>
  );
};

export default AdminDoctorRequests;
