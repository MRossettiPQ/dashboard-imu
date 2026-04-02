import * as THREE from 'three';

export function useMannequin() {
  let scene: THREE.Scene;
  let camera: THREE.PerspectiveCamera;
  let renderer: THREE.WebGLRenderer;
  let animationId: number;

  const joints: Record<string, THREE.Group> = {};

  // Controles de Órbita customizados baseados no código original
  class CustomOrbitControls {
    camera: THREE.PerspectiveCamera;
    target = new THREE.Vector3(0, 1.2, 0);
    spherical = new THREE.Spherical();
    sphericalDelta = new THREE.Spherical();
    scale = 1;
    panOffset = new THREE.Vector3();

    private rotateStart = new THREE.Vector2();
    private rotateEnd = new THREE.Vector2();
    private rotateDelta = new THREE.Vector2();
    private panStart = new THREE.Vector2();
    private isMouseDown = false;
    private isPanning = false;

    constructor(camera: THREE.PerspectiveCamera, domElement: HTMLElement) {
      this.camera = camera;
      const offset = new THREE.Vector3().copy(camera.position).sub(this.target);
      this.spherical.setFromVector3(offset);

      domElement.addEventListener('mousedown', (e) => {
        if (e.button === 0) { this.isMouseDown = true; this.rotateStart.set(e.clientX, e.clientY); }
        else if (e.button === 2) { this.isPanning = true; this.panStart.set(e.clientX, e.clientY); }
      });
      domElement.addEventListener('mousemove', (e) => {
        if (this.isMouseDown) {
          this.rotateEnd.set(e.clientX, e.clientY);
          this.rotateDelta.subVectors(this.rotateEnd, this.rotateStart);
          this.sphericalDelta.theta -= 2 * Math.PI * this.rotateDelta.x / domElement.clientHeight * 0.8;
          this.sphericalDelta.phi -= 2 * Math.PI * this.rotateDelta.y / domElement.clientHeight * 0.8;
          this.rotateStart.copy(this.rotateEnd);
        }
        if (this.isPanning) {
          const dx = (e.clientX - this.panStart.x) * 0.005;
          const dy = (e.clientY - this.panStart.y) * 0.005;
          const right = new THREE.Vector3().setFromMatrixColumn(this.camera.matrix, 0);
          const up = new THREE.Vector3().setFromMatrixColumn(this.camera.matrix, 1);
          this.panOffset.addScaledVector(right, -dx).addScaledVector(up, dy);
          this.panStart.set(e.clientX, e.clientY);
        }
      });
      domElement.addEventListener('mouseup', () => { this.isMouseDown = false; this.isPanning = false; });
      domElement.addEventListener('mouseleave', () => { this.isMouseDown = false; this.isPanning = false; });
      domElement.addEventListener('wheel', (e) => {
        e.preventDefault();
        this.scale *= e.deltaY > 0 ? 1.05 : 0.95;
      }, { passive: false });
      domElement.addEventListener('contextmenu', e => e.preventDefault());
    }

    update() {
      const offset = new THREE.Vector3().copy(this.camera.position).sub(this.target);
      this.spherical.setFromVector3(offset);
      this.spherical.theta += this.sphericalDelta.theta;
      this.spherical.phi += this.sphericalDelta.phi;
      this.spherical.phi = Math.max(0.1, Math.min(Math.PI - 0.1, this.spherical.phi));
      this.spherical.radius *= this.scale;
      this.spherical.radius = Math.max(2, Math.min(20, this.spherical.radius));
      this.target.add(this.panOffset);
      offset.setFromSpherical(this.spherical);
      this.camera.position.copy(this.target).add(offset);
      this.camera.lookAt(this.target);
      this.sphericalDelta.set(0, 0, 0);
      this.scale = 1;
      this.panOffset.set(0, 0, 0);
    }
  }

  let controls: CustomOrbitControls;

  // Materiais (Low Poly Mannequin)
  const jointMat = new THREE.MeshStandardMaterial({ color: 0x22d3ee, roughness: 0.3, emissive: 0x22d3ee, emissiveIntensity: 0.3, flatShading: true });
  const mats = {
    skin: new THREE.MeshStandardMaterial({ color: 0xf5deb3, roughness: 0.7, flatShading: true }),
    body: new THREE.MeshStandardMaterial({ color: 0x2d6a4f, roughness: 0.6, flatShading: true }),
    dark: new THREE.MeshStandardMaterial({ color: 0x1b4332, roughness: 0.5, flatShading: true }),
    shoe: new THREE.MeshStandardMaterial({ color: 0xf0f0f0, roughness: 0.4, flatShading: true }),
    hair: new THREE.MeshStandardMaterial({ color: 0x1a1a2e, roughness: 0.8, flatShading: true })
  };

  function makeBox(w: number, h: number, d: number, mat: THREE.Material) {
    const m = new THREE.Mesh(new THREE.BoxGeometry(w, h, d), mat);
    m.castShadow = true;
    return m;
  }

  function createJoint(name: string) {
    const group = new THREE.Group();
    group.name = name;
    joints[name] = group;
    return group;
  }

  function buildMannequin() {
    const root = createJoint('root');
    scene.add(root);

    // Torso / Spine / Head
    const hip = createJoint('hip'); hip.position.set(0, 1.0, 0); root.add(hip);
    const torsoLower = makeBox(0.5, 0.35, 0.25, mats.dark); torsoLower.position.set(0, 0.175, 0); hip.add(torsoLower);

    const spine = createJoint('spine'); spine.position.set(0, 0.35, 0); hip.add(spine);
    const torsoUpper = makeBox(0.55, 0.4, 0.28, mats.body); torsoUpper.position.set(0, 0.2, 0); spine.add(torsoUpper);

    const neck = createJoint('neck'); neck.position.set(0, 0.42, 0); spine.add(neck);
    const neckMesh = makeBox(0.12, 0.1, 0.12, mats.skin); neckMesh.position.set(0, 0.05, 0); neck.add(neckMesh);

    const head = createJoint('head'); head.position.set(0, 0.1, 0); neck.add(head);
    const headMesh = makeBox(0.35, 0.35, 0.35, mats.skin); headMesh.position.set(0, 0.2, 0); head.add(headMesh);
    const hairMesh = makeBox(0.36, 0.15, 0.36, mats.hair); hairMesh.position.set(0, 0.32, -0.01); head.add(hairMesh);

    // Olhos
    const eyeGeo = new THREE.BoxGeometry(0.06, 0.06, 0.02);
    const eyeMat = new THREE.MeshStandardMaterial({ color: 0x1a1a2e, flatShading: true });
    const eyeL = new THREE.Mesh(eyeGeo, eyeMat); eyeL.position.set(-0.08, 0.22, 0.176); head.add(eyeL);
    const eyeR = new THREE.Mesh(eyeGeo, eyeMat); eyeR.position.set(0.08, 0.22, 0.176); head.add(eyeR);

    // Helper para braços e pernas
    const buildLimb = (side: 'left' | 'right', isArm: boolean) => {
      const sign = side === 'left' ? -1 : 1;

      if (isArm) {
        const shoulder = createJoint(`${side}_shoulder`); shoulder.position.set(sign * 0.32, 0.38, 0); spine.add(shoulder);
        shoulder.add(new THREE.Mesh(new THREE.SphereGeometry(0.06, 6, 4), jointMat));
        const upper = makeBox(0.12, 0.3, 0.12, mats.body); upper.position.set(0, -0.15, 0); shoulder.add(upper);

        const elbow = createJoint(`${side}_elbow`); elbow.position.set(0, -0.3, 0); shoulder.add(elbow);
        elbow.add(new THREE.Mesh(new THREE.SphereGeometry(0.055, 6, 4), jointMat));
        const lower = makeBox(0.1, 0.28, 0.1, mats.body); lower.position.set(0, -0.14, 0); elbow.add(lower);

        const hand = makeBox(0.1, 0.1, 0.06, mats.skin); hand.position.set(0, -0.3, 0); elbow.add(hand);
      } else {
        const h = createJoint(`${side}_hip`); h.position.set(sign * 0.15, 0, 0); hip.add(h);
        h.add(new THREE.Mesh(new THREE.SphereGeometry(0.065, 6, 4), jointMat));
        const upper = makeBox(0.14, 0.35, 0.14, mats.dark); upper.position.set(0, -0.175, 0); h.add(upper);

        const knee = createJoint(`${side}_knee`); knee.position.set(0, -0.37, 0); h.add(knee);
        knee.add(new THREE.Mesh(new THREE.SphereGeometry(0.06, 6, 4), jointMat));
        const lower = makeBox(0.12, 0.35, 0.12, mats.dark); lower.position.set(0, -0.175, 0); knee.add(lower);

        const ankle = createJoint(`${side}_ankle`); ankle.position.set(0, -0.37, 0); knee.add(ankle);
        const foot = makeBox(0.14, 0.08, 0.22, mats.shoe); foot.position.set(0, -0.04, 0.04); ankle.add(foot);
      }
    };

    buildLimb('left', true); buildLimb('right', true);
    buildLimb('left', false); buildLimb('right', false);
  }

  function init(container: HTMLElement) {
    scene = new THREE.Scene();
    scene.background = new THREE.Color(0x0a0e17);
    scene.fog = new THREE.FogExp2(0x0a0e17, 0.04);

    camera = new THREE.PerspectiveCamera(50, container.clientWidth / container.clientHeight, 0.1, 100);
    camera.position.set(3, 3, 5);

    renderer = new THREE.WebGLRenderer({ antialias: true });
    renderer.setSize(container.clientWidth, container.clientHeight);
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    container.appendChild(renderer.domElement);

    controls = new CustomOrbitControls(camera, renderer.domElement);

    // Luzes
    scene.add(new THREE.AmbientLight(0x334466, 0.6));
    const dirLight = new THREE.DirectionalLight(0xffffff, 0.9);
    dirLight.position.set(5, 8, 5);
    dirLight.castShadow = true;
    dirLight.shadow.mapSize.set(1024, 1024);
    scene.add(dirLight);

    const fillLight = new THREE.DirectionalLight(0x22d3ee, 0.3); fillLight.position.set(-3, 2, -2); scene.add(fillLight);
    const rimLight = new THREE.DirectionalLight(0xa78bfa, 0.25); rimLight.position.set(0, 3, -5); scene.add(rimLight);

    // Chão e Grid
    const ground = new THREE.Mesh(new THREE.PlaneGeometry(30, 30), new THREE.MeshStandardMaterial({ color: 0x0d1117, roughness: 0.9 }));
    ground.rotation.x = -Math.PI / 2; ground.receiveShadow = true; scene.add(ground);
    scene.add(new THREE.GridHelper(20, 40, 0x1a2332, 0x131b28));

    buildMannequin();

    const animate = () => {
      animationId = requestAnimationFrame(animate);
      controls.update();
      jointMat.emissiveIntensity = 0.2 + Math.sin(performance.now() * 0.002) * 0.15; // Pulso dos joints
      renderer.render(scene, camera);
    };
    animate();
  }

  function resize(width: number, height: number) {
    if (!camera || !renderer) return;
    camera.aspect = width / height;
    camera.updateProjectionMatrix();
    renderer.setSize(width, height);
  }

  function setJointRotation(name: string, rx: number, ry: number, rz: number) {
    if (joints[name]) {
      joints[name].rotation.set(
        THREE.MathUtils.degToRad(rx),
        THREE.MathUtils.degToRad(ry),
        THREE.MathUtils.degToRad(rz)
      );
    }
  }

  function destroy() {
    if (animationId) cancelAnimationFrame(animationId);
    renderer?.dispose();
  }

  return { init, resize, setJointRotation, destroy };
}
