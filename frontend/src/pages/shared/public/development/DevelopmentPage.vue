<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls';

const canvasRef = ref<HTMLCanvasElement | null>(null);

let renderer: THREE.WebGLRenderer;
let scene: THREE.Scene;
let camera: THREE.PerspectiveCamera;
let controls: OrbitControls;
let skinnedMesh: THREE.SkinnedMesh;
let skeleton: THREE.Skeleton;
let animationId: number;
let clock: THREE.Clock;

// Referências dos bones para animação
let hip: THREE.Bone;
let spine: THREE.Bone;
let neck: THREE.Bone;
let head: THREE.Bone;
let leftUpperArm: THREE.Bone;
let leftForearm: THREE.Bone;
let rightUpperArm: THREE.Bone;
let rightForearm: THREE.Bone;
let leftUpperLeg: THREE.Bone;
let leftLowerLeg: THREE.Bone;
let rightUpperLeg: THREE.Bone;
let rightLowerLeg: THREE.Bone;

function createBone(name: string, length: number): THREE.Bone {
  const bone = new THREE.Bone();
  bone.name = name;
  bone.position.y = length;
  return bone;
}

function buildSkeleton(): THREE.Bone[] {
  const bones: THREE.Bone[] = [];

  // Raiz — quadril
  hip = new THREE.Bone();
  hip.name = 'hip';
  hip.position.set(0, 0, 0);
  bones.push(hip);

  // Coluna
  spine = createBone('spine', 1.2);
  hip.add(spine);
  bones.push(spine);

  // Pescoço
  neck = createBone('neck', 1.0);
  spine.add(neck);
  bones.push(neck);

  // Cabeça
  head = createBone('head', 0.6);
  neck.add(head);
  bones.push(head);

  // === Braço esquerdo ===
  leftUpperArm = new THREE.Bone();
  leftUpperArm.name = 'leftUpperArm';
  leftUpperArm.position.set(-0.8, 0.9, 0);
  spine.add(leftUpperArm);
  bones.push(leftUpperArm);

  leftForearm = createBone('leftForearm', -1.0);
  leftUpperArm.add(leftForearm);
  bones.push(leftForearm);

  const leftHand = createBone('leftHand', -0.8);
  leftForearm.add(leftHand);
  bones.push(leftHand);

  // === Braço direito ===
  rightUpperArm = new THREE.Bone();
  rightUpperArm.name = 'rightUpperArm';
  rightUpperArm.position.set(0.8, 0.9, 0);
  spine.add(rightUpperArm);
  bones.push(rightUpperArm);

  rightForearm = createBone('rightForearm', -1.0);
  rightUpperArm.add(rightForearm);
  bones.push(rightForearm);

  const rightHand = createBone('rightHand', -0.8);
  rightForearm.add(rightHand);
  bones.push(rightHand);

  // === Perna esquerda ===
  leftUpperLeg = new THREE.Bone();
  leftUpperLeg.name = 'leftUpperLeg';
  leftUpperLeg.position.set(-0.4, 0, 0);
  hip.add(leftUpperLeg);
  bones.push(leftUpperLeg);

  leftLowerLeg = createBone('leftLowerLeg', -1.4);
  leftUpperLeg.add(leftLowerLeg);
  bones.push(leftLowerLeg);

  const leftFoot = createBone('leftFoot', -1.2);
  leftLowerLeg.add(leftFoot);
  bones.push(leftFoot);

  // === Perna direita ===
  rightUpperLeg = new THREE.Bone();
  rightUpperLeg.name = 'rightUpperLeg';
  rightUpperLeg.position.set(0.4, 0, 0);
  hip.add(rightUpperLeg);
  bones.push(rightUpperLeg);

  rightLowerLeg = createBone('rightLowerLeg', -1.4);
  rightUpperLeg.add(rightLowerLeg);
  bones.push(rightLowerLeg);

  const rightFoot = createBone('rightFoot', -1.2);
  rightLowerLeg.add(rightFoot);
  bones.push(rightFoot);

  return bones;
}

function createSkinnedBody(bones: THREE.Bone[]): THREE.SkinnedMesh {
  // Geometria — cilindro vertical como corpo simplificado
  const segmentHeight = 1;
  const segmentCount = 8;
  const height = segmentHeight * segmentCount;
  const halfHeight = height / 2;

  const geometry = new THREE.CylinderGeometry(
    0.3, // raio topo
    0.4, // raio base
    height,
    8, // segmentos radiais
    segmentCount * 3, // segmentos de altura (mais = deformação mais suave)
  );

  // Atribuir skinIndex e skinWeight a cada vértice
  const positionAttr = geometry.attributes.position;
  const vertexCount = positionAttr.count;
  const skinIndices: number[] = [];
  const skinWeights: number[] = [];

  for (let i = 0; i < vertexCount; i++) {
    const y = positionAttr.getY(i);
    // Normalizar Y de [−halfHeight, +halfHeight] → [0, 1]
    const normalized = (y + halfHeight) / height;

    // Mapear para os primeiros 4 bones da coluna (hip, spine, neck, head)
    const boneFloat = normalized * 3;
    const boneIndex = Math.floor(boneFloat);
    const blendWeight = boneFloat - boneIndex;

    const idx0 = Math.min(boneIndex, 3);
    const idx1 = Math.min(boneIndex + 1, 3);

    skinIndices.push(idx0, idx1, 0, 0);
    skinWeights.push(1 - blendWeight, blendWeight, 0, 0);
  }

  geometry.setAttribute('skinIndex', new THREE.Uint16BufferAttribute(skinIndices, 4));
  geometry.setAttribute('skinWeight', new THREE.Float32BufferAttribute(skinWeights, 4));

  const material = new THREE.MeshPhongMaterial({
    color: 0x6b8cff,
    emissive: 0x1a1a3a,
    shininess: 40,
    transparent: true,
    opacity: 0.35,
    side: THREE.DoubleSide,
    wireframe: false,
  });

  const mesh = new THREE.SkinnedMesh(geometry, material);

  // Vincular bones ao mesh
  skeleton = new THREE.Skeleton(bones);
  const rootBone = bones[0];
  mesh.add(rootBone);
  mesh.bind(skeleton);

  // Posicionar o corpo
  mesh.position.y = 2;

  return mesh;
}

