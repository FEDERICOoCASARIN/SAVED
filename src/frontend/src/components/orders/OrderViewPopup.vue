<template>
  <div class="fixed inset-0 flex items-center justify-center bg-opacity-50 z-[100]">
    <div class="fixed inset-0 bg-orange-100/90"></div>
    <div class="pt-3 p-1 bg-[#E57229] rounded-md relative ">
      <div class="bg-white bg-opacity-50 rounded-md w-[1200px] z-[101]">
        <div class="p-6 border-b border-gray-200">
          <div class="flex justify-between items-center mb-4">
            <div class="flex flex-col gap-1">
                <div class="flex items-baseline gap-4">
                    <h2 class="text-5xl font-bold">Order Info</h2>
                    <span class="text-4xl font-bold text-[#E57229]">#{{order.orderId || '123412'}}</span>
                </div>
                <span class="text-sm text-gray-500">
                    Requested by: <span class="font-medium uppercase">{{ order.requester || 'Unknown User' }}</span>
                </span>
            </div>
            <div class="flex gap-10">
             <div v-if="order.shared" class="flex items-center gap-4 border-3 border-red-500 rounded-xl px-2 max-h-12 justify-center">
              <span class="text-1xl  text-[#E57229] font-bold"> SHARED ORDER </span>
              </div>
              <button @click="$emit('close')" class="text-gray-500 hover:text-gray-700 cursor-pointer text-2xl">×</button>
            </div>
          </div>

          <div class="flex justify-between">
            

         <div class="flex items-center gap-4">
            <span class="px-4 py-3 rounded-lg text-sm font-medium text-white uppercase" 
                  :class="{
                    'bg-[#6B7280] ': order.status === 'created',
                    'bg-[#3B82F6] ': order.status === 'scheduled',
                    'bg-[#F97316] ': order.status === 'undergoing',
                    'bg-[#10B981] ': order.status === 'completed',
                    'bg-[#EF4444] ': order.status === 'canceled',

                  }">
              {{ order.status }}
            </span>
            <div class="flex items-center gap-2"
                 :class="{
                   'text-[#FF0004]': order.inOut === 'Outbound',
                   'text-[#57BA00]': order.inOut === 'Inbound',
                 }">
              <template v-if="order.inOut === 'Inbound'">
                <ArrowIncomingSVG class="w-4 h-4" />
              </template>
              <template v-else-if="order.inOut === 'Outbound'">
                <ArrowOutgoingSVG class="w-4 h-4" />
              </template>
              <span class="text-sm font-medium uppercase">{{ order.inOut }}</span>
            </div>
            
          </div>
           <div class="bg-[#f5f5f5] rounded-lg px-4 py-2  max-w-2xl">
                <div class="flex items-center justify-between">
                  <div class="flex flex-col items-center">
                  <span class="text-sm text-gray-500">
                    {{ order.status === 'created' ? 'Estimated Departure' : 'Departure Time' }}
                  </span>
                    <span class="text-lg font-bold">{{ convertDateToTime(order.twStart) }}</span>
                    <span class="text-xs text-gray-600 uppercase">{{ order.source }}</span>
                  </div>

                  <div class="flex flex-col items-center gap-2 mx-5">
                    <span class="text-sm font-medium text-gray-600">{{ formattedDistance }}</span>
                    <div class="w-30 h-[1px] bg-gray-300"></div>
                  </div>

                  <div class="flex flex-col items-center">
                    <span class="text-sm text-gray-500">{{ order.status !== 'completed' ? 'Estimated Arrival' : 'Arrival Time' }}</span>
                    <span class="text-lg font-bold" :class="order.eta ? 'text-green-700' : 'text-black'">
                        {{ order.eta ? convertDateToTime(order.eta) : 'TBD' }}
                    </span>
                    <span class="text-xs text-gray-600 uppercase">{{ order.destination || 'Destination' }}</span>
                  </div>
                </div>
              </div>
            </div>

        </div>

        <div class="p-6 bg-gray-100/50">     
          <div class="grid grid-cols-[1fr_2fr] gap-6 h-full">
            <div class="space-y-6">
              <div class="bg-white rounded-lg p-4 shadow-sm">
                <h3 class="text-sm font-medium text-gray-500 mb-4">Basic Information</h3>
                <div class="space-y-4">
                  <div class="flex items-center ">
                    <label class="text-sm font-medium text-gray-700 w-32">Date</label>
                    <span class="text-base">{{ order.madeAt }}</span>
                  </div>
                  <div class="flex items-center">
                    <label class="text-sm font-medium text-gray-700 w-32">Pick-up Point</label>
                    <span class="text-base uppercase">{{ order.source }}</span>
                  </div>
                  <div class="flex flex-row justify-between ">
                    <div class="flex items-center">
                      <label class="text-sm font-bold text-gray-700 w-32">Vehicle ID</label>
                      <span class="text-base">{{ order.vehicleId || 'N/A' }}</span>
                    </div>
                    <div class="flex items-center">
                      <label class="text-sm font-bold text-gray-700 w-32">Container ID</label>
                      <span class="text-base">{{ order.containerId || 'N/A' }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="bg-white rounded-lg p-4 shadow-sm">
                <h3 class="text-sm font-medium text-gray-500 mb-4">Time Windows</h3>
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="text-sm font-medium text-gray-700 block">Start Time</label>
                    <span class="text-base">{{ order.twStart }}</span>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-gray-700 block">End Time</label>
                    <span class="text-base">{{ order.twEnd }}</span>
                  </div>
                </div>
              </div>

              <div class="bg-white rounded-lg p-4 shadow-sm">
                <h3 class="text-sm font-medium text-gray-500 mb-4">Freight Details</h3>
                <div class="space-y-4">
                  <div class="flex items-center">
                    <label class="text-sm font-medium text-gray-700 w-32">Type</label>
                    <span class="text-base uppercase">{{ order.freightType || 'N/A' }}</span>
                  </div>
                  <div class="flex items-center">
                    <label class="text-sm font-medium text-gray-700 w-32">Weight</label>
                    <span class="text-base">{{ order.freightWeight ? order.freightWeight + ' kg' : 'N/A' }}</span>
                  </div>
                  <div class="flex items-center">
                    <label class="text-sm font-medium text-gray-700 w-32">Value</label>
                    <span class="text-base">{{ order.freightValue ? '€' + order.freightValue : 'N/A' }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="bg-white rounded-lg shadow-sm overflow-hidden">
              <MapComponent
                :markers="[
                  { lat: Number(order.sourcePosition.y), lng: Number(order.sourcePosition.x), name: 'Source Point' },
                  { lat: Number(order.destinationPosition.y), lng: Number(order.destinationPosition.x), name: 'Destination Point'}
                ]"
                :enableRouting="true"
                :source="{ lat: Number(order.sourcePosition.y), lng: Number(order.sourcePosition.x) }"
                :destination="{ lat: Number(order.destinationPosition.y), lng: Number(order.destinationPosition.x) }"
                @routeCalculated="handleRouteDistanceAndTimeFromMap"
                :zoom="12"
              />
            </div>
            
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import MapComponent from "@/components/MapComponent.vue";
import ArrowIncomingSVG from "@/components/svg/ArrowIncomingSVG.vue";
import ArrowOutgoingSVG from "@/components/svg/ArrowOutgoingSVG.vue";

export default {
  components: {
    MapComponent,
    ArrowIncomingSVG,
    ArrowOutgoingSVG
  },
  props: {
    order: {
      type: Object,
      required: true,
    },
  },
  data(){
    return{
      routeDistance :null,
      routeTime: null,
      routeTotalTimeInSeconds: null,
      routeTimeDisplay: null
    }
  },
   mounted() {
       document.body.style.overflow = 'hidden'; // Disable scrolling
    },
   
    beforeUnmount() {
        document.body.style.overflow = 'auto'; // Enable scrolling
        document.removeEventListener('click', this.handleOutsideDropdown); // Handle dropdown
    },
  methods: {
    /**
     * Handles the route distance and time information received from a map component or service.
     * Updates reactive data properties with the distance and total time, and formats the time for display.
     */
    handleRouteDistanceAndTimeFromMap(info){
       if (info && info.time !== null && info.distance !== null) {
        this.routeTotalTimeInSeconds = info.time;

        this.routeDistance = info.distance;

        console.log('Route Distance (km):', this.routeDistanceInKm);
        console.log('Route Total Time (seconds):', this.routeTotalTimeInSeconds);

     
        const minutes = Math.floor(info.time / 60);
        const seconds = info.time % 60;
        this.routeTimeDisplay = `${minutes}m ${seconds}s`; 
        console.log('Route Time (display format):', this.routeTimeDisplay);
       }
    },
 
    /**
     * Converts a date-time string (e.g., "DD/MM/YYYY, HH:MM:SS") to a 24-hour time string (e.g., "HH:MM").
     */
    convertDateToTime(dateTimeString) {
        if (!dateTimeString) return '';
        
        try {
            const timePart = dateTimeString.split(', ')[1];
            
            const time = new Date(`2000-01-01 ${timePart}`).toLocaleTimeString('en-US', {
                hour: '2-digit',
                minute: '2-digit',
                hour12: false
            });
            
            return time;
        } catch (error) {
            console.error('Error converting date to time:', error);
            return dateTimeString;
        }
    }
  },
   computed: {
    /**
     * Displays the distance in kilometers or "Calculating..." if the distance is not yet available.
     */
    formattedDistance() {
      return this.routeDistance ? `${this.routeDistance} km` : 'Calculating...';
    }
  },

};
</script>
