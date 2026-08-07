import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Cpu, Terminal, BookOpen, Download, Settings, RefreshCw, CheckCircle } from 'lucide-react';

const docTabs = {
  requirements: {
    title: "Requirements",
    icon: Cpu,
    content: (
      <div className="doc-detail-pane">
        <h3 className="doc-detail-title">System Requirements</h3>
        <p className="doc-detail-desc">Before installing the `cicd` CLI tool, ensure your workspace environment meets the following baseline dependencies:</p>
        
        <div className="requirements-list">
          <div className="req-item">
            <span className="req-label">Java Runtime JRE / JDK</span>
            <span className="req-val">Java 21 or higher (LTS release recommended)</span>
          </div>
          <div className="req-item">
            <span className="req-label">Maven (for Java stack build check)</span>
            <span className="req-val">Version 3.9+ (if validating Java codebases)</span>
          </div>
          <div className="req-item">
            <span className="req-label">Docker CLI</span>
            <span className="req-val">Version 25.0+ (required for `doctor` and build image audits)</span>
          </div>
          <div className="req-item">
            <span className="req-label">Git Version Control</span>
            <span className="req-val">Version 2.40.0+ (required for codebase commit scanning)</span>
          </div>
        </div>
      </div>
    )
  },
  installation: {
    title: "Installation",
    icon: Download,
    content: (
      <div className="doc-detail-pane">
        <h3 className="doc-detail-title">Installation Guide</h3>
        <p className="doc-detail-desc">Install the CLI tool globally on your system to run it from any repository workspace:</p>
        
        <div className="install-tabs">
          <div className="install-box">
            <span className="install-box-header">macOS / Linux Terminal</span>
            <pre className="install-code-snippet">
              <code>curl -sSL https://raw.githubusercontent.com/shuvmraj/cicd-cli/main/install.sh | bash</code>
            </pre>
          </div>
          
          <div className="install-box">
            <span className="install-box-header">Windows PowerShell (Admin)</span>
            <pre className="install-code-snippet">
              <code>irm https://raw.githubusercontent.com/shuvmraj/cicd-cli/main/install.ps1 | iex</code>
            </pre>
          </div>
        </div>
      </div>
    )
  },
  commands: {
    title: "CLI Commands",
    icon: Terminal,
    content: <CommandReference />
  },
  translator: {
    title: "Workflow Translator",
    icon: RefreshCw,
    content: (
      <div className="doc-detail-pane">
        <h3 className="doc-detail-title">Cross-Platform Translation</h3>
        <p className="doc-detail-desc">Translate existing pipeline configuration files between distinct CI/CD vendor formats natively:</p>
        
        <div className="translation-grid">
          <div className="trans-direction-card">
            <span className="trans-header">Supported Translations</span>
            <div className="trans-path">
              <span>GitHub Actions</span>
              <span>➜</span>
              <span>GitLab CI</span>
            </div>
            <div className="trans-path">
              <span>GitLab CI</span>
              <span>➜</span>
              <span>Jenkins Pipeline</span>
            </div>
            <div className="trans-path">
              <span>Jenkinsfile</span>
              <span>➜</span>
              <span>Azure DevOps</span>
            </div>
          </div>

          <div className="trans-features-list">
            <div className="trans-feat-item">
              <CheckCircle size={14} className="feat-check-icon" />
              <span>Translates script commands and env context variables natively</span>
            </div>
            <div className="trans-feat-item">
              <CheckCircle size={14} className="feat-check-icon" />
              <span>Resolves steps/jobs concurrency mapping models</span>
            </div>
            <div className="trans-feat-item">
              <CheckCircle size={14} className="feat-check-icon" />
              <span>Maintains caching directories and artifact archiving directives</span>
            </div>
          </div>
        </div>
      </div>
    )
  }
};

