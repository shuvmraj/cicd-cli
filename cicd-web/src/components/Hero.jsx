import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Terminal, Clipboard, Check, Code, Settings, Share2, ShieldCheck } from 'lucide-react';

const codeTemplates = {
  react_github_docker: `# Resolved Stack: React -> GitHub Actions -> Docker
name: Production Release
on:
  push:
    branches: [ main ]
jobs:
  pipeline:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Audit & Validate Stack
        run: cicd validate -d ./
      - name: Build Assets
        run: npm install && npm run build
      - name: Containerize App
        run: docker build -t app:latest .`,
  react_github_k8s: `# Resolved Stack: React -> GitHub Actions -> Kubernetes
name: Production Release
on:
  push:
    branches: [ main ]
jobs:
  pipeline:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Audit & Validate Stack
        run: cicd validate -d ./
      - name: Kubernetes Spec Check
        run: cicd validate -f k8s/deployment.yml
      - name: Rollout Deploy
        run: kubectl apply -f k8s/deployment.yml`,
  react_github_vercel: `# Resolved Stack: React -> GitHub Actions -> Vercel
name: Production Release
on:
  push:
    branches: [ main ]
jobs:
  pipeline:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Validate & Compile Configuration
        run: cicd generate vercel -w
      - name: Vercel Deploy
        run: vercel --prod --token $VERCEL_TOKEN`,
  spring_github_docker: `# Resolved Stack: Spring Boot -> GitHub Actions -> Docker
name: Production Release
on:
  push:
    branches: [ main ]
jobs:
  pipeline:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Java Stack Doctor Check
        run: cicd doctor
      - name: Build Spring Boot Jar
        run: ./gradlew bootJar
      - name: Package Container
        run: docker build -t server:latest .`,
  spring_github_k8s: `# Resolved Stack: Spring Boot -> GitHub Actions -> Kubernetes
name: Production Release
on:
  push:
    branches: [ main ]
jobs:
  pipeline:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Java Stack Doctor Check
        run: cicd doctor
      - name: Package Manifests
        run: kubectl apply -f deploy/pod-spec.yaml`,
  spring_github_vercel: `# Resolved Stack: Spring Boot -> GitHub Actions -> Vercel
name: Production Release
on:
  push:
    branches: [ main ]
jobs:
  pipeline:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run Stack Validator
        run: cicd validate -d ./
      - name: Deploy Serverless Config
        run: vercel --prod`,
  python_github_docker: `# Resolved Stack: Python -> GitHub Actions -> Docker
name: Production Release
on:
  push:
    branches: [ main ]
jobs:
  pipeline:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Codebase Audit & Check
        run: cicd detect -d ./
      - name: Test Suite
        run: pytest
      - name: Docker Push
        run: docker build -t api:latest .`,
  python_github_k8s: `# Resolved Stack: Python -> GitHub Actions -> Kubernetes
name: Production Release
on:
  push:
    branches: [ main ]
jobs:
  pipeline:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Kubernetes Audit
        run: cicd validate -f deploy.yaml
      - name: Deploy Pod
        run: kubectl apply -f deploy.yaml`,
  python_github_vercel: `# Resolved Stack: Python -> GitHub Actions -> Vercel
name: Production Release
on:
  push:
    branches: [ main ]
jobs:
  pipeline:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Audit Spec
        run: cicd validate -d ./
      - name: Vercel serverless push
        run: vercel --prod`,
  // GitLab configurations
  react_gitlab_docker: `# Resolved Stack: React -> GitLab CI -> Docker
stages:
  - test
  - containerize
run-audit:
  stage: test
  script:
    - cicd validate -d ./
publish-image:
  stage: containerize
  script:
    - docker build -t app:latest .`,
  spring_gitlab_docker: `# Resolved Stack: Spring Boot -> GitLab CI -> Docker
stages:
  - compile
  - containerize
build-jar:
  stage: compile
  script:
    - ./gradlew bootJar
package-image:
  stage: containerize
  script:
    - docker build -t server:latest .`,
  python_gitlab_docker: `# Resolved Stack: Python -> GitLab CI -> Docker
stages:
  - test
  - containerize
run-pytest:
  stage: test
  script:
    - pytest
package-image:
  stage: containerize
  script:
    - docker build -t api:latest .`,
};

// Fallback logic generator for non-explicit templates
const generateFallbackConfig = (fw, pl, tg) => {
  const key = `${fw}_${pl}_${tg}`;
  if (codeTemplates[key]) return codeTemplates[key];
  
  // Dynamic fallback generator
  const pName = pl === 'gitlab' ? 'GitLab CI' : pl === 'circle' ? 'CircleCI' : 'GitHub Actions';
  return `# Resolved Stack: ${fw} -> ${pName} -> ${tg}
# Configured via universal mustache validator engine
pipeline:
  environment: ${fw}
  orchestration: ${pl}
  target: ${tg}
  steps:
    - run: cicd validate -d ./
    - run: ${fw === 'react' ? 'npm run build' : fw === 'spring' ? './gradlew build' : 'python -m pytest'}
    - run: echo "Deploying to ${tg}"`;
};

