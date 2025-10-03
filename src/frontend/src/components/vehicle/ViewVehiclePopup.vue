<template>
  <div class="fixed inset-0 flex items-center justify-center bg-opacity-50 z-[100]">
    <div class="fixed inset-0 bg-orange-100/70"></div>
    <div class="p-5 bg-white rounded-md relative z-[101]">
      <div class="bg-[#f5f5f5] bg-opacity-50 rounded-md h-[600px] min-w-[800px]">
        <!-- Header -->
        <div class="flex justify-between items-center p-6 border-b border-gray-200">
          <h2 class="text-4xl font-bold">
            Vehicle Info <span class="text-[#E57229]">#{{ vehicleDetail.vehicleID }}</span>
          </h2>
          <button @click="$emit('close')" class="text-gray-500 hover:text-gray-700 cursor-pointer">&times;</button>
        </div>

        <!-- Content -->
        <div class="p-6 flex gap-8">
          <!-- Left side - Details -->
          <div class="flex-1 space-y-6">
            <h3 class="text-lg font-semibold text-gray-700 mb-4">Vehicle Details</h3>
            
            <!-- Details Grid -->
            <div class="grid grid-cols-2 gap-4">
              <!-- Distance -->
              <div class="flex flex-col">
                <label class="text-sm font-medium text-gray-500 mb-1">Distance</label>
                <div class="bg-white rounded-md p-2 border border-gray-200">
                  {{ vehicleDetail.distance }} km
                </div>
              </div>

              <!-- Battery -->
              <div class="flex flex-col">
                <label class="text-sm font-medium text-gray-500 mb-1">Battery Level</label>
                <div class="bg-white rounded-md p-2 border border-gray-200">
                  {{ vehicleDetail.battery }}%
                </div>
              </div>

              <!-- Status -->
              <div class="flex flex-col">
                <label class="text-sm font-medium text-gray-500 mb-1">Status</label>
                <div class="bg-white rounded-md p-2 border border-gray-200">
                  <span :class="{
                    'text-green-600': vehicleDetail.status === 'AVAILABLE',
                    'text-blue-600': vehicleDetail.status === 'IN-USE',
                    'text-red-600': vehicleDetail.status === 'OUT OF ORDER'
                  }">
                    {{ vehicleDetail.status }}
                  </span>
                </div>
              </div>

        

              <!-- Location -->
              <div class="col-span-2">
                <label class="text-sm font-medium text-gray-500 mb-1">Location</label>
                <div class="grid grid-cols-2 gap-4">
                  <div class="bg-white rounded-md p-2 border border-gray-200">
                    Lat: {{ vehicleDetail.lat }}
                  </div>
                  <div class="bg-white rounded-md p-2 border border-gray-200">
                    Long: {{ vehicleDetail.long }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Right side - Map -->
          <div class="flex-1">
            <h3 class="text-lg font-semibold text-gray-700 mb-4">Vehicle Location</h3>
            <div class="h-[400px] w-full rounded-lg overflow-hidden border border-gray-200">
              <MapComponent
                :lat="Number(vehicleDetail.lat)"
                :lng="Number(vehicleDetail.long)"
                icon="truck"
                class="w-full h-full"
                :zoomable="false"
                :zoom="16"
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

export default {
  components: {
    MapComponent,
  },
  props: {
    vehicleDetail: {
      type: Object,
      required: true,
    },
  },

   mounted(){
      document.body.style.overflow = 'hidden'; // Disable scrolling
    },
    beforeUnmount(){
      document.body.style.overflow = 'auto'; // Enable scrolling
    }
};
</script>