const commandsList = {
  init: {
    desc: "Initializes a target project workspace directory by creating a `.cicd-config.json` configuration manifest containing system metadata.",
    syntax: "cicd init [-d <baseDir>]",
    options: ["-d, --dir=<baseDir>: Specify custom base directory to initialize"],
    output: [
      "$ cicd init",
      "Initializing target workspace workspace...",
      "✔ Configuration file successfully created: /path/to/project/.cicd-config.json"
    ]
  },
  detect: {
    desc: "Scans project code files (e.g. pom.xml, package.json, requirements.txt) to automatically build a normalized model of the project stack.",
    syntax: "cicd detect [-d <baseDir>]",
    options: ["-d, --dir=<baseDir>: Base directory of codebase to scan"],
    output: [
      "$ cicd detect",
      "Scanning project codebase target directories...",
      "Framework: React.js",
      "Language: JavaScript",
      "Build Tool: Vite",
      "Docker: Found (Dockerfile detected)",
      "Deployment Target: Static Web Hosting",
      "CI Platform Recommendation: GitHub Actions"
    ]
  },
  validate: {
    desc: "Validates a pipeline file (e.g. Jenkinsfile, .github/workflows/main.yml) against rule heuristics to search for build issues and circular steps.",
    syntax: "cicd validate [-d <baseDir>] [-f <pipelineFile>]",
    options: [
      "-d, --dir=<baseDir>: Base directory of target codebase",
      "-f, --file=<pipelineFile>: Path to target pipeline configuration file"
    ],
    output: [
      "$ cicd validate -f Jenkinsfile",
      "Evaluating pipeline stages against rule engine...",
      "WARNING",
      "  - Project output artifact type is 'JAR', but no archiving target step was declared.",
      "INFO",
      "  - Successfully referenced 4 unique environment credentials safely.",
      "Overall Status: PASSED"
    ]
  },
  generate: {
    desc: "Generates an optimized, secure pipeline setup configuration file matching the project's codebase model (e.g. outputs optimized YAML workflows).",
    syntax: "cicd generate <platform> [-d <baseDir>] [-w]",
    options: [
      "platform: Target platform config (github, gitlab, jenkins, azure)",
      "-d, --dir=<baseDir>: Base directory of target codebase",
      "-w, --write: Write generated content directly to file path"
    ],
    output: [
      "$ cicd generate github -w",
      "Compiling optimized pipeline using template rules...",
      "✔ Workflow file successfully written to: .github/workflows/main.yml"
    ]
  },
  explain: {
    desc: "Analyzes a pipeline configuration file to describe the execution stages flow, concurrency, and estimate workflow runtime.",
    syntax: "cicd explain [-d <baseDir>] [-f <pipelineFile>]",
    options: [
      "-d, --dir=<baseDir>: Base directory of target codebase",
      "-f, --file=<pipelineFile>: Path to target pipeline configuration file"
    ],
    output: [
      "$ cicd explain -f .github/workflows/main.yml",
      "Pipeline Stages Flow:",
      "  install ➜ test ➜ build ➜ deploy",
      "Parallelism: 1 sequential flow",
      "Estimated Execution Duration: ~4.5 minutes"
    ]
  },
  doctor: {
    desc: "Verifies local machine environment dependency status, checking paths for Java, Maven, Docker, Git, and Kubernetes CLI systems.",
    syntax: "cicd doctor",
    options: ["No options available."],
    output: [
      "$ cicd doctor",
      "Checking local tools health...",
      "  - Git: version 2.47.0 (PASS)",
      "  - Docker: version 28.3.2 (PASS)",
      "  - Java JRE: version 21.0.2 (PASS)",
      "✔ Environment health: 100% operational"
    ]
  }
};

function CommandReference() {
  const [selectedCmd, setSelectedCmd] = useState('detect');

  return (
    <div className="cmd-ref-container">
      <div className="cmd-ref-sidebar">
        {Object.keys(commandsList).map((cmdKey) => (
          <button
            key={cmdKey}
            onClick={() => setSelectedCmd(cmdKey)}
            className={`cmd-sidebar-btn ${selectedCmd === cmdKey ? 'active' : ''}`}
          >
            cicd {cmdKey}
          </button>
        ))}
      </div>
      
      <div className="cmd-ref-detail">
        <AnimatePresence mode="wait">
          <motion.div
            key={selectedCmd}
            initial={{ opacity: 0, x: 8 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -8 }}
            transition={{ duration: 0.15 }}
          >
            <h4 className="cmd-detail-title">Command: cicd {selectedCmd}</h4>
            <p className="cmd-detail-desc">{commandsList[selectedCmd].desc}</p>
            
            <div className="cmd-block-section">
              <span className="cmd-block-label">Syntax</span>
              <pre className="cmd-syntax-code">
                <code>{commandsList[selectedCmd].syntax}</code>
              </pre>
            </div>

            <div className="cmd-block-section">
              <span className="cmd-block-label">Options</span>
              <ul className="cmd-options-list">
                {commandsList[selectedCmd].options.map((opt, index) => (
                  <li key={index}>{opt}</li>
                ))}
              </ul>
            </div>

            <div className="cmd-block-section">
              <span className="cmd-block-label">Sample Output</span>
              <pre className="cmd-output-terminal">
                {commandsList[selectedCmd].output.map((line, idx) => (
                  <div key={idx} className={idx === 0 ? 'terminal-prompt-line' : ''}>{line}</div>
                ))}
              </pre>
            </div>
          </motion.div>
        </AnimatePresence>
      </div>
    </div>
  );
}

export default function Documentation() {
  const [activeTab, setActiveTab] = useState('requirements');

  return (
    <section id="docs" className="docs-section">
      <motion.h2 
        className="section-title"
        initial={{ opacity: 0, y: 15 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-100px" }}
        transition={{ duration: 0.5 }}
      >
        Workspace Manual & CLI Directory
      </motion.h2>

      <motion.div 
        className="docs-card"
        initial={{ opacity: 0, y: 32 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-120px" }}
        transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
      >
        <div className="docs-tabs-nav">
          {Object.keys(docTabs).map((tabKey) => {
            const Icon = docTabs[tabKey].icon;
            return (
              <button
                key={tabKey}
                onClick={() => setActiveTab(tabKey)}
                className={`docs-nav-tab ${activeTab === tabKey ? 'active' : ''}`}
              >
                <Icon size={14} />
                <span>{docTabs[tabKey].title}</span>
              </button>
            );
          })}
        </div>

        <div className="docs-content-viewport">
          <AnimatePresence mode="wait">
            <motion.div
              key={activeTab}
              initial={{ opacity: 0, y: 6 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -6 }}
              transition={{ duration: 0.2 }}
            >
              {docTabs[activeTab].content}
            </motion.div>
          </AnimatePresence>
        </div>
      </motion.div>
    </section>
  );
}