export default function Hero() {
  const [installOs, setInstallOs] = useState('mac');
  const [copied, setCopied] = useState(false);
  const [copyCode, setCopyCode] = useState(false);

  // Stack validator sandbox options states
  const [stackFw, setStackFw] = useState('react');
  const [stackPl, setStackPl] = useState('github');
  const [stackTg, setStackTg] = useState('docker');

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

  const handleCopyCode = () => {
    const yaml = generateFallbackConfig(stackFw, stackPl, stackTg);
    navigator.clipboard.writeText(yaml);
    setCopyCode(true);
    setTimeout(() => setCopyCode(false), 2000);
  };

  return (
    <section className="hero-section">
      <div className="hero-grid">
        {/* Left Column: Headline and Installer Widget */}
        <div className="hero-text-block">

          
          <motion.h1 
            className="hero-title"
            initial={{ opacity: 0, y: 15 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            style={{ textAlign: 'left', fontFamily: 'var(--font-mono)' }}
          >
            universal ci/cd <span className="hero-title-gradient">validator & generator</span>
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

            <div className="code-box" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', overflow: 'hidden' }}>
              <div style={{ flexGrow: 1, overflowX: 'auto', marginRight: '12px', display: 'flex', alignItems: 'center' }}>
                <span className="code-text" style={{ color: '#f8fafc', fontFamily: 'var(--font-mono)' }}>{getInstallCmd()}</span>
              </div>
              <button 
                onClick={handleCopy}
                className="icon-btn"
                title="Copy to clipboard"
                style={{ flexShrink: 0 }}
              >
                {copied ? <Check size={16} style={{ color: 'var(--color-green)' }} /> : <Clipboard size={16} />}
              </button>
            </div>

            <div className="widget-footer" style={{ fontFamily: 'var(--font-mono)' }}>
          
            </div>
          </motion.div>
        </div>

        {/* Right Column: Interactive Pipeline Planner Sandbox (Ditch static image) */}
        <motion.div 
          className="hero-image-block"
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.8, delay: 0.2 }}
        >
          <div className="pipeline-sandbox-card">
            <div className="sandbox-header">
              <div className="sandbox-header-left">
                <Code size={16} className="header-icon" />
                <span className="sandbox-title">Stack Compiler</span>
              </div>
              <div className="sandbox-status-badge">
                <ShieldCheck size={12} style={{ color: 'var(--color-green)' }} />
                <span>Validated</span>
              </div>
            </div>

            {/* 1. Interactive Selector Toggles */}
            <div className="sandbox-options-grid">
              {/* Row 1: Framework */}
              <div className="option-row">
                <h4 className="option-label">Stack</h4>
                <div className="option-chips">
                  {[
                    { id: 'react', label: 'React' },
                    { id: 'spring', label: 'Spring Boot' },
                    { id: 'python', label: 'Python CLI' },
                  ].map((fw) => (
                    <button
                      key={fw.id}
                      onClick={() => setStackFw(fw.id)}
                      className={`chip-btn ${stackFw === fw.id ? 'active' : ''}`}
                    >
                      {fw.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Row 2: CI Orchestrator */}
              <div className="option-row">
                <h4 className="option-label">Platform</h4>
                <div className="option-chips">
                  {[
                    { id: 'github', label: 'GitHub Actions' },
                    { id: 'gitlab', label: 'GitLab CI' },
                    { id: 'circle', label: 'CircleCI' },
                  ].map((pl) => (
                    <button
                      key={pl.id}
                      onClick={() => setStackPl(pl.id)}
                      className={`chip-btn ${stackPl === pl.id ? 'active' : ''}`}
                    >
                      {pl.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Row 3: Target Deployment */}
              <div className="option-row">
                <h4 className="option-label">Deploy To</h4>
                <div className="option-chips">
                  {[
                    { id: 'docker', label: 'Docker' },
                    { id: 'k8s', label: 'Kubernetes' },
                    { id: 'vercel', label: 'Vercel' },
                  ].map((tg) => (
                    <button
                      key={tg.id}
                      onClick={() => setStackTg(tg.id)}
                      className={`chip-btn ${stackTg === tg.id ? 'active' : ''}`}
                    >
                      {tg.label}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* 2. Visual Pipeline Flow Chart representation */}
            <div className="pipeline-flow-chart">
              <div className="flow-node">
                <span className="node-icon">📦</span>
                <span className="node-text">{stackFw === 'react' ? 'React' : stackFw === 'spring' ? 'Spring' : 'Python'}</span>
              </div>
              <div className="flow-arrow-dashed" />
              <div className="flow-node highlighted">
                <span className="node-icon">⚙️</span>
                <span className="node-text">{stackPl === 'github' ? 'GitHub' : stackPl === 'gitlab' ? 'GitLab' : 'CircleCI'}</span>
              </div>
              <div className="flow-arrow-dashed" />
              <div className="flow-node">
                <span className="node-icon">🚀</span>
                <span className="node-text">{stackTg === 'docker' ? 'Docker' : stackTg === 'k8s' ? 'K8s' : 'Vercel'}</span>
              </div>
            </div>

            {/* 3. Output YAML Editor Preview */}
            <div className="sandbox-editor-box">
              <div className="editor-top-bar">
                <span className="editor-file-name">
                  {stackPl === 'github' ? '.github/workflows/main.yml' : stackPl === 'gitlab' ? '.gitlab-ci.yml' : 'circleci/config.yml'}
                </span>
                <button 
                  onClick={handleCopyCode} 
                  className="editor-copy-btn"
                  title="Copy generated spec file"
                >
                  {copyCode ? (
                    <>
                      <Check size={12} style={{ color: 'var(--color-green)' }} />
                      <span>Copied</span>
                    </>
                  ) : (
                    <>
                      <Clipboard size={12} />
                      <span>Copy Config</span>
                    </>
                  )}
                </button>
              </div>
              <pre className="editor-code-block">
                <code>{generateFallbackConfig(stackFw, stackPl, stackTg)}</code>
              </pre>
            </div>

            <div className="sandbox-footer">
              <span className="footer-status-text">
                Blueprint generated successfully using Mustache templates.
              </span>
            </div>
          </div>
        </motion.div>
      </div>
    </section>
  );
}
