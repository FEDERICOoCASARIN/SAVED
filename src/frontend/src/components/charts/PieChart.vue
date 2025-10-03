<template>
  <div class="h-full w-full">
    <!-- Renders the Pie chart component using Vue Chart.js. -->
    <Pie :data="chartData" :options="mergedOptions" />
  </div>
</template>

<script>
import { Pie } from 'vue-chartjs'
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js'

// Register necessary components from Chart.js for the Pie chart.
ChartJS.register(ArcElement, Tooltip, Legend)

export default {
  name: 'PieChart', // Component name for identification in Vue DevTools
  components: {
    Pie // Registering the Pie component for use in this template
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
     * The labels for each segment of the pie chart.
     * @type {Array<string>}
     * @required
     */
    labels: {
      type: Array,
      required: true
    },
    /**
     * An array of colors to be used for the pie chart segments.
     * Defaults to a set of orange and red colors.
     * @type {Array<string>}
     */
    colors: {
      type: Array,
      default: () => ['#f28c0f', '#F25C54']
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
     * Computes the data object required by the Chart.js Pie component.
     * This includes labels and a dataset with data, background colors, and border width.
     * @returns {Object} Chart.js data object.
     */
    chartData() {
      return {
        labels: this.labels,
        datasets: [{
          data: this.data, // The actual data values for the segments
          backgroundColor: this.colors, // Colors for each segment
          borderWidth: 0 // No border between segments
        }]
      }
    },
    /**
     * Merges default chart options with any custom options provided via props.
     * Configures responsiveness and hides the legend.
     * @returns {Object} Merged Chart.js options object.
     */
    mergedOptions() {
      return {
        responsive: true, // Chart resizes with its container
        maintainAspectRatio: false, // Allows chart to resize freely without maintaining aspect ratio
        plugins: {
          legend: {
            display: false // Hide the legend as it's typically not needed when labels are clear
          }
        },
        ...this.options // Merge any additional options passed via props, overriding defaults if duplicated
      }
    }
  }
}
</script>
