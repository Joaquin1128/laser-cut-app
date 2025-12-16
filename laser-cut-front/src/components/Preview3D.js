import React, { useRef, Suspense } from 'react';
import { Canvas } from '@react-three/fiber';
import { OrbitControls, PerspectiveCamera } from '@react-three/drei';
import * as THREE from 'three';
import { SVGLoader } from 'three/examples/jsm/loaders/SVGLoader.js';
import { SUBTRACTION, Brush, Evaluator } from 'three-bvh-csg';
import './Preview3D.css';

// Componente para extruir el SVG a 3D
function ExtrudedShape({ svgString, thickness }) {
  const groupRef = useRef();

  React.useEffect(() => {
    if (!svgString || !groupRef.current) return;

    try {
      const loader = new SVGLoader();
      const svgData = loader.parse(svgString);

      // Limpiar el grupo anterior
      while (groupRef.current.children.length > 0) {
        groupRef.current.remove(groupRef.current.children[0]);
      }

      // Recopilar TODAS las formas con su información
      const allShapes = [];
      
      svgData.paths.forEach((path, pathIndex) => {
        try {
          const shapes = path.toShapes(true);
          shapes.forEach((shape) => {
            const points = shape.getPoints();
            if (points.length > 0) {
              // Calcular área y bounding box de esta forma
              let area = 0;
              let minX = Infinity, maxX = -Infinity;
              let minY = Infinity, maxY = -Infinity;
              
              for (let i = 0; i < points.length; i++) {
                const j = (i + 1) % points.length;
                area += points[i].x * points[j].y;
                area -= points[j].x * points[i].y;
                minX = Math.min(minX, points[i].x);
                maxX = Math.max(maxX, points[i].x);
                minY = Math.min(minY, points[i].y);
                maxY = Math.max(maxY, points[i].y);
              }
              
              allShapes.push({
                shape: shape,
                area: Math.abs(area / 2),
                minX, maxX, minY, maxY,
                centerX: (minX + maxX) / 2,
                centerY: (minY + maxY) / 2,
                pathIndex
              });
            }
          });
        } catch (err) {
          console.warn(`Error procesando path ${pathIndex}:`, err);
        }
      });

      if (allShapes.length === 0) {
        console.warn('No se encontraron formas válidas en el SVG');
        return;
      }

      // Ordenar por área (mayor a menor)
      allShapes.sort((a, b) => b.area - a.area);

      // La forma más grande es el exterior
      const exteriorShapeData = allShapes[0];

      // Calcular bounding box global
      const minX = Math.min(...allShapes.map(s => s.minX));
      const maxX = Math.max(...allShapes.map(s => s.maxX));
      const minY = Math.min(...allShapes.map(s => s.minY));
      const maxY = Math.max(...allShapes.map(s => s.maxY));

      const width = maxX - minX;
      const height = maxY - minY;
      const centerX = (minX + maxX) / 2;
      const centerY = (minY + maxY) / 2;
      const maxDim = Math.max(width, height);
      const scale = maxDim > 0 ? 80 / maxDim : 1;

      // Configuración de extrusión con mejor tessellation
      const depth = Math.max((thickness || 1) * scale * 0.15, 0.5);
      const extrudeSettings = {
        depth: depth,
        bevelEnabled: false,
        curveSegments: 32, // Más segmentos para círculos suaves
        steps: 1,
      };

      // Identificar agujeros: formas más pequeñas que están dentro del exterior
      const holes = [];
      const exteriorArea = exteriorShapeData.area;
      
      for (let i = 1; i < allShapes.length; i++) {
        const shapeData = allShapes[i];
        
        // Filtrar formas que son demasiado grandes (probablemente duplicados del exterior)
        // Solo considerar como agujero si el área es menos del 80% del exterior
        if (shapeData.area > exteriorArea * 0.8) {
          continue;
        }
        
        // Verificar si está dentro del exterior usando el centro de la forma
        const testPoint = { x: shapeData.centerX, y: shapeData.centerY };
        const exteriorPoints = exteriorShapeData.shape.getPoints();
        
        let inside = false;
        for (let j = 0, k = exteriorPoints.length - 1; j < exteriorPoints.length; k = j++) {
          const xi = exteriorPoints[j].x, yi = exteriorPoints[j].y;
          const xk = exteriorPoints[k].x, yk = exteriorPoints[k].y;
          const intersect = ((yi > testPoint.y) !== (yk > testPoint.y)) &&
            (testPoint.x < (xk - xi) * (testPoint.y - yi) / (yk - yi) + xi);
          if (intersect) inside = !inside;
        }
        
        if (inside) {
          holes.push(shapeData.shape);
        }
      }

      // Crear la forma final con agujeros usando ExtrudeGeometry
      try {
        const exteriorPoints = exteriorShapeData.shape.getPoints();
        const finalShape = new THREE.Shape(exteriorPoints);
        
        // Agregar cada agujero como Path con orientación opuesta
        holes.forEach((holeShape, index) => {
          const holePoints = holeShape.getPoints();
          
          // Calcular orientación del agujero
          let holeArea = 0;
          for (let i = 0; i < holePoints.length; i++) {
            const j = (i + 1) % holePoints.length;
            holeArea += holePoints[i].x * holePoints[j].y;
            holeArea -= holePoints[j].x * holePoints[i].y;
          }
          
          // Calcular orientación del exterior
          let exteriorArea = 0;
          for (let i = 0; i < exteriorPoints.length; i++) {
            const j = (i + 1) % exteriorPoints.length;
            exteriorArea += exteriorPoints[i].x * exteriorPoints[j].y;
            exteriorArea -= exteriorPoints[j].x * exteriorPoints[i].y;
          }
          
          // Si tienen la misma orientación (mismo signo), invertir el agujero
          const needsReverse = (holeArea * exteriorArea) > 0;
          const finalHolePoints = needsReverse ? [...holePoints].reverse() : holePoints;
          
          const holePath = new THREE.Path(finalHolePoints);
          finalShape.holes.push(holePath);
        });

        const geometry = new THREE.ExtrudeGeometry(finalShape, extrudeSettings);
        
        // Aplicar transformaciones
        geometry.translate(-centerX, -centerY, 0);
        geometry.scale(scale, scale, 1);
        geometry.rotateX(Math.PI);
        
        // Recalcular normales
        geometry.computeVertexNormals();
        
        const material = new THREE.MeshStandardMaterial({
          color: 0xe0e0e0,
          metalness: 0.1,
          roughness: 0.6,
          side: THREE.DoubleSide, // DoubleSide para ver agujeros de ambos lados
        });
        
        // Si hay agujeros, usar CSG para crearlos correctamente
        if (holes.length > 0) {
          try {
            // Crear brush base
            const baseBrush = new Brush(geometry);
            baseBrush.updateMatrixWorld();
            
            const evaluator = new Evaluator();
            let resultBrush = baseBrush;
            
            // Procesar agujeros
            holes.forEach((holeShape, index) => {
              const holePoints = holeShape.getPoints();
              const holeData = allShapes.find(s => s.shape === holeShape);
              
              if (!holeData || holePoints.length < 3) return;
              
              // Calcular radio promedio
              let totalRadius = 0;
              holePoints.forEach(point => {
                const dist = Math.sqrt(
                  Math.pow(point.x - holeData.centerX, 2) + 
                  Math.pow(point.y - holeData.centerY, 2)
                );
                totalRadius += dist;
              });
              const avgRadius = (totalRadius / holePoints.length) * scale;
              
              // Crear cilindro para restar (mucho más largo para asegurar que atraviese)
              // Más segmentos (32) para bordes suaves en los agujeros
              const cylGeo = new THREE.CylinderGeometry(avgRadius, avgRadius, depth * 3, 32);
              cylGeo.rotateX(Math.PI / 2);
              cylGeo.translate(
                (holeData.centerX - centerX) * scale,
                (holeData.centerY - centerY) * scale,
                0
              );
              cylGeo.rotateX(Math.PI);
              
              const holeBrush = new Brush(cylGeo);
              holeBrush.updateMatrixWorld();
              
              // Restar
              resultBrush = evaluator.evaluate(resultBrush, holeBrush, SUBTRACTION);
            });
            
            const finalGeometry = resultBrush.geometry;
            finalGeometry.computeVertexNormals();
            
            const finalMesh = new THREE.Mesh(finalGeometry, material);
            groupRef.current.add(finalMesh);
          } catch (err) {
            console.error('Error aplicando CSG a los agujeros:', err);
            const mesh = new THREE.Mesh(geometry, material);
            groupRef.current.add(mesh);
          }
        } else {
          const mesh = new THREE.Mesh(geometry, material);
          groupRef.current.add(mesh);
        }
      } catch (err) {
        console.error('Error creando geometría:', err);
      }

      // Centrar la cámara en el grupo
      if (groupRef.current.children.length > 0) {
        const box = new THREE.Box3().setFromObject(groupRef.current);
        const center = box.getCenter(new THREE.Vector3());
        groupRef.current.position.set(-center.x, -center.y, -center.z);
      }

    } catch (error) {
      console.error('Error procesando SVG:', error);
    }
  }, [svgString, thickness]);

  return <group ref={groupRef} />;
}