function initScene() {
  if (!canvasRef.value) return;

  clock = new THREE.Clock();

  // Cena
  scene = new THREE.Scene();

  // Câmera
  camera = new THREE.PerspectiveCamera(
    50,
    canvasRef.value.clientWidth / canvasRef.value.clientHeight,
    0.1,
    100,
  );
  camera.position.set(0, 3, 8);

  // Renderer
  renderer = new THREE.WebGLRenderer({
    canvas: canvasRef.value,
    antialias: true,
    alpha: true,
  });
  renderer.setSize(canvasRef.value.clientWidth, canvasRef.value.clientHeight);
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

  // Controles de órbita
  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  controls.dampingFactor = 0.08;
  controls.target.set(0, 2, 0);

  // Iluminação
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
  scene.add(ambientLight);

  const directionalLight = new THREE.DirectionalLight(0xffffff, 1.0);
  directionalLight.position.set(5, 10, 7);
  scene.add(directionalLight);

  // Grid no chão
  const gridHelper = new THREE.GridHelper(10, 10, 0x444466, 0x333355);
  scene.add(gridHelper);

  // Construir esqueleto
  const bones = buildSkeleton();

  // Criar mesh com skinning
  skinnedMesh = createSkinnedBody(bones);
  scene.add(skinnedMesh);

  // Helper visual dos ossos (linhas coloridas)
  const skeletonHelper = new THREE.SkeletonHelper(skinnedMesh);
  (skeletonHelper.material as THREE.LineBasicMaterial).linewidth = 2;
  scene.add(skeletonHelper);

  // Iniciar loop
  animate();
}

function animate() {
  animationId = requestAnimationFrame(animate);

  const t = clock.getElapsedTime();

  // Animação de "respiração" na coluna
  spine.rotation.z = Math.sin(t * 1.5) * 0.05;
  spine.rotation.x = Math.sin(t * 0.8) * 0.03;

  // Cabeça olhando ao redor
  head.rotation.y = Math.sin(t * 0.7) * 0.3;
  head.rotation.x = Math.sin(t * 1.1) * 0.1;

  // Balanço dos braços
  leftUpperArm.rotation.z = Math.sin(t * 2) * 0.4 - 0.2;
  leftForearm.rotation.z = Math.sin(t * 2 + 0.5) * 0.3 - 0.4;

  rightUpperArm.rotation.z = -Math.sin(t * 2) * 0.4 + 0.2;
  rightForearm.rotation.z = -Math.sin(t * 2 + 0.5) * 0.3 + 0.4;

  // Caminhada nas pernas
  leftUpperLeg.rotation.x = Math.sin(t * 2) * 0.3;
  leftLowerLeg.rotation.x = Math.max(0, Math.sin(t * 2 + 0.4)) * 0.5;

  rightUpperLeg.rotation.x = -Math.sin(t * 2) * 0.3;
  rightLowerLeg.rotation.x = Math.max(0, -Math.sin(t * 2 + 0.4)) * 0.5;

  // Leve balanço do quadril
  hip.rotation.y = Math.sin(t * 1.0) * 0.05;
  hip.position.y = Math.sin(t * 4) * 0.04;

  controls.update();
  renderer.render(scene, camera);
}

function onResize() {
  if (!canvasRef.value) return;
  const w = canvasRef.value.clientWidth;
  const h = canvasRef.value.clientHeight;
  camera.aspect = w / h;
  camera.updateProjectionMatrix();
  renderer.setSize(w, h);
}

onMounted(() => {
  initScene();
  window.addEventListener('resize', onResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize);
  cancelAnimationFrame(animationId);
  controls.dispose();
  renderer.dispose();
});
</script>

<template>
  <div class="skeleton-viewer">
    <canvas ref="canvasRef" />
    <div class="overlay-info">
      <span>Arraste para girar · Scroll para zoom</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
.skeleton-viewer {
  position: relative;
  width: 100%;
  height: 100vh;
  background: #0d0d1a;
  overflow: hidden;

  canvas {
    display: block;
    width: 100%;
    height: 100%;
  }
}

.overlay-info {
  position: absolute;
  bottom: 1.5rem;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 0.5rem 1.2rem;

  span {
    font-family: 'SF Mono', 'Fira Code', monospace;
    font-size: 0.75rem;
    color: rgba(255, 255, 255, 0.45);
    letter-spacing: 0.03em;
  }
}
</style>
