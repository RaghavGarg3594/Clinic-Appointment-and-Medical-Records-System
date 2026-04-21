import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';

import Login from './pages/Login';
import PatientDashboard from './pages/PatientDashboard';
import DoctorDashboard from './pages/DoctorDashboard';
import LabDashboard from './pages/LabDashboard';
import AdminDashboard from './pages/AdminDashboard';
import PatientRegistration from './pages/PatientRegistration';
import AdminDoctorManagement from './pages/AdminDoctorManagement';
import AdminDoctorRequests from './pages/AdminDoctorRequests';
import AdminInventoryManagement from './pages/AdminInventoryManagement';
import AdminBilling from './pages/AdminBilling';
import AdminReports from './pages/AdminReports';
import AdminAuditLogs from './pages/AdminAuditLogs';
import PatientAppointments from './pages/PatientAppointments';
import PatientMedicalRecords from './pages/PatientMedicalRecords';
import PatientBills from './pages/PatientBills';
import PatientProfile from './pages/PatientProfile';
import PatientLabResults from './pages/PatientLabResults';
import DoctorAppointments from './pages/DoctorAppointments';
import DoctorConsultation from './pages/DoctorConsultation';
import DoctorPrescriptions from './pages/DoctorPrescriptions';
import DoctorPrescriptionHistory from './pages/DoctorPrescriptionHistory';
import LabPendingTests from './pages/LabPendingTests';
import DoctorJoinRequest from './pages/DoctorJoinRequest';
import AdminLabStaffManagement from './pages/AdminLabStaffManagement';

function App() {
  return (
    <Router>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<PatientRegistration />} />
          <Route path="/doctor-request" element={<DoctorJoinRequest />} />

          <Route path="/patient/dashboard" element={<ProtectedRoute allowedRoles={['PATIENT']}><PatientDashboard /></ProtectedRoute>} />
          <Route path="/patient/appointments" element={<ProtectedRoute allowedRoles={['PATIENT']}><PatientAppointments /></ProtectedRoute>} />
          <Route path="/patient/records" element={<ProtectedRoute allowedRoles={['PATIENT']}><PatientMedicalRecords /></ProtectedRoute>} />
          <Route path="/patient/lab-results" element={<ProtectedRoute allowedRoles={['PATIENT']}><PatientLabResults /></ProtectedRoute>} />
          <Route path="/patient/bills" element={<ProtectedRoute allowedRoles={['PATIENT']}><PatientBills /></ProtectedRoute>} />
          <Route path="/patient/profile" element={<ProtectedRoute allowedRoles={['PATIENT']}><PatientProfile /></ProtectedRoute>} />

          <Route path="/doctor/dashboard" element={<ProtectedRoute allowedRoles={['DOCTOR']}><DoctorDashboard /></ProtectedRoute>} />
          <Route path="/doctor/appointments" element={<ProtectedRoute allowedRoles={['DOCTOR']}><DoctorAppointments /></ProtectedRoute>} />
          <Route path="/doctor/consultation" element={<ProtectedRoute allowedRoles={['DOCTOR']}><DoctorConsultation /></ProtectedRoute>} />
          <Route path="/doctor/prescriptions" element={<ProtectedRoute allowedRoles={['DOCTOR']}><DoctorPrescriptions /></ProtectedRoute>} />
          <Route path="/doctor/prescription-history" element={<ProtectedRoute allowedRoles={['DOCTOR']}><DoctorPrescriptionHistory /></ProtectedRoute>} />

          <Route path="/lab/dashboard" element={<ProtectedRoute allowedRoles={['LAB_STAFF']}><LabDashboard /></ProtectedRoute>} />
          <Route path="/lab/tests" element={<ProtectedRoute allowedRoles={['LAB_STAFF']}><LabPendingTests /></ProtectedRoute>} />

          <Route path="/admin/dashboard" element={<ProtectedRoute allowedRoles={['ADMIN_STAFF']}><AdminDashboard /></ProtectedRoute>} />
          <Route path="/admin/doctors" element={<ProtectedRoute allowedRoles={['ADMIN_STAFF']}><AdminDoctorManagement /></ProtectedRoute>} />
          <Route path="/admin/doctor-requests" element={<ProtectedRoute allowedRoles={['ADMIN_STAFF']}><AdminDoctorRequests /></ProtectedRoute>} />
          <Route path="/admin/inventory" element={<ProtectedRoute allowedRoles={['ADMIN_STAFF']}><AdminInventoryManagement /></ProtectedRoute>} />
          <Route path="/admin/billing" element={<ProtectedRoute allowedRoles={['ADMIN_STAFF']}><AdminBilling /></ProtectedRoute>} />
          <Route path="/admin/lab-staff" element={<ProtectedRoute allowedRoles={['ADMIN_STAFF']}><AdminLabStaffManagement /></ProtectedRoute>} />
          <Route path="/admin/reports" element={<ProtectedRoute allowedRoles={['ADMIN_STAFF']}><AdminReports /></ProtectedRoute>} />
          <Route path="/admin/audit-logs" element={<ProtectedRoute allowedRoles={['ADMIN_STAFF']}><AdminAuditLogs /></ProtectedRoute>} />

          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthProvider>
    </Router>
  );
}

export default App;
