import React, { useState } from 'react';
import { motion } from 'framer-motion';

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

export default function StackExplorer() {
  const [selectedStack, setSelectedStack] = useState('react');
  const [activeTab, setActiveTab] = useState('docker');

  return (
    <section id="explorer" className="explorer-section">
      <motion.h2 
        className="section-title"
        initial={{ opacity: 0, y: 15 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-100px" }}
        transition={{ duration: 0.5 }}
      >
        Codebase Template Explorer
      </motion.h2>

      <motion.div 
        className="explorer-grid"
        initial={{ opacity: 0, y: 32 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-120px" }}
        transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
      >
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
              <h3 style={{ color: 'var(--text-primary)' }}>{techStacks[selectedStack].name} Configuration</h3>
              <p style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>
                Identified by: <code style={{ fontSize: '10px', background: 'var(--bg-deep)', border: '1px solid var(--border-color)', padding: '1px 4px', borderRadius: '4px' }}>{techStacks[selectedStack].triggerFile}</code>
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
      </motion.div>
    </section>
  );
}
