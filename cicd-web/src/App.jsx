import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Terminal, ShieldCheck, Cpu, Code, RefreshCw, FileCode, CheckCircle, 
  AlertTriangle, Info, Play, Clipboard, Check, HelpCircle, 
  ChevronRight, AlertCircle
} from 'lucide-react';

// Custom inline GitHub SVG component since brand icons are removed from Lucide-React
const GitHubIcon = ({ size = 18 }) => (
  <svg height={size} width={size} viewBox="0 0 16 16" fill="currentColor" xmlns="http://www.w3.org/2050/svg" style={{ display: 'inline-block', verticalAlign: 'middle' }}>
    <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"/>
  </svg>
);

// Color formatting utils inside Simulated Terminal
const colors = {
  green: (text) => <span style={{ color: 'var(--color-green)' }}>{text}</span>,
  cyan: (text) => <span style={{ color: 'var(--color-cyan)' }}>{text}</span>,
  yellow: (text) => <span style={{ color: 'var(--color-orange)' }}>{text}</span>,
  red: (text) => <span style={{ color: 'var(--color-red)' }}>{text}</span>,
  bold: (text) => <span style={{ fontWeight: 700, color: '#fff' }}>{text}</span>,
};

// Simulated CLI execution outputs
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

// Tech stacks config definitions for Stack Explorer
const techStacks = {
  react: {
    name: "React.js",
    lang: "JavaScript",
    build: "npm install && npm run build",
    test: "npm test",
    triggerFile: "package.json (dependencies: react)",
    docker: `FROM node:20-alpine AS build\nWORKDIR /app\nCOPY package*.json ./\nRUN npm install\nCOPY . .\nRUN npm run build\n\nFROM nginx:alpine\nCOPY --from=build /app/dist /usr/share/nginx/html\nEXPOSE 80\nCMD ["nginx", "-g", "daemon off;"]`,
    github: `name: React CI\non: [push]\njobs:\n  build-test:\n    runs-on: ubuntu-latest\n    steps:\n      - uses: actions/checkout@v4\n      - run: npm install && npm run build\n      - run: npm test`
  },
  spring: {
    name: "Spring Boot",
    lang: "Java",
    build: "mvn clean package -DskipTests",
    test: "mvn test",
    triggerFile: "pom.xml (dependencies: spring-boot)",
    docker: `FROM maven:3.9-eclipse-temurin-21-alpine AS build\nWORKDIR /app\nCOPY pom.xml .\nCOPY src ./src\nRUN mvn clean package -DskipTests\n\nFROM eclipse-temurin:21-jre-alpine\nCOPY --from=build /app/target/*.jar app.jar\nEXPOSE 8080\nENTRYPOINT ["java", "-jar", "app.jar"]`,
    github: `name: Java CI\non: [push]\njobs:\n  mvn-build:\n    runs-on: ubuntu-latest\n    steps:\n      - uses: actions/checkout@v4\n      - uses: actions/setup-java@v4\n        with:\n          java-version: '21'\n          distribution: 'temurin'\n      - run: mvn clean package -DskipTests\n      - run: mvn test`
  },
  django: {
    name: "Django",
    lang: "Python",
    build: "pip install -r requirements.txt",
    test: "pytest",
    triggerFile: "manage.py & requirements.txt",
    docker: `FROM python:3.11-slim\nWORKDIR /app\nCOPY requirements.txt .\nRUN pip install --no-cache-dir -r requirements.txt\nCOPY . .\nEXPOSE 8000\nCMD ["python", "manage.py", "runserver", "0.0.0.0:8000"]`,
    github: `name: Django CI\non: [push]\njobs:\n  django-tests:\n    runs-on: ubuntu-latest\n    steps:\n      - uses: actions/checkout@v4\n      - uses: actions/setup-python@v5\n        with:\n          python-version: '3.11'\n      - run: pip install -r requirements.txt\n      - run: pytest`
  },
  node: {
    name: "Node / Express",
    lang: "JavaScript",
    build: "npm install",
    test: "npm test",
    triggerFile: "package.json (dependencies: express)",
    docker: `FROM node:20-alpine\nWORKDIR /app\nCOPY package*.json ./\nRUN npm install --production\nCOPY . .\nEXPOSE 3000\nCMD ["npm", "start"]`,
    github: `name: Node Express CI\non: [push]\njobs:\n  node-test:\n    runs-on: ubuntu-latest\n    steps:\n      - uses: actions/checkout@v4\n      - run: npm install\n      - run: npm test`
  }
};

