import React, { useState } from 'react';
import { useNavigate, Link, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { motion } from 'framer-motion';
import { Activity, CalendarHeart, ClipboardList, Beaker, ShieldCheck, ArrowRight } from 'lucide-react';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Forgot password state
  const [forgotOpen, setForgotOpen] = useState(false);
  const [fpFirstName, setFpFirstName] = useState('');
  const [fpLastName, setFpLastName] = useState('');
  const [fpDob, setFpDob] = useState('');
  const [fpNewPassword, setFpNewPassword] = useState('');
  const [fpConfirmPassword, setFpConfirmPassword] = useState('');
  const [fpMessage, setFpMessage] = useState('');
  const [fpError, setFpError] = useState('');
  const [fpLoading, setFpLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const justRegistered = searchParams.get('registered') === 'true';

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const response = await api.post('/auth/login', { email, password });
      const userData = response.data;
      login(userData);
      switch (userData.role) {
        case 'DOCTOR': navigate('/doctor/dashboard'); break;
        case 'ADMIN_STAFF': navigate('/admin/dashboard'); break;
        case 'LAB_STAFF': navigate('/lab/dashboard'); break;
        default: navigate('/patient/dashboard');
      }
    } catch (err) {
      console.error('Login failed:', err);
      const msg = err.response?.data?.message || 'Invalid email or password';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = async (e) => {
    e.preventDefault();
    setFpError(''); setFpMessage(''); setFpLoading(true);
    if (fpNewPassword !== fpConfirmPassword) {
      setFpError('Passwords do not match.');
      setFpLoading(false);
      return;
    }
    if (fpNewPassword.length < 6) {
      setFpError('Password must be at least 6 characters.');
      setFpLoading(false);
      return;
    }
    try {
      const res = await api.post('/auth/forgot-password', {
        firstName: fpFirstName,
        lastName: fpLastName,
        dateOfBirth: fpDob,
        newPassword: fpNewPassword,
      });
      setFpMessage(res.data.message);
    } catch (err) {
      setFpError(err.response?.data?.message || 'Failed to reset password. Please check your details.');
    } finally {
      setFpLoading(false);
    }
  };

  const features = [
    {
      title: "Patient Management",
      description: "Comprehensive data capture including demographics, contact information, and medical history.",
      icon: <Activity className="w-5 h-5 text-terracotta" />
    },
    {
      title: "Appointment Scheduling",
      description: "Manage doctor availability and consultation scheduling with a unified calendar.",
      icon: <CalendarHeart className="w-5 h-5 text-rustic" />
    },
    {
      title: "Medical Records",
      description: "Maintain complete visit history, digital prescriptions, and structured diagnoses.",
      icon: <ClipboardList className="w-5 h-5 text-oxford" />
    },
    {
      title: "Laboratory Services",
      description: "Seamless integration for tracking test orders and publishing secure lab reports.",
      icon: <Beaker className="w-5 h-5 text-terracotta" />
    }
  ];

  return (
    <div className="h-screen w-full flex flex-col lg:flex-row bg-background overflow-hidden relative">
      {/* LEFT SECTION: Marketing Landing Page */}
      <div className="relative flex-1 lg:flex-[1.2] hidden md:flex flex-col px-12 lg:px-24 py-16 bg-gradient-to-br from-oxford via-oxford to-[#1F3A5F] text-white overflow-y-auto overflow-x-hidden">
        
        {/* Abstract Background Elements */}
        <div className="absolute top-[-10%] left-[-10%] w-[600px] h-[600px] bg-terracotta/15 rounded-full blur-[120px] pointer-events-none" />
        <div className="absolute bottom-[-10%] right-[-10%] w-[500px] h-[500px] bg-rustic/15 rounded-full blur-[100px] pointer-events-none" />
        
        <div className="relative z-10 w-full max-w-2xl mx-auto space-y-12">
          
          <motion.div initial={{ opacity: 0, y: 30 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6 }}>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 border border-white/20 mb-6 backdrop-blur-md">
              <ShieldCheck className="w-4 h-4 text-peach" />
              <span className="text-sm font-medium tracking-wide text-peach">CAMRS Platform v1.0</span>
            </div>
            
            <h1 className="text-4xl lg:text-5xl font-extrabold tracking-tight mb-4 leading-tight">
              Transforming Healthcare Operations <br/>
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-peach to-[#D4946A]">
                Through Digital Innovation.
              </span>
            </h1>
            
            <p className="text-lg text-white/60 max-w-xl leading-relaxed">
              Automating clinical operations by streamlining patient registration, appointments, medical records, lab tests, and billing. A unified comprehensive web-based platform tailored for modern healthcare providers.
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

      {/* RIGHT SECTION: Login / Authentication */}
      <div className="flex-1 flex flex-col items-center justify-center p-6 lg:p-12 relative bg-background border-l border-border/50 overflow-y-auto h-full">
        
        {/* Mobile Background */}
        <div className="md:hidden absolute top-[-100px] right-[-100px] w-[300px] h-[300px] bg-terracotta/8 rounded-full blur-[80px]" />
        
        <motion.div
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className="w-full max-w-[420px] relative z-10"
        >
          <div className="mb-8 md:hidden text-center">
            <h2 className="text-3xl font-extrabold tracking-tight text-foreground">CAMRS</h2>
            <p className="text-muted-foreground mt-2">Clinic Appointment & Medical Records</p>
          </div>

          <Card className="shadow-xl border-border bg-card">
            <CardHeader className="pb-4">
              <CardTitle className="text-2xl font-bold tracking-tight">Access Account</CardTitle>
              <CardDescription>Enter your credentials to access the CAMRS portal.</CardDescription>
            </CardHeader>
            <CardContent>
              {justRegistered && (
                <Alert className="mb-6 border-terracotta/30 bg-terracotta/8 text-terracotta">
                  <AlertDescription>Your account has been strictly verified. Please sign in.</AlertDescription>
                </Alert>
              )}
              {error && (
                <Alert variant="destructive" className="mb-6">
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              )}

              <form onSubmit={handleLogin} className="space-y-5">
                <div className="space-y-2">
                  <Label htmlFor="email" className="font-semibold">Email Address</Label>
                  <Input
                    id="email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="patient@camrs.com"
                    className="h-11 bg-muted/40"
                    required
                  />
                </div>
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <Label htmlFor="password" className="font-semibold">Password</Label>
                    <button
                      type="button"
                      onClick={() => { setForgotOpen(true); setFpMessage(''); setFpError(''); }}
                      className="text-xs text-oxford hover:underline font-medium"
                    >
                      Forgot Password?
                    </button>
                  </div>
                  <Input
                    id="password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                    className="h-11 bg-muted/40"
                    required
                  />
                </div>
                
                <Button type="submit" className="w-full h-11 text-base font-semibold shadow-md mt-2 bg-oxford hover:bg-oxford/90" disabled={loading}>
                  {loading ? 'Authenticating...' : 'Sign in'}
                </Button>
              </form>

              <div className="mt-8 space-y-4">
                <div className="relative">
                  <div className="absolute inset-0 flex items-center">
                    <span className="w-full border-t border-border" />
                  </div>
                  <div className="relative flex justify-center text-xs uppercase">
                    <span className="bg-card px-2 text-muted-foreground font-semibold">Portals</span>
                  </div>
                </div>

                <div className="flex flex-col space-y-3 pt-2">
                  <Link 
                    to="/register" 
                    className="flex text-sm items-center justify-center p-3 rounded-lg border border-border bg-muted/30 hover:bg-muted/60 transition duration-200 text-foreground"
                  >
                    Don't have an account? <span className="font-bold text-oxford ml-1">Register here</span>
                  </Link>
                  
                  <Link 
                    to="/doctor-request" 
                    className="flex text-sm items-center justify-between p-3 rounded-lg border border-terracotta/20 bg-terracotta/5 hover:bg-terracotta/10 transition duration-200 text-terracotta group"
                  >
                    <span>Are you a doctor? <span className="font-bold">Submit a join request</span></span>
                    <ArrowRight className="w-4 h-4 ml-2 transition-transform group-hover:translate-x-1" />
                  </Link>
                </div>
              </div>
            </CardContent>
          </Card>
        </motion.div>
      </div>

      {/* Forgot Password Dialog */}
      <Dialog open={forgotOpen} onOpenChange={setForgotOpen}>
        <DialogContent className="sm:max-w-[440px]">
          <DialogHeader>
            <DialogTitle>Reset Password</DialogTitle>
          </DialogHeader>
          <p className="text-sm text-muted-foreground mb-4">
            Enter your name and date of birth to reset your password.
          </p>

          {fpMessage && (
            <Alert className="mb-4 border-primary/30 bg-primary/10 text-primary-foreground">
              <AlertDescription>{fpMessage}</AlertDescription>
            </Alert>
          )}
          {fpError && (
            <Alert variant="destructive" className="mb-4">
              <AlertDescription>{fpError}</AlertDescription>
            </Alert>
          )}

          <form onSubmit={handleForgotPassword} className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label>First Name</Label>
                <Input value={fpFirstName} onChange={e => setFpFirstName(e.target.value)} placeholder="Rahul" required />
              </div>
              <div className="space-y-2">
                <Label>Last Name</Label>
                <Input value={fpLastName} onChange={e => setFpLastName(e.target.value)} placeholder="Kumar" required />
              </div>
            </div>
            <div className="space-y-2">
              <Label>Date of Birth</Label>
              <Input type="date" value={fpDob} onChange={e => setFpDob(e.target.value)} required />
            </div>
            <div className="space-y-2">
              <Label>New Password</Label>
              <Input type="password" value={fpNewPassword} onChange={e => setFpNewPassword(e.target.value)} placeholder="Min. 6 characters" required />
            </div>
            <div className="space-y-2">
              <Label>Confirm Password</Label>
              <Input type="password" value={fpConfirmPassword} onChange={e => setFpConfirmPassword(e.target.value)} placeholder="Re-enter password" required />
            </div>
            <div className="flex gap-2 pt-2">
              <Button type="submit" className="flex-1 bg-oxford hover:bg-oxford/90" disabled={fpLoading}>
                {fpLoading ? 'Resetting...' : 'Reset Password'}
              </Button>
              <Button type="button" variant="outline" onClick={() => setForgotOpen(false)}>Cancel</Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default Login;
