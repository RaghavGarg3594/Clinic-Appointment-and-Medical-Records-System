import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { motion } from 'framer-motion';
import { CalendarHeart, ShieldCheck, Stethoscope, Network } from 'lucide-react';

const DoctorJoinRequest = () => {
  const [formData, setFormData] = useState({
    firstName: '', lastName: '', email: '', phone: '',
    specialization: '', qualification: '', licenseNumber: '',
    experienceYears: '', message: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
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
    if (formData.phone && !/^\d{10}$/.test(formData.phone.replace(/[\s+\-]/g, ''))) {
      setError('Phone number must be exactly 10 digits');
      return;
    }

    setLoading(true);
    try {
      await api.post('/auth/doctor-request', {
        ...formData,
        experienceYears: formData.experienceYears ? parseInt(formData.experienceYears) : null
      });
      setSuccess(true);
    } catch (err) {
      setError(err.response?.data?.message || 'Submission failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const features = [
    {
      title: "Streamlined Scheduling",
      description: "Manage your availability natively. No double-bookings.",
      icon: <CalendarHeart className="w-5 h-5 text-terracotta" />
    },
    {
      title: "Digital Consultations",
      description: "Document diagnoses and instantly write secure digital prescriptions.",
      icon: <Stethoscope className="w-5 h-5 text-rustic" />
    },
    {
      title: "Integrated Labs",
      description: "Prescribe tests directly to the lab portal and view results seamlessly.",
      icon: <Network className="w-5 h-5 text-oxford" />
    },
    {
      title: "Secure Network",
      description: "Role-based access controls protecting you and your patients.",
      icon: <ShieldCheck className="w-5 h-5 text-terracotta" />
    }
  ];

  if (success) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background p-6 overflow-x-hidden">
        <motion.div
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.4 }}
          className="w-full max-w-[440px]"
        >
          <Card className="shadow-2xl text-center border-border">
            <CardContent className="pt-8 pb-8">
              <div className="text-5xl mb-4">✅</div>
              <h2 className="text-xl font-bold text-terracotta mb-2">Request Submitted!</h2>
              <p className="text-muted-foreground mb-6">
                Thank you for your interest in joining CAMRS. Our admin team will review your details
                and reach out to you at <strong>{formData.email}</strong> shortly to complete onboarding!
              </p>
              <Link to="/login">
                <Button className="w-full bg-oxford hover:bg-oxford/90">Back to Login</Button>
              </Link>
            </CardContent>
          </Card>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="min-h-screen w-full flex flex-col lg:flex-row bg-background overflow-x-hidden">
      
      {/* LEFT SECTION: Doctor Benefits */}
      <div className="relative flex-1 lg:flex-[1.2] hidden md:flex flex-col justify-center px-12 lg:px-24 py-16 bg-gradient-to-br from-oxford via-oxford to-[#3A4556] text-white overflow-hidden">
        
        <div className="absolute top-[-10%] left-[-10%] w-[600px] h-[600px] bg-terracotta/15 rounded-full blur-[120px] pointer-events-none" />
        <div className="absolute bottom-[-10%] right-[-10%] w-[500px] h-[500px] bg-rustic/15 rounded-full blur-[100px] pointer-events-none" />
        
        <div className="relative z-10 w-full max-w-2xl mx-auto space-y-12">
          
          <motion.div initial={{ opacity: 0, y: 30 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6 }}>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 border border-white/20 mb-6 backdrop-blur-md">
              <Stethoscope className="w-4 h-4 text-peach" />
              <span className="text-sm font-medium tracking-wide text-peach">Provider Network</span>
            </div>
            
            <h1 className="text-4xl lg:text-5xl font-extrabold tracking-tight mb-4 leading-tight">
              Modernize Your <br/>
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-peach to-[#D4946A]">
                Medical Practice.
              </span>
            </h1>
            
            <p className="text-lg text-white/60 max-w-xl leading-relaxed">
              Join the CAMRS provider network to digitize your consultations, effortlessly manage your patient queue, and simplify your billing and prescription workflows.
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

      {/* RIGHT SECTION: Join Request Form */}
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
            <p className="text-muted-foreground mt-2">Submit a provider request</p>
          </div>

          <Card className="shadow-2xl border-border bg-card">
            <CardHeader className="text-center pb-2">
              <CardTitle className="text-2xl font-bold tracking-tight">Doctor Join Request</CardTitle>
              <CardDescription>Fill in your credentials to apply to the CAMRS network.</CardDescription>
            </CardHeader>
            <CardContent className="pt-4">
              {error && (
                <Alert variant="destructive" className="mb-4">
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              )}

              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div className="space-y-2">
                    <Label className="font-semibold">First Name *</Label>
                    <Input name="firstName" value={formData.firstName} onChange={handleChange} required placeholder="John" className="bg-muted/40"/>
                  </div>
                  <div className="space-y-2">
                    <Label className="font-semibold">Last Name *</Label>
                    <Input name="lastName" value={formData.lastName} onChange={handleChange} required placeholder="Smith" className="bg-muted/40"/>
                  </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div className="space-y-2">
                    <Label className="font-semibold">Email Address *</Label>
                    <Input type="email" name="email" value={formData.email} onChange={handleChange} required placeholder="doctor@example.com" className="bg-muted/40"/>
                  </div>
                  <div className="space-y-2">
                    <Label className="font-semibold">Phone Number</Label>
                    <Input name="phone" value={formData.phone} onChange={handleChange} placeholder="+91 98765 43210" className="bg-muted/40"/>
                  </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div className="space-y-2">
                    <Label className="font-semibold">Specialization *</Label>
                    <Input name="specialization" value={formData.specialization} onChange={handleChange} required placeholder="Cardiology" className="bg-muted/40"/>
                  </div>
                  <div className="space-y-2">
                    <Label className="font-semibold">Qualification *</Label>
                    <Input name="qualification" value={formData.qualification} onChange={handleChange} required placeholder="MBBS, MD" className="bg-muted/40"/>
                  </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div className="space-y-2">
                    <Label className="font-semibold">License Number *</Label>
                    <Input name="licenseNumber" value={formData.licenseNumber} onChange={handleChange} required placeholder="MED-123456" className="bg-muted/40"/>
                  </div>
                  <div className="space-y-2">
                    <Label className="font-semibold">Years of Exp.</Label>
                    <Input type="number" name="experienceYears" value={formData.experienceYears} onChange={handleChange} placeholder="5" min="0" max="60" className="bg-muted/40"/>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label className="font-semibold">Cover Note</Label>
                  <Textarea
                    name="message"
                    value={formData.message}
                    onChange={handleChange}
                    rows={3}
                    placeholder="Briefly describe your background..."
                    className="bg-muted/40 resize-none"
                  />
                </div>

                <Button type="submit" className="w-full h-11 text-base shadow-md mt-4 bg-oxford hover:bg-oxford/90" disabled={loading}>
                  {loading ? 'Submitting...' : 'Submit Request'}
                </Button>
              </form>

              <div className="mt-8 pt-6 border-t border-border flex justify-center text-sm text-muted-foreground">
                <p>Not rendering services?{' '}
                <Link to="/login" className="text-terracotta font-bold hover:underline transition">
                  Back to Login
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

export default DoctorJoinRequest;
