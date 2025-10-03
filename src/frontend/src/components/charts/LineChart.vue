<template>
    <!-- Pass the height prop directly to the Line component -->
    <Line :data="chartData" :options="mergedOptions" :height="height" />
</template>

<script>
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
)

export default{
    name: 'LineChart',
    components: {
        Line
    },
    props: {
        data: {
            type: Array,
            required: true
        },
        colors:{
            type: Array,
            default: () => ['#F68E62']
        },
        labels: {
            type: Array,
            required: true
        },
        options :{
            type: Object,
            required: true
        },
        // Add height prop to control the chart's canvas height directly
        height: {
            type: Number,
            default: 300 // A default height if not provided by the parent
        }
    },

    computed: {
        chartData() {
        console.log('LineChart: data prop received:', this.data);
        console.log('LineChart: labels prop received:', this.labels);
        console.log('LineChart: colors prop received:', this.colors);
        console.log('LineChart: height prop received:', this.height); // Log the received height
        return {
            labels: this.labels,
            datasets: [{
            label: 'Total Orders',
            data: this.data,
            borderColor: this.colors[0],
            tension: 0.4,
            fill: false,
            pointHoverBackgroundColor: 'black',
            pointRadius: 4
            }]
        }
    },
    mergedOptions() {
      return {
        responsive: true,
        maintainAspectRatio: false, // Keep this false so Chart.js doesn't force a 2:1 ratio
        plugins: {
          legend: {
            display: false
            },
            filler: {
                propagate: true
            }
        },
        scales: {
          y: {
            beginAtZero: true,
            grid: {
              display: false
            },
            border:{
                color: 'black'
            }
          },
          x: {
            grid: {
              display: false
            },
            border:{
                color: 'black'
            }
          }
        },
        ...this.options
      }
    }
  }
}
</script>
