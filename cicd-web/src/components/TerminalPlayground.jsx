import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronRight, Settings, Info } from 'lucide-react';

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

const commandInfo = {
  init: {
    usage: 'cicd init [OPTIONS]',
    description: 'Initializes the CLI in the current directory by creating a `.cicd-config.json` file. Allows customizing compilation triggers and overriding detector assumptions.',
    options: ['-d, --dir=<targetDir> : Set configuration folder location']
  },
  detect: {
    usage: 'cicd detect [OPTIONS]',
    description: 'Scans the directory for configuration descriptors, package assets, and build configurations to construct the normalized stack blueprint.',
    options: ['-d, --dir=<targetDir> : Directory path to execute scan']
  },
  generate: {
    usage: 'cicd generate <platform> [OPTIONS]',
    description: 'Compiles custom Mustache templates to generate optimized workflow pipelines. Supports creating custom Dockerfiles as well.',
    options: [
      '<platform>            : github, gitlab, jenkins, azure, docker',
      '-w, --write           : Write output directly to standard directory',
      '-o, --output=<path>   : Export pipeline to custom file path'
    ]
  },
  validate: {
    usage: 'cicd validate [OPTIONS]',
    description: 'Audits pipeline structure using a multi-rule verification framework. Runs dependency graph sort to detect cyclic deadlocks and scans for plain credentials.',
    options: [
      '-f, --file=<filePath> : Custom pipeline configuration file path',
      '-d, --dir=<baseDir>   : Root directory to analyze context'
    ]
  },
  explain: {
    usage: 'cicd explain [OPTIONS]',
    description: 'Constructs the execution tree, highlighting parallel jobs, stage sequences, and estimated compilation build times.',
    options: ['-f, --file=<filePath> : Path to the target pipeline configuration file']
  },
  convert: {
    usage: 'cicd convert <source> <target> [OPTIONS]',
    description: 'Translates pipeline syntax directly between different platform structures. Maps script blocks and cache mappings dynamically.',
    options: [
      '<source> <target>     : Options are github, gitlab, jenkins, azure',
      '-f, --file=<filePath> : Path of source configuration to convert',
      '-o, --output=<path>   : Write translated schema to custom location'
    ]
  },
  doctor: {
    usage: 'cicd doctor',
    description: 'Checks and reports the health of the system toolchains. Evaluates Git, Docker, Java, Node, kubectl, and Helm versions.',
    options: []
  }
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

        {/* Terminal Screen (Column 2) */}
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

        {/* Documentation / Info Column (Column 3) */}
        <div className="terminal-info-card">
          <div className="terminal-info-card-header">
            <Settings size={18} style={{ color: 'var(--text-secondary)' }} />
            <span style={{ fontWeight: 700, fontFamily: 'var(--font-mono)', fontSize: '12px' }}>manual reference</span>
          </div>

          <div className="terminal-info-card-body">
            <div style={{ marginBottom: '16px' }}>
              <span className="info-tag-header">USAGE</span>
              <div className="info-code-block">{commandInfo[selectedCommand].usage}</div>
            </div>

            <div style={{ marginBottom: '16px' }}>
              <span className="info-tag-header">DESCRIPTION</span>
              <p className="info-text-paragraph">{commandInfo[selectedCommand].description}</p>
            </div>

            {commandInfo[selectedCommand].options.length > 0 && (
              <div>
                <span className="info-tag-header">OPTIONS</span>
                <div className="info-options-list">
                  {commandInfo[selectedCommand].options.map((opt, i) => (
                    <div key={i} className="info-option-item">{opt}</div>
                  ))}
                </div>
              </div>
            )}

            {/* Laptop developer illustration decoration */}
            <div className="terminal-info-decorator">
              <img 
                src="/laptop_developer.png" 
                alt="Developer looking at code schemas" 
                className="decorator-image"
              />
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
