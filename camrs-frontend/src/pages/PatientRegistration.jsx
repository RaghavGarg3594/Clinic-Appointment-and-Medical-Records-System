import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../services/api';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { motion } from 'framer-motion';
import { Activity, Clock, FileText, UserCheck, ShieldCheck } from 'lucide-react';

const PatientRegistration = () => {
  const [formData, setFormData] = useState({
    firstName: '', lastName: '', email: '', password: '',
    phone: '', dateOfBirth: '', gender: 'Other',
    medicalHistory: '', allergies: '', insuranceDetails: '',
    emergencyContact: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');

    // Client-side validation
    if (!formData.firstName.trim() || !formData.lastName.trim()) {
      setError('First name and last name are required');
      return;
    }
    if (!/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(formData.email)) {
      setError('Please provide a valid email address');
      return;
    }
    if (formData.phone && !/^\d{10}$/.test(formData.phone)) {
      setError('Phone number must be exactly 10 digits');
      return;
    }
    if (formData.dateOfBirth && new Date(formData.dateOfBirth) >= new Date()) {
      setError('Date of birth must be a past date');
      return;
    }
    if (formData.password.length < 8) {
      setError('Password must be at least 8 characters');
      return;
    }

    setLoading(true);
    try {
      await api.post('/auth/register', formData);
      navigate('/login?registered=true');
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || 'Failed to register. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const features = [
    {
      title: "Instant Appointments",
      description: "Book, reschedule, or cancel clinic visits entirely online.",
      icon: <Clock className="w-5 h-5 text-terracotta" />
    },
    {
      title: "Digital Records",
      description: "Securely access your medical diagnoses and clinical notes.",
      icon: <FileText className="w-5 h-5 text-rustic" />
    },
    {
      title: "Health Monitoring",
      description: "Track your lab test results clearly with historical comparisons.",
      icon: <Activity className="w-5 h-5 text-oxford" />
    },
    {
      title: "Personalized Care",
      description: "Maintain accurate health history for doctors to review instantly.",
      icon: <UserCheck className="w-5 h-5 text-terracotta" />
    }
  ];

  return (
    <div className="min-h-screen w-full flex flex-col lg:flex-row bg-background overflow-x-hidden">
      
      {/* LEFT SECTION: Patient Benefits */}
      <div className="relative flex-1 lg:flex-[1.2] hidden md:flex flex-col justify-center px-12 lg:px-24 py-16 bg-gradient-to-br from-oxford via-oxford to-[#3A4556] text-white overflow-hidden">
        
        <div className="absolute top-[-10%] left-[-10%] w-[600px] h-[600px] bg-terracotta/15 rounded-full blur-[120px] pointer-events-none" />
        <div className="absolute bottom-[-10%] right-[-10%] w-[500px] h-[500px] bg-rustic/15 rounded-full blur-[100px] pointer-events-none" />
        
        <div className="relative z-10 w-full max-w-2xl mx-auto space-y-12">
          
          <motion.div initial={{ opacity: 0, y: 30 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6 }}>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 border border-white/20 mb-6 backdrop-blur-md">
              <ShieldCheck className="w-4 h-4 text-peach" />
              <span className="text-sm font-medium tracking-wide text-peach">Patient Portal</span>
            </div>
            
            <h1 className="text-4xl lg:text-5xl font-extrabold tracking-tight mb-4 leading-tight">
              Take Control of Your <br/>
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-peach to-[#D4946A]">
                Healthcare Journey.
              </span>
            </h1>
            
            <p className="text-lg text-white/60 max-w-xl leading-relaxed">
              Register securely to book appointments seamlessly, access your private digital clinical notes, and manage your billing history all from one unified dashboard.
            </p>
          </motion.div>

          <motion.div 
            initial="hidden"
            animate="show"
            variants={{
              show: { transition: { staggerChildren: 0.1 } }
            }}
            className="grid grid-cols-1 sm:grid-cols-2 gap-5 mt-8"
          >
            {features.map((feature, idx) => (
              <motion.div 
                key={idx}
                variants={{
                  hidden: { opacity: 0, y: 20 },
                  show: { opacity: 1, y: 0, transition: { duration: 0.5 } }
                }}
                className="bg-white/5 border border-white/10 rounded-xl p-5 backdrop-blur-sm hover:bg-white/10 transition-colors duration-300"
              >
                <div className="w-10 h-10 rounded-lg bg-white/10 flex items-center justify-center mb-4">
                  {feature.icon}
                </div>
                <h3 className="font-semibold text-white/90 mb-2">{feature.title}</h3>
                <p className="text-sm text-white/50 leading-relaxed">{feature.description}</p>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </div>

      {/* RIGHT SECTION: Registration Form */}
      <div className="flex-1 flex items-center justify-center p-6 lg:p-12 relative bg-background border-l border-border/50 overflow-y-auto min-h-screen lg:min-h-0">
        
        <div className="md:hidden absolute top-[-100px] right-[-100px] w-[300px] h-[300px] bg-terracotta/8 rounded-full blur-[80px]" />
        
        <motion.div
          initial={{ opacity: 0, x: 20, scale: 0.98 }}
          animate={{ opacity: 1, x: 0, scale: 1 }}
          transition={{ duration: 0.5, ease: 'easeOut' }}
          className="relative z-10 w-full max-w-[560px] py-10"
        >
          <div className="mb-8 md:hidden text-center">
            <h2 className="text-3xl font-extrabold tracking-tight text-foreground">Join CAMRS</h2>
            <p className="text-muted-foreground mt-2">Create your patient profile</p>
          </div>

          <Card className="shadow-2xl border-border bg-card">
            <CardHeader className="text-center pb-2">
              <CardTitle className="text-2xl font-bold tracking-tight">Create an Account</CardTitle>
              <CardDescription>Register as a patient to securely book appointments and view records</CardDescription>
            </CardHeader>
            <CardContent className="pt-4">
              {error && (
                <Alert variant="destructive" className="mb-4">
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              )}

              <form onSubmit={handleRegister} className="space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div className="space-y-2">
                    <Label className="font-semibold">First Name</Label>
                    <Input name="firstName" value={formData.firstName} onChange={handleChange} required className="bg-muted/40" />
                  </div>
                  <div className="space-y-2">
                    <Label className="font-semibold">Last Name</Label>
                    <Input name="lastName" value={formData.lastName} onChange={handleChange} required className="bg-muted/40" />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label className="font-semibold">Email Address</Label>
                  <Input type="email" name="email" value={formData.email} onChange={handleChange} required className="bg-muted/40" />
                </div>

                <div className="space-y-2">
                  <Label className="font-semibold">Password</Label>
                  <Input type="password" name="password" value={formData.password} onChange={handleChange} minLength={8} required className="bg-muted/40" />
                  <p className="text-xs text-muted-foreground">Must be at least 8 characters</p>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div className="space-y-2">
                    <Label className="font-semibold">Phone Number</Label>
                    <Input type="tel" name="phone" value={formData.phone} onChange={handleChange} required className="bg-muted/40" />
                  </div>
                  <div className="space-y-2">
                    <Label className="font-semibold">Date of Birth</Label>
                    <Input type="date" name="dateOfBirth" value={formData.dateOfBirth} onChange={handleChange} required className="bg-muted/40" />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label className="font-semibold">Gender</Label>
                  <select
                    name="gender"
                    value={formData.gender}
                    onChange={handleChange}
                    className="flex h-10 w-full rounded-md border border-input bg-muted/40 px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                  >
                    <option value="Male">Male</option>
                    <option value="Female">Female</option>
                    <option value="Other">Other</option>
                  </select>
                </div>

                <div className="space-y-2">
                  <Label className="font-semibold">Medical History</Label>
                  <Textarea name="medicalHistory" value={formData.medicalHistory} onChange={handleChange}
                    placeholder="e.g. Diabetes, Hypertension, Previous surgeries..." rows={2} className="bg-muted/40" />
                  <p className="text-xs text-muted-foreground">Optional — helps doctors provide better care</p>
                </div>

                <div className="space-y-2">
                  <Label className="font-semibold">Known Allergies</Label>
                  <Textarea name="allergies" value={formData.allergies} onChange={handleChange}
                    placeholder="e.g. Penicillin, Aspirin, Sulfa drugs..." rows={2} className="bg-muted/40" />
                  <p className="text-xs text-muted-foreground">Optional — used for prescription safety checks</p>
                </div>

                <div className="space-y-2">
                  <Label className="font-semibold">Insurance Details</Label>
                  <Input name="insuranceDetails" value={formData.insuranceDetails} onChange={handleChange}
                    placeholder="e.g. Policy number, Provider name" className="bg-muted/40" />
                </div>

                <div className="space-y-2">
                  <Label className="font-semibold">Emergency Contact</Label>
                  <Input name="emergencyContact" value={formData.emergencyContact} onChange={handleChange}
                    placeholder="e.g. Jane Doe (Wife) - 9876543210" className="bg-muted/40" />
                </div>

                <Button type="submit" className="w-full h-11 text-base shadow-md mt-4 bg-oxford hover:bg-oxford/90" disabled={loading}>
                  {loading ? 'Registering...' : 'Register Now'}
                </Button>
              </form>

              <div className="mt-8 pt-6 border-t border-border flex justify-center text-sm text-muted-foreground">
                <p>Already have an account?{' '}
                <Link to="/login" className="text-terracotta font-bold hover:underline transition">
                  Sign In here
                </Link>
                </p>
              </div>
            </CardContent>
          </Card>
        </motion.div>
      </div>
    </div>
  );
};

export default PatientRegistration;
