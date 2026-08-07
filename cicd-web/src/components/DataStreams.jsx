import React, { useRef, useEffect } from 'react';

export default function DataStreams({ theme }) {
  const canvasRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    let animationFrameId;
    let width = (canvas.width = canvas.parentElement.clientWidth);
    let height = (canvas.height = canvas.parentElement.clientHeight);

    const columns = Math.floor(width / 24);
    const drops = Array(columns).fill(0);
    const charList = "01010110011010010110001101100100".split(""); // Binary representation of "cicd"

    const handleResize = () => {
      if (!canvas.parentElement) return;
      width = canvas.width = canvas.parentElement.clientWidth;
      height = canvas.height = canvas.parentElement.clientHeight;
    };
    window.addEventListener('resize', handleResize);

    const animate = () => {
      // Very faint clear to leave trails
      const isDark = theme === 'dark';
      ctx.fillStyle = isDark ? 'rgba(4, 5, 8, 0.1)' : 'rgba(255, 255, 255, 0.1)';
      ctx.fillRect(0, 0, width, height);

      ctx.fillStyle = isDark ? 'rgba(168, 85, 247, 0.05)' : 'rgba(0, 0, 0, 0.02)';
      ctx.font = '10px monospace';

      for (let i = 0; i < drops.length; i++) {
        const text = charList[Math.floor(Math.random() * charList.length)];
        const x = i * 24;
        const y = drops[i] * 12;

        ctx.fillText(text, x, y);

        // Reset drop
        if (y > height && Math.random() > 0.985) {
          drops[i] = 0;
        }

        drops[i]++;
      }

      animationFrameId = requestAnimationFrame(animate);
    };

    animate();

    return () => {
      cancelAnimationFrame(animationFrameId);
      window.removeEventListener('resize', handleResize);
    };
  }, [theme]);

  return (
    <canvas
      ref={canvasRef}
      style={{
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        zIndex: 0,
        pointerEvents: 'none',
      }}
    />
  );
}