// Fallback si no hay geometría
function Fallback() {
  return (
    <mesh>
      <boxGeometry args={[20, 20, 5]} />
      <meshStandardMaterial color="#e0e0e0" />
    </mesh>
  );
}

// Componente principal del Canvas 3D
function Preview3D({ svgString, thickness }) {
  // Usar espesor por defecto de 3mm si no se especifica
  const thicknessValue = thickness ? parseFloat(thickness) : 3;
  const controlsRef = useRef();

  const handleZoomIn = () => {
    if (controlsRef.current) {
      const camera = controlsRef.current.object;
      camera.position.multiplyScalar(0.8);
      camera.updateProjectionMatrix();
    }
  };

  const handleZoomOut = () => {
    if (controlsRef.current) {
      const camera = controlsRef.current.object;
      camera.position.multiplyScalar(1.25);
      camera.updateProjectionMatrix();
    }
  };

  const handleResetView = () => {
    if (controlsRef.current) {
      const camera = controlsRef.current.object;
      camera.position.set(100, 80, 120);
      controlsRef.current.target.set(0, 0, 0);
      camera.updateProjectionMatrix();
      controlsRef.current.update();
    }
  };

  return (
    <div className="preview-3d-container">
      <Canvas>
        <ambientLight intensity={0.6} />
        <directionalLight position={[10, 10, 5]} intensity={0.8} castShadow />
        <directionalLight position={[-10, -10, -5]} intensity={0.3} />
        <pointLight position={[0, 0, 20]} intensity={0.5} />
        
        <PerspectiveCamera makeDefault position={[100, 80, 120]} fov={50} />
        
        <Suspense fallback={<Fallback />}>
          <ExtrudedShape svgString={svgString} thickness={thicknessValue} />
        </Suspense>
        
        <OrbitControls 
          ref={controlsRef}
          enablePan={true}
          enableZoom={true}
          enableRotate={true}
          minDistance={30}
          maxDistance={500}
          target={[0, 0, 0]}
        />
      </Canvas>
      
      <div className="preview-3d-zoom-controls">
        <button 
          className="zoom-control-btn" 
          onClick={handleZoomIn}
          title="Acercar"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="11" cy="11" r="8"/>
            <path d="m21 21-4.35-4.35"/>
            <line x1="11" y1="8" x2="11" y2="14"/>
            <line x1="8" y1="11" x2="14" y2="11"/>
          </svg>
        </button>
        <button 
          className="zoom-control-btn" 
          onClick={handleResetView}
          title="Restablecer vista"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            <polyline points="9 22 9 12 15 12 15 22"/>
          </svg>
        </button>
        <button 
          className="zoom-control-btn" 
          onClick={handleZoomOut}
          title="Alejar"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="11" cy="11" r="8"/>
            <path d="m21 21-4.35-4.35"/>
            <line x1="8" y1="11" x2="14" y2="11"/>
          </svg>
        </button>
      </div>
    </div>
  );
}

export default Preview3D;
