import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronRight } from 'lucide-react';

const colors = {
  green: (text) => <span style={{ color: 'var(--color-green)' }}>{text}</span>,
  cyan: (text) => <span style={{ color: 'var(--color-cyan)' }}>{text}</span>,
  yellow: (text) => <span style={{ color: 'var(--color-orange)' }}>{text}</span>,
  red: (text) => <span style={{ color: 'var(--color-red)' }}>{text}</span>,
  bold: (text) => <span style={{ fontWeight: 700, color: '#fff' }}>{text}</span>,
};

const terminalOutputs = {
  init: [
    { text: "$ cicd init --help", delay: 50 },
    { text: `${colors.yellow("Usage:")}        cicd init [OPTIONS]`, delay: 150 },
    { text: `${colors.yellow("Description:")}  Initializes the CLI config template.`, delay: 250 },
    { text: `${colors.yellow("Options:")}      -d, --dir=<targetDir> : Config folder location`, delay: 350 },
    { text: "", delay: 450 },
    { text: "Initializing cicd configuration in current directory...", delay: 650 },
    { text: colors.green("✔ Created config file at: /Users/username/project/.cicd-config.json"), delay: 950 },
    { text: "", delay: 1100 },
    { text: colors.bold("[Config Details]"), delay: 1200 },
    { text: "  projectName:        my-awesome-app", delay: 1300 },
    { text: "  version:            1.0.0", delay: 1400 },
    { text: "  customBuildCommand: null", delay: 1500 },
    { text: "  customTestCommand:  null", delay: 1600 },
  ],
  detect: [
    { text: "$ cicd detect --help", delay: 50 },
    { text: `${colors.yellow("Usage:")}        cicd detect [OPTIONS]`, delay: 150 },
    { text: `${colors.yellow("Description:")}  Scans codebase to resolve stack blueprints.`, delay: 250 },
    { text: `${colors.yellow("Options:")}      -d, --dir=<targetDir> : Path to execute scan`, delay: 350 },
    { text: "", delay: 450 },
    { text: "Scanning project files...", delay: 650 },
    { text: "Resolving build descriptors and source patterns...", delay: 950 },
    { text: "", delay: 1100 },
    { text: `${colors.cyan("Framework:")}          React`, delay: 1200 },
    { text: `${colors.cyan("Language:")}           JavaScript`, delay: 1300 },
    { text: `${colors.cyan("Build Tool:")}         npm`, delay: 1400 },
    { text: `${colors.cyan("Testing:")}            Jest`, delay: 1500 },
    { text: `${colors.cyan("Docker:")}             ${colors.green("Found (Dockerfile)")}`, delay: 1600 },
    { text: `${colors.cyan("Deployment Target:")}  Docker Container`, delay: 1700 },
    { text: `${colors.cyan("CI Platform:")}        GitHub Actions`, delay: 1800 },
    { text: "", delay: 1950 },
    { text: colors.green("Done."), delay: 2050 },
  ],
  generate: [
    { text: "$ cicd generate github -w --help", delay: 50 },
    { text: `${colors.yellow("Usage:")}        cicd generate <platform> [OPTIONS]`, delay: 150 },
    { text: `${colors.yellow("Description:")}  Compiles Mustache workflow templates.`, delay: 250 },
    { text: `${colors.yellow("Options:")}      -w, --write : Save | -o, --output=<path> : Custom path`, delay: 350 },
    { text: "", delay: 450 },
    { text: "Analyzing codebase stack model...", delay: 650 },
    { text: colors.green("✔ Resolved platform generator strategy: GitHubActions"), delay: 950 },
    { text: colors.green("✔ Compiled resources/templates/github/actions-template.yml"), delay: 1250 },
    { text: colors.green("✔ Injected project properties & caching scopes"), delay: 1550 },
    { text: "", delay: 1700 },
    { text: colors.bold("Success: Pipeline generated and written to:"), delay: 1800 },
    { text: colors.cyan("  .github/workflows/main.yml"), delay: 1900 },
  ],
  validate: [
    { text: "$ cicd validate --help", delay: 50 },
    { text: `${colors.yellow("Usage:")}        cicd validate [OPTIONS]`, delay: 150 },
    { text: `${colors.yellow("Description:")}  Audits yaml configurations for cyclical loops & leaks.`, delay: 250 },
    { text: `${colors.yellow("Options:")}      -f, --file=<path> : Target file | -d : Root directory`, delay: 350 },
    { text: "", delay: 450 },
    { text: "Reading pipeline configuration...", delay: 650 },
    { text: "Loaded Rule Engine (7 checks active)", delay: 950 },
    { text: "Running topological graph analysis for job dependencies...", delay: 1250 },
    { text: "", delay: 1400 },
    { text: colors.bold("Validation Report"), delay: 1500 },
    { text: `${colors.yellow("WARNING")} - Cache path not configured.`, delay: 1650 },
    { text: `${colors.green("INFO")}    - Docker build steps detected.`, delay: 1750 },
    { text: "", delay: 1850 },
    { text: "Status: PASSED", delay: 1950 },
  ],
  explain: [
    { text: "$ cicd explain --help", delay: 50 },
    { text: `${colors.yellow("Usage:")}        cicd explain [OPTIONS]`, delay: 150 },
    { text: `${colors.yellow("Description:")}  Depicts execution paths and stages.`, delay: 250 },
    { text: `${colors.yellow("Options:")}      -f, --file=<path> : Target workflow config path`, delay: 350 },
    { text: "", delay: 450 },
    { text: "Parsing pipeline stages graph...", delay: 650 },
    { text: "", delay: 800 },
    { text: colors.bold("Pipeline Flow:"), delay: 900 },
    { text: "  install-dependencies -> test -> docker-build-push -> deploy-kubernetes", delay: 1100 },
    { text: "", delay: 1250 },
    { text: `${colors.cyan("Estimated Runtime:")} 6 min`, delay: 1350 },
    { text: `${colors.cyan("Parallel Jobs:")}     1`, delay: 1450 },
  ],
  convert: [
    { text: "$ cicd convert --help", delay: 50 },
    { text: `${colors.yellow("Usage:")}        cicd convert <source> <target> [OPTIONS]`, delay: 150 },
    { text: `${colors.yellow("Description:")}  Translates pipeline yaml between platforms.`, delay: 250 },
    { text: `${colors.yellow("Options:")}      -f, --file=<path> : Source yaml | -o : Export path`, delay: 350 },
    { text: "", delay: 450 },
    { text: "Parsing GitHub Actions YAML workflow...", delay: 650 },
    { text: "Extracting stage script commands...", delay: 950 },
    { text: "Translating to GitLab CI variables and job format...", delay: 1250 },
    { text: "", delay: 1400 },
    { text: colors.green("Success: Translated config printed to console."), delay: 1500 },
  ],
  doctor: [
    { text: "$ cicd doctor --help", delay: 50 },
    { text: `${colors.yellow("Usage:")}        cicd doctor`, delay: 150 },
    { text: `${colors.yellow("Description:")}  Audits local system toolchain health.`, delay: 250 },
    { text: "", delay: 350 },
    { text: "Checking system toolchains and dependencies...", delay: 550 },
    { text: "Git        git version 2.47.0              PASS", delay: 750 },
    { text: "Docker     Docker version 28.3.2           PASS", delay: 900 },
    { text: "Java       java 23.0.2                     PASS", delay: 1050 },
    { text: "Node       v24.5.0                         PASS", delay: 1200 },
    { text: "", delay: 1350 },
    { text: colors.green("System Health: 100% (All critical tools available)"), delay: 1450 },
  ]
};

