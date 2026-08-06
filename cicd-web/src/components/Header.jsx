import React from 'react';
import { Sun, Moon, User } from 'lucide-react';

const GitHubIcon = ({ size = 20 }) => (
  <svg height={size} width={size} viewBox="0 0 16 16" fill="currentColor" xmlns="http://www.w3.org/2000/svg" style={{ display: 'inline-block', verticalAlign: 'middle' }}>
    <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"/>
  </svg>
);

export default function Header({ theme, toggleTheme }) {
  return (
    <header className="app-header">
      <div className="logo-box">
        <span className="logo-title" style={{ fontFamily: 'var(--font-mono)' }}>CI/CD CLI</span>
      </div>
      
      <div className="right-controls">
        {/* Developer Portfolio Link - Icon Only */}
        <a 
          href="https://shuvmraj.netlify.app/" 
          target="_blank" 
          rel="noreferrer"
          className="btn-theme"
          title="Visit developer portfolio"
        >
          <User size={18} />
        </a>

        {/* Theme Switcher Button - Icon Only */}
        <button 
          onClick={toggleTheme} 
          className="btn-theme"
          title={theme === 'dark' ? "Switch to Light Mode" : "Switch to Dark Mode"}
        >
          {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
        </button>

        {/* GitHub Link - Icon Only */}
        <a 
          href="https://github.com/shuvmraj/cicd-cli" 
          target="_blank" 
          rel="noreferrer"
          className="btn-theme"
          title="GitHub Repository"
        >
          <GitHubIcon size={18} />
        </a>
      </div>
    </header>
  );
}
