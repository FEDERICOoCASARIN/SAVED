<template lang="">
  <transition name="fade-slide" appear>
    <div class="flex flex-col">
      <div class="flex-row max-h-10px">
        <div class="flex px-5 py-5">
          <div class="w-[75%] flex justify-between">
            <div class="flex gap-4 flex-1">
              <div
                class="flex items-center gap-2 border-1 border-gray-300 p-1 px-4 rounded-xl w-full focus-within:outline-[#FA812F] focus-within:outline focus-within:outline-2"
              >
                <label>
                  <SearchSVG />
                </label>
                <input
                  type="text"
                  v-model="searchQuery"
                  class="text-base p-2 rounded-md ml-2 focus:outline-none focus:ring-0 cursor-text w-full"
                  placeholder="Search Vehicle by ID"
                  style="outline: none !important; box-shadow: none !important;"
                />

                <CancelSVG
                  v-if="searchQuery || isSearchActive"
                  @click="clearSearch"
                  class="w-5 h-5 text-red-500 cursor-pointer items-center"
                />
                <div
                  v-else
                  class="w-5 h-6 cursor-pointer text-gray-500 hover:text-gray-700"
                />
              </div>
            </div>

            <div class="flex items-center ml-2">
              <div class="flex ml-auto gap-2">
                <button
                  type="submit"
                  class="bg-[#FA812F] w-25 rounded-md p-2 text-white cursor-pointer"
                  @click="getVehicleByID"
                >
                  Search
                </button>
                <FilterFunc
                  :options="['AVAILABLE', 'OUT OF ORDER', 'IN-USE']"
                  v-model="filterOption"
                  title="VEHICLE STATUS"
                  @update:modelValue="getAllVehicle"
                />
              </div>
            </div>
          </div>

          <div class="flex-1 flex pl-4 justify-between">
            <div class="flex items-center space-x-3">
              <div class="p-2 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl shadow-lg">
                <VehiclesSVG class="size-6 text-white" />
              </div>
              <div
                class="flex text-2xl items-center justify-center text-gray-700 font-semibold"
              >
                Vehicles
              </div>
            </div>

            <div class="flex gap-1">
              <div class="flex gap-5 relative">
                <button
                  type="button"
                  @click="toggleExportVehicleButton"
                  class="flex-1 bg-[#EFEEF5] hover:bg-orange-50 hover:text-[#FA812F] rounded-md p-2 text-black hover:shadow-lg hover:-translate-y-0.5 cursor-pointer font-semibold"
                >
                  <span class="flex items-center gap-2 px-2">
                    <ExportArrowSVG />
                  </span>
                </button>

                <transition name="dropdown">
                  <div
                    v-if="showExportDropdown"
                    ref="exportDropdown"
                    class="absolute top-[105%] right-0 z-10 bg-white border border-gray-300 rounded-md shadow-lg w-48"
                  >
                    <ul class="text-sm text-gray-700">
                      <li
                        @click="selectExportType('xlsx')"
                        class="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                      >
                        Export as XLSX
                      </li>
                      <li
                        @click="selectExportType('csv')"
                        class="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                      >
                        Export as CSV
                      </li>
                    </ul>
                  </div>
                </transition>
              </div>

              <button
                @click="showAddVehiclePopup = true"
                type="submit"
                class="flex-1 p-2 w-12 text-gray-500 hover:text-[#FA812F] hover:bg-orange-50 rounded-lg transition-colors cursor-pointer bg-[#EFEEF5] z-[101]"
              >
                +
              </button>
            </div>
          </div>
        </div>

        <div class="flex flex-row">
          <div
            class="relative w-[75%] bg-white rounded-md shadow-sm overflow-hidden"
          >
            <template v-if="isLoading">
              <div class="absolute inset-0 flex items-center justify-center bg-gray-100 bg-opacity-75 z-20">
                <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div>
              </div>
            </template>
            <template v-else-if="filteredVehicles.length > 0">
              <MapComponent
                :markers="vehicleMarkers"
                class="z-0"
                :zoom="mapZoom"
                :center="mapCenter"
              />
              <div
                v-if="activeIndex !== null"
                class="absolute top-4 right-5 flex bg-white/95 backdrop-blur-sm rounded-lg shadow-lg px-5 py-2 z-10"
              >
                <button
                  @click="resetIndex"
                  class="cursor-pointer"
                  aria-label="Show all vehicles on the map"
                >
                  See all
                </button>
              </div>
            </template>
            <div
              v-else
              class="flex justify-center items-center h-full text-gray-500 text-lg shadow-xl"
            >
              <p>No vehicles to display on the map with the current filters.</p>
            </div>
          </div>
          <div class="flex-1">
            <div
              class="flex flex-col h-[calc(100vh)] rounded-t-md shadow-lg px-2 space-y-3"
            >
              <div class="overflow-y-auto flex-1 bg-white p-2">
                <template v-if="isLoading">
                  <div class="flex items-center justify-center h-full">
                    <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div>
                  </div>
                </template>
                <template v-else>
                  <transition-group name="stagger-fade" tag="div">
                    <VehiclesTableItem
                      v-for="(vehicle, i) in filteredVehicles"
                      :key="vehicle.vehicleID"
                      :vehicleID="vehicle.vehicleID"
                      :vehicleDetail="vehicle"
                      :style="{ animationDelay: i * 100 + 'ms' }"
                      class="animated-row"
                      @deleteVehicle="deleteVehicleFunction"
                      @editVehicle="editVehicleFunction"
                      :is-active="activeIndex === i"
                      @select="selectVehicle(i)"
                    />
                  </transition-group>
                  <div
                    v-if="filteredVehicles.length === 0"
                    class="flex justify-center items-center h-full text-gray-500 text-lg py-10"
                  >
                    <p v-if="searchQuery && searchQuery.length > 0">
                      No vehicles found for "{{ searchQuery }}". Try a different
                      ID.
                    </p>
                    <p v-else-if="filterOption">
                      No vehicles match the "{{ filterOption }}" status.
                    </p>
                    <p v-else>No vehicles to display.</p>
                  </div>
                </template>
              </div>
            </div>
          </div>
        </div>
        <teleport to="body">
          <transition name="fade">
            <AddVehiclePopup
              v-if="showAddVehiclePopup"
              @close="showAddVehiclePopup = false"
              @save="addVehicleFunction"
            />
          </transition>
        </teleport>

        <teleport to="body">
          <SuccessConfirmationPopup
            :show="showSuccessfulConfirmation"
            :title="this.successTitle"
            :caption="this.successCaption"
            @close="showSuccessfulConfirmation = false"
          />
        </teleport>
      </div>
    </div>
  </transition>
