<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import type { ECharts } from 'echarts/core';
import * as echarts from 'echarts/core';
import _ from 'lodash';
import type { Sensor } from 'src/common/models/sensor/Sensor';
import type { Measurement } from 'src/common/models/measurement/Measurement';
import { BlobDownloader } from 'src/common/utils/BlobUtils';

interface Props {
  sensors: Sensor[];
  allowedColumn: (keyof Measurement)[];
}

// reference -> https://echarts.apache.org/
const defaultOptions = ref({
  toolbox: {
    feature: {
      restore: {},
      saveAsImage: {},
      // dataView: { readOnly: true },
      // magicType: { type: ['line', 'bar'] },
    },
  },
  grid: {
    left: '5%',
    top: '7%',
    right: '2%',
  },
  tooltip: {
    trigger: 'axis',
  },
  yAxis: {
    type: 'value',
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    axisLine: { onZero: true },
  },
  dataZoom: [
    {
      type: 'inside',
      start: 0,
      end: 10,
    },
    {
      start: 0,
      end: 10,
    },
  ],
});
const blob = ref(new BlobDownloader());
const options = ref();
const chartState = ref<ECharts | undefined>();
const chartVisualizer = ref<HTMLElement | null>(null);
const chartElement = ref<HTMLElement | null>(null);
const props = defineProps<Props>();
const count = computed(() => props.sensors.length);
const sensors = computed(() => props.sensors);

watch(count, () => {
  if (chartState.value && count.value > 0) {
    throttledSetOption();
  }
});

onMounted(() => {
  chartState.value = echarts.init(chartElement.value, 'infographic', {
    renderer: 'svg',
  });
});

const throttledSetOption = _.throttle(setOption, 500);

function exportSvg() {
  try {
    const svg = chartState.value?.renderToSVGString();
    blob.value.download(svg, 'test', { type: 'image/svg+xml;' });
    console.log(svg);
  } catch (e) {
    console.log(e);
  }
}

function setOption() {
  const series = [];
  for (const sensor of sensors.value) {
    if (sensor.measurements.length === 0) {
      continue;
    }

    for (const col of props.allowedColumn) {
      const data = _.map(sensor.measurements, col);
      const serie = {
        name: `${sensor.sensorName}-${col}`,
        type: 'line',
        symbol: 'none',
        sampling: 'average',
        itemStyle: {
          color: `#${Number.parseInt(String(Math.random() * 0xffffff)).toString(16)}`,
        },
        smooth: true,
        data,
      };
      series.push(serie);
    }
  }

  options.value = _.merge(options.value, defaultOptions.value, { series });
  chartState.value?.setOption(options.value);
}

function resize() {
  if (chartState.value && count.value > 0) {
    chartState.value.resize();
  }
}

// Expor a função exportSvg para uso externo
defineExpose({
  exportSvg,
});
</script>

<template>
  <div ref="chartVisualizer" class="h-100 w-100 p-relative overflow-auto">
    <div v-show="count > 0" class="h-100 w-100 z-index-1 chart-container">
      <div id="chartElement" ref="chartElement" class="h-100 w-100 chart" />
      <q-resize-observer @resize="resize" />
    </div>
    <div v-if="count == 0" class="p-absolute z-index-10 w-100 h-100 bg-white" style="top: 0">
      <div class="column div-lottie bg-white">
        <!--        <lottie-vue-player-->
        <!--          class="lottie"-->
        <!--          :src="'/lottie/chart-reports.json'"-->
        <!--          :autoplay="true"-->
        <!--          :loop="true"-->
        <!--          :speed="1"-->
        <!--        />-->
        <span class="text-weight-bold"> Não há dados a serem exibidos </span>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.chart-container {
  transition:
    width 1s ease-in-out,
    height 1s ease-in-out;
}
</style>
