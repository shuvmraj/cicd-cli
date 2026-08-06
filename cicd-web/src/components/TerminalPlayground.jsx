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
    { text: "$ cicd init", delay: 100 },
    { text: "Initializing cicd configuration in current directory...", delay: 400 },
    { text: colors.green("✔ Created config file at: /Users/username/project/.cicd-config.json"), delay: 700 },
    { text: "", delay: 850 },
    { text: colors.bold("[Config Details]"), delay: 900 },
    { text: "  projectName:        my-awesome-app", delay: 1000 },
    { text: "  version:            1.0.0", delay: 1100 },
    { text: "  customBuildCommand: null", delay: 1200 },
    { text: "  customTestCommand:  null", delay: 1300 },
  ],
  detect: [
    { text: "$ cicd detect", delay: 100 },
    { text: "Scanning project files...", delay: 300 },
    { text: "Resolving build descriptors and source patterns...", delay: 600 },
    { text: "", delay: 750 },
    { text: `${colors.cyan("Framework:")}          React`, delay: 850 },
    { text: `${colors.cyan("Language:")}           JavaScript`, delay: 950 },
    { text: `${colors.cyan("Build Tool:")}         npm`, delay: 1050 },
    { text: `${colors.cyan("Testing:")}            Jest`, delay: 1150 },
    { text: `${colors.cyan("Docker:")}             ${colors.green("Found (Dockerfile)")}`, delay: 1250 },
    { text: `${colors.cyan("Kubernetes:")}         ${colors.yellow("Not Found")}`, delay: 1350 },
    { text: `${colors.cyan("Deployment Target:")}  Docker Container`, delay: 1450 },
    { text: `${colors.cyan("CI Platform:")}        GitHub Actions (recommended)`, delay: 1550 },
    { text: "", delay: 1650 },
    { text: colors.green("Done."), delay: 1750 },
  ],
  generate: [
    { text: "$ cicd generate github -w", delay: 100 },
    { text: "Analyzing codebase stack model...", delay: 300 },
    { text: colors.green("✔ Resolved platform generator strategy: GitHubActions"), delay: 600 },
    { text: colors.green("✔ Compiled resources/templates/github/actions-template.yml"), delay: 900 },
    { text: colors.green("✔ Injected project properties & caching scopes"), delay: 1200 },
    { text: "", delay: 1350 },
    { text: colors.bold("Success: Pipeline generated and written to:"), delay: 1450 },
    { text: colors.cyan("  .github/workflows/main.yml"), delay: 1550 },
  ],
  validate: [
    { text: "$ cicd validate -f .gitlab-ci.yml", delay: 100 },
    { text: "Reading pipeline configuration...", delay: 300 },
    { text: "Loaded Rule Engine (7 checks active)", delay: 600 },
    { text: "Running topological graph analysis for job dependencies...", delay: 900 },
    { text: "", delay: 1100 },
    { text: colors.bold("Validation Report"), delay: 1200 },
    { text: "", delay: 1250 },
    { text: `${colors.yellow("WARNING")} - Cache path not configured. Adding dependency caching speeds up build durations.`, delay: 1400 },
    { text: `${colors.green("INFO")}    - Docker build steps detected.`, delay: 1550 },
    { text: "", delay: 1650 },
    { text: colors.bold("Overall Status"), delay: 1750 },
    { text: "  PASSED", delay: 1850 },
  ],
  explain: [
    { text: "$ cicd explain", delay: 100 },
    { text: "Parsing pipeline stages graph...", delay: 300 },
    { text: "", delay: 500 },
    { text: colors.bold("Pipeline Flow:"), delay: 600 },
    { text: "  install-dependencies", delay: 750 },
    { text: "    ↓", delay: 800 },
    { text: "  test", delay: 900 },
    { text: "    ↓", delay: 950 },
    { text: "  docker-build-push", delay: 1050 },
    { text: "    ↓", delay: 1100 },
    { text: "  deploy-kubernetes", delay: 1200 },
    { text: "", delay: 1300 },
    { text: `${colors.cyan("Estimated Runtime:")} 6 min`, delay: 1400 },
    { text: `${colors.cyan("Parallel Jobs:")}     1`, delay: 1500 },
  ],
  convert: [
    { text: "$ cicd convert github gitlab -f .github/workflows/main.yml", delay: 100 },
    { text: "Parsing GitHub Actions YAML workflow...", delay: 300 },
    { text: "Extracting stage script commands...", delay: 600 },
    { text: "Translating to GitLab CI variables and job format...", delay: 900 },
    { text: "", delay: 1100 },
    { text: colors.green("Success: Translated config printed to console."), delay: 1200 },
    { text: "Use -o to write directly to .gitlab-ci.yml", delay: 1300 },
  ],
  doctor: [
    { text: "$ cicd doctor", delay: 100 },
    { text: "Checking system toolchains and dependencies...", delay: 350 },
    { text: "", delay: 500 },
    { text: "Git        git version 2.47.0              PASS", delay: 650 },
    { text: "Docker     Docker version 28.3.2           PASS", delay: 800 },
    { text: "Java       java 23.0.2                     PASS", delay: 950 },
    { text: "Node       v24.5.0                         PASS", delay: 1100 },
    { text: "kubectl    Client Version v1.35.1          PASS", delay: 1250 },
    { text: "helm       v4.1.1                          PASS", delay: 1400 },
    { text: "", delay: 1500 },
    { text: colors.green("System Health: 100% (All critical tools available)"), delay: 1600 },
  ]
};

