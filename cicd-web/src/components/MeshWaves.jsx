import React, { useRef, useEffect } from 'react';

export default function MeshWaves({ theme }) {
  const canvasRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    let animationFrameId;
    let width = (canvas.width = canvas.parentElement.clientWidth);
    let height = (canvas.height = canvas.parentElement.clientHeight);

    let count = 0;
    const lineSpacing = 30;

    const handleResize = () => {
      if (!canvas.parentElement) return;
      width = canvas.width = canvas.parentElement.clientWidth;
      height = canvas.height = canvas.parentElement.clientHeight;
    };
    window.addEventListener('resize', handleResize);

    const animate = () => {
      ctx.clearRect(0, 0, width, height);

      const isDark = theme === 'dark';
      ctx.strokeStyle = isDark ? 'rgba(6, 182, 212, 0.03)' : 'rgba(0, 0, 0, 0.015)';
      ctx.lineWidth = 1;

      count += 0.005;

      // Draw horizontal wavy lines
      for (let y = 40; y < height; y += lineSpacing) {
        ctx.beginPath();
        for (let x = 0; x < width; x += 10) {
          // Calculate wave height using sine waves
          const angle = (x * 0.003) + count + (y * 0.005);
          const waveY = y + Math.sin(angle) * 12;
          
          if (x === 0) {
            ctx.moveTo(x, waveY);
          } else {
            ctx.lineTo(x, waveY);
          }
        }
        ctx.stroke();
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
