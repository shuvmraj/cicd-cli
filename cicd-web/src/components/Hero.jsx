import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Terminal, Clipboard, Check, HelpCircle } from 'lucide-react';
import ReactBitsBg from './ReactBitsBg';
import DotGrid from './DotGrid';

const cmdDemos = {
  detect: {
    cmd: 'cicd detect',
    output: [
      'Scanning workspace descriptors...',
      '✔ Resolved project stack: React (frontend) + Node.js (backend)',
      '✔ Generated configuration models successfully.'
    ]
  },
  validate: {
    cmd: 'cicd validate Jenkinsfile',
    output: [
      'Reading pipeline configuration graph...',
      '✔ Rule audit complete: 0 errors, 1 warning (cache missing)',
      '✔ Status: PASSED (Execution paths resolved)'
    ]
  },
  doctor: {
    cmd: 'cicd doctor',
    output: [
      'Checking toolchain environment...',
      '  - Git: 2.47.0 (PASS)  - Docker: 28.3.2 (PASS)  - Java: 21 (PASS)',
      '✔ System Health Status: 100% operational'
    ]
  }
};

export default function Hero({ theme }) {
  const [installOs, setInstallOs] = useState('mac');
  const [copied, setCopied] = useState(false);
  const [activeMode, setActiveMode] = useState('install'); // 'install' or 'demo'
  const [activeDemoCmd, setActiveDemoCmd] = useState('detect');
  const [typedLines, setTypedLines] = useState([]);

  const getInstallCmd = () => {
    if (installOs === 'mac') {
      return 'curl -sSL https://raw.githubusercontent.com/shuvmraj/cicd-cli/main/install.sh | bash';
    }
    return 'irm https://raw.githubusercontent.com/shuvmraj/cicd-cli/main/install.ps1 | iex';
  };

  const handleCopy = () => {
    navigator.clipboard.writeText(getInstallCmd());
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  // Run typing effect for demo outputs
  useEffect(() => {
    if (activeMode !== 'demo') return;
    setTypedLines([]);
    const lines = cmdDemos[activeDemoCmd].output;
    
    const timers = [];
    // Render cmd line first
    setTypedLines([`$ ${cmdDemos[activeDemoCmd].cmd}`]);

    lines.forEach((line, idx) => {
      const timer = setTimeout(() => {
        setTypedLines(prev => [...prev, line]);
      }, (idx + 1) * 350);
      timers.push(timer);
    });

    return () => timers.forEach(clearTimeout);
  }, [activeDemoCmd, activeMode]);

  return (
    <section className="hero-section center-hero">
      {/* Localized background for Hero section only */}
      <div style={{
        position: 'absolute',
        top: 0,
        left: '50%',
        transform: 'translateX(-50%)',
        width: '100vw',
        height: '100%',
        zIndex: 1,
        pointerEvents: 'none',
        overflow: 'hidden',
        maskImage: 'linear-gradient(to bottom, rgba(0,0,0,1) 60%, rgba(0,0,0,0) 100%)',
        WebkitMaskImage: 'linear-gradient(to bottom, rgba(0,0,0,1) 60%, rgba(0,0,0,0) 100%)',
        opacity: theme === 'dark' ? 0.35 : 0.8,
      }}>
        {theme === 'light' ? (
          <ReactBitsBg theme={theme} />
        ) : (
          <DotGrid
            dotSize={5}
            gap={15}
            baseColor="#2F293A"
            activeColor="#5227FF"
            proximity={120}
            shockRadius={250}
            shockStrength={5}
            resistance={750}
            returnDuration={1.5}
          />
        )}
      </div>

      <div className="hero-content-centered" style={{ position: 'relative', zIndex: 2 }}>
        
        {/* Main Minimal Headline */}
        <motion.h1 
          className="hero-title"
          initial={{ opacity: 0, y: 15 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.1 }}
          style={{ textAlign: 'center', fontFamily: 'var(--font-mono)', margin: '0 auto 24px' }}
        >
          universal ci/cd <span className="hero-title-gradient">validator & generator</span>
        </motion.h1>

        {/* Descriptive subtitle */}
        <motion.p 
          className="hero-subtitle"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.6, delay: 0.2 }}
          style={{ textAlign: 'center', margin: '0 auto 40px', maxWidth: '720px' }}
        >
          An extensible command-line utility built in Java 21 to audit security leaks, resolve topological dependency graphs, and compile optimized platform config workflows.
        </motion.p>

        {/* Minimal Interactive Console Widget */}
        <motion.div 
          className="minimal-console-card"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.3 }}
        >
          {/* Header tabs */}
          <div className="console-tabs-bar">
            <button 
              onClick={() => setActiveMode('install')} 
              className={`console-tab ${activeMode === 'install' ? 'active' : ''}`}
            >
              <Terminal size={14} />
              <span>Install CLI</span>
            </button>
            <button 
              onClick={() => setActiveMode('demo')} 
              className={`console-tab ${activeMode === 'demo' ? 'active' : ''}`}
            >
              <HelpCircle size={14} />
              <span>Interactive Commands</span>
            </button>
          </div>

          <div className="console-body">
            {activeMode === 'install' ? (
              // Installation pane
              <div className="install-pane">
                <div className="pane-control-row">
                  <span className="pane-info-text">Select system script:</span>
                  <div className="pane-chips">
                    <button 
                      onClick={() => setInstallOs('mac')}
                      className={`pane-chip ${installOs === 'mac' ? 'active' : ''}`}
                    >
                      macOS / Linux
                    </button>
                    <button 
                      onClick={() => setInstallOs('windows')}
                      className={`pane-chip ${installOs === 'windows' ? 'active' : ''}`}
                    >
                      Windows (PowerShell)
                    </button>
                  </div>
                </div>

                <div className="console-code-box">
                  <div className="code-text-scroll">
                    <span className="code-text-display">{getInstallCmd()}</span>
                  </div>
                  <button 
                    onClick={handleCopy} 
                    className="console-copy-btn"
                    title="Copy command"
                  >
                    {copied ? <Check size={14} style={{ color: 'var(--color-green)' }} /> : <Clipboard size={14} />}
                  </button>
                </div>
              </div>
            ) : (
              // Interactive command execution demo pane
              <div className="demo-pane">
                <div className="pane-control-row">
                  <span className="pane-info-text">Choose execution demo:</span>
                  <div className="pane-chips">
                    {['detect', 'validate', 'doctor'].map((cmdKey) => (
                      <button 
                        key={cmdKey}
                        onClick={() => setActiveDemoCmd(cmdKey)}
                        className={`pane-chip ${activeDemoCmd === cmdKey ? 'active' : ''}`}
                      >
                        {cmdKey}
                      </button>
                    ))}
                  </div>
                </div>

                <div className="console-output-box">
                  <div className="output-lines">
                    <AnimatePresence>
                      {typedLines.map((line, idx) => (
                        <motion.div 
                          key={idx}
                          initial={{ opacity: 0, x: -4 }}
                          animate={{ opacity: 1, x: 0 }}
                          transition={{ duration: 0.15 }}
                          className={`output-line-item ${idx === 0 ? 'command-prompt-line' : ''}`}
                        >
                          {line}
                        </motion.div>
                      ))}
                    </AnimatePresence>
                  </div>
                </div>
              </div>
            )}
          </div>

        </motion.div>
      </div>
    </section>
  );
}
