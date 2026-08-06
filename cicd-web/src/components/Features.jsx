import React from 'react';
import { Cpu, ShieldCheck, FileCode } from 'lucide-react';

export default function Features() {
  return (
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
  );
}