export default function App() {
  const [selectedCommand, setSelectedCommand] = useState('detect');
  const [typedLines, setTypedLines] = useState([]);
  const [installOs, setInstallOs] = useState('mac');
  const [selectedStack, setSelectedStack] = useState('react');
  const [activeTab, setActiveTab] = useState('docker');
  const [copiedText, setCopiedText] = useState(null);
  
  // Interactive Validation Engine demo state
  const [isScanning, setIsScanning] = useState(false);
  const [scanProgress, setScanProgress] = useState(0);
  const [scanStepText, setScanStepText] = useState('');
  const [scanReport, setScanReport] = useState(null);
  const [rawYamlCode, setRawYamlCode] = useState(
`# Sample pipeline file containing issues
name: Pipeline
jobs:
  build:
    runs-on: ubuntu-latest
    needs: deploy # Circular dependency! (deploy needs docker-build needs build)
    steps:
      - run: npm install && npm run build
  docker-build:
    needs: build
    steps:
      - run: docker build -t MyRepo/app:latest .
      - run: docker login -u user -p mySuperSecretPassword123
  deploy:
    needs: docker-build
    steps:
      - run: kubectl apply -f deploy.yaml`
  );

  // Handle typing animation inside Simulated Terminal
  useEffect(() => {
    setTypedLines([]);
    const lines = terminalOutputs[selectedCommand];
    let index = 0;
    
    const timers = [];
    lines.forEach((line) => {
      const timer = setTimeout(() => {
        setTypedLines(prev => [...prev, line.text]);
      }, line.delay);
      timers.push(timer);
    });

    return () => timers.forEach(clearTimeout);
  }, [selectedCommand]);

  const copyToClipboard = (text, label) => {
    navigator.clipboard.writeText(text);
    setCopiedText(label);
    setTimeout(() => setCopiedText(null), 2000);
  };

  // Run validation engine sandbox scan
  const startValidationScan = () => {
    setIsScanning(true);
    setScanProgress(0);
    setScanReport(null);
    
    const steps = [
      { text: 'Parsing YAML syntax...', progress: 20 },
      { text: 'Resolving dependency graph nodes...', progress: 40 },
      { text: 'Running Kahn\'s topological sort to discover cycles...', progress: 70 },
      { text: 'Scanning CLI scripts for hardcoded secrets...', progress: 90 },
      { text: 'Compiling issues list...', progress: 100 }
    ];

    steps.forEach((step, idx) => {
      setTimeout(() => {
        setScanStepText(step.text);
        setScanProgress(step.progress);
        
        if (step.progress === 100) {
          setTimeout(() => {
            setIsScanning(false);
            setScanReport({
              status: 'FAILED',
              errors: [
                "Circular dependency detected in execution path: build -> docker-build -> deploy -> build",
                "Hardcoded credential in CLI script: DOCKER_PASSWORD=mySuperSecret****"
              ],
              warnings: [
                "Cache not configured. Adding dependency caching speeds up build durations.",
                "Invalid Docker image naming format: 'MyRepo/app:latest' contains uppercase characters."
              ],
              infos: [
                "Docker containerization detected and integration stage configured."
              ]
            });
          }, 600);
        }
      }, (idx + 1) * 800);
    });
  };

  const getInstallCmd = () => {
    if (installOs === 'mac') {
      return 'curl -sSL https://raw.githubusercontent.com/shuvmraj/cicd-cli/main/install.sh | bash';
    }
    return 'irm https://raw.githubusercontent.com/shuvmraj/cicd-cli/main/install.ps1 | iex';
  };

  return (
    <div className="app-container">
      {/* Background glow effects */}
      <div className="bg-glow-purple" />
      <div className="bg-glow-cyan" />

      {/* Header / Navigation */}
      <header className="app-header">
        <div className="logo-box">
          <div className="logo-icon">C</div>
          <div>
            <span className="logo-title">cicd</span>
            <span className="logo-version">v1.0.0</span>
          </div>
        </div>
        
        <nav className="nav-links">
          <a href="#features" className="nav-link">Features</a>
          <a href="#terminal" className="nav-link">CLI Playground</a>
          <a href="#explorer" className="nav-link">Stack Templates</a>
          <a href="#sandbox" className="nav-link">Rule Sandbox</a>
        </nav>

        <a 
          href="https://github.com/shuvmraj/cicd-cli" 
          target="_blank" 
          rel="noreferrer"
          className="btn-github"
        >
          <GitHubIcon size={18} />
          GitHub
        </a>
      </header>

      {/* Hero Section */}
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
              onClick={() => copyToClipboard(getInstallCmd(), 'install')}
              className="icon-btn"
              title="Copy to clipboard"
            >
              {copiedText === 'install' ? <Check size={16} style={{ color: 'var(--color-green)' }} /> : <Clipboard size={16} />}
            </button>
          </div>

          <div className="widget-footer">
            <span>✔ No manual folder sharing</span>
            <span>✔ Automatic environment variables config</span>
          </div>
        </motion.div>
      </section>

      {/* Core Features Grid */}
      <section id="features" className="features-section">
        <h2 className="section-title">Engineered for Security & Speed</h2>
        <div className="features-grid">
          <div className="feature-card">
            <div className="feature-icon-wrapper">
              <Cpu size={24} />
            </div>
            <h3 className="feature-title">Automated Tech Detection</h3>
            <p className="feature-desc">
              Scans descriptors (`pom.xml`, `package.json`, `manage.py`, C# files) to construct a normalized technology blueprint.
            </p>
          </div>

          <div className="feature-card">
            <div className="feature-icon-wrapper">
              <ShieldCheck size={24} />
            </div>
            <h3 className="feature-title">Graph Dependency Analyzer</h3>
            <p className="feature-desc">
              Implements topological cycle sorting algorithms to detect stage-execution circular deadlocks before committing files.
            </p>
          </div>

          <div className="feature-card">
            <div className="feature-icon-wrapper">
              <FileCode size={24} />
            </div>
            <h3 className="feature-title">Cross-Platform Translation</h3>
            <p className="feature-desc">
              Converts existing workflows (GitHub Actions to GitLab CI, Jenkins, or Azure DevOps) by translating script stages natively.
            </p>
          </div>
        </div>
      </section>

      {/* Interactive Command Playground */}
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

      {/* Stack Explorer Section */}
      <section id="explorer" className="explorer-section">
        <h2 className="section-title">Codebase Template Explorer</h2>
        <div className="explorer-grid">
          {/* Stack Buttons */}
          <div className="explorer-sidebar">
            {Object.keys(techStacks).map((stackKey) => (
              <button
                key={stackKey}
                onClick={() => setSelectedStack(stackKey)}
                className={`stack-btn ${selectedStack === stackKey ? 'active' : ''}`}
              >
                {techStacks[stackKey].name}
              </button>
            ))}
          </div>

          {/* Config Viewer */}
          <div className="config-viewer">
            <div className="config-viewer-header">
              <div>
                <h3 style={{ color: '#fff' }}>{techStacks[selectedStack].name} Configuration</h3>
                <p style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>
                  Identified by: <code style={{ fontSize: '10px', background: '#010204', border: '1px solid #141621', padding: '1px 4px', borderRadius: '4px' }}>{techStacks[selectedStack].triggerFile}</code>
                </p>
              </div>
              
              {/* File Switcher */}
              <div className="tab-group">
                <button 
                  onClick={() => setActiveTab('docker')}
                  className={`tab-btn ${activeTab === 'docker' ? 'active' : ''}`}
                >
                  Dockerfile
                </button>
                <button 
                  onClick={() => setActiveTab('github')}
                  className={`tab-btn ${activeTab === 'github' ? 'active' : ''}`}
                >
                  GitHub Actions
                </button>
              </div>
            </div>

            {/* Code Panel */}
            <div className="code-panel">
              <pre style={{ margin: 0, whiteSpace: 'pre-wrap' }}>
                {activeTab === 'docker' ? techStacks[selectedStack].docker : techStacks[selectedStack].github}
              </pre>
            </div>
          </div>
        </div>
      </section>

      {/* Validation Sandbox */}
      <section id="sandbox" className="sandbox-section">
        <h2 className="section-title">Rule Engine Sandbox</h2>
        <div className="sandbox-grid">
          {/* YAML Editor Panel */}
          <div className="editor-panel">
            <div className="panel-header">
              <span style={{ fontSize: '11px', fontWeight: 800, color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>YAML Input Simulator</span>
              <button 
                onClick={startValidationScan}
                disabled={isScanning}
                className="btn-play"
              >
                <Play size={12} fill="black" />
                Scan Pipeline
              </button>
            </div>
            
            <textarea
              value={rawYamlCode}
              onChange={(e) => setRawYamlCode(e.target.value)}
              className="yaml-textarea"
            />
          </div>

          {/* Scanning / Output Report Panel */}
          <div className="report-panel">
            {!isScanning && !scanReport && (
              <div className="empty-state">
                <AlertCircle size={48} style={{ color: 'var(--text-muted)', marginBottom: '16px' }} />
                <h4 style={{ color: '#fff', fontSize: '18px' }}>Awaiting Audit Execution</h4>
                <p style={{ color: 'var(--text-muted)', fontSize: '13px', maxWidth: '280px', marginTop: '6px' }}>
                  Click the **Scan Pipeline** button inside the editor simulator to evaluate rule heuristics.
                </p>
              </div>
            )}

            {isScanning && (
              <div className="scanning-state">
                <RefreshCw size={32} className="animate-spin" style={{ color: 'var(--color-cyan)', marginBottom: '16px' }} />
                <h4 style={{ color: '#fff', fontSize: '13px', fontFamily: 'var(--font-mono)' }}>{scanStepText}</h4>
                <div className="progress-bar">
                  <motion.div 
                    className="progress-fill"
                    initial={{ width: 0 }}
                    animate={{ width: `${scanProgress}%` }}
                    transition={{ duration: 0.2 }}
                  />
                </div>
              </div>
            )}

            {scanReport && !isScanning && (
              <motion.div 
                className="report-container"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
              >
                <div className="report-header">
                  <span>Validation Report</span>
                  <span className="severity-badge failed">
                    {scanReport.status}
                  </span>
                </div>

                {/* Errors */}
                {scanReport.errors.length > 0 && (
                  <div className="report-group">
                    <span className="report-group-title error">
                      <AlertTriangle size={12} />
                      Errors ({scanReport.errors.length})
                    </span>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                      {scanReport.errors.map((err, i) => (
                        <div key={i} className="issue-list-item error">
                          <span style={{ color: 'var(--color-red)' }}>•</span>
                          <span>{err}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Warnings */}
                {scanReport.warnings.length > 0 && (
                  <div className="report-group">
                    <span className="report-group-title warning">
                      <AlertTriangle size={12} />
                      Warnings ({scanReport.warnings.length})
                    </span>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                      {scanReport.warnings.map((warn, i) => (
                        <div key={i} className="issue-list-item warning">
                          <span style={{ color: 'var(--color-orange)' }}>•</span>
                          <span>{warn}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Infos */}
                {scanReport.infos.length > 0 && (
                  <div className="report-group">
                    <span className="report-group-title info">
                      <Info size={12} />
                      Information ({scanReport.infos.length})
                    </span>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                      {scanReport.infos.map((info, i) => (
                        <div key={i} className="issue-list-item info">
                          <span style={{ color: 'var(--color-cyan)' }}>•</span>
                          <span>{info}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </motion.div>
            )}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="app-footer">
        <span>© 2026 shuvmraj/cicd-cli. Released under the MIT License.</span>
        <span>Built with Java 21 & React Vite</span>
      </footer>
    </div>
  );
}
