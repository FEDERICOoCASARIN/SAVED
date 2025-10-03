<template>
  <transition name="fade-slide" appear>
    <div class="flex flex-col">
        <div class="flex mb-5 justify-between px-10 pt-5 mt-8">
          <div class="flex items-center space-x-3">
            <div class="p-2 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl shadow-lg">
              <AnalyticsSVG class="size-6 text-white" />
            </div>
            <h1 class="text-4xl font-bold">Analytics</h1>
					</div>

          <div class="flex gap-3">
            <div class="flex flex-col">
              <!-- Warning Message -->
              <div v-if="dateError" class="text-red-600 text-sm mb-2 font-semibold">
                {{ dateErrorMessage }}
              </div>

              <div class="flex justify-center">
                <!-- Company Filter Dropdown and Date Filters. -->
                <div class="flex gap-2 border rounded-lg p-2 transition-all duration-300"
                  :class="{ 'border-red-500': dateError, 'border-gray-200': !dateError }">
                  <FilterDropdown v-model="companyFilter" :options="companyList" title="Company" class="uppercase" />
                  <div class="flex gap-3 items-center">
                    <label class="text-sm">
                      <input type="date" v-model="startDate"
                        class="border rounded-md px-2 py-2 mr-1 transition duration-300 ease-in-out focus:outline-none focus:ring-2 focus:ring-orange-400"
                        :class="{ 'border-red-500': dateError }" />
                    </label>
                    <span class="text-gray-600 mr-1">to</span>
                    <label class="text-sm">
                      <input type="date" :max="maxDate" v-model="endDate" class="border rounded-md px-2 py-2"
                        :class="{ 'border-red-500': dateError }" />
                    </label>
                  </div>
                  <button @click="applyFilters"
                    class="bg-[#FA812F] hover:bg-orange-500 rounded-lg px-3 py-1 text-white cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                    :disabled="!hasFiltersApplied || dateError"
                    :class="{ 'border-red-500 ring-2 ring-red-500': dateError }">
                    <span>Apply</span>
                  </button>
                  <!-- Clear Filters Button -->
                  <button v-if="isFilterActive" @click="clearFilters"
                    class="rounded-lg text-red-500 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                    :disabled="!isFilterActive">
                    <span>Clear</span>
                  </button>
                </div>
              </div>
          </div>
        </div>
      </div>

      <!-- Area 1: Filtered Analytics (Order Overview, Shipment Overview, Container Utilization) -->
      <div class="rounded-lg mx-10">
        <div class="flex flex-col gap-4">
          <!-- Order Overview Section (Card 1): Displays key metrics related to orders. -->
          <transition name="slide-up-ease-in" appear>
            <div v-if="showSection1" class="flex flex-col rounded-lg p-4 bg-[#f4f4f4]">
              <div class="flex justify-between items-center mb-5 px-3">
                <div class="text-[#f28c0f] font-bold text-4xl">
                  <h1>Order Overview</h1>
                </div>
              </div>
              <div class="flex flex-1 rounded-lg flex-row gap-4">
                <!-- Total Orders Card -->
                <div class="flex-1 flex rounded-xl p-2 flex-col bg-white shadow-md">
                  <div class="flex">
                    <h1 class="ml-3 text-2xl mt-2 font-semibold">
                      Total Orders
                    </h1>
                  </div>
                  <div class="flex-1 text-[5rem] flex items-end ml-2">
                    <AnimatedNumber :value="totalOrders" />
                  </div>
                </div>
                <!-- Completed Orders Card -->
                <div class="flex-1 flex rounded-xl p-2 flex-col bg-white shadow-md">
                  <div class="flex">
                    <h1 class="ml-3 text-2xl mt-2 font-semibold">
                      Completed Orders
                    </h1>
                  </div>
                  <div class="flex-1 text-[5rem] flex items-end ml-2">
                    <AnimatedNumber :value="completedOrders" />
                  </div>
                </div>
                <!-- Completion Rate Card -->
                <div class="flex-1 flex rounded-xl p-2 flex-col bg-white shadow-md">
                  <div class="flex">
                    <h1 class="ml-3 text-2xl mt-2 font-semibold">
                      Completion rate
                    </h1>
                  </div>
                  <div class="flex-1 text-[5rem] flex items-end ml-2">
                    <AnimatedNumber :value="this.completionRate" :fixed="1" />
                    <div class="text-xl ml-1 mb-4"> %</div>
                  </div>
                </div>
                <!-- Order Volume Card with Period Toggle -->
                <div class="flex-1 flex rounded-xl p-2 flex-col bg-white shadow-md">
                  <div class="flex">
                    <div class="flex">
                      <h1 class="ml-3 text-2xl mt-2 font-semibold">
                        Order Volume
                      </h1>
                    </div>
                    <div class="flex-1 flex">
                      <div
                        class="relative flex bg-[#EFEEF5] rounded-xl ml-auto mt-2 items-center gap-1 overflow-hidden mx-3">
                        <div
                          class="absolute top-0 bottom-0 w-1/3 bg-[#FA812F] rounded-xl transition-left duration-300 ease-in-out"
                          :style="{ left: sliderOrderVolumePosition }"></div>

                        <!-- Period Selection Buttons (Month, Week, Day) -->
                        <button
                          class="flex-1 flex justify-center rounded-xl p-2 font-bold cursor-pointer z-10 text-center select-none"
                          :class="selectedOrderVolumeView === 'M' ? 'text-white' : 'text-gray-700'"
                          @click="selectedOrderVolumeView = 'M'">
                          M
                        </button>
                        <button
                          class="flex-1 flex justify-center rounded-xl p-2 font-bold cursor-pointer z-10 text-center select-none"
                          :class="selectedOrderVolumeView === 'W' ? 'text-white' : 'text-gray-700'"
                          @click="selectedOrderVolumeView = 'W'">
                          W
                        </button>
                        <button
                          class="flex-1 flex justify-center rounded-xl p-2 font-bold cursor-pointer z-10 text-center select-none"
                          :class="selectedOrderVolumeView === 'D' ? 'text-white' : 'text-gray-700'"
                          @click="selectedOrderVolumeView = 'D'">
                          D
                        </button>
                      </div>
                    </div>
                  </div>
                  <div class="flex flex-1 flex-row items-end text-[5rem] ml-2">
                    <AnimatedNumber :value="orderVolume" />
                    <div v-if="selectedOrderVolumeView === 'M'" class="text-xl ml-1 mb-7">/month</div>
                    <div v-else-if="selectedOrderVolumeView === 'W'" class="text-xl ml-1 mb-7">/week</div>
                    <div v-else-if="selectedOrderVolumeView === 'D'" class="text-xl ml-1 mb-7">/day</div>
                  </div>
                </div>
              </div>
              <!-- Order Bar Chart -->
              <div class="flex flex-col rounded-lg p-4 bg-white shadow-md mb-4 mt-4">
                <div class="flex">
                  <div class="ml-3 text-2xl mt-2 font-semibold mb-4">
                    <h1>Overall Orders</h1>
                  </div>
                  <div class="flex-1 flex mb-4">
                    <div
                      class="relative flex bg-[#EFEEF5] rounded-xl ml-auto mt-2 items-center gap-1 overflow-hidden mx-3">
                      <div
                        class="absolute top-0 bottom-0 w-1/3 bg-[#FA812F] rounded-xl transition-left duration-300 ease-in-out"
                        :style="{ left: sliderChartPosition }"></div>

                      <!-- Period Selection Buttons (Month, Week, Day) -->
                      <button
                        class="flex-1 flex justify-center rounded-xl p-2 font-bold cursor-pointer z-10 text-center select-none"
                        :class="selectedChartView === 'D' ? 'text-white' : 'text-gray-700'"
                        @click="selectedChartView = 'D'">
                        D
                      </button>
                      <button
                        class="flex-1 flex justify-center rounded-xl p-2 font-bold cursor-pointer z-10 text-center select-none"
                        :class="selectedChartView === 'M' ? 'text-white' : 'text-gray-700'"
                        @click="selectedChartView = 'M'">
                        M
                      </button>
                      <button
                        class="flex-1 flex justify-center rounded-xl p-2 font-bold cursor-pointer z-10 text-center select-none"
                        :class="selectedChartView === 'Y' ? 'text-white' : 'text-gray-700'"
                        @click="selectedChartView = 'Y'">
                        Y
                      </button>
                    </div>
                  </div>
                </div>

                <div class="flex-1 h-[300px]">
                  <div class="flex-1 p-4">
                    <div v-if="OrdersChartData && OrdersChartData.length > 0" class="h-full">
                      <BarChart :chart-id="'orders-chart'" :data="OrdersChartData" :labels="OrdersChartLabels"
                        :colors="OrdersChartColors" :options="{ height: 300 }" :height="300" />
                    </div>
                    <div v-else class="flex items-center justify-center h-full text-gray-500 text-xl">
                      No data yet to display for the selected period.
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </transition>

          <!-- Shipment Overview (Card 2) & Container Utilization (Card 3) Sections -->
          <transition name="slide-up-ease-in" appear>
            <div v-if="showSection2" class="flex flex-row gap-4">
              <!-- Shipment Overview Card -->
              <div class="flex flex-col bg-[#f4f4f4] rounded-xl p-4 w-[690px]">
                <div class="flex text-[#f28c0f] font-bold text-4xl p-4">
                  <h1>Shipment Overview</h1>
                </div>
                <div class="flex-1">
                  <div class="grid grid-cols-2 grid-rows-2 gap-4">
                    <!-- Total Trip Card -->
                    <div class="bg-white rounded-xl p-4 shadow-md flex flex-col h-[220px]">
                      <h1 class="text-2xl font-semibold">Total Trip</h1>
                      <div class="flex-1 text-[4rem] flex items-end">
                        <AnimatedNumber :value="totalTrips" />
                      </div>
                    </div>

                    <!-- Distance Traveled Card -->
                    <div class="bg-white rounded-xl p-4 shadow-md flex flex-col h-[220px]">
                      <h1 class="text-2xl font-semibold">Distance Traveled</h1>
                      <div class="flex-1 text-[4rem] flex items-end">
                        <div>
                          <AnimatedNumber :value="distanceTraveled" :fixed="1" />
                        </div>
                        <div class="text-xl ml-1 mb-4"> km</div>
                      </div>
                    </div>

                    <!-- Traveling Hours Card -->
                    <div class="bg-white flex-1 rounded-xl p-4 shadow-md flex flex-col h-[240px]">
                      <h1 class="text-2xl font-semibold">Traveling Hours</h1>
                      <div class="flex-1 text-[4rem] flex items-end">
                        <div>
                          <AnimatedNumber :value="travelingHours" />
                        </div>
                        <div class="text-xl ml-1 mb-4"> hour(s)</div>
                      </div>
                    </div>

                    <!-- Average Waiting Time Card -->
                    <div class="bg-white rounded-xl p-4 shadow-md flex flex-col h-[240px]">
                      <h1 class="text-2xl font-semibold">Average Waiting Time</h1>
                      <div class="flex-1 text-[4rem] flex items-end">
                        <div>
                          <AnimatedNumber :value="avgWaitingTime" />
                        </div>
                        <div class="text-xl ml-1 mb-4"> minute(s)</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Container Utilization Card -->
              <div class="flex-1 flex flex-col bg-[#f4f4f4] rounded-xl p-4">
                <div class="flex text-[#f28c0f] font-bold text-4xl p-4">
                  <h1>Container Utilization</h1>
                </div>
                <div class="flex-1 flex flex-col">
                  <div class="flex flex-1 flex-col gap-4">
                    <div class="flex flex-row gap-4">
                      <!-- Average Weight per Shipment Card -->
                      <div class="flex-1 flex rounded-xl p-2 flex-col bg-white shadow-md">
                        <div class="flex">
                          <h1 class="ml-3 text-2xl mt-2 font-semibold">
                            Average Weight
                          </h1>
                        </div>
                        <div class="flex-1 text-[4rem] flex items-end ml-2">
                          <AnimatedNumber :value="avgWeight" :fixed="1" />
                          <div class="text-xl ml-1 mb-4"> kg</div>
                        </div>
                      </div>
                      <!-- Shared Containers Card -->
                      <div class="flex-1 flex rounded-xl p-2 flex-col bg-white shadow-md">
                        <div class="flex">
                          <h1 class="ml-3 text-2xl mt-2 font-semibold">
                            Shared Containers
                          </h1>
                        </div>
                        <div class="flex-1 text-[4rem] flex items-end ml-2">
                          <AnimatedNumber :value="sharedContainers" />
                        </div>
                      </div>
                    </div>

                    <!-- Freight Type Composition Card with Pie Chart and Legend -->
                    <div class="flex flex-1">
                      <div class="flex-1 flex rounded-xl p-2 flex-col bg-white shadow-md">
                        <h1 class="ml-3 mt-2 text-2xl font-semibold mb-4">Freight Type</h1>
                        <div class="flex-1 flex p-4 max-h-200px">
                          <div class="w-[180px] h-[180px]">
                            <PieChart :data="freightTypeChartData" :labels="freightTypeChartLabels"
                              :colors="freightTypeChartColors" :options="{}" />
                          </div>
                          <!-- Freight Type Legend: Displays color-coded labels and data in two columns. -->
                          <div class="ml-8 grid grid-cols-2 gap-x-8">
                            <div
                              v-for="(label, index) in ['Machinery', 'Food', 'Chemical', 'Textile', 'Electronic', 'Other']"
                              :key="label" class="flex items-center">
                              <span class="w-4 h-4 rounded-full mr-2"
                                :style="{ backgroundColor: freightTypeOriginalColors[index] }"></span>
                              <span class="leading-tight whitespace-nowrap text-lg">{{ label }}: {{
                                freightTypeData[index] }}</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </transition>
        </div>
      </div>

      <!-- Area 2: Vehicle Overview (Unfiltered) -->
      <div class="rounded-lg mt-4 mx-10 md-10">
        <transition name="slide-up-ease-in" appear>
          <div v-if="showSection3" class="flex flex-col rounded-lg p-4 bg-[#f4f4f4]">
            <div class="flex justify-between items-center mb-5 px-3">
              <div class="text-[#f28c0f] font-bold text-4xl">
                <h1>Vehicle Overview</h1>
              </div>
            </div>

            <div class="flex flex-1 rounded-lg flex-row gap-4">
              <!-- Availability Rate Card -->
              <div class="flex rounded-xl p-2 flex-col bg-white shadow-md w-[335px]">
                <div class="flex">
                  <h1 class="ml-3 text-3xl mt-2 font-semibold">
                    Availability Rate
                  </h1>
                </div>
                <div class="flex-1 text-[6rem] flex items-end ml-2">
                  <div>
                    <AnimatedNumber :value="availabilityRate" />
                  </div>
                  <div class="text-xl ml-1 mb-4"> %</div>
                </div>
              </div>
              <!-- Total Vehicles Card -->
              <div class=" flex rounded-xl p-2 flex-col bg-white shadow-md w-[335px]">
                <div class="flex">
                  <h1 class="ml-3 text-3xl mt-2 font-semibold">
                    Total Vehicle
                  </h1>
                </div>
                <div class="flex-1 text-[6rem] flex items-end ml-2">
                  <AnimatedNumber :value="totalVehicles" />
                </div>
              </div>
              <!-- Vehicle Status Pie Chart and Legend -->
              <div class="flex-1 flex rounded-xl p-2 flex-col bg-white shadow-md">
                <h1 class="ml-3 text-3xl mt-2 font-semibold mb-5">
                  Vehicle Status
                </h1>
                <div class="flex-1 flex p-4">
                  <div class="w-[180px] h-[180px]">
                    <PieChart :data="vehicleStatusChartData" :labels="vehicleStatusChartLabels"
                      :colors="vehicleStatusChartColors" :options="{}" />
                  </div>
                  <div class="ml-12 flex flex-col space-y-6">
                    <div v-for="(label, index) in ['Available', 'In-Use', 'Out-of-order']" :key="label"
                      class="flex items-center">
                      <span class="w-4 h-4 rounded-full mr-2"
                        :style="{ backgroundColor: vehicleStatusOriginalColors[index] }"></span>
                      <span class="text-lg">{{ label }}: {{ vehicleStatusData[index] }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </transition>
</template>

<script>
import FilterSVG from "./svg/FilterSVG.vue";
import PieChart from "./charts/PieChart.vue";
import BarChart from "./charts/BarChart.vue";
import AnimatedNumber from './AnimatedNumber.vue';
import FilterDropdown from '@/components/FilterFunc.vue'
import AnalyticsSVG from "./svg/StatisticsSVG.vue";

export default {
  components: {
    FilterSVG,
    PieChart,
    BarChart,
    AnimatedNumber,
    FilterDropdown,
    AnalyticsSVG
  },
  data() {
    return {
      // Date range for filtering analytics data
      startDate: '',
      endDate: '',
      // Sets the maximum selectable date to today
      maxDate: new Date().toISOString().split("T")[0],

      // Controls the visibility of different analytics sections for staggered animation
      showSection1: false,
      showSection2: false,
      showSection3: false,

      // Data for Order Overview section
      selectedOrderVolumeView: 'M', // Currently selected period for order volume ('M'onth, 'W'eek, 'D'ay)
      totalOrders: 0, // Total number of orders
      completedOrders: 0, // Number of completed orders
      orderVolume: 0, // Volume of orders for the selected period
      completionRate: 0, // Percentage of completed orders out of total orders
      sliderOrderVolumePosition: '0%', // Controls the visual position of the slider for period selection

      // Data for Vehicle Overview section
      totalVehicles: 0, // Total number of vehicles
      availableVehicles: 0, // Total number of available vehicle
      vehicleStatusData: [0, 0, 0], // Holds counts for available, in-use, and out-of-order vehicles

      // Data for Shipment Overview section
      totalTrips: 0, // Total number of trips
      distanceTraveled: 0, // Total distance traveled in kilometers
      travelingHours: 0, // Total traveling hours
      avgWaitingTime: 0, // Average waiting time in minutes

      // Data for Container Utilization section
      avgWeight: 0, // Average weight per shipment in kilograms
      sharedContainers: 0, // Number of shared containers
      freightTypeData: [0, 0, 0, 0, 0, 0], // Holds counts for different freight types (machinery, food, chemical, textile, electronic, other)

      // Filter related data
      companyFilter: null, // Currently selected company name for filtering
      companyList: [], // List of available companies for the filter dropdown
      dateError: false, // To track if there's a date validation error
      dateErrorMessage: '', // To store the error message

      // Chart configuration data
      // Colors for the vehicle status pie chart segments
      vehicleStatusOriginalColors: ['#28A745', '#007BFF', '#DC3545'], // Green, Blue, Red
      // Labels corresponding to vehicle status colors
      vehicleStatusOriginalLabels: ['Available', 'In-Use', 'Out-of-order'],

      // Colors for the freight type pie chart segments
      freightTypeOriginalColors: ['#FFC107', '#6F42C1', '#20C997', '#17A2B8', '#FD7E14', '#b38c72'], // Yellow, Purple, Teal, Cyan, Orange, Brown
      // Labels corresponding to freight type colors
      freightTypeOriginalLabels: ['machinery', 'food', 'chemical', 'textile', 'electronic', 'other'],
      // Color used when there is no data to display in a chart (a neutral grey)
      noDataGrey: '#B0B0B0',

      // Data for Monthly Orders Line Chart - HARDCODED DUMMY DATA
      selectedChartView: 'Y', // Currently selected period for order volume ('Y'year, 'M'month, 'D'ay)
      sliderChartPosition: '0%', // Controls the visual position of the slider for period selection
      OrdersChartLabels: [],
      OrdersChartData: [],
      OrdersChartColors: ['#FA812F'], // Orange color for the line chart
      isChartLoading: false,
    }
  },
  /**
   * Lifecycle hook called after the component is mounted to the DOM.
   * Performs initial data loading and sets up section visibility for animations.
   */
  mounted() {
    // this.endDate = this.maxDate; // Set initial end date to today
    this.fetchFilteredAnalytics(); // Fetch analytics data for filtered sections on component mount
    this.fetchVehicleStats(); // Fetch vehicle-specific analytics data
    this.getCompanyList(); // Fetch list of companies for the filter
    // this.fetchOverviewOrdersData();

    // Staggered animation for showing analytics sections sequentially
    this.showSection1 = true;
    setTimeout(() => {
      this.showSection2 = true;
    }, 300); // Show section 2 after 300ms
    setTimeout(() => {
      this.showSection3 = true;
    }, 600); // Show section 3 after 600ms
  },
  computed: {
    /**
     * Computes the CSS `left` percentage for the order volume period slider
     * based on the `selectedOrderVolumeView` (Month, Week, or Day).
     * @returns {string} The CSS 'left' percentage as a string (e.g., '0%', '33.33%', '66.66%').
     */
    sliderOrderVolumePosition() {
      switch (this.selectedOrderVolumeView) {
        case 'M':
          return '0%';
        case 'W':
          return '33.33%';
        case 'D':
          return '66.66%';
        default:
          return '0%'; // Default to Month if somehow an invalid view is selected
      }
    },
    /**
     * Maps the `selectedOrderVolumeView` shorthand ('M', 'W', 'D') to the full period string
     * ('month', 'week', 'day') for API request parameters.
     * @returns {string} The full period string.
     */
    selectedOrderVolumePeriod() {
      if (this.selectedOrderVolumeView === 'M') {
        return 'month';
      } else if (this.selectedOrderVolumeView === 'W') {
        return 'week';
      } else {
        return 'day';
      }
    },
    /**
     * Computes the CSS `left` percentage for the chart period toggle slider
     * based on the `selectedChartView` (Year, Month, or Day).
     * @returns {string} The CSS 'left' percentage as a string.
     */
    sliderChartPosition() {
      switch (this.selectedChartView) {
        case 'D':
          return '0%';
        case 'M':
          return '33.33%';
        case 'Y':
          return '66.66%';
        default:
          return '0%'; // Default to Month if somehow an invalid view is selected
      }
    },
    /**
     * Maps the `selectedChartView` shorthand ('Y', 'M', 'D') to the full period string
     * ('year', 'month', 'day') for API request parameters for the chart.
     * @returns {string} The full period string.
     */
    selectedChartPeriod() {
      if (this.selectedChartView === 'D') {
        return 'day';
      } else if (this.selectedChartView === 'M') {
        return 'month';
      } else {
        return 'year';
      }
    },
    /**
     * Calculates the order completion rate as a percentage.
     * Returns '0' if `totalOrders` is zero to prevent division by zero errors.
     * @returns {number|string} The completion rate as a number (e.g., 75.5) or '0'.
     */
    completionRate() {
      if (this.totalOrders === 0) {
        return '0';
      }
      return (this.completedOrders / this.totalOrders) * 100;
    },
    /**
     * Calculates the availability rate of vehicles as a percentage.
     * Returns '0' if `totalVehicles` is zero to prevent division by zero errors.
     * @returns {number|string} The availability rate as a number (e.g., 75.5) or '0'.
     */
    availabilityRate() {
      if (this.totalVehicles === 0) {
        return '0';
      }
      return (this.availableVehicles / this.totalVehicles) * 100;
    },
    /**
     * Calculates the sum of all vehicle counts (Available, In-Use, Out-of-order).
     * Used to determine if there's any vehicle data for the chart.
     * @returns {number} The total sum of vehicle status counts.
     */
    totalVehiclesSum() {
      return this.vehicleStatusData.reduce((sum, val) => sum + val, 0);
    },
    /**
     * Calculates the sum of all freight type counts.
     * Used to determine if there's any freight type data for the chart.
     * @returns {number} The total sum of freight type counts.
     */
    totalFreightSum() {
      return this.freightTypeData.reduce((sum, val) => sum + val, 0);
    },

    // --- Computed properties for Vehicle Status Chart (PieChart) ---
    /**
     * Determines the data array for the vehicle status pie chart.
     * If `totalVehiclesSum` is 0, returns `[1]` to render a full grey circle indicating no data.
     * @returns {Array<number>} An array of numbers representing vehicle counts.
     */
    vehicleStatusChartData() {
      return this.totalVehiclesSum === 0 ? [1] : this.vehicleStatusData;
    },
    /**
     * Determines the labels array for the vehicle status pie chart.
     * Returns `['No Vehicle Data']` if `totalVehiclesSum` is 0.
     * @returns {Array<string>} An array of strings representing vehicle status labels.
     */
    vehicleStatusChartLabels() {
      return this.totalVehiclesSum === 0 ? ['No Vehicle Data'] : this.vehicleStatusOriginalLabels;
    },
    /**
     * Determines the colors array for the vehicle status pie chart.
     * Uses `noDataGrey` if `totalVehiclesSum` is 0.
     * @returns {Array<string>} An array of color codes (e.g., '#RRGGBB').
     */
    vehicleStatusChartColors() {
      return this.totalVehiclesSum === 0 ? [this.noDataGrey] : this.vehicleStatusOriginalColors;
    },

    // --- Computed properties for Freight Type Chart (PieChart) ---
    /**
     * Determines the data array for the freight type pie chart.
     * If `totalFreightSum` is 0, returns `[1]` to render a full grey circle indicating no data.
     * @returns {Array<number>} An array of numbers representing freight type counts.
     */
    freightTypeChartData() {
      return this.totalFreightSum === 0 ? [1] : this.freightTypeData;
    },
    /**
     * Determines the labels array for the freight type pie chart.
     * Returns `['No Freight Data']` if `totalFreightSum` is 0.
     * @returns {Array<string>} An array of strings representing freight type labels.
     */
    freightTypeChartLabels() {
      return this.totalFreightSum === 0 ? ['No Freight Data'] : this.freightTypeOriginalLabels;
    },
    /**
     * Determines the colors array for the freight type pie chart.
     * Uses `noDataGrey` if `totalFreightSum` is 0.
     * @returns {Array<string>} An array of color codes (e.g., '#RRGGBB').
     */
    freightTypeChartColors() {
      return this.totalFreightSum === 0 ? [this.noDataGrey] : this.freightTypeOriginalColors;
    },
    // Checks if the date range is valid (startDate <= endDate)
    isDateRangeValid() {
      // If both dates are set, perform the comparison
      if (this.startDate && this.endDate) {
        return new Date(this.startDate) <= new Date(this.endDate);
      }
      // If one or neither is set, consider it valid (or handle as per your specific logic)
      // We only want to flag an error if an invalid range is explicitly chosen
      return true;
    },
    /**
     * Checks if any filters (company, start date, end date) are currently active.
     * Used to enable/disable the "Apply" button for filtered analytics.
     * @returns {boolean} True if any filter is set, false otherwise.
     */
    hasFiltersApplied() {
      // Add isDateRangeValid to the condition for enabling the Apply button
      return (this.companyFilter !== null || this.startDate !== '' || this.endDate !== '');
    },
    /**
 * Checks if any filters are currently active, indicating a filter has been applied.
 * Used to show/hide the "Clear" filter button.
 * @returns {boolean} True if any filter is set, false otherwise.
 */
    isFilterActive() {
      return this.companyFilter !== null || this.startDate !== '' || this.endDate !== '';
    }
  },
  watch: {
    // Watch for changes in the selected chart view (Y/M/D)
    selectedChartView(newValue, oldValue) {
      if (newValue !== oldValue) {
        this.fetchOverviewOrdersData();
      }
    },
    
    selectedOrderVolumeView(newValue, oldValue) {
    // Only re-fetch if the value actually changed (though clicking buttons guarantees this)
    if (newValue !== oldValue) {
      console.log(`Order Volume view changed to: ${newValue}. Refetching analytics.`);
      this.fetchFilteredAnalytics(); // <--- Trigger data fetch here!
    }
  }
  },
  methods: {
    /**
     * Asynchronously fetches the list of available companies from the backend API.
     * Populates the `companyList` data property, which is used by the `FilterDropdown` component.
     * Includes error handling for network or API issues.
     * @returns {Promise<void>} A promise that resolves when the company list is successfully fetched.
     */
    async getCompanyList() {
      try {
        const result = await fetch('/api/companies');
        // Check if the HTTP response was successful
        if (!result.ok) {
          const errorBody = await result.text();
          throw new Error(`HTTP error! Status: ${result.status} - ${errorBody}`);
        }

        const data = await result.json();
        // Map the raw company names to the format required by the FilterDropdown component
        this.companyList = data.map(company => ({
          value: company.username,
          label: company.username
        }));
      } catch (err) {
        console.error("Error getting company list for analytics:", err);
      }
    },
    /**
     * Asynchronously fetches filtered analytics data from the backend API.
     * Constructs the API URL based on the currently selected filters (period, start date, end date, company).
     * Updates data properties related to Order Overview, Shipment Overview, and Container Utilization.
     * Includes comprehensive error handling for API call failures.
     * @returns {Promise<void>} A promise that resolves when filtered analytics data is successfully fetched.
     */
    async fetchFilteredAnalytics() {
      try {
        // Construct the base URL with the selected period
        let url = `api/analytics/all?period=${this.selectedOrderVolumePeriod}`;
        // Append start date filter if available
        if (this.startDate) {
          url += `&startDate=${this.startDate}`;
        }
        // Append end date filter if available
        if (this.endDate) {
          url += `&endDate=${this.endDate}`;
        }
        console.log(this.companyFilter)
        // Append company filter if selected
        if (this.companyFilter) {
          url += `&companyName=${this.companyFilter}`;
        }

        const res = await fetch(url);
        // Check if the HTTP response was successful
        if (!res.ok) {
          const errorBody = await res.text();
          throw new Error(`HTTP error! Status: ${res.status} - ${errorBody}`);
        }
        const data = await res.json();

        // Update Order Overview data properties
        this.totalOrders = data.totalOrders;
        this.completedOrders = data.completedOrders;
        this.completionRate = data.completionRate;
        this.orderVolume = data.orderVolume;
        this.fetchOverviewOrdersData();

        // Update Shipment Overview data properties
        this.totalTrips = data.totalTrips;
        this.distanceTraveled = data.totalDistance;
        this.travelingHours = data.travelingHours;
        this.avgWaitingTime = data.averageWaitingTime;

        // Update Container Utilization data properties
        this.avgWeight = data.avgWeightPerShipment;
        this.sharedContainers = data.sharedContainers;
        // Map freight type composition from API response to component's array format
        this.freightTypeData = [
          data.freightTypeComposition.machinery || 0,
          data.freightTypeComposition.food || 0,
          data.freightTypeComposition.chemical || 0,
          data.freightTypeComposition.textile || 0,
          data.freightTypeComposition.electronic || 0,
          data.freightTypeComposition.other || 0,
        ];
      } catch (err) {
        console.error("Failed to fetch filtered analytics:", err);
      }
    },
    /**
     * Asynchronously fetches vehicle-specific analytics data from the backend API.
     * This method makes a call to an endpoint that does not take company or date filters.
     * Updates data properties related to Vehicle Overview.
     * @returns {Promise<void>} A promise that resolves when vehicle analytics data is successfully fetched.
     */
    async fetchVehicleStats() {
      try {
        const res = await fetch('api/analytics/vehicle-overview'); // No query parameters for this endpoint
        if (!res.ok) {
          const errorBody = await res.text();
          throw new Error(`HTTP error! Status: ${res.status} - ${errorBody}`);
        }
        const data = await res.json();

        // Update Vehicle Overview data properties
        this.availableVehicles = data.availableVehicles;
        this.totalVehicles = data.totalVehicles;
        this.vehicleStatusData = [
          data.vehicleStatusDistribution.available || 0,
          data.vehicleStatusDistribution.in_use || 0,
          data.vehicleStatusDistribution.out_of_order || 0,
        ];
      } catch (err) {
        console.error("Failed to fetch vehicle analytics:", err);
      }
    },
    /**
     * Fetches orders data for the chart based on the selected period (year, month, or day),
     * applying default date ranges if global filters are not fully specified.
     */
    async fetchOverviewOrdersData() {
      this.isChartLoading = true;
      try {
        let url = `/api/analytics/orders-by-period?period=${this.selectedChartPeriod}`;

        if (this.companyFilter) {
          url += `&companyName=${this.companyFilter}`;
        }
        if (this.startDate) {
          url += `&startDate=${this.startDate}`;
        }
        if (this.endDate) {
          url += `&endDate=${this.endDate}`;
        }

        const res = await fetch(url);
        if (!res.ok) {
          const errorBody = await res.text();
          throw new Error(`HTTP error! Status: ${res.status} - ${errorBody}`);
        }
        const responseData = await res.json();

        // Combine data and labels into an array of objects for easier sorting
        const combinedData = responseData.labels.map((label, index) => ({
          label: label,
          data: responseData.data[index]
        }));

        // Sort the combined data chronologically based on the date in the label
        combinedData.sort((a, b) => {
          const year = new Date().getFullYear(); // Use current year for comparison
          const dateA = new Date(`${a.label} ${year}`);
          const dateB = new Date(`${b.label} ${year}`);
          return dateA - dateB;
        });

        // Separate them back into OrdersChartData and OrdersChartLabels
        this.OrdersChartLabels = combinedData.map(item => item.label);
        this.OrdersChartData = combinedData.map(item => item.data);

      } catch (err) {
        console.error("Failed to fetch orders data for chart:", err);
        this.OrdersChartLabels = [];
        this.OrdersChartData = [];
      } finally {
        this.isChartLoading = false;
      }
    },
    /**
     * Handles the click event for the "Apply" button.
     * Validates the date range before calling fetchFilteredAnalytics.
     */
    applyFilters() {
      // Reset error state
      this.dateError = false;
      this.dateErrorMessage = '';

      // Perform date validation only if both dates are set
      if (this.startDate && this.endDate) {
        if (!this.isDateRangeValid) {
          this.dateError = true;
          this.dateErrorMessage = 'Start date cannot be after end date.';
          return; // Stop execution if validation fails
        }
      }

      // If validation passes (or dates are not both set), proceed to fetch data
      this.fetchFilteredAnalytics();
    },

    // ... existing fetchFilteredAnalytics method (no change needed here, as it's called by applyFilters) ...

    /**
     * Clears all currently applied filters (company, start date, end date) and re-fetches
     * the filtered analytics data to display the unfiltered state.
     */
    clearFilters() {
      this.companyFilter = null;
      this.startDate = '';
      this.endDate = '';
      this.dateError = false; // Reset error on clear
      this.dateErrorMessage = ''; // Clear error message on clear
      this.fetchFilteredAnalytics(); // Re-fetch data after clearing filters
      // fetchOverviewOrdersData is already called by fetchFilteredAnalytics now.
    },
  },
}
</script>

<style scoped>
/* Component-specific styles */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.slide-up-ease-in-enter-active {
  transition: all 0.7s ease-out;
}

.slide-up-ease-in-leave-active {
  transition: all 0.7s ease-in;
}

.slide-up-ease-in-enter-from,
.slide-up-ease-in-leave-to {
  transform: translateY(30px);
  opacity: 0;
}
</style>
