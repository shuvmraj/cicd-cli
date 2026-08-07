import React from 'react';

export default function Footer() {
  return (
    <footer className="app-footer">
      <span>© 2026 </span>
      <span>
        Designed & developed by{' '}
        <a 
          href="https://shuvmraj.netlify.app/" 
          target="_blank" 
          rel="noreferrer"
          style={{ fontWeight: 700, color: 'var(--color-cyan)' }}
        >
          shuvmraj
        </a>
      </span>
    </footer>
  );
}
