import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import DataStreams from './DataStreams';

// Colors configured as HTML string templates to prevent [object Object] serialization bugs
const colors = {
  green: (text) => `<span style="color: var(--color-green)">${text}</span>`,
  cyan: (text) => `<span style="color: var(--color-cyan)">${text}</span>`,
  yellow: (text) => `<span style="color: var(--color-orange)">${text}</span>`,
  red: (text) => `<span style="color: var(--color-red)">${text}</span>`,
  bold: (text) => `<span style="font-weight: 700; color: #fff">${text}</span>`,
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

const desktopShortcuts = [
  { id: 'init', name: 'init.sh', icon: '⚙️' },
  { id: 'detect', name: 'detect.sh', icon: '🔍' },
  { id: 'generate', name: 'generate.sh', icon: '⚡' },
  { id: 'validate', name: 'validate.sh', icon: '🛡️' },
  { id: 'explain', name: 'explain.sh', icon: '📊' },
  { id: 'convert', name: 'convert.sh', icon: '🔄' },
  { id: 'doctor', name: 'doctor.sh', icon: '🩺' },
];

export default function TerminalPlayground({ theme }) {
  const [selectedCommand, setSelectedCommand] = useState('detect');
  const [typedLines, setTypedLines] = useState([]);
  const [timeStr, setTimeStr] = useState('');
  const [showNotification, setShowNotification] = useState(true);

  // Update clock date and time
  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
      const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
      const day = days[now.getDay()];
      const month = months[now.getMonth()];
      const date = now.getDate();
      let hours = now.getHours();
      const minutes = String(now.getMinutes()).padStart(2, '0');
      const ampm = hours >= 12 ? 'PM' : 'AM';
      hours = hours % 12;
      hours = hours ? hours : 12;
      setTimeStr(`${day} ${month} ${date} ${hours}:${minutes} ${ampm}`);
    };
    updateTime();
    const interval = setInterval(updateTime, 30000);
    return () => clearInterval(interval);
  }, []);

  // Dismiss user help notification banner after 6 seconds on mount
  useEffect(() => {
    const timer = setTimeout(() => {
      setShowNotification(false);
    }, 6000);
    return () => clearTimeout(timer);
  }, []);

  // Types terminal command output
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

  const handleCommandChange = (cmdId) => {
    setSelectedCommand(cmdId);
    setShowNotification(false);
  };

  return (
    <section id="terminal" className="terminal-section" style={{ position: 'relative', overflow: 'hidden' }}>
      <div className="terminal-glow-bg" />
      <DataStreams theme={theme} />
      <motion.h2 
        className="section-title" 
        style={{ marginBottom: '48px', position: 'relative', zIndex: 2 }}
        initial={{ opacity: 0, y: 15 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-100px" }}
        transition={{ duration: 0.5 }}
      >
        Command Reference Console
      </motion.h2>
      
      <motion.div 
        className="terminal-playground-layout" 
        style={{ justifyContent: 'center', position: 'relative', zIndex: 2 }}
        initial={{ opacity: 0, y: 32 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-120px" }}
        transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
      >
        {/* CSS-based Macbook Container - Fully static, flat, and open */}
        <div className="macbook-wrapper" style={{ maxWidth: '1000px', margin: '0 auto', perspective: 'none' }}>
          <div className="macbook-device" style={{ transform: 'none', transition: 'none' }}>
            {/* Display screen lid - Forced to open state natively */}
            <div className="macbook-lid open" style={{ transform: 'none', transition: 'none' }}>
              <div 
                className="macbook-screen macos-desktop"
                style={{
                  backgroundImage: 'url(https://raw.githubusercontent.com/knmac/my_wallpapers/master/macos-big-sur-apple-layers-fluidic-colorful-wwdc-stock-4096x2304-1455.jpg)',
                  backgroundSize: 'cover',
                  backgroundPosition: 'center'
                }}
              >
                {/* macOS Menu bar */}
                <div className="macos-menubar">
                  <div className="menubar-left">
                    <span className="apple-menu-icon"></span>
                    <span className="menubar-item active-app">Terminal</span>
                    <span className="menubar-item">File</span>
                    <span className="menubar-item">Edit</span>
                    <span className="menubar-item">View</span>
                    <span className="menubar-item">Go</span>
                    <span className="menubar-item">Help</span>
                  </div>
                  <div className="menubar-right">
                    <svg width="10" height="10" viewBox="0 0 16 16" fill="currentColor">
                      <path d="M15.384 6.115a.485.485 0 0 0-.047-.736A12.444 12.444 0 0 0 8 3 12.44 12.44 0 0 0 .663 5.379a.485.485 0 0 0-.048.736l.518.518a.48.48 0 0 0 .634.027A10.457 10.457 0 0 1 8 4.768a10.457 10.457 0 0 1 6.233 1.892.48.48 0 0 0 .634-.027l.517-.518z"/>
                      <path d="M12.553 8.946a.486.486 0 0 0-.02-.705A8.455 8.455 0 0 0 8 6.556a8.453 8.453 0 0 0-4.533 1.685.486.486 0 0 0-.02.705l.518.518a.48.48 0 0 0 .647.018A6.47 6.47 0 0 1 8 8.136a6.47 6.47 0 0 1 3.388 1.346.48.48 0 0 0 .647-.018l.518-.518z"/>
                      <path d="M9.73 11.77a.486.486 0 0 0 .007-.677A4.475 4.475 0 0 0 8 10.106a4.475 4.475 0 0 0-1.737.987.486.486 0 0 0 .007.677l.518.518a.48.48 0 0 0 .673-.01A2.488 2.488 0 0 1 8 11.666a2.488 2.488 0 0 1 .539.613.48.48 0 0 0 .673.01l.518-.519zm-2.4 1.63a.5.5 0 0 0 0 .707l.67.67a.5.5 0 0 0 .707 0l.67-.67a.5.5 0 0 0 0-.707l-.67-.67a.5.5 0 0 0-.707 0l-.67.67z"/>
                    </svg>
                    <svg width="12" height="10" viewBox="0 0 16 16" fill="currentColor">
                      <rect x="1" y="4" width="12" height="8" rx="1" fill="none" stroke="currentColor" strokeWidth="1.5" />
                      <path d="M14 6h1v4h-1z" />
                      <rect x="3" y="6" width="8" height="4" fill="currentColor" />
                    </svg>
                    <span className="live-clock">{timeStr}</span>
                  </div>
                </div>

                {/* macOS Slide-in Notification Banner */}
                <AnimatePresence>
                  {showNotification && (
                    <motion.div 
                      initial={{ opacity: 0, x: 80, y: -10 }}
                      animate={{ opacity: 1, x: 0, y: 0 }}
                      exit={{ opacity: 0, x: 80 }}
                      transition={{ type: 'spring', damping: 15 }}
                      className="macos-notification"
                    >
                      <div className="notification-header">
                        <span className="notification-icon"></span>
                        <span className="notification-title">Terminal Guide</span>
                        <button 
                          className="notification-close-btn"
                          onClick={() => setShowNotification(false)}
                        >
                          ×
                        </button>
                      </div>
                      <div className="notification-message">
                        Tap any script icon on the desktop screen to execute the command.
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>

                {/* Desktop Interactive Shortcut Icons */}
                <div className="macos-desktop-shortcuts">
                  {desktopShortcuts.map((shortcut) => (
                    <button
                      key={shortcut.id}
                      onClick={() => handleCommandChange(shortcut.id)}
                      className={`desktop-icon-btn ${selectedCommand === shortcut.id ? 'active' : ''}`}
                      title={`Run ${shortcut.name}`}
                    >
                      <div className="desktop-icon-wrapper">
                        <span className="desktop-emoji-icon">{shortcut.icon}</span>
                      </div>
                      <span className="desktop-icon-label">{shortcut.name}</span>
                    </button>
                  ))}
                </div>

                {/* Floating macOS Terminal App Window */}
                <div className="macos-terminal-window" style={{ right: '140px', left: '16px' }}>
                  <div className="macos-terminal-header">
                    <div className="macos-dot-group">
                      <div className="macos-dot macos-red" />
                      <div className="macos-dot macos-yellow" />
                      <div className="macos-dot macos-green" />
                    </div>
                    <span className="macos-terminal-title">shubhams — cicd-cli — 80×24</span>
                  </div>
                  <div className="macos-terminal-body">
                    <AnimatePresence mode="popLayout">
                      {typedLines.map((line, idx) => (
                        <motion.div 
                          key={idx}
                          initial={{ opacity: 0, x: -2 }}
                          animate={{ opacity: 1, x: 0 }}
                          transition={{ duration: 0.08 }}
                          style={{ minHeight: '14px', marginBottom: '3px' }}
                          dangerouslySetInnerHTML={{ __html: line }}
                        />
                      ))}
                    </AnimatePresence>
                    <div className="cursor" style={{ height: '11px' }} />
                  </div>
                </div>

                {/* macOS Dock */}
                <div className="macos-dock-wrapper">
                  <div className="macos-dock">
                    {/* Finder */}
                    <div className="dock-item">
                      <svg className="dock-svg" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect width="64" height="64" rx="14" fill="url(#finderGrad)" />
                        <path d="M32 6C17.64 6 6 17.64 6 32c0 14.36 11.64 26 26 26c1.64 0 3.25-.15 4.81-.44V34.56H16.48v-4.88H36.8V6.44C35.25 6.15 33.64 6 32 6z" fill="#58A6FF" />
                        <path d="M32 6c14.36 0 26 11.64 26 26c0 14.36-11.64 26-26 26c-1.64 0-3.25-.15-4.81-.44V34.56h20.32v-4.88H27.2V6.44C28.75 6.15 30.36 6 32 6z" fill="#0A84FF" />
                        <path d="M22 28a2.5 2.5 0 100-5 2.5 2.5 0 000 5zm20 0a2.5 2.5 0 100-5 2.5 2.5 0 000 5z" fill="#1C1C1E" />
                        <path d="M27.2 6.44v23.24H36.8v4.88H16.48v9.42c2.4 4.54 6.74 7.84 11.89 8.84v-9.38h7.24a4.4 4.4 0 004.4-4.4v-4.52" stroke="#1C1C1E" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
                        <path d="M25 43.5c3.5 3.5 10.5 3.5 14 0" stroke="#1C1C1E" strokeWidth="3" strokeLinecap="round" />
                        <defs>
                          <linearGradient id="finderGrad" x1="0" y1="0" x2="64" y2="64" gradientUnits="userSpaceOnUse">
                            <stop stopColor="#E5F1FF" />
                            <stop offset="1" stopColor="#A8D1FF" />
                          </linearGradient>
                        </defs>
                      </svg>
                    </div>
                    {/* Safari */}
                    <div className="dock-item">
                      <svg className="dock-svg" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="32" cy="32" r="28" fill="url(#safariSky)" />
                        <circle cx="32" cy="32" r="23" stroke="rgba(255,255,255,0.4)" strokeWidth="1" />
                        <path d="M32 8A24 24 0 1056 32 24.03 24.03 0 0032 8zm0 45a21 21 0 1121-21 21.02 21.02 0 01-21 21z" fill="url(#safariRing)" />
                        <path d="M43.5 20.5l-16.2 8.7 4.7 4.7 11.5-13.4z" fill="#FF453A" />
                        <path d="M20.5 43.5l16.2-8.7-4.7-4.7-11.5 13.4z" fill="#F2F2F7" />
                        <circle cx="32" cy="32" r="2.5" fill="#FFD60A" />
                        <path d="M32 9v2M32 53v2M9 32h2M53 32h2" stroke="rgba(255,255,255,0.8)" strokeWidth="1.5" strokeLinecap="round" />
                        <defs>
                          <linearGradient id="safariSky" x1="0" y1="0" x2="64" y2="64" gradientUnits="userSpaceOnUse">
                            <stop stopColor="#0A84FF" />
                            <stop offset="1" stopColor="#0055B3" />
                          </linearGradient>
                          <linearGradient id="safariRing" x1="8" y1="8" x2="56" y2="56" gradientUnits="userSpaceOnUse">
                            <stop stopColor="#FFD60A" stopOpacity="0.8" />
                            <stop offset="1" stopColor="#FF453A" stopOpacity="0.8" />
                          </linearGradient>
                        </defs>
                      </svg>
                    </div>
                    {/* Settings */}
                    <div className="dock-item">
                      <svg className="dock-svg" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect width="64" height="64" rx="14" fill="url(#settingsMetal)" />
                        <circle cx="32" cy="32" r="14" fill="#8E8E93" stroke="#AEAEB2" strokeWidth="1.5" />
                        <circle cx="32" cy="32" r="8" fill="#D1D1D6" />
                        <path d="M32 14v4M32 46v4M14 32h4M46 32h4M19.3 19.3l2.8 2.8M41.9 41.9l2.8 2.8M19.3 44.7l2.8-2.8M41.9 22.1l2.8-2.8" stroke="#AEAEB2" strokeWidth="3" strokeLinecap="round" />
                        <circle cx="32" cy="32" r="4" fill="#505054" />
                        <defs>
                          <linearGradient id="settingsMetal" x1="0" y1="0" x2="0" y2="64" gradientUnits="userSpaceOnUse">
                            <stop stopColor="#F2F2F7" />
                            <stop offset="1" stopColor="#C7C7CC" />
                          </linearGradient>
                        </defs>
                      </svg>
                    </div>
                    {/* Terminal app */}
                    <div className="dock-item active">
                      <div className="terminal-dock-icon">
                        <span>&gt;_</span>
                      </div>
                      <div className="active-dot" />
                    </div>
                  </div>
                </div>
              </div>
              <div className="macbook-logo">macbook</div>
            </div>

            {/* Laptop lower base keyboard plate */}
            <div className="macbook-base">
              <div className="macbook-notch" />
              <div className="macbook-trackpad" />
            </div>
          </div>
        </div>
      </motion.div>
    </section>
  );
}
