import React, { useRef, useEffect } from 'react';

/**
 * ReactBits-inspired interactive Canvas background (Grid + Particles with cursor distortion)
 */
export default function ReactBitsBg({ theme }) {
  const canvasRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    
    let animationFrameId;
    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = window.innerHeight);

    // Grid config
    const gridSize = 40;
    const points = [];
    const mouse = { x: null, y: null, radius: 140 };

    // Initialize grid points
    const initGrid = () => {
      points.length = 0;
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
      
      for (let x = 0; x < width + gridSize; x += gridSize) {
        for (let y = 0; y < height + gridSize; y += gridSize) {
          points.push({
            x,
            y,
            originalX: x,
            originalY: y,
            vx: 0,
            vy: 0,
          });
        }
      }
    };

    initGrid();

    // Event listeners
    const handleMouseMove = (e) => {
      mouse.x = e.clientX;
      mouse.y = e.clientY;
    };

    const handleMouseLeave = () => {
      mouse.x = null;
      mouse.y = null;
    };

    const handleResize = () => {
      initGrid();
    };

    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('mouseleave', handleMouseLeave);
    window.addEventListener('resize', handleResize);

    // Animation Loop
    const render = () => {
      // Clear canvas
      ctx.clearRect(0, 0, width, height);

      // Color scheme based on theme
      const isLight = theme === 'light';
      ctx.strokeStyle = isLight ? 'rgba(0, 0, 0, 0.03)' : 'rgba(255, 255, 255, 0.02)';
      ctx.fillStyle = isLight ? 'rgba(0, 242, 254, 0.2)' : 'rgba(0, 242, 254, 0.15)';

      // Draw Grid Lines and distort points
      points.forEach((p) => {
        if (mouse.x !== null && mouse.y !== null) {
          const dx = mouse.x - p.originalX;
          const dy = mouse.y - p.originalY;
          const dist = Math.sqrt(dx * dx + dy * dy);

          if (dist < mouse.radius) {
            const force = (mouse.radius - dist) / mouse.radius;
            // Distort away from mouse
            const angle = Math.atan2(dy, dx);
            const tx = p.originalX - Math.cos(angle) * force * 20;
            const ty = p.originalY - Math.sin(angle) * force * 20;

            // Ease to target distortion
            p.x += (tx - p.x) * 0.15;
            p.y += (ty - p.y) * 0.15;
          } else {
            // Return to original grid point
            p.x += (p.originalX - p.x) * 0.1;
            p.y += (p.originalY - p.y) * 0.1;
          }
        } else {
          // Return to original grid point
          p.x += (p.originalX - p.x) * 0.1;
          p.y += (p.originalY - p.y) * 0.1;
        }

        // Draw small dot at grid intersections
        ctx.beginPath();
        ctx.arc(p.x, p.y, 1, 0, Math.PI * 2);
        ctx.fill();
      });

      // Optional: draw glowing links around mouse
      if (mouse.x !== null && mouse.y !== null) {
        ctx.beginPath();
        ctx.arc(mouse.x, mouse.y, mouse.radius, 0, Math.PI * 2);
        ctx.fillStyle = isLight ? 'rgba(127, 0, 255, 0.005)' : 'rgba(127, 0, 255, 0.01)';
        ctx.fill();
      }

      animationFrameId = requestAnimationFrame(render);
    };

    render();

    // Clean up
    return () => {
      cancelAnimationFrame(animationFrameId);
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('mouseleave', handleMouseLeave);
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
