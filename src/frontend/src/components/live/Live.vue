<template>
    <transition name="fade-slide" appear>
    <div class="flex flex-row">
        <!-- Left Order -->
        <div class="flex flex-col w-[30%] p-6 gap-5">
            <div class="flex flex-col gap-2">
                <div class="flex items-center space-x-3">
                <div class="p-2 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl shadow-lg">
                    <LiveTrackingSVG class="size-6 text-white" />
                </div>
                <h1 class="text-3xl font-bold text-gray-800">Active Orders</h1>
                </div>

                <div class="flex justify-between mt-2">
                <h1 class="text-2xl text-gray-500/50">{{ activeOrder.length }}</h1>
                <SortFunc
                    v-model="sortOption"
                    :options="['ETA', 'ETD', 'WEIGHT']"
                    class="w-40"
                />
                </div>
            </div>

            
            <div class="flex rounded-xl place-content-between w-full">
                <!-- Filter -->
                    <div class="flex-1 flex flex-col gap-2 h-full border-1 p-2 rounded-md border-gray-100">
                        <span class="text-gray-500 text-xs">
                            Filter
                        </span>
                        <div class="flex gap-2">
                            <div class="flex">
                                <FilterDropdown 
                                    class="cursor-pointer flex uppercase"
                                    v-model="operationTypeFilter"
                                    :options="[
                                    { value: 'loading', label: 'loading' },
                                    { value: 'unloading', label: 'unloading' },
                                    ]"
                                    title="OPT. TYPE"
                                    @dropdownToggled="(isOpen) => showSortDropdown = isOpen ? false : showSortDropdown"
                                />
                            </div>
    
                            <div class="flex" v-if="authStore.isAdmin">
                
                                <FilterDropdown
                                    v-model="orderForFilter"
                                    :options="companyList"
                                    title="COMPANIES"
                                    class="uppercase"
                                />
    
                            </div>
                                    
                        </div>
                        
                         <div class="flex w-full justify-between gap-2">
                            <div class="flex w-full">
                                <button type="submit" @click="getAllOrders" class="bg-[#FA812F] w-full hover:bg-orange-500 rounded-lg px-3 py-1 text-white cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
											:disabled="!hasFilters">
                                    <span class="flex  gap-1">
                                        Apply
                                    </span>
                                </button>
                            </div>
                            <div class="flex">
                                <button
                                    type="submit"
                                    class="bg-red-500  rounded-lg px-3 py-1 text-white cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                                    :disabled="!hasFilters || !isFilterActive"
                                    @click="clearFilter"
                                    >
                                    <span class="flex items-center gap-1">
                                        <CancelSVG class="w-4 h-4" />
                                    </span>
                                </button>
                            </div>
                        </div>
                    </div>
                        
                 
            </div>
            <!-- Order List -->
             <div class="flex-1 rounded-md overflow-y-auto text-black px-2 space-y-3 flex flex-col p-2">
                
              <transition-group name="stagger-fade" tag="div" key="id">
                    <template v-if="filteredAndSortedActiveOrders.length > 0">
                        <LiveList
                            v-for="(order, index) in filteredAndSortedActiveOrders"
                            :key="order.id"
                            :order="order"
                            :style="{ animationDelay: (index * 200) + 'ms' }"
                            class="animated-row mb-3"
                            :is-active="activeIndex === index"
                            @select="selectItem(index)"
                        />
                    </template>
                    <div v-else
                        key="empty"
                        class="text-center text-gray-400 italic py-10 border rounded-lg border-dashed border-gray-300 bg-white"
                    >
                        No active orders found
                    </div>
                </transition-group>

            </div>
          
        </div>
        <!-- Right map + Order details -->
        <div class="flex-1 flex flex-col py-6 pr-6 gap-5">
            <!-- Map -->
            <div class="flex h-[70%] w-full rounded-xl bg-gray-50 justify-center items-center border border-dashed border-gray-300"
                v-if="selectedOrder">
                <MapComponent
                    :key="activeIndex"
                    :markers="currentMarkers"
                    :enableRouting="true"
                    :source="currentSource"
                    :destination="currentDestination"
                    class="w-full h-full z-0"
                    :zoomable="true"
                    @routeCalculated="handleDistanceFromMap"
                    :zoom="12"
                />
                 <div v-if="selectedOrder"
                        class="absolute top-9 right-8 w-72 bg-white/95 backdrop-blur-sm rounded-lg shadow-lg p-4 z-10">
                        <div class="space-y-4">
                            <!-- Source -->
                            <div class="space-y-1">
                                <div class="flex items-center gap-2">
                                    <div class="w-2 h-2 rounded-full bg-green-500"></div>
                                    <span class="text-sm font-medium uppercase">{{ selectedOrder.orders[localSelectedIndex].source }}</span>
                                </div>
                                <p class="text-xs text-gray-500 ml-4">
                                    {{ selectedOrder.orders[localSelectedIndex].sourcePosition.y  }}, {{ selectedOrder.orders[localSelectedIndex].sourcePosition.x  }}
                                </p>
                            </div>

                            <div class="flex justify-center">
                                <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 14l-7 7m0 0l-7-7m7 7V3"></path>
                                </svg>
                            </div>

                            <div class="space-y-1">
                                <div class="flex items-center gap-2">
                                    <div class="w-2 h-2 rounded-full bg-red-500"></div>
                                    <span class="text-sm font-medium uppercase">{{ selectedOrder.orders[localSelectedIndex].destination  }}</span>
                                </div>
                                <p class="text-xs text-gray-500 ml-4">
                                    {{ selectedOrder.orders[localSelectedIndex].destinationPosition.y }}, {{ selectedOrder.orders[localSelectedIndex].destinationPosition.x  }}
                                </p>
                            </div>

                            <div class="pt-2 mt-2 border-t border-gray-200 space-y-2">
                                <!-- Distance -->
                                <div class="flex justify-between items-center">
                                    <span class="text-xs text-gray-500">Distance</span>
                                    <span class="text-sm font-medium">{{ formattedDistance }}</span>
                                </div>

                                <!-- ETD -->
                                <div v-if="selectedOrder.orders[localSelectedIndex].status === 'created' & selectedOrder.orders[localSelectedIndex].status === 'scheduled'"class="flex justify-between items-center">
                                    <span class="text-xs text-gray-500">Estimated Time Departure</span>
                                    <span class="text-sm font-medium">
                                        {{ convertDateToTime(selectedOrder.orders[localSelectedIndex].twStart) }}
                                    </span>
                                </div>

                                <!-- actual departure -->
                                <div v-else class="flex justify-between items-center">
                                    <span class="text-xs text-gray-500">Departure Time</span>
                                    <span class="text-sm font-medium">
                                        {{ convertDateToTime(selectedOrder.orders[localSelectedIndex].departureTime) }}
                                    </span>
                                </div>
                                
                                <!-- ETA -->
                                <div class="flex justify-between items-center">
                                    <span class="text-xs text-gray-500">Estimated Time Arrival</span>
                                    <span class="text-sm font-medium">{{ convertDateToTime(selectedOrder.orders[localSelectedIndex].eta || 'TBD') }}</span>
                                </div>
                            </div>
                        </div>
                    </div>

            </div>

            <div v-else class="flex h-[70%] w-full rounded-xl bg-gray-50 justify-center items-center border border-dashed border-gray-300">
                <p v-if="activeIndex===-1" class="text-gray-400 italic">No orders to view. I guess you should make one...</p>
            </div>


            <!-- order details -->
           <div class="flex-1 rounded-xl" :class="selectedOrder ? 'bg-white' : 'bg-gray-50 border border-dashed border-gray-300 flex items-center justify-center'">
                <LiveDetail 
                    v-if="selectedCoordinates" 
                    :orderDetails="activeOrder[activeIndex]"
                    class="flex-1"
                    :selectedIndex="this.localSelectedIndex"
                    @update:selectedIndex="updateLocalSelectedIndex" 
                />
                <p v-if="activeIndex===-1" class="text-gray-400 italic">Still no orders to view...</p>
            </div>
        </div>
    </div>
    </transition>
