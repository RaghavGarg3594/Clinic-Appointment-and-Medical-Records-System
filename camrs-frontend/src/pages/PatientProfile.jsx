import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import api from '../services/api';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Separator } from '@/components/ui/separator';
import { Skeleton } from '@/components/ui/skeleton';
import { motion } from 'framer-motion';

const selectClasses = "flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-xs transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring";

const PatientProfile = () => {
  const [profile, setProfile] = useState(null);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({});
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await api.get('/patients/me');
        setProfile(res.data);
        setForm(res.data);
      } catch (err) { console.error(err); }
      setLoading(false);
    };
    fetchProfile();
  }, []);

  const handleSave = async () => {
    if (form.phone && !/^\d{10}$/.test(form.phone)) {
      setMessage('Phone number must be exactly 10 digits');
      setTimeout(() => setMessage(''), 3000);
      return;
    }
    try {
      const res = await api.put('/patients/me', form);
      setProfile(res.data);
      setEditing(false);
      setMessage('Profile updated successfully!');
      setTimeout(() => setMessage(''), 3000);
    } catch (err) { alert('Failed to update profile'); }
  };

  if (loading) {
    return (
      <Layout>
        <div className="space-y-4">
          <Skeleton className="h-10 w-1/3" />
          <Skeleton className="h-64 rounded-xl" />
        </div>
      </Layout>
    );
  }

  const initials = `${profile?.firstName?.[0] || ''}${profile?.lastName?.[0] || ''}`.toUpperCase();

  return (
    <Layout>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold tracking-tight">My Profile</h1>
        {!editing && <Button onClick={() => setEditing(true)}>Edit Profile</Button>}
      </div>

      {message && (
        <Alert className="mb-4 border-primary/30 bg-primary/10 text-primary-foreground">
          <AlertDescription>{message}</AlertDescription>
        </Alert>
      )}

      <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }}>
        <Card>
          <CardContent className="pt-6">
            {/* Avatar + Name header */}
            <div className="flex items-center gap-4 mb-6">
              <Avatar className="h-16 w-16 text-xl">
                <AvatarFallback className="bg-primary/10 text-primary font-bold">{initials}</AvatarFallback>
              </Avatar>
              <div>
                <p className="text-lg font-semibold">{profile?.firstName} {profile?.lastName}</p>
                <p className="text-sm text-muted-foreground">{profile?.email}</p>
              </div>
            </div>

            <Separator className="mb-6" />

            <div className="grid grid-cols-2 gap-x-6 gap-y-5">
              <div className="space-y-1.5">
                <Label className="text-xs uppercase tracking-wide text-muted-foreground font-semibold">First Name</Label>
                {editing ? (
                  <Input value={form.firstName || ''} onChange={e => setForm({ ...form, firstName: e.target.value })} />
                ) : (
                  <p className="text-sm">{profile?.firstName}</p>
                )}
              </div>

              <div className="space-y-1.5">
                <Label className="text-xs uppercase tracking-wide text-muted-foreground font-semibold">Last Name</Label>
                {editing ? (
                  <Input value={form.lastName || ''} onChange={e => setForm({ ...form, lastName: e.target.value })} />
                ) : (
                  <p className="text-sm">{profile?.lastName}</p>
                )}
              </div>

              <div className="space-y-1.5">
                <Label className="text-xs uppercase tracking-wide text-muted-foreground font-semibold">Email</Label>
                <p className="text-sm">{profile?.email}</p>
              </div>

              <div className="space-y-1.5">
                <Label className="text-xs uppercase tracking-wide text-muted-foreground font-semibold">Phone</Label>
                {editing ? (
                  <Input value={form.phone || ''} onChange={e => setForm({ ...form, phone: e.target.value })} />
                ) : (
                  <p className="text-sm">{profile?.phone || 'Not set'}</p>
                )}
              </div>

              <div className="space-y-1.5">
                <Label className="text-xs uppercase tracking-wide text-muted-foreground font-semibold">Date of Birth</Label>
                <p className="text-sm">{profile?.dateOfBirth || 'Not set'}</p>
              </div>

              <div className="space-y-1.5">
                <Label className="text-xs uppercase tracking-wide text-muted-foreground font-semibold">Age</Label>
                <p className="text-sm">{profile?.age}</p>
              </div>

              <div className="space-y-1.5">
                <Label className="text-xs uppercase tracking-wide text-muted-foreground font-semibold">Gender</Label>
                {editing ? (
                  <select className={selectClasses} value={form.gender || ''} onChange={e => setForm({ ...form, gender: e.target.value })}>
                    <option value="Male">Male</option>
                    <option value="Female">Female</option>
                    <option value="Other">Other</option>
                  </select>
                ) : (
                  <p className="text-sm">{profile?.gender}</p>
                )}
              </div>

              <div className="space-y-1.5">
                <Label className="text-xs uppercase tracking-wide text-muted-foreground font-semibold">Address</Label>
                {editing ? (
                  <Textarea rows={2} value={form.address || ''} onChange={e => setForm({ ...form, address: e.target.value })} />
                ) : (
                  <p className="text-sm">{profile?.address || 'Not set'}</p>
                )}
              </div>

              <div className="space-y-1.5 col-span-2">
                <Label className="text-xs uppercase tracking-wide text-muted-foreground font-semibold">Medical History</Label>
                {editing ? (
                  <Textarea rows={3} value={form.medicalHistory || ''} onChange={e => setForm({ ...form, medicalHistory: e.target.value })} />
                ) : (
                  <p className="text-sm">{profile?.medicalHistory || 'None recorded'}</p>
                )}
              </div>

              <div className="space-y-1.5">
                <Label className="text-xs uppercase tracking-wide text-muted-foreground font-semibold">Allergies</Label>
                {editing ? (
                  <Textarea rows={2} value={form.allergies || ''} onChange={e => setForm({ ...form, allergies: e.target.value })} />
                ) : (
                  <p className="text-sm">{profile?.allergies || 'None recorded'}</p>
                )}
              </div>

              <div className="space-y-1.5">
                <Label className="text-xs uppercase tracking-wide text-muted-foreground font-semibold">Insurance Details</Label>
                {editing ? (
                  <Textarea rows={2} value={form.insuranceDetails || ''} onChange={e => setForm({ ...form, insuranceDetails: e.target.value })} />
                ) : (
                  <p className="text-sm">{profile?.insuranceDetails || 'None recorded'}</p>
                )}
              </div>

              <div className="space-y-1.5">
                <Label className="text-xs uppercase tracking-wide text-muted-foreground font-semibold">Emergency Contact</Label>
                {editing ? (
                  <Input value={form.emergencyContact || ''} onChange={e => setForm({ ...form, emergencyContact: e.target.value })} placeholder="Name & Phone" />
                ) : (
                  <p className="text-sm">{profile?.emergencyContact || 'Pending'}</p>
                )}
              </div>
            </div>

            {editing && (
              <div className="flex gap-2 mt-6">
                <Button onClick={handleSave}>Save Changes</Button>
                <Button variant="outline" onClick={() => { setEditing(false); setForm(profile); }}>Cancel</Button>
              </div>
            )}
          </CardContent>
        </Card>
      </motion.div>
    </Layout>
  );
};

export default PatientProfile;
