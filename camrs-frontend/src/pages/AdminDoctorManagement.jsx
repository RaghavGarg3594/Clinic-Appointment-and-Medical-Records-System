import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import api from '../services/api';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { motion } from 'framer-motion';
import { Search } from 'lucide-react';

const AdminDoctorManagement = () => {
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editDoctor, setEditDoctor] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [formData, setFormData] = useState({
    firstName: '', lastName: '', email: '', password: '',
    specialization: '', qualification: '', licenseNumber: '', phone: '', consultationFee: ''
  });

  useEffect(() => { fetchDoctors(); }, []);

  const fetchDoctors = async () => {
    try {
      const resp = await api.get('/admin/doctors');
      setDoctors(resp.data);
      setError('');
    } catch (err) {
      console.error(err);
      setError('Failed to load doctors. Please try again.');
    }
    finally { setLoading(false); }
  };

  const handleInputChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      // Build payload, exclude empty password for edits
      const payload = { ...formData };
      if (editDoctor && (!payload.password || payload.password.trim() === '')) {
        delete payload.password;
      }

      // Ensure consultationFee is sent as a number or null (not empty string)
      if (payload.consultationFee === '' || payload.consultationFee === undefined) {
        payload.consultationFee = null;
      } else {
        payload.consultationFee = parseFloat(payload.consultationFee);
      }

      if (editDoctor) {
        await api.put(`/admin/doctors/${editDoctor.id}`, payload);
        setSuccess('Doctor updated successfully!');
      } else {
        await api.post('/admin/doctors', payload);
        setSuccess('Doctor added successfully!');
      }
      setShowModal(false);
      fetchDoctors();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      const msg = err.response?.data?.message
        || (typeof err.response?.data === 'string' ? err.response.data : null)
        || 'Operation failed. Please check the details and try again.';
      setError(msg);
    }
  };

  const toggleActive = async (doc) => {
    try {
      await api.put(`/admin/doctors/${doc.id}/toggle-active`);
      fetchDoctors();
    } catch (err) {
      setError(`Failed to ${doc.isActive ? 'deactivate' : 'activate'} doctor.`);
      setTimeout(() => setError(''), 3000);
    }
  };

  return (
    <Layout>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold tracking-tight">Doctor Management</h1>
        <div className="flex gap-3 items-center">
          <div className="relative">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input 
              type="text" 
              placeholder="Search doctors..." 
              className="pl-9 h-9 w-[250px] bg-white border-border focus:ring-oxford"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <Button onClick={() => {
            setEditDoctor(null);
            setFormData({ firstName: '', lastName: '', email: '', password: '', specialization: '', qualification: '', licenseNumber: '', phone: '', consultationFee: '' });
            setError('');
            setSuccess('');
            setShowModal(true);
          }}>
            + Add Doctor
          </Button>
        </div>
      </div>

      {success && (
        <Alert className="mb-4 border-primary/30 bg-primary/10 text-primary-foreground">
          <AlertDescription>{success}</AlertDescription>
        </Alert>
      )}
      {error && !showModal && (
        <Alert variant="destructive" className="mb-4">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }}>
        <Card>
          <CardContent className="p-0">
            {loading ? (
              <div className="p-8 text-center text-muted-foreground">Loading doctors...</div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Specialization</TableHead>
                    <TableHead>Email</TableHead>
                    <TableHead>Phone</TableHead>
                    <TableHead>Fee</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {doctors.filter(d => 
                    d.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) || 
                    d.lastName?.toLowerCase().includes(searchTerm.toLowerCase()) || 
                    d.specialization?.toLowerCase().includes(searchTerm.toLowerCase())
                  ).map(doc => (
                    <TableRow key={doc.id}>
                      <TableCell className="font-medium">Dr. {doc.firstName} {doc.lastName}</TableCell>
                      <TableCell>{doc.specialization}</TableCell>
                      <TableCell className="text-muted-foreground">{doc.email}</TableCell>
                      <TableCell className="text-muted-foreground">{doc.phone || '—'}</TableCell>
                      <TableCell className="font-semibold">₹{doc.consultationFee}</TableCell>
                      <TableCell>
                        <Badge variant={doc.isActive ? 'default' : 'secondary'}>
                          {doc.isActive ? 'Active' : 'Inactive'}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className="flex gap-2">
                          <Button size="sm" variant="outline" onClick={() => {
                            setEditDoctor(doc);
                            setFormData({
                              firstName: doc.firstName, lastName: doc.lastName, email: doc.email, password: '',
                              specialization: doc.specialization, qualification: doc.qualification || '',
                              licenseNumber: doc.licenseNumber || '', phone: doc.phone || '', consultationFee: doc.consultationFee || ''
                            });
                            setError('');
                            setSuccess('');
                            setShowModal(true);
                          }}>
                            Edit
                          </Button>
                          <Button size="sm" variant={doc.isActive ? 'destructive' : 'default'} onClick={() => toggleActive(doc)}>
                            {doc.isActive ? 'Deactivate' : 'Activate'}
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                  {doctors.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">No doctors found.</TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </motion.div>

      <Dialog open={showModal} onOpenChange={setShowModal}>
        <DialogContent className="sm:max-w-[550px]">
          <DialogHeader>
            <DialogTitle>{editDoctor ? 'Edit Doctor' : 'Add New Doctor'}</DialogTitle>
          </DialogHeader>
          {error && showModal && (
            <Alert variant="destructive" className="mt-2">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}
          <form onSubmit={handleSubmit} className="space-y-4 mt-2">
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label>First Name</Label>
                <Input name="firstName" value={formData.firstName} onChange={handleInputChange} required />
              </div>
              <div className="space-y-2">
                <Label>Last Name</Label>
                <Input name="lastName" value={formData.lastName} onChange={handleInputChange} required />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label>Email</Label>
                <Input type="email" name="email" value={formData.email} onChange={handleInputChange} required />
              </div>
              <div className="space-y-2">
                <Label>Password {editDoctor && '(leave blank to keep)'}</Label>
                <Input type="password" name="password" value={formData.password} onChange={handleInputChange} {...(!editDoctor && { required: true, minLength: 8 })} />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label>Specialization</Label>
                <Input name="specialization" value={formData.specialization} onChange={handleInputChange} required />
              </div>
              <div className="space-y-2">
                <Label>Qualification</Label>
                <Input name="qualification" value={formData.qualification} onChange={handleInputChange} required />
              </div>
            </div>
            <div className="grid grid-cols-3 gap-3">
              <div className="space-y-2">
                <Label>License Number</Label>
                <Input name="licenseNumber" value={formData.licenseNumber} onChange={handleInputChange} required />
              </div>
              <div className="space-y-2">
                <Label>Phone</Label>
                <Input name="phone" value={formData.phone} onChange={handleInputChange} placeholder="e.g. +91-9876543210" />
              </div>
              <div className="space-y-2">
                <Label>Consultation Fee (₹)</Label>
                <Input type="number" name="consultationFee" value={formData.consultationFee} onChange={handleInputChange} />
              </div>
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <Button type="button" variant="outline" onClick={() => { setShowModal(false); setError(''); }}>Cancel</Button>
              <Button type="submit">Save</Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </Layout>
  );
};

export default AdminDoctorManagement;
