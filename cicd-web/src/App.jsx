import React, { useState, useEffect } from 'react';
import Lenis from 'lenis';

// Modular Components
import ReactBitsBg from './components/ReactBitsBg';
import Prism from './components/Prism';
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
      {/* Background switcher: Grid on light, animated Prism shader on dark */}
      {theme === 'light' ? (
        <ReactBitsBg theme={theme} />
      ) : (
        <div style={{ position: 'fixed', inset: 0, width: '100%', height: '100%', zIndex: 0, pointerEvents: 'none', overflow: 'hidden' }}>
          <Prism
            animationType="rotate"
            timeScale={0.5}
            height={3.5}
            baseWidth={5.5}
            scale={3.6}
            hueShift={0}
            colorFrequency={1}
            noise={0}
            glow={1}
          />
        </div>
      )}

      {/* Glow ambient blobs */}
      <div className="bg-glow-purple" />
      <div className="bg-glow-cyan" />

      {/* Header (contains developer profile link & theme toggler) */}
      <Header theme={theme} toggleTheme={toggleTheme} />

      {/* Hero section with Headline and CLI Download widget */}
      <Hero />

      {/* Tech features detail display */}
      <Features />

      {/* Command playground terminal simulator */}
      <TerminalPlayground />

      {/* Codebase Stack Explorer templates */}
      <StackExplorer />

      {/* Rule engine live audit sandbox */}
      <RuleSandbox />

      {/* Footer details */}
      <Footer />
    </div>
  );
}