export default function TerminalPlayground() {
  const [selectedCommand, setSelectedCommand] = useState('detect');
  const [typedLines, setTypedLines] = useState([]);

  useEffect(() => {
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
  }, [selectedCommand]);

  return (
    <section id="terminal" className="terminal-section">
      <h2 className="section-title">Interactive Command Playground</h2>
      <div className="terminal-grid">
        {/* Menu Selector */}
        <div className="terminal-sidebar">
          {[
            { id: 'init', name: 'cicd init', desc: 'Initialize local workspace' },
            { id: 'detect', name: 'cicd detect', desc: 'Inspect project stack' },
            { id: 'generate', name: 'cicd generate', desc: 'Output CI configuration' },
            { id: 'validate', name: 'cicd validate', desc: 'Audit syntax & rules' },
            { id: 'explain', name: 'cicd explain', desc: 'Inspect stages runtime' },
            { id: 'convert', name: 'cicd convert', desc: 'Translate pipeline formats' },
            { id: 'doctor', name: 'cicd doctor', desc: 'Check local toolchain health' },
          ].map((cmd) => (
            <button
              key={cmd.id}
              onClick={() => setSelectedCommand(cmd.id)}
              className={`cmd-btn ${selectedCommand === cmd.id ? 'active' : ''}`}
            >
              <div>
                <h4 className="cmd-btn-name">{cmd.name}</h4>
                <p className="cmd-btn-desc">{cmd.desc}</p>
              </div>
              <ChevronRight size={16} />
            </button>
          ))}
        </div>

        {/* Terminal Screen */}
        <div className="terminal-window">
          <div className="terminal-window-header">
            <div className="dot-group">
              <div className="dot red" />
              <div className="dot yellow" />
              <div className="dot green" />
            </div>
            <span className="terminal-header-title">bash - shubhams-macbook-air</span>
            <div style={{ width: '40px' }} />
          </div>

          <div className="terminal-window-body">
            <AnimatePresence mode="popLayout">
              {typedLines.map((line, idx) => (
                <motion.div 
                  key={idx}
                  initial={{ opacity: 0, x: -3 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ duration: 0.1 }}
                  style={{ minHeight: '20px' }}
                >
                  {line}
                </motion.div>
              ))}
            </AnimatePresence>
            
            <div className="cursor" />
          </div>
        </div>
      </div>
    </section>
  );
}
