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
import { motion } from 'framer-motion';
import { Search } from 'lucide-react';

const AdminInventoryManagement = () => {
  const [medications, setMedications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editId, setEditId] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [formData, setFormData] = useState({
    name: '', category: '', stockQuantity: 0, reorderLevel: 10, expiryDate: '', price: ''
  });

  useEffect(() => { fetchInventory(); }, []);

  const fetchInventory = async () => {
    try {
      const resp = await api.get('/admin/inventory');
      setMedications(resp.data);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handleInputChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleEdit = (med) => {
    setEditId(med.id);
    setFormData({
      name: med.name, category: med.category, stockQuantity: med.stockQuantity,
      reorderLevel: med.reorderLevel, expiryDate: med.expiryDate || '', price: med.price || ''
    });
    setShowModal(true);
  };

  const handleAdd = () => {
    setEditId(null);
    setFormData({ name: '', category: '', stockQuantity: 0, reorderLevel: 10, expiryDate: '', price: '' });
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editId) {
        await api.put(`/admin/inventory/${editId}`, formData);
      } else {
        await api.post('/admin/inventory', formData);
      }
      setShowModal(false);
      fetchInventory();
    } catch (err) { console.error(err); }
  };

  return (
    <Layout>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold tracking-tight">Pharmacy Inventory</h1>
        <div className="flex gap-3 items-center">
          <div className="relative">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input 
              type="text" 
              placeholder="Search medications..." 
              className="pl-9 h-9 w-[250px] bg-white border-border focus:ring-oxford"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <Button onClick={handleAdd}>+ Add Medication</Button>
        </div>
      </div>

      <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }}>
        <Card>
          <CardContent className="p-0">
            {loading ? (
              <div className="p-8 text-center text-muted-foreground">Loading inventory...</div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Category</TableHead>
                    <TableHead>Price</TableHead>
                    <TableHead>Stock</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {medications.filter(m => 
                    m.name.toLowerCase().includes(searchTerm.toLowerCase()) || 
                    m.category.toLowerCase().includes(searchTerm.toLowerCase())
                  ).map(med => (
                    <TableRow key={med.id}>
                      <TableCell className="font-medium">{med.name}</TableCell>
                      <TableCell className="text-muted-foreground">{med.category}</TableCell>
                      <TableCell className="font-mono">₹{med.price}</TableCell>
                      <TableCell className="font-semibold">{med.stockQuantity}</TableCell>
                      <TableCell>
                        {med.stockQuantity <= med.reorderLevel ? (
                          <Badge variant="destructive">Low Stock</Badge>
                        ) : (
                          <Badge variant="default">In Stock</Badge>
                        )}
                      </TableCell>
                      <TableCell>
                        <Button size="sm" variant="outline" onClick={() => handleEdit(med)}>Update</Button>
                      </TableCell>
                    </TableRow>
                  ))}
                  {medications.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={6} className="text-center py-8 text-muted-foreground">No inventory available.</TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </motion.div>

      <Dialog open={showModal} onOpenChange={setShowModal}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>{editId ? 'Update Medication' : 'Add Medication'}</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit} className="space-y-4 mt-2">
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label>Medication Name</Label>
                <Input name="name" value={formData.name} onChange={handleInputChange} required placeholder="Medication Name" />
              </div>
              <div className="space-y-2">
                <Label>Category</Label>
                <Input name="category" value={formData.category} onChange={handleInputChange} required placeholder="Category" />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label>Stock Quantity</Label>
                <Input type="number" name="stockQuantity" value={formData.stockQuantity} onChange={handleInputChange} required />
              </div>
              <div className="space-y-2">
                <Label>Reorder Level</Label>
                <Input type="number" name="reorderLevel" value={formData.reorderLevel} onChange={handleInputChange} required />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label>Expiry Date (Optional)</Label>
                <Input type="date" name="expiryDate" value={formData.expiryDate} onChange={handleInputChange} />
              </div>
              <div className="space-y-2">
                <Label>Price (₹)</Label>
                <Input type="number" step="0.01" name="price" value={formData.price} onChange={handleInputChange} required />
              </div>
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <Button type="button" variant="outline" onClick={() => setShowModal(false)}>Cancel</Button>
              <Button type="submit">Save</Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </Layout>
  );
};

export default AdminInventoryManagement;
