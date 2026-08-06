import React from 'react';
import { useGLTF, Html } from '@react-three/drei';

export default function Macbook3D({ children }) {
  const { nodes, materials } = useGLTF('/apple_macbook_pro.glb');

  return (
    <group dispose={null} scale={1.2}>
      {/* 3D Macbook model mesh */}
      <mesh 
        geometry={nodes['macbookpro-material'].geometry} 
        material={materials.macbookpro} 
        rotation={[-Math.PI / 2, 0, 0]} 
        userData={{ name: 'macbookpro-material' }}
      />

      {/* HTML Portal screen scaled and positioned to fit inside the model's display bezel */}
      <Html
        transform
        occlude
        position={[0, 0.225, -0.265]} // Moved down and forward to align with the bezel
        rotation={[-0.28, 0, 0]}      // Aligned with physical lid slant
        distanceFactor={0.315}        // Scaled down to fit within the physical border frame
        style={{
          width: '780px',
          height: '510px',
          background: '#040508',
          overflow: 'hidden',
          borderRadius: '4px'
        }}
      >
        {children}
      </Html>
    </group>
  );
}

useGLTF.preload('/apple_macbook_pro.glb');
