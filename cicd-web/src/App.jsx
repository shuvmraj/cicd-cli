import React, { useState, useEffect } from 'react';
import Lenis from 'lenis';

// Modular Components
import ReactBitsBg from './components/ReactBitsBg';
import DotGrid from './components/DotGrid';
import Header from './components/Header';
import Hero from './components/Hero';
import Features from './components/Features';
import TerminalPlayground from './components/TerminalPlayground';
import StackExplorer from './components/StackExplorer';
import RuleSandbox from './components/RuleSandbox';
import Footer from './components/Footer';

export default function App() {
  const [theme, setTheme] = useState('light'); // 'light' is default as requested

  // Initialize Lenis Smooth Scroll
  useEffect(() => {
    const lenis = new Lenis({
      duration: 1.2,
      easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
      smoothWheel: true,
    });

    function raf(time) {
      lenis.raf(time);
      requestAnimationFrame(raf);
    }

    requestAnimationFrame(raf);

    return () => {
      lenis.destroy();
    };
  }, []);

  const toggleTheme = () => {
    setTheme(prev => (prev === 'dark' ? 'light' : 'dark'));
  };

  return (
    <div className={`app-container ${theme === 'dark' ? 'dark-theme' : ''}`}>
      {/* Glow ambient blobs */}
      <div className="bg-glow-purple" />
      <div className="bg-glow-cyan" />

      {/* Header (contains developer profile link & theme toggler) */}
      <Header theme={theme} toggleTheme={toggleTheme} />

      {/* Hero section with Headline and CLI Download widget */}
      <Hero theme={theme} />

      {/* Tech features detail display */}
      <Features theme={theme} />

      {/* Command playground terminal simulator */}
      <TerminalPlayground />

      {/* Codebase Stack Explorer templates */}
      <StackExplorer theme={theme} />

      {/* Rule engine live audit sandbox */}
      <RuleSandbox />

      {/* Footer details */}
      <Footer />
    </div>
  );
}
