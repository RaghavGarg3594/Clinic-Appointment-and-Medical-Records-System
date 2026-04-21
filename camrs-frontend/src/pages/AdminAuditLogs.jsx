import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { ShieldAlert, Search } from 'lucide-react';
import { Input } from '@/components/ui/input';
import api from '../services/api';
import Layout from '../components/Layout';
import { motion } from 'framer-motion';

const AdminAuditLogs = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchLogs();
  }, []);

  const fetchLogs = async () => {
    try {
      const response = await api.get('/admin/audit-logs');
      setLogs(response.data);
    } catch (error) {
      console.error('Failed to fetch audit logs:', error);
    } finally {
      setLoading(false);
    }
  };

  const filteredLogs = logs.filter(log =>
    log.action.toLowerCase().includes(searchTerm.toLowerCase()) ||
    log.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
    log.entityType.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <Layout>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold tracking-tight">Audit Logs</h1>
        <div className="relative w-72">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search logs..."
            className="pl-9 h-9 bg-white border-border focus:ring-oxford"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }}>
        <Card className="shadow-lg border-border/60 overflow-hidden">
          <CardHeader className="bg-muted/30 pb-4 border-b border-border/50">
            <div className="flex items-center gap-2">
              <ShieldAlert className="w-5 h-5 text-terracotta" />
              <CardTitle className="text-lg">System Audit Logs</CardTitle>
            </div>
            <CardDescription>
              Chronological record of system administrative and user events.
            </CardDescription>
          </CardHeader>
          <CardContent className="p-0">
            {loading ? (
              <div className="p-8 text-center text-muted-foreground">Loading audit logs...</div>
            ) : logs.length === 0 ? (
              <div className="p-8 text-center text-muted-foreground">No audit logs found.</div>
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader className="bg-muted/10">
                    <TableRow className="hover:bg-transparent">
                      <TableHead className="font-semibold text-oxford">Timestamp</TableHead>
                      <TableHead className="font-semibold text-oxford">User/Actor</TableHead>
                      <TableHead className="font-semibold text-oxford">Action</TableHead>
                      <TableHead className="font-semibold text-oxford">Target Entity</TableHead>
                      <TableHead className="font-semibold text-oxford">Location (IP)</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredLogs.map((log) => (
                      <TableRow key={log.id} className="cursor-default hover:bg-muted/30 transition-colors">
                        <TableCell className="font-mono text-xs whitespace-nowrap text-muted-foreground">
                          {new Date(log.timestamp).toLocaleString()}
                        </TableCell>
                        <TableCell className="font-medium text-oxford">
                          {log.username}
                        </TableCell>
                        <TableCell>
                          <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-terracotta/10 text-terracotta-deep border border-terracotta/20">
                            {log.action}
                          </span>
                        </TableCell>
                        <TableCell>
                          <div className="flex flex-col">
                            <span className="text-sm font-medium">{log.entityType}</span>
                            <span className="text-xs text-muted-foreground">ID: {log.entityId}</span>
                          </div>
                        </TableCell>
                        <TableCell className="text-sm font-mono text-muted-foreground">
                          {log.ipAddress || 'Internal'}
                        </TableCell>
                      </TableRow>
                    ))}
                    {filteredLogs.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={5} className="h-24 text-center text-muted-foreground">
                          No matches found for "{searchTerm}"
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>
      </motion.div>
    </Layout>
  );
};

export default AdminAuditLogs;
