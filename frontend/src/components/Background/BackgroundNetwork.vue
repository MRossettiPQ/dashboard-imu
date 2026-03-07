<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';

interface Node {
  x: number;
  y: number;
  baseX: number;
  baseY: number;
  r: number;
  // drift parameters (unique per node)
  speedX: number;
  speedY: number;
  phaseX: number;
  phaseY: number;
  range: number;
}

interface Edge {
  from: number;
  to: number;
}

const svgRef = ref<SVGSVGElement | null>(null);
let animId = 0;

// Define nodes (neurons) with base positions
const nodes: Node[] = [
  { baseX: 80, baseY: 120, r: 3 },
  { baseX: 240, baseY: 200, r: 4 },
  { baseX: 420, baseY: 140, r: 3.5 },
  { baseX: 580, baseY: 260, r: 4 },
  { baseX: 760, baseY: 180, r: 3 },
  { baseX: 940, baseY: 280, r: 4.5 },
  { baseX: 1100, baseY: 160, r: 3 },
  { baseX: 150, baseY: 400, r: 4 },
  { baseX: 320, baseY: 340, r: 3.5 },
  { baseX: 500, baseY: 420, r: 4 },
  { baseX: 680, baseY: 360, r: 3 },
  { baseX: 860, baseY: 440, r: 4.5 },
  { baseX: 1040, baseY: 380, r: 3.5 },
  { baseX: 100, baseY: 600, r: 3.5 },
  { baseX: 280, baseY: 540, r: 4 },
  { baseX: 460, baseY: 620, r: 3 },
  { baseX: 640, baseY: 560, r: 4.5 },
  { baseX: 820, baseY: 640, r: 3.5 },
  { baseX: 1000, baseY: 580, r: 4 },
  { baseX: 1140, baseY: 660, r: 3 },
].map((n) => ({
  ...n,
  x: n.baseX,
  y: n.baseY,
  speedX: 0.15 + Math.random() * 0.25,
  speedY: 0.15 + Math.random() * 0.25,
  phaseX: Math.random() * Math.PI * 2,
  phaseY: Math.random() * Math.PI * 2,
  range: 12 + Math.random() * 18,
}));

// Define edges (synapses) as index pairs
const edges: Edge[] = [
  // Horizontal layer 1
  { from: 0, to: 1 },
  { from: 1, to: 2 },
  { from: 2, to: 3 },
  { from: 3, to: 4 },
  { from: 4, to: 5 },
  { from: 5, to: 6 },
  // Horizontal layer 2
  { from: 7, to: 8 },
  { from: 8, to: 9 },
  { from: 9, to: 10 },
  { from: 10, to: 11 },
  { from: 11, to: 12 },
  // Horizontal layer 3
  { from: 13, to: 14 },
  { from: 14, to: 15 },
  { from: 15, to: 16 },
  { from: 16, to: 17 },
  { from: 17, to: 18 },
  { from: 18, to: 19 },
  // Cross-layer connections
  { from: 1, to: 8 },
  { from: 2, to: 9 },
  { from: 3, to: 10 },
  { from: 4, to: 11 },
  { from: 8, to: 14 },
  { from: 9, to: 15 },
  { from: 10, to: 16 },
  { from: 11, to: 17 },
  { from: 5, to: 12 },
  { from: 12, to: 18 },
  { from: 0, to: 7 },
  { from: 7, to: 13 },
  { from: 6, to: 12 },
];

// SVG element refs
const circleEls = ref<SVGCircleElement[]>([]);
const lineEls = ref<SVGLineElement[]>([]);

function animate(time: number) {
  const t = time * 0.001; // seconds

  // Update node positions
  for (const node of nodes) {
    node.x = node.baseX + Math.sin(t * node.speedX + node.phaseX) * node.range;
    node.y = node.baseY + Math.cos(t * node.speedY + node.phaseY) * node.range;
  }

  // Apply to DOM
  for (let i = 0; i < circleEls.value.length; i++) {
    const el = circleEls.value[i];
    const n = nodes[i];
    if (!el || !n) continue;
    el.setAttribute('cx', String(n.x));
    el.setAttribute('cy', String(n.y));
    // Pulse radius
    const pulse = n.r + Math.sin(t * 1.2 + n.phaseX) * 1.5;
    el.setAttribute('r', String(pulse));
    // Pulse opacity
    const op = 0.4 + Math.sin(t * 1.2 + n.phaseY) * 0.3;
    el.setAttribute('opacity', String(op));
  }

  for (let i = 0; i < lineEls.value.length; i++) {
    const el = lineEls.value[i];
    const edge = edges[i];
    if (!el || !edge) continue;
    const fromNode = nodes[edge.from];
    const toNode = nodes[edge.to];
    if (!fromNode || !toNode) continue;
    el.setAttribute('x1', String(fromNode.x));
    el.setAttribute('y1', String(fromNode.y));
    el.setAttribute('x2', String(toNode.x));
    el.setAttribute('y2', String(toNode.y));
  }

  animId = requestAnimationFrame(animate);
}

onMounted(() => {
  animId = requestAnimationFrame(animate);
});

onBeforeUnmount(() => {
  cancelAnimationFrame(animId);
});
</script>

<template>
  <svg
    ref="svgRef"
    class="neural-bg"
    viewBox="0 0 1200 800"
    preserveAspectRatio="xMidYMid slice"
    xmlns="http://www.w3.org/2000/svg"
  >
    <g class="neural-edges" stroke="currentColor" fill="none" stroke-width="0.6">
      <line
        v-for="(edge, i) in edges"
        :key="'e' + i"
        :ref="
          (el: unknown) => {
            if (el) lineEls[i] = el as unknown as SVGLineElement;
          }
        "
        :x1="nodes[edge.from]!.x"
        :y1="nodes[edge.from]!.y"
        :x2="nodes[edge.to]!.x"
        :y2="nodes[edge.to]!.y"
        class="edge"
        :class="'edge--' + ((i % 3) + 1)"
      />
    </g>
    <g class="neural-nodes" fill="currentColor">
      <circle
        v-for="(node, i) in nodes"
        :key="'n' + i"
        :ref="
          (el: unknown) => {
            if (el) circleEls[i] = el as unknown as SVGCircleElement;
          }
        "
        :cx="node.x"
        :cy="node.y"
        :r="node.r"
        opacity="0.5"
      />
    </g>
  </svg>
</template>

<style scoped lang="scss">
.neural-bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  color: $primary;
  opacity: 0.22;
}

.edge {
  stroke-dasharray: 6 4;
  animation: dash 30s linear infinite;

  &--1 {
    animation-duration: 28s;
    opacity: 0.6;
  }
  &--2 {
    animation-duration: 34s;
    opacity: 0.45;
    animation-direction: reverse;
  }
  &--3 {
    animation-duration: 40s;
    opacity: 0.55;
  }
}

@keyframes dash {
  to {
    stroke-dashoffset: -200;
  }
}
</style>
