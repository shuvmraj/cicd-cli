import React, { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

const colors = {
  green: (text) => <span style={{ color: 'var(--color-green)' }}>{text}</span>,
  cyan: (text) => <span style={{ color: 'var(--color-cyan)' }}>{text}</span>,
  yellow: (text) => <span style={{ color: 'var(--color-orange)' }}>{text}</span>,
  red: (text) => <span style={{ color: 'var(--color-red)' }}>{text}</span>,
  bold: (text) => <span style={{ fontWeight: 700, color: '#fff' }}>{text}</span>,
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

// Desktop Icons vector descriptors representing custom Mac shortcut files
const desktopShortcuts = [
  { id: 'init', name: 'init.sh', icon: '⚙️' },
  { id: 'detect', name: 'detect.sh', icon: '🔍' },
  { id: 'generate', name: 'generate.sh', icon: '⚡' },
  { id: 'validate', name: 'validate.sh', icon: '🛡️' },
  { id: 'explain', name: 'explain.sh', icon: '📊' },
  { id: 'convert', name: 'convert.sh', icon: '🔄' },
  { id: 'doctor', name: 'doctor.sh', icon: '🩺' },
];

export default function TerminalPlayground() {
  const [selectedCommand, setSelectedCommand] = useState('detect');
  const [typedLines, setTypedLines] = useState([]);
  const [animPhase, setAnimPhase] = useState('closed');
  const [timeStr, setTimeStr] = useState('');
  const [showNotification, setShowNotification] = useState(false);
  
  const sectionRef = useRef(null);

  // Update menu bar clock dynamically with correct live date/time format
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

  // Trigger Apple MacBook intro sequence ONLY ONCE when the section scrolls into view
  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          // Immediately disconnect observer to prevent animation replay when scrolling back up
          observer.unobserve(entry.target);
          
          // Phase 1: Spin device to face forward (Y-axis 180 -> 0)
          const revTimer = setTimeout(() => {
            setAnimPhase('revolving');
          }, 300);

          // Phase 2: Open screen lid (X-axis -95 -> 0)
          const openTimer = setTimeout(() => {
            setAnimPhase('opening');
          }, 1500);

          // Phase 3: Terminal starts typing, notification pops up
          const readyTimer = setTimeout(() => {
            setAnimPhase('ready');
            setShowNotification(true);
          }, 2900);

          // Phase 4: Auto-dismiss help notification after 7 seconds
          const notifTimer = setTimeout(() => {
            setShowNotification(false);
          }, 9900);
        }
      },
      { threshold: 0.15 }
    );

    if (sectionRef.current) {
      observer.observe(sectionRef.current);
    }

    return () => {
      if (sectionRef.current) {
        observer.unobserve(sectionRef.current);
      }
    };
  }, []);

  // Types terminal command output only after laptop lid is open ('ready')
  useEffect(() => {
    if (animPhase !== 'ready') return;

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
  }, [selectedCommand, animPhase]);

  const handleCommandChange = (cmdId) => {
    setSelectedCommand(cmdId);
    setShowNotification(false);
  };

  const isLidOpen = animPhase === 'opening' || animPhase === 'ready';

  // Animate parameters using Framer Motion objects for CPU/GPU hardware accelerated 60fps rendering
  const deviceRotation = 
    animPhase === 'closed' ? { rotateY: 180, rotateX: 12 } :
    animPhase === 'revolving' ? { rotateY: 0, rotateX: 12 } :
    { rotateY: 0, rotateX: 0 };

  const lidRotation = isLidOpen ? { rotateX: 0 } : { rotateX: -95 };

  return (
    <section ref={sectionRef} id="terminal" className="terminal-section">
      <h2 className="section-title" style={{ marginBottom: '48px' }}>Command Reference Console</h2>
      
      <div className="terminal-playground-layout" style={{ justifyContent: 'center' }}>
        {/* 3D CSS MacBook Container - Centered to take full screen width */}
        <div className="macbook-wrapper" style={{ maxWidth: '1000px', margin: '0 auto' }}>
          <motion.div 
            className="macbook-device" 
            animate={deviceRotation}
            transition={{ duration: 1.6, ease: [0.16, 1, 0.3, 1] }}
          >
            {/* Display screen lid - Animate with Framer Motion for maximum smoothness */}
            <motion.div 
              className="macbook-lid"
              animate={lidRotation}
              transition={{ duration: 1.4, ease: [0.16, 1, 0.3, 1] }}
            >
              
              {/* Outer Lid cover (Apple outline logo) - visible when screen is closed */}
              {!isLidOpen && (
                <div className="macbook-lid-back">
                  <svg 
                    viewBox="0 0 170 170" 
                    className="apple-logo-svg"
                  >
                    <path d="M150.37 130.25c-2.45 5.66-5.35 10.87-8.71 15.66-4.58 6.53-8.33 11.05-11.22 13.56-4.48 4.12-9.28 6.23-14.42 6.35-3.69 0-8.14-1.05-13.32-3.18-5.19-2.12-9.97-3.17-14.34-3.17-4.58 0-9.49 1.05-14.75 3.17-5.26 2.13-9.5 3.24-12.74 3.35-4.39.13-9.13-1.92-14.21-6.14-3.1-2.61-6.95-7.23-11.56-13.86-5.07-7.24-9.28-15.86-12.62-25.86-3.35-10.02-5.03-19.88-5.03-29.59 0-14.83 3.82-26.62 11.47-35.38 7.64-8.75 17.13-13.19 28.46-13.31 5.92.12 11.72 1.83 17.41 5.16 5.69 3.33 10.05 5 13.08 5 2.5 0 6.64-1.54 12.44-4.63 7.07-3.76 13.79-5.54 20.17-5.35 15.17.62 26.68 6.44 34.52 17.47-13.19 8.01-19.68 18.99-19.46 32.96.22 10.66 4.16 19.5 11.84 26.54 7.67 7.03 16.7 10.74 27.09 11.13-2.11 6.09-4.8 12.18-8.07 18.25zm-22.37-97.77c0-7.39 2.65-14.4 7.96-21.03 6.68-8.13 15-12.56 24.96-13.28.1 1 .15 1.86.15 2.58 0 7.15-2.73 13.89-8.17 20.24-3.33 3.89-7.39 6.89-12.21 9.01-4.81 2.12-9.29 3.22-13.43 3.28-.79-.8-1.26-6.19-1.26-10.8z"/>
                  </svg>
                </div>
              )}

              {/* Inner macOS Desktop Display */}
              {isLidOpen && (
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
                      {/* Wifi icon */}
                      <svg width="10" height="10" viewBox="0 0 16 16" fill="currentColor">
                        <path d="M15.384 6.115a.485.485 0 0 0-.047-.736A12.444 12.444 0 0 0 8 3 12.44 12.44 0 0 0 .663 5.379a.485.485 0 0 0-.048.736l.518.518a.48.48 0 0 0 .634.027A10.457 10.457 0 0 1 8 4.768a10.457 10.457 0 0 1 6.233 1.892.48.48 0 0 0 .634-.027l.517-.518z"/>
                        <path d="M12.553 8.946a.486.486 0 0 0-.02-.705A8.455 8.455 0 0 0 8 6.556a8.453 8.453 0 0 0-4.533 1.685.486.486 0 0 0-.02.705l.518.518a.48.48 0 0 0 .647.018A6.47 6.47 0 0 1 8 8.136a6.47 6.47 0 0 1 3.388 1.346.48.48 0 0 0 .647-.018l.518-.518z"/>
                        <path d="M9.73 11.77a.486.486 0 0 0 .007-.677A4.475 4.475 0 0 0 8 10.106a4.475 4.475 0 0 0-1.737.987.486.486 0 0 0 .007.677l.518.518a.48.48 0 0 0 .673-.01A2.488 2.488 0 0 1 8 11.666a2.488 2.488 0 0 1 .539.613.48.48 0 0 0 .673.01l.518-.519zm-2.4 1.63a.5.5 0 0 0 0 .707l.67.67a.5.5 0 0 0 .707 0l.67-.67a.5.5 0 0 0 0-.707l-.67-.67a.5.5 0 0 0-.707 0l-.67.67z"/>
                      </svg>
                      {/* Battery icon */}
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

                  {/* Desktop Interactive Shortcut Icons (Right aligned, takes 0 outer layout space) */}
                  <div className="macos-desktop-shortcuts">
                    {desktopShortcuts.map((shortcut) => (
                      <button
                        key={shortcut.id}
                        onClick={() => handleCommandChange(shortcut.id)}
                        disabled={animPhase !== 'ready'}
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

                  {/* Floating macOS Terminal App Window (Left-aligned on desktop screen) */}
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
                          >
                            {line}
                          </motion.div>
                        ))}
                      </AnimatePresence>
                      <div className="cursor" style={{ height: '11px' }} />
                    </div>
                  </div>

                  {/* macOS Dock at bottom */}
                  <div className="macos-dock-wrapper">
                    <div className="macos-dock">
                      {/* Real Finder icon (High fidelity Vector) */}
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
                      {/* Real Safari compass icon */}
                      <div className="dock-item">
                        <svg className="dock-svg" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                          <circle cx="32" cy="32" r="28" fill="url(#safariSky)" />
                          <circle cx="32" cy="32" r="23" stroke="rgba(255,255,255,0.4)" strokeWidth="1" />
                          <path d="M32 8A24 24 0 1056 32 24.03 24.03 0 0032 8zm0 45a21 21 0 1121-21 21.02 21.02 0 01-21 21z" fill="url(#safariRing)" />
                          {/* Needle */}
                          <path d="M43.5 20.5l-16.2 8.7 4.7 4.7 11.5-13.4z" fill="#FF453A" />
                          <path d="M20.5 43.5l16.2-8.7-4.7-4.7-11.5 13.4z" fill="#F2F2F7" />
                          <circle cx="32" cy="32" r="2.5" fill="#FFD60A" />
                          {/* Dial markers */}
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
                      {/* Real System Preferences icon */}
                      <div className="dock-item">
                        <svg className="dock-svg" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                          <rect width="64" height="64" rx="14" fill="url(#settingsMetal)" />
                          <circle cx="32" cy="32" r="14" fill="#8E8E93" stroke="#AEAEB2" strokeWidth="1.5" />
                          {/* Inner gear */}
                          <circle cx="32" cy="32" r="8" fill="#D1D1D6" />
                          {/* Cog teeth */}
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
                      {/* Real Terminal App icon with active status dot */}
                      <div className="dock-item active">
                        <div className="terminal-dock-icon">
                          <span>&gt;_</span>
                        </div>
                        <div className="active-dot" />
                      </div>
                    </div>
                  </div>
                </div>
              )}
              
              <div className="macbook-logo">macbook</div>
            </motion.div>

            {/* Laptop lower base keyboard plate */}
            <div className="macbook-base">
              <div className="macbook-notch" />
              <div className="macbook-trackpad" />
            </div>
          </motion.div>
        </div>
      </div>
    </section>
  );
}
