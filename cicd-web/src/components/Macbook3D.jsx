import React from 'react';
import { useGLTF, Html } from '@react-three/drei';

export default function Macbook3D({ children }) {
  const { nodes, materials } = useGLTF('/apple_macbook_pro.glb');

  return (
    <group dispose={null}>
      {/* 3D Macbook model mesh */}
      <mesh 
        geometry={nodes['macbookpro-material'].geometry} 
        material={materials.macbookpro} 
        rotation={[-Math.PI / 2, 0, 0]} 
        userData={{ name: 'macbookpro-material' }}
      />

      {/* HTML Portal screen projected on the 3D model bezel */}
      <Html
        transform
        occlude
        position={[0, 0.408, -0.428]}
        rotation={[-0.14, 0, 0]} // Fits screen's rear angle tilt
        distanceFactor={0.53}
        style={{
          width: '780px',
          height: '510px',
          background: '#040508',
          overflow: 'hidden'
        }}
      >
        {children}
      </Html>
    </group>
  );
}

useGLTF.preload('/apple_macbook_pro.glb');
