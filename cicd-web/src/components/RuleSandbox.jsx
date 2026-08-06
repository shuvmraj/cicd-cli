import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { Play, AlertCircle, RefreshCw, AlertTriangle, Info } from 'lucide-react';

export default function RuleSandbox() {
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

  return (
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
              <Play size={12} fill="currentColor" />
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
              <h4 style={{ color: 'var(--text-primary)', fontSize: '18px' }}>Awaiting Audit Execution</h4>
              <p style={{ color: 'var(--text-muted)', fontSize: '13px', maxWidth: '280px', marginTop: '6px' }}>
                Click the **Scan Pipeline** button inside the editor simulator to evaluate rule heuristics.
              </p>
            </div>
          )}

          {isScanning && (
            <div className="scanning-state">
              <RefreshCw size={32} className="animate-spin" style={{ color: 'var(--color-cyan)', marginBottom: '16px' }} />
              <h4 style={{ color: 'var(--text-primary)', fontSize: '13px', fontFamily: 'var(--font-mono)' }}>{scanStepText}</h4>
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
  );
}
