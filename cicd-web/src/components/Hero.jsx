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
      <div className="hero-grid">
        {/* Left Column: Headline and Installer Widget */}
        <div className="hero-text-block">
          <motion.div
            initial={{ opacity: 0, y: 15 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="text-left-mobile-center"
          >
            <span className="badge-cyan">cli command-line engine</span>
          </motion.div>
          
          <motion.h1 
            className="hero-title"
            initial={{ opacity: 0, y: 15 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            style={{ textAlign: 'left', fontFamily: 'var(--font-mono)' }}
          >
            universal ci/cd validator & generator
          </motion.h1>

          <motion.p 
            className="hero-subtitle"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.6, delay: 0.2 }}
            style={{ textAlign: 'left' }}
          >
            An extensible command-line utility built in Java 21 to audit security leaks, resolve topological dependency graphs, and compile optimized platform config workflows.
          </motion.p>

          {/* Installer Widget */}
          <motion.div 
            className="installer-widget"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.3 }}
          >
            <div className="widget-header">
              <span className="widget-title" style={{ fontFamily: 'var(--font-mono)' }}>
                <Terminal size={16} />
                $ env install --global
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

            {/* Changed inline style color to #f8fafc (high contrast white) so text is visible on the dark background */}
            <div className="code-box">
              <span className="code-text" style={{ color: '#f8fafc', fontFamily: 'var(--font-mono)' }}>{getInstallCmd()}</span>
              <button 
                onClick={handleCopy}
                className="icon-btn"
                title="Copy to clipboard"
              >
                {copied ? <Check size={16} style={{ color: 'var(--color-green)' }} /> : <Clipboard size={16} />}
              </button>
            </div>

            <div className="widget-footer" style={{ fontFamily: 'var(--font-mono)' }}>
              <span># no folder sharing</span>
              <span># dynamic path resolution</span>
            </div>
          </motion.div>
        </div>

        {/* Right Column: Cartoon Developer Illustration */}
        <motion.div 
          className="hero-image-block"
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.8, delay: 0.2 }}
        >
          <div className="image-card-container">
            <img 
              src="/cartoon_dev.png" 
              alt="Developer building and deploying container applications" 
              className="tech-illustration"
            />
          </div>
        </motion.div>
      </div>
    </section>
  );
}