export default function TerminalPlayground() {
  const [selectedCommand, setSelectedCommand] = useState('detect');
  const [typedLines, setTypedLines] = useState([]);
  
  // Animation phases: 'closed' -> 'revolving' -> 'opening' -> 'ready'
  const [animPhase, setAnimPhase] = useState('closed');

  // Trigger Apple MacBook intro sequence on mount
  useEffect(() => {
    const revTimer = setTimeout(() => {
      setAnimPhase('revolving');
    }, 400);

    const openTimer = setTimeout(() => {
      setAnimPhase('opening');
    }, 1600);

    const readyTimer = setTimeout(() => {
      setAnimPhase('ready');
    }, 2800);

    return () => {
      clearTimeout(revTimer);
      clearTimeout(openTimer);
      clearTimeout(readyTimer);
    };
  }, []);

  // Types terminal command output only after laptop lid is open ('ready')
  useEffect(() => {
    if (animPhase !== 'ready') return;

    setTypedLines([]);
    const lines = terminalOutputs[selectedCommand];
    
    const timers = [];
    lines.forEach((line) => {
      const timer = setTimeout(() => {
        setTypedLines(prev => [...prev, line.text]);
      }, line.delay);
      timers.push(timer);
    });

    return () => timers.forEach(clearTimeout);
  }, [selectedCommand, animPhase]);

  const handleCommandChange = (cmdId) => {
    setSelectedCommand(cmdId);
  };

  const getDeviceStyle = () => {
    if (animPhase === 'closed') {
      return { transform: 'rotateY(180deg) rotateX(12deg)' };
    }
    if (animPhase === 'revolving') {
      return { transform: 'rotateY(0deg) rotateX(12deg)' };
    }
    return { transform: 'rotateY(0deg) rotateX(0deg)' };
  };

  const isLidOpen = animPhase === 'opening' || animPhase === 'ready';

  return (
    <section id="terminal" className="terminal-section">
      <h2 className="section-title" style={{ marginBottom: '48px' }}>Command Reference Console</h2>
      
      <div className="terminal-playground-layout">
        {/* Sidebar Selector (Column 1) */}
        <div className="terminal-sidebar">
          {[
            { id: 'init', name: 'cicd init', desc: 'Initialize configuration' },
            { id: 'detect', name: 'cicd detect', desc: 'Auto-scan codebases' },
            { id: 'generate', name: 'cicd generate', desc: 'Compile pipeline configs' },
            { id: 'validate', name: 'cicd validate', desc: 'Audit security & graphs' },
            { id: 'explain', name: 'cicd explain', desc: 'Render workflow cycles' },
            { id: 'convert', name: 'cicd convert', desc: 'Translate specifications' },
            { id: 'doctor', name: 'cicd doctor', desc: 'Check local binaries' },
          ].map((cmd) => (
            <button
              key={cmd.id}
              onClick={() => handleCommandChange(cmd.id)}
              disabled={animPhase !== 'ready'}
              className={`cmd-btn ${selectedCommand === cmd.id ? 'active' : ''}`}
              style={{ opacity: animPhase !== 'ready' ? 0.6 : 1 }}
            >
              <div>
                <h4 className="cmd-btn-name">{cmd.name}</h4>
                <p className="cmd-btn-desc">{cmd.desc}</p>
              </div>
              <ChevronRight size={16} />
            </button>
          ))}
        </div>

        {/* 3D CSS MacBook Terminal Container (Column 2) - Expanded to Max Width */}
        <div className="macbook-wrapper">
          <div className="macbook-device" style={getDeviceStyle()}>
            {/* Display screen lid */}
            <div className={`macbook-lid ${isLidOpen ? 'open' : ''}`}>
              
              {/* Outer Lid panel (Apple outline logo) - visible when screen is closed */}
              {!isLidOpen && (
                <div className="macbook-lid-back">
                  <svg 
                    viewBox="0 0 170 170" 
                    className="apple-logo-svg"
                  >
                    <path d="M150.37 130.25c-2.45 5.66-5.35 10.87-8.71 15.66-4.58 6.53-8.33 11.05-11.22 13.56-4.48 4.12-9.28 6.23-14.42 6.35-3.69 0-8.14-1.05-13.32-3.18-5.19-2.12-9.97-3.17-14.34-3.17-4.58 0-9.49 1.05-14.75 3.17-5.26 2.13-9.5 3.24-12.74 3.35-4.39.13-9.13-1.92-14.21-6.14-3.1-2.61-6.95-7.23-11.56-13.86-5.07-7.24-9.28-15.86-12.62-25.86-3.35-10.02-5.03-19.88-5.03-29.59 0-14.83 3.82-26.62 11.47-35.38 7.64-8.75 17.13-13.19 28.46-13.31 5.92.12 11.72 1.83 17.41 5.16 5.69 3.33 10.05 5 13.08 5 2.5 0 6.64-1.54 12.44-4.63 7.07-3.76 13.79-5.54 20.17-5.35 15.17.62 26.68 6.44 34.52 17.47-13.19 8.01-19.68 18.99-19.46 32.96.22 10.66 4.16 19.5 11.84 26.54 7.67 7.03 16.7 10.74 27.09 11.13-2.11 6.09-4.8 12.18-8.07 18.25zm-22.37-97.77c0-7.39 2.65-14.4 7.96-21.03 6.68-8.13 15-12.56 24.96-13.28.1 1 .15 1.86.15 2.58 0 7.15-2.73 13.89-8.17 20.24-3.33 3.89-7.39 6.89-12.21 9.01-4.81 2.12-9.29 3.22-13.43 3.28-.79-.8-1.26-6.19-1.26-10.8z"/>
                  </svg>
                </div>
              )}

              {/* Inner Display (Terminal output screen) */}
              {isLidOpen && (
                <div className="macbook-screen">
                  <div className="terminal-window-body" style={{ height: '100%', border: 'none', background: 'transparent', padding: '12px' }}>
                    <AnimatePresence mode="popLayout">
                      {typedLines.map((line, idx) => (
                        <motion.div 
                          key={idx}
                          initial={{ opacity: 0, x: -3 }}
                          animate={{ opacity: 1, x: 0 }}
                          transition={{ duration: 0.1 }}
                          style={{ minHeight: '18px', fontSize: '11px' }}
                        >
                          {line}
                        </motion.div>
                      ))}
                    </AnimatePresence>
                    <div className="cursor" style={{ height: '12px' }} />
                  </div>
                </div>
              )}
              
              <div className="macbook-logo">macbook</div>
            </div>

            {/* Laptop lower base keyboard plate */}
            <div className="macbook-base">
              <div className="macbook-notch" />
              <div className="macbook-trackpad" />
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
