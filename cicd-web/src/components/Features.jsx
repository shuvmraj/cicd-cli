import React from 'react';
import { motion } from 'framer-motion';
import { Cpu, ShieldCheck, FileCode } from 'lucide-react';
import ParticleNetwork from './ParticleNetwork';

export default function Features({ theme }) {
  return (
    <section id="features" className="features-section" style={{ position: 'relative', overflow: 'hidden' }}>
      <ParticleNetwork theme={theme} />
      <motion.h2 
        className="section-title"
        style={{ position: 'relative', zIndex: 2 }}
        initial={{ opacity: 0, y: 15 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-100px" }}
        transition={{ duration: 0.5 }}
      >
        Engineered for Security & Speed
      </motion.h2>

      <motion.div 
        className="features-grid"
        style={{ position: 'relative', zIndex: 2 }}
        initial={{ opacity: 0, y: 32 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-120px" }}
        transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
      >
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
      </motion.div>
    </section>
  );
}
