<template>
  <!-- Renders the Bar chart component using Vue Chart.js. -->
  <Bar :data="chartData" :options="mergedOptions" />
</template>

<script>
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale
} from 'chart.js'

// Register necessary components from Chart.js for the Bar chart.
ChartJS.register(
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale
)

export default {
  name: 'BarChart', // Component name for debugging and identification in Vue DevTools
  components: {
    Bar // Registering the Bar component for use in this template
  },
  props: {
    /**
     * The data array for the chart's dataset.
     * @type {Array<number>}
     * @required
     */
    data: {
      type: Array,
      required: true
    },
    /**
     * An array of colors to be used for the chart's bars.
     * Defaults to a single orange color consistent with other charts in the application.
     * @type {Array<string>}
     */
    colors: {
      type: Array,
      default: () => ['#FA812F']
    },
    /**
     * The labels for the chart's X-axis (categories).
     * @type {Array<string>}
     * @required
     */
    labels: {
      type: Array,
      required: true
    },
    /**
     * Custom options object to extend or override default Chart.js options.
     * @type {Object}
     */
    options: {
      type: Object,
      default: () => ({}) // Default to an empty object if no options are provided
    }
  },
  computed: {
    /**
     * Computes the data object required by the Chart.js Bar component.
     * This includes labels and datasets with data and styling.
     * @returns {Object} Chart.js data object.
     */
    chartData() {
      return {
        labels: this.labels,
        datasets: [
          {
            label: 'Total Orders', // Label for the bar dataset
            data: this.data, // The actual data values for the bars
            backgroundColor: this.colors[0], // Use the first color from the colors prop for all bars
            borderRadius: 5, // Apply border radius for rounded bars
          }
        ]
      }
    },
    /**
     * Merges default chart options with any custom options provided via props.
     * Configures responsiveness, hides legend, and sets axis styles.
     * @returns {Object} Merged Chart.js options object.
     */
    mergedOptions() {
      return {
        responsive: true, // Chart resizes with its container
        maintainAspectRatio: false, // Allows chart to resize freely without maintaining aspect ratio
        plugins: {
          legend: {
            display: false, // Hide the legend as it's typically not needed for single-dataset bar charts
          }
        },
        scales: {
          y: {
            beginAtZero: true, // Y-axis starts at zero
            grid: {
              display: false // Hide Y-axis grid lines
            },
            border: {
              color: 'black' // Y-axis border color
            },
            ticks: {
              color: 'black' // Y-axis tick label colors
            }
          },
          x: {
            grid: {
              display: false // Hide X-axis grid lines
            },
            border: {
              color: 'black' // X-axis border color
            },
            ticks: {
              color: 'black' // X-axis tick label colors
            }
          }
        },
        ...this.options // Merge any additional options passed via props, overriding defaults if duplicated
      }
    }
  }
}
</script>