</template>

<script>
import FilterSVG from "@/components/svg/FilterSVG.vue";
import SortArrowSVG from "@/components/svg/SortArrowSVG.vue";
import CancelSVG from "@/components/svg/CancelSVG.vue";
import LiveList from "./LiveList.vue";
import MapComponent from "@/components/MapComponent.vue";
import LiveDetail from "./LiveDetail.vue";
import FilterDropdown from "@/components/FilterFunc.vue";
import SortFunc from "@/components/SortFunc.vue";
import { useAuthStore } from '@/stores/auth.js';
import LiveTrackingSVG from "../svg/LiveSVG.vue";


export default {
    components: {
        FilterSVG, 
        SortArrowSVG,
        LiveList,
        MapComponent,
        LiveDetail,
        FilterDropdown,
        SortFunc,
        CancelSVG,
        LiveTrackingSVG
    },
    data(){
        return {
             localSelectedIndex: 0, // Default selection of order details tab
              activeIndex: null, // Current selected order
              orderForFilter: null, // Query build up for requested by filter
              showSortDropdown: false, // True if user presses the 'Sort' button
              showFilterDropdown: false, // True if user selects one of the 'filter'
              operationTypeFilter: null, //  Query build up for operation type
              companyList: [], // Gets all the company available (for filter)
              sortOption: null, // Selected sort option
              routeDistance: null, // Distance for map overlay
              isFilterActive: false, // True if filter is active
              logged_in_username: '', // Current user
              activeOrder: [] // Arrays to hold all of the orders.
        }
    },

    computed: {
        /**
         * Formats the `routeDistance` for display; shows "Calculating..." if `routeDistance` is null.
         */
        formattedDistance() {
            return this.routeDistance ? `${this.routeDistance} km` : 'Calculating...';
        },

        /**
         * Returns the coordinates (latitude and longitude) of the currently selected order.
         * Used to center the map.
         */
        selectedCoordinates() {
            if (this.activeIndex !== null && this.activeOrder[this.activeIndex]) {
                const selectedOrder = this.activeOrder[this.activeIndex];
                return {
                lat: selectedOrder.latitude,
                lng: selectedOrder.longitude
                }
            }
            return null
        },

         /**
         * Returns the currently selected order object from `activeOrder`.
         */
        selectedOrder() {
            return this.activeIndex !== null ? this.activeOrder[this.activeIndex] : null;
        },

         /**
         * Generates markers for the map component based on the selected order's source and destination positions.
         */
        currentMarkers() {

            if (!this.selectedOrder) return [];        

            let markers = []
         
            if(this.selectedOrder.orders.length > 1){
                markers=[
                {
                    lat: Number(this.selectedOrder.orders[this.localSelectedIndex].sourcePosition.y),
                    lng: Number(this.selectedOrder.orders[this.localSelectedIndex].sourcePosition.x),
                    name: this.selectedOrder.orders[this.localSelectedIndex].source
                },
                {
                    lat: Number(this.selectedOrder.orders[this.localSelectedIndex].destinationPosition.y),
                    lng: Number(this.selectedOrder.orders[this.localSelectedIndex].destinationPosition.x),
                    name: this.selectedOrder.orders[this.localSelectedIndex].destination
                }
                ]
            } else{
                markers=[
                {
                    lat: Number(this.selectedOrder.orders[this.localSelectedIndex].sourcePosition.y),
                    lng: Number(this.selectedOrder.orders[this.localSelectedIndex].sourcePosition.x),
                    name: this.selectedOrder.orders[this.localSelectedIndex].source
                },
                {
                    lat: Number(this.selectedOrder.orders[this.localSelectedIndex].destinationPosition.y),
                    lng: Number(this.selectedOrder.orders[this.localSelectedIndex].destinationPosition.x),
                    name: this.selectedOrder.orders[this.localSelectedIndex].destination
                }
                ]
            }
            
            return markers;
        },

        /**
         * Returns the source coordinates for routing on the map component.
         */
        currentSource() {
            if (!this.selectedOrder) return null;


            if (this.selectedOrder.orders && this.selectedOrder.orders.length > 0) {
                return {
                    lat: Number(this.selectedOrder.orders[this.localSelectedIndex].sourcePosition.y),
                    lng: Number(this.selectedOrder.orders[this.localSelectedIndex].sourcePosition.x)
                };
            } else {
                return {
                    lat: Number(this.selectedOrder.sourcePosition.y),
                    lng: Number(this.selectedOrder.sourcePosition.x)
                };
            }
            
        },
        
        /**
         * Returns the destination coordinates for routing on the map component.
         */
        currentDestination() {
            if (!this.selectedOrder) return null;
            if (this.selectedOrder.orders && this.selectedOrder.orders.length > 0) {
                return {
                    lat: Number(this.selectedOrder.orders[0].destinationPosition.y),
                    lng: Number(this.selectedOrder.orders[0].destinationPosition.x)
                };
            } else {
                return {
                    lat: Number(this.selectedOrder.destinationPosition.y),
                    lng: Number(this.selectedOrder.destinationPosition.x)
                };
            }
        },

        /**
         * Calculates the proportion of the current order's freight weight relative to a total weight.
         */
        weightProportion() {
            if (!this.orderDetails?.freightWeight) return 0
            return (Number(this.orderDetails.freightWeight) / this.totalWeight) * 100
        },

         /**
         * Calculates the total freight weight of all orders in the `activeOrder` array.
         */
        totalWeightComputed() {
            return this.activeOrder.reduce((acc, order) => acc + Number(order.freightWeight || 0), 0);
        },
       
        /**
         * Applies sorting to the `activeOrder` list based on selected options.
         */
        filteredAndSortedActiveOrders() {
            let result = this.activeOrder;

            // Sort
            if (this.sortOption === "ETD") {
                result = [...result].sort((a, b) => {
                    const timeA = new Date(a.twStart).getTime();
                    const timeB = new Date(b.twStart).getTime();
                    return timeA - timeB;
                });
            } else if (this.sortOption === "ETA") {
                result = [...result].sort((a, b) => {
                    const timeA = a.eta ? new Date(a.eta).getTime() : 0; // Handles null ETA by placing it at the beginning
                    const timeB = b.eta ? new Date(b.eta).getTime() : 0; 
                    return timeA - timeB; 
                 });
            } else if (this.sortOption === "WEIGHT") {
                result = [...result].sort((a, b) => {
                    const aWeight = Number(a.freightWeight || 0); // Converts to number, default to 0 if null/undefined
                    const bWeight = Number(b.freightWeight || 0); 
                    return aWeight - bWeight;
                });
            }
            return result;
        },

        /**
         * Determines if any filters are active.
         * If filters are present, it returns `true`. If no filters are present, it calls `getAllOrders`
         */
        hasFilters() {
            if(this.operationTypeFilter || this.orderForFilter){
                return true
            } else {
                this.getAllOrders()
            };
	    }
    },
    
    methods: {
        /**
         * Updates the `localSelectedIndex` which controls the displayed order detail tab
         * when multiple orders are grouped.
         */
        updateLocalSelectedIndex(newIndex) {
            this.localSelectedIndex = newIndex;
     
        },
        
        /**
         * Clears all active filters (`orderForFilter`, `operationTypeFilter`) and
         * resets `isFilterActive` to `false`. Then, it re-fetches all orders without filters.
         */
        clearFilter(){
			 this.orderForFilter = null
			 this.operationTypeFilter = null 
			 this.isFilterActive = false;

			 this.getAllOrders();
		},

        /**
         * Sets the `activeIndex` to select a specific order from the `activeOrder` list.
         * Resets `localSelectedIndex` to 0, ensuring the first detail tab is shown for the new selection.
         */
        selectItem(index){
            this.activeIndex = index;
            this.localSelectedIndex = 0;
        },

        /**
         * Handles route calculation information received from the `MapComponent`.
         * Updates the `routeDistance` data property.
         */
        handleDistanceFromMap(info) {
            this.routeDistance = info.distance;
        },

        /**
         * Retrieve all the order with the received routeId
         */
        async getOrderByRouteId(routeId){
            try{
                const queryParams = new URLSearchParams();

                queryParams.append("route_id", routeId);
                console.log(queryParams)
                queryParams.append("route_id", routeId);

                const result = await fetch(`api/orders?${queryParams.toString()}`);

                if (!result.ok) {
                    const errorBody = await result.text();
                    throw new Error(`HTTP error! Status: ${result.status} - ${errorBody}`);
                }

                let data = await result.json();
                return data.map((order) => this.mapBackendToFrontend(order));

                } catch(err){
                console.log("Error getting all orders for live page: ", err)
            }
        },

        /** 
         * DB METHOD
         * Fetches all active orders from the backend API, applying specified filters
         * and user-specific order_for if the user is not an admin.
         */
        async getAllOrders(){
            this.activeOrder = [];
            let statusFilter = ['created','scheduled', 'undergoing']

            let filters = this.applyFilters();
            
            try{
                const queryParams = new URLSearchParams();

                
                for(const status of statusFilter){
                    queryParams.append("status", status)
                }

                for(const [key, value] of Object.entries(filters)){
                    if (value != null && value !== ''){
						queryParams.append(key,value);
					}
                }

                if(this.authStore.isUser){
                    queryParams.append('order_for', this.logged_in_username)
                }

                console.log(queryParams.toString())
                const result = await fetch(`api/orders?${queryParams.toString()}`);

                if(!result.ok){
                    const errorBody= await result.text()
          			throw new Error(`HTTP error! Status: ${result.status} - ${errorBody}`);
                }
                 

                let data = await result.json();
                this.activeOrder = data.map((order) => this.mapBackendToFrontend(order))
                console.log(this.activeOrder)

                if (this.activeOrder.length > 0){
                    this.activeIndex = 0;
                    this.activeOrder = this.checkForShared()
                    console.log('after filter')
                    console.log(this.activeOrder)
                } else{
                    this.activeIndex = -1;
                };
        
                console.log('active')
                console.log(this.activeOrder)
            } catch(err){
                console.error("Error getting all orders for live page: ", err)
            }
        },

        /**
         * Fetches a list of company usernames from the backend API, specifically for admin users.
         * Populates the `companyList` data property.
         */
		async getCompanyList(){
            if(this.authStore.isAdmin){
                try{
					const result = await fetch('api/companies?limit=100');
					if(!result.ok){
						const errorBody = await result.text();
						  throw new Error(`HTTP error! Status: ${result.status} - ${errorBody}`);
					}
					
					let data = await result.json();
					this.companyList = data.map((cur) => cur.username)
						
				} catch(err){
					console.error("Error getting all orders:", err);
				}
            }
		},

         /**
         * Gathers current filter values from data properties and prepares them for API requests.
         * Sets `isFilterActive` to true if any filter is present.
         */
        applyFilters(){
            const filterMap = {}
            
            if(this.operationTypeFilter !== null){
                filterMap.operation_type = this.operationTypeFilter;
                this.isFilterActive = true;
            }
            if(this.orderForFilter !== null){
                filterMap.order_for = this.orderForFilter;
                this.isFilterActive = true;
            }

            return filterMap;
        },

        /**
         * Maps backend order data structure to a more frontend-friendly format.
         * Formats dates, times, and determines 'Incoming'/'Outgoing' status.
         */
        mapBackendToFrontend(order) {
		
			const isPort = String(order.source).toLowerCase() === 'port';
			const inOut = isPort ? 'Incoming' : 'Outgoing';
			return {
				...order,
                departureTime: order.departureTime ? new Date(order.departureTime).toLocaleString() : null,
				madeAt: order.madeAt ? new Date(order.madeAt).toLocaleDateString() : null,
				twStart: order.twStart ? new Date(order.twStart).toLocaleString() : null,
				twEnd: order.twEnd ? new Date(order.twEnd).toLocaleString() : null,
				eta: order.eta ? new Date(order.eta).toLocaleString() : null,
				inOut: inOut 
			}
        },

        /**
         * Processes the `activeOrder` array to group 'scheduled' orders by `vehicleId`,
         * indicating shared routes or vehicles. 'created' orders remain ungrouped.
         */
        checkForShared() {
            // arrays for shared
            const scheduledOrders = this.activeOrder.filter((order) => order.status === 'scheduled'  || order.status === 'undergoing');
            const createdOrders = this.activeOrder.filter((order) => order.status === 'created');

            // group by vehicle
            const activeOrderMapGroupedByRouteId = new Map();
            scheduledOrders.forEach(order => {
                // now is grouped by route id
                const key = order.routeId;
                if (activeOrderMapGroupedByRouteId.has(key)) {
                    activeOrderMapGroupedByRouteId.get(key).push(order);
                } else {
                    activeOrderMapGroupedByRouteId.set(key, [order]);
                }
            });

            const combinedOrders = [];

            activeOrderMapGroupedByRouteId.forEach((orders) => {
                // Always push an object with an 'orders' property, even if it's a single order
                combinedOrders.push({
                    orders: orders
                });
            });

            createdOrders.forEach(order => {
                combinedOrders.push({
                    orders: [order]
                });
            });

            console.log('combine');
            console.log(combinedOrders);
            return combinedOrders;
        },

        /**
         * Converts a date-time string to just the time part (e.g., "HH:MM").     .
         */
        convertDateToTime(dateTimeString) {
            if (!dateTimeString) return '';
            console.log(dateTimeString)
            try {
                const timePart = dateTimeString.split(', ')[1];
                return timePart ? timePart.slice(0, 5) : '';
            } catch (error) {
                console.error('Error converting date to time:', error);
                return '';
            }
        },
    },
     props: {
        orderDetails: {
            type: Object,
            default: null
        }
    },
    created() {
        this.logged_in_username =  this.authStore.getUser.username.string; 

        this.getAllOrders();
        this.getCompanyList();
    },
    setup(){
        const authStore = useAuthStore();
        return {authStore}
    }
}
</script>

<!-- ANIMATIONS -->
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