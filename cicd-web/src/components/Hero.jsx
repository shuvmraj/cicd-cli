import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { Terminal, Clipboard, Check } from 'lucide-react';

export default function Hero() {
  const [installOs, setInstallOs] = useState('mac');
  const [copied, setCopied] = useState(false);

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

  return (
    <section className="hero-section">
      <motion.div
        initial={{ opacity: 0, y: 15 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <span className="badge-cyan">Production-Ready DevOps tool</span>
      </motion.div>
      
      <motion.h1 
        className="hero-title"
        initial={{ opacity: 0, y: 15 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.1 }}
      >
        Universal CI/CD Validator <br />
        <span className="hero-title-gradient">and Generator CLI</span>
      </motion.h1>

      <motion.p 
        className="hero-subtitle"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.6, delay: 0.2 }}
      >
        Automatically analyze codebase tech stacks, discover circular job dependency graphs, flag hardcoded secrets, and compile optimized Docker & pipeline files.
      </motion.p>

      {/* Global Installer Widget */}
      <motion.div 
        className="installer-widget"
        initial={{ opacity: 0, y: 25 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, delay: 0.3 }}
      >
        <div className="widget-header">
          <span className="widget-title">
            <Terminal size={16} style={{ color: 'var(--color-cyan)' }} />
            Global Install (Mac, Linux & Windows)
          </span>
          <div className="widget-tab-group">
            <button 
              onClick={() => setInstallOs('mac')}
              className={`widget-tab-btn ${installOs === 'mac' ? 'active' : ''}`}
            >
              macOS / Linux
            </button>
            <button 
              onClick={() => setInstallOs('windows')}
              className={`widget-tab-btn ${installOs === 'windows' ? 'active' : ''}`}
            >
              Windows (PowerShell)
            </button>
          </div>
        </div>

        <div className="code-box">
          <span className="code-text">{getInstallCmd()}</span>
          <button 
            onClick={handleCopy}
            className="icon-btn"
            title="Copy to clipboard"
          >
            {copied ? <Check size={16} style={{ color: 'var(--color-green)' }} /> : <Clipboard size={16} />}
          </button>
        </div>

        <div className="widget-footer">
          <span>✔ No manual folder sharing</span>
          <span>✔ Automatic environment variables config</span>
        </div>
      </motion.div>
    </section>
  );
}
