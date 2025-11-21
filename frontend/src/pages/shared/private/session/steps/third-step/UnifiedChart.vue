<script setup lang="ts">
import { computed, onMounted, ref, watch, nextTick } from 'vue';
import type { ECharts } from 'echarts/core';
import * as echarts from 'echarts/core';
import _ from 'lodash';
import type { Sensor } from 'src/common/models/sensor/Sensor';
import type { Measurement } from 'src/common/models/measurement/Measurement';
import { BlobDownloader } from 'src/common/utils/BlobUtils';
import { LineChart } from 'echarts/charts';
import {
  TooltipComponent,
  GridComponent,
  ToolboxComponent,
  DataZoomComponent,
  LegendComponent,
} from 'echarts/components';
import { CanvasRenderer, SVGRenderer } from 'echarts/renderers';
import { LegacyGridContainLabel } from 'echarts/features';

interface Props {
  sensors: Sensor[];
  allowedColumn: (keyof Measurement)[];
}

// 🧩 Props
const props = defineProps<Props>();

// ⚙️ ECharts setup
echarts.use([
  LineChart,
  TooltipComponent,
  GridComponent,
  LegacyGridContainLabel,
  ToolboxComponent,
  DataZoomComponent,
  LegendComponent,
  CanvasRenderer,
  SVGRenderer,
]);

const chartElement = ref<HTMLElement | null>(null);
const chartState = ref<ECharts>();
const options = ref();
const blob = ref(new BlobDownloader());
const sensors = computed(() => props.sensors ?? []);

const measurementCount = computed(() =>
  sensors.value.reduce((sum, s) => sum + (s.measurements?.length ?? 0), 0),
);

const hasMeasurements = computed(() => measurementCount.value > 0);

// ⚡ Atualiza o gráfico sempre que houver medições
watch(
  sensors,
  async () => {
    const count = sensors.value.reduce((sum, s) => sum + (s.measurements?.length ?? 0), 0);
    if (count > 0) {
      await nextTick();
      throttledSetOption();
    }
  },
  { deep: true },
);

onMounted(async () => {
  await nextTick();
  chartState.value = echarts.init(chartElement.value, 'infographic', {
    renderer: 'canvas',
  });
  throttledSetOption();
});

// ⏳ Atualização com throttling (500ms)
const throttledSetOption = _.throttle(setOption, 500);

// 📊 Função para exportar SVG
function exportSvg() {
  try {
    const svg = chartState.value?.renderToSVGString();
    blob.value.download(svg, 'chart-export', { type: 'image/svg+xml;' });
  } catch (e) {
    console.error(e);
  }
}

// 🧠 Cria as opções do gráfico
function setOption() {
  if (!chartState.value || !hasMeasurements.value) return;

  const series = [];
  const legendData = [];
  let xAxisData: string[] = [];
  let maxValue = 0;
  let minValue = 0;

  for (const sensor of sensors.value) {
    if (!sensor.measurements || sensor.measurements.length === 0) continue;

    if (xAxisData.length === 0) {
      xAxisData = sensor.measurements.map((_, i) => i.toString());
    }

    for (const col of props.allowedColumn) {
      const data = _.map(sensor.measurements, col);
      const numericData = _.map(sensor.measurements, col).map((val) => {
        if (typeof val === 'number') return val;
        if (typeof val === 'string') return Number.parseFloat(val) || 0;
        if (val && 'valueOf' in val && typeof val.valueOf === 'function') return val.valueOf();
        return 0;
      });

      const serieMax = Math.max(...numericData);
      const serieMin = Math.min(...numericData);

      // atualiza máximos e mínimos globais
      if (serieMax > maxValue) maxValue = serieMax;
      if (serieMin < minValue) minValue = serieMin;

      const serieName = `${sensor.sensorName}-${col}`;
      legendData.push(serieName);

      series.push({
        name: serieName,
        type: 'line',
        smooth: true,
        symbol: 'none',
        showSymbol: false,
        sampling: 'average',
        itemStyle: { color: `#${Math.floor(Math.random() * 0xffffff).toString(16)}` },
        data,
      });
    }
  }

  const newOptions = {
    legend: { data: legendData, top: 0 },
    tooltip: { trigger: 'axis' },
    grid: { left: '5%', right: '3%', top: '10%', bottom: '10%', containLabel: true },
    toolbox: { feature: { restore: {}, saveAsImage: {} } },
    dataZoom: [
      { type: 'inside', start: 0, end: 100 },
      { start: 0, end: 100 },
    ],
    xAxis: { type: 'category', boundaryGap: false, data: xAxisData },
    yAxis: {
      type: 'value',
      min: Math.floor(minValue * 0.9), // 10% abaixo do menor valor
      max: Math.ceil(maxValue * 1.1), // deixa 10% de folga acima do valor máximo
    },
    series,
  };

  options.value = _.merge(options.value, newOptions);

  chartState.value.clear();
  chartState.value.setOption(options.value, true);
  resize();
}

// 📐 Redimensiona ao mudar layout
function resize() {
  chartState.value?.resize();
}

defineExpose({
  exportSvg,
});
</script>

<template>
  <div ref="chartElement" id="chart-container" class="u-h-100 u-w-100 u-w-min-0 u-h-min-0">
    <div v-if="measurementCount == 0" class="no-data-container">
      <span class="text-weight-bold">Não há medições disponíveis</span>
    </div>
    <q-resize-observer @resize="resize" />
  </div>
</template>

<style scoped lang="scss">
.chart-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
  height: 100%;
  width: 100%;
  min-height: 0;
}

#chart-container {
  position: relative;
  flex: 1 1 auto;
  width: 100%;
  height: 100%;
  min-height: 70px;
  overflow: hidden;

  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: stretch;

  // deixa os gráficos se ajustar 100%
  canvas {
    width: 100% !important;
    height: 100% !important;
  }

  // força o wrapper do ECharts a preencher tudo
  > div:first-child {
    position: absolute !important;
    top: 0 !important;
    left: 0 !important;
    right: 0 !important;
    bottom: 0 !important;
    width: 100% !important;
    height: 100% !important;
    padding: 0 !important;
    margin: 0 !important;
  }
}

.no-data-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background: #fff;
  color: #666;
  font-weight: 600;
}
</style>