</template>

<script>
import VehiclesTableItem from "@/components/vehicle/VehiclesTableItem.vue";
import AddVehiclePopup from "@/components/vehicle/AddVehiclePopup.vue";
import EditVehiclePopup from "@/components/vehicle/EditVehiclePopup.vue";
import ExportArrowSVG from "@/components/svg/ExportArrowSVG.vue";
import FilterSVG from "@/components/svg/FilterSVG.vue";
import SearchSVG from "@/components/svg/SearchSVG.vue";
import SortArrowSVG from "@/components/svg/SortArrowSVG.vue";
import CancelSVG from "@/components/svg/CancelSVG.vue";
import FilterDropdown from "@/components/FilterFunc.vue";
import MapComponent from "../MapComponent.vue";
import SuccessConfirmationPopup from "../SuccessfulConfirmationPopup.vue";
import FilterFunc from "@/components/FilterFunc.vue";
import VehiclesSVG from "../svg/VehiclesSVG.vue";

export default {
  components: {
    FilterSVG,
    SearchSVG,
    SortArrowSVG,
    ExportArrowSVG,
    VehiclesTableItem,
    AddVehiclePopup,
    EditVehiclePopup,
    FilterDropdown,
    MapComponent,
    SuccessConfirmationPopup,
    FilterFunc,
    CancelSVG,
    VehiclesSVG,
  },

  props: {
    data: {
      type: Object,
      required: false,
    },
  },
  data() {
    return {
      showAddVehiclePopup: false,
      searchQuery: "",
      successTitle: "",
      successCaption: "",
      filterOption: null,
      sortOption: null,
      showSuccessfulConfirmation: false,
      showSuccessfulEditConfirmation: false,
      vehicles: [],
      vehicleMarkers: [],
      filterOption: null,
      activeIndex: null,
      isSearchActive: false,
      vehicles: [],
      searchId: "",
      showExportDropdown: false,
      // New data property for loading state
      isLoading: false,
    };
  },
  methods: {
    /**
     * Clears the search query and, if a search was active, reloads all vehicles.
     */
    clearSearch() {
      console.log("true");
      console.log(this.isSearchActive);
      if (this.isSearchActive) {
        this.getAllVehicle();
      }
      this.searchQuery = "";
      this.isSearchActive = false;
    },

    /**
     * Calculates the appropriate map bounds (center and zoom level) based on the current
     * list of vehicles. If no vehicles are present, it defaults to a predefined location.
     */
    getMapBounds() {
      if (!this.vehicles.length) {
        return {
          center: { lat: 51.9244, lng: 4.4777 },
          zoom: 12,
        };
      }
      const lats = this.vehicles.map((v) => Number(v.lat));
      const lngs = this.vehicles.map((v) => Number(v.long));

      console.log(lats, lngs);
      const center = {
        lat: (Math.max(...lats) + Math.min(...lats)) / 2,
        lng: (Math.max(...lngs) + Math.min(...lngs)) / 2,
      };

      return { center, zoom: 12 };
    },

    /**
     * Resets the active index, effectively deselecting any currently highlighted vehicle on the map/list.
     */
    resetIndex() {
      this.activeIndex = null;
    },

   /**
     * Sets the active index to highlight a specific vehicle in the list and on the map.
     */
    selectVehicle(index) {
      this.activeIndex = index;
    },

    /**
     * DB METHOD
     * Deletes a vehicle from the backend and then refreshes the local vehicle list.
     * Displays an error if the deletion fails.
     * @param {string} vehicleID The ID of the vehicle to delete.
     */
    async deleteVehicleFunction(vehicleID) {
      this.isLoading = true; // Set loading to true before the async operation
      try {
        const result = await fetch(`api/vehicle/${vehicleID}`, {
          method: "DELETE",
        });
        if (!result.ok) {
          const errorBody = await result.text();
          throw new Error(
            `HTTP error! Status: ${result.status} - ${errorBody}`
          );
        }

        await this.getAllVehicle();
      } catch (err) {
        console.log("error saving new vehicle");
      } finally {
        this.isLoading = false; // Set loading to false after the operation, regardless of success or failure
      }
    },

    /**
     * DB METHOD
     * Searches for a vehicle by its ID and updates the vehicle list to show only the found vehicle.
     * Displays an error if the ID is invalid or no vehicle is found.
     */
    async getVehicleByID() {
      this.vehicles = [];
      this.error = null;
      this.isSearchActive = true;
      this.isLoading = true; // Set loading to true

      try {
        const parsedVehicleId = Number(this.searchQuery);
        console.log("Searching for Vehicle ID:", parsedVehicleId);

        if (isNaN(parsedVehicleId) || !this.searchQuery) {
          this.error = "Please enter a valid number for the Vehicle ID.";
          this.vehicles = [];
          return;
        }

        const result = await fetch(`api/vehicle/${parsedVehicleId}`);

        if (!result.ok) {
          const errorBody = await result.text();
          throw new Error(
            `HTTP error! Status: ${result.status} - ${errorBody}`
          );
        }

        const data = await result.json();

        if (data && typeof data === "object" && !Array.isArray(data)) {
          this.vehicles = [this.mapBackendToFrontend(data)];
        } else if (Array.isArray(data) && data.length > 0) {
          this.vehicles = data.map((vehicle) =>
            this.mapBackendToFrontend(vehicle)
          );
        } else {
          this.vehicles = [];
          this.error = `No vehicle found with ID ${parsedVehicleId}.`;
        }
        console.log("Fetched Vehicles:", this.vehicles);
      } catch (err) {
        console.error("Error fetching vehicle:", err);
        this.error = `Failed to fetch vehicle: ${err.message || "An unknown error occurred."}`;
        this.vehicles = [];
      } finally {
        this.isLoading = false; // Set loading to false
      }
    },

    /**
     * DB METHOD
     * Adds a new vehicle to the backend using the provided vehicle data.
     * Displays a success message and refreshes the vehicle list upon successful addition.
     */
    async addVehicleFunction(vehicleData) {
      const data = {
        position: {
          x: 6.894637,
          y: 52.221821,
        },
        battery_level: vehicleData.battery,
        status: this.mapFrontendToBackend(vehicleData.status),
        distance: vehicleData.distance,
      };

      console.log(data);

      this.error = null;
      this.isLoading = true; // Set loading to true
      try {
        const result = await fetch("api/vehicle", {
          method: "POST",
          headers: {
            "Content-type": "application/json",
          },
          body: JSON.stringify(data),
        });

        if (!result.ok) {
          const errorBody = await result.text();
          throw new Error(
            `HTTP error! Status: ${result.status} - ${errorBody}`
          );
        }

        this.successTitle = "Vehicle Added Successfully";
        this.successCaption = "Your vehicle has been added to the fleet.";
        this.showSuccessfulConfirmation = true;
        //refresh
        await this.getAllVehicle();
      } catch (err) {
        console.log("error saving new vehicle");
      } finally {
        this.isLoading = false; // Set loading to false
      }
    },

    /**
     * DB METHOD
     * Edits an existing vehicle on the backend with new data.
     * Displays a success message and refreshes the vehicle list upon successful update.
     */
    async editVehicleFunction(vehicleID, newVehicleData) {
      const data = {
        position: {
          x: 6.884,
          y: 52.22186,
        },
        battery_level: newVehicleData.battery,
        status: this.mapFrontendToBackend(newVehicleData.status),
        distance: newVehicleData.distance,
      };

      console.log(data);

      this.error = null;
      this.isLoading = true; // Set loading to true
      try {
        const result = await fetch(`api/vehicle/${vehicleID}`, {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(data),
        });

        if (!result.ok) {
          const errorBody = await result.text();
          throw new Error(
            `HTTP error! Status: ${result.status} - ${errorBody}`
          );
        }

        //show edit successful

        this.successTitle = "Vehicle Edited Successfully";
        this.successCaption = "Your vehicle has been updated.";
        this.showSuccessfulEditConfirmation = true;

        //refresh
        await this.getAllVehicle();
      } catch (err) {
        console.error("Failed to update vehicles:", err);
        this.error = `Could not fetch vehicles: ${err.message}`;
      } finally {
        this.isLoading = false; // Set loading to false
      }
    },

     /**
     * Applies the selected filter option to create a map of query parameters for the backend.
     */
    applyFilter() {
      const filterMap = {};

      if (this.filterOption) {
        filterMap.status = this.mapFrontendToBackend(this.filterOption);
      }

      return filterMap;
    },
    /**
     * DB METHOD
     * Fetches all vehicles from the backend, optionally applying filters.
     * Updates the component's vehicle list and map markers.
     */    
    async getAllVehicle() {
      this.error = null;
      this.vehicles = [];
      this.isLoading = true; // Set loading to true

      let filters = this.applyFilter();
      try {
        const queryParams = new URLSearchParams();
        for (const [key, value] of Object.entries(filters)) {
          if (value != null && value !== "") {
            queryParams.append(key, value);
          }
        }

        const result = await fetch(`api/vehicle?${queryParams.toString()}`);

        if (!result.ok) {
          const errorBody = await result.text();
          throw new Error(
            `HTTP error! Status: ${result.status} - ${errorBody}`
          );
        }

        const data = await result.json();
        this.vehicles = data;
        console.log(this.vehicles);

        this.vehicles = data.map(this.mapBackendToFrontend);
        console.log(this.vehicles)
        //update map
        this.vehicleMarkers = this.updateVehicleMarkers();
      } catch (err) {
        console.error("Failed to fetch vehicles:", err);
        this.error = `Could not fetch vehicles: ${err.message}`;
      } finally {
        this.isLoading = false; // Set loading to false
      }
    },

    /**
     * Maps frontend status strings to their backend equivalents
     */
    mapFrontendToBackend(status) {
      const statusMap = {
        "IN-USE": "in_use",
        "OUT OF ORDER": "out_of_order",
        AVAILABLE: "available",
      };
      return statusMap[status] || status.toLowerCase();
    },

    /**
     * Maps backend vehicle data structure to the frontend's expected format.
     */
    mapBackendToFrontend(dbData) {
      const statusMap = {
        in_use: "IN-USE",
        out_of_order: "OUT OF ORDER",
        available: "AVAILABLE",
      };
      return {
        vehicleID: dbData.vehicleId,
        lat: String(dbData.position.y),
        long: String(dbData.position.x),
        battery: String(Math.round(dbData.battery_level)),
        status: statusMap[dbData.status] || dbData.status.toUpperCase(),
        inOut: "Outgoing",
        distance: String(dbData.distance),
      };
    },

    /**
     * Generates map marker data from the current list of vehicles.
     */
    updateVehicleMarkers() {
      return this.vehicles.map((vehicle) => ({
        lat: Number(vehicle.lat),
        lng: Number(vehicle.long),
        icon: "truck",
        name: `Vehicle ${vehicle.vehicleID}`,
      }));
    },

    /**
     * Toggles the visibility of the export dropdown menu.
     */

    toggleExportVehicleButton() {
      this.showExportDropdown = !this.showExportDropdown;
    },

    /**
     * DB METHOD
     * Triggers the download of vehicle data in the specified format (XLSX or CSV).
     */
    async selectExportType(type) {
      this.showExportDropdown = false;
      this.isLoading = true; // Set loading to true
      try {
        window.location.href = `/api/vehicle/export/${type}`;
      } catch (err) {
        console.error("Failed to export vehicles:", err);
        this.error = `Could not export vehicles: ${err.message}`;
      } finally {
        this.isLoading = false; // Set loading to false
      }
    },
  },
  computed: {
    filteredAndSortedVehicles() {
      let result = this.vehicles;

      // Filter
      if (this.filterOption) {
        result = result.filter((v) => v.status === this.filterOption);
      }

      // Sort
      if (this.sortOption === "Distance") {
        result = [...result].sort(
          (a, b) => Number(a.distance) - Number(b.distance)
        );
      } else if (this.sortOption === "Battery") {
        result = [...result].sort(
          (a, b) => Number(a.battery) - Number(b.battery)
        );
      }

      return result;
    },
    mapCenter() {
      const center =
        this.activeIndex !== null && this.filteredVehicles[this.activeIndex]
          ? {
              lat: Number(this.filteredVehicles[this.activeIndex].lat),
              lng: Number(this.filteredVehicles[this.activeIndex].long),
            }
          : this.getMapBounds().center;

      return center;
    },
    mapZoom() {
      return this.activeIndex !== null ? 15 : 8;
    },
    filteredVehicles() {
      let result = this.filteredAndSortedVehicles;

      if (this.searchId) {
        const searchTerm = this.searchId.trim();
        result = result.filter(
          (vehicle) => String(vehicle.vehicleID) === String(searchTerm)
        );
      }

      return result;
    },

    // Add a new computed property for no results message
    hasNoResults() {
      return this.filteredVehicles.length === 0;
    },
  },

  async created() {
    // or mounted()
    this.getAllVehicle();
  },
};
</script>

<style scoped>
.animated-row {
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.4s ease forwards;
  position: relative;
  z-index: 1;
}

.fade-enter-active {
  @apply transition-opacity duration-300;
}
.fade-enter-from {
  @apply opacity-0;
}

.scale-fade-enter-active {
  @apply transition transform duration-300;
}
.scale-fade-enter-from {
  @apply opacity-0 scale-90;
}

.fade-slide-enter-active {
  transition: all 0.4s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}
.fade-slide-enter-to {
  opacity: 1;
  transform: translateY(0);
}

.stagger-fade-enter-active {
  transition: all 0.4s ease;
}
.stagger-fade-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.stagger-fade-leave-active {
  opacity: 0 !important;
  position: absolute !important;
}

.stagger-fade-leave-to {
  opacity: 0;
}

.stagger-fade-move {
  transition: none;
}

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-list-item {
  transition: all 0.3s ease;
}
.fade-list-enter-active {
  transition: all 0.3s ease;
}
.fade-list-enter-from {
  opacity: 0;
  transform: translateY(30px);
}
</style>