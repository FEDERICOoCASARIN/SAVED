<template>
  <div class="fixed inset-0 flex items-center justify-center bg-opacity-50 z-[100]">
    <div class="fixed inset-0 bg-orange-100/70"></div>
    <div class="p-5 bg-white rounded-md relative z-[101]">
      <div class="bg-[#f5f5f5] bg-opacity-50 rounded-md w-[800px]">
        <!-- Header -->
        <div class="flex justify-between items-start p-6 border-b border-gray-200">
          <div>
            <div class="flex items-baseline gap-3">
              <h2 class="text-4xl font-bold text-gray-800">Vehicle</h2>
              <span class="text-4xl font-bold text-[#E57229]">#{{ vehicleDetail.vehicleID }}</span>
            </div>
            <p class="text-sm text-gray-500 mt-2">Edit vehicle details</p>
          </div>
          <button @click="$emit('close')" class="text-gray-500 hover:text-gray-700 cursor-pointer text-2xl">&times;</button>
        </div>
        
        <!-- Form Content -->
        <div class="p-6">
          <form @submit.prevent="handleSubmit" class="w-full">
            <div class="grid grid-cols-2 gap-6">
              <!-- Left Column -->
              <div class="space-y-4">
                <!-- Vehicle ID -->
                <div class="form-group">
                  <label class="block text-sm font-medium text-gray-700 mb-1">Vehicle ID</label>
                  <input 
                    v-model="formData.vehicleID" 
                    type="text"
                    disabled
                    class="w-full text-base p-2 bg-gray-50 rounded-md border border-gray-200"
                  />
                </div>

                <!-- Distance -->
                <div class="form-group">
                  <label class="block text-sm font-medium text-gray-700 mb-1">
                    Distance (km) <span class="text-red-500">*</span>
                  </label>
                  <input 
                    v-model="formData.distance" 
                    type="number"
                    step="0.1"
                    required
                    class="w-full text-base p-2 bg-white rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#FA812F]"
                  />
                </div>

                <!-- Battery -->
                <div class="form-group">
                  <label class="block text-sm font-medium text-gray-700 mb-1">
                    Battery Level (%) <span class="text-red-500">*</span>
                  </label>
                  <input 
                    v-model="formData.battery" 
                    type="number"
                    min="0"
                    max="100"
                    required
                    class="w-full text-base p-2 bg-white rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#FA812F]"
                  />
                </div>
              </div>

              <!-- Right Column -->
              <div class="space-y-4">
                <!-- Status -->
                <div class="form-group">
                  <label class="block text-sm font-medium text-gray-700 mb-1">
                    Status <span class="text-red-500">*</span>
                  </label>
                  <select
                    v-model="formData.status"
                    required
                    class="w-full text-base p-2 bg-white rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#FA812F]"
                  >
                    <option value="IN-USE">In Use</option>
                    <option value="AVAILABLE">Available</option>
                    <option value="OUT OF ORDER">Out of Order</option>
                  </select>
                </div>

              

                <!-- Location -->
                <div class="form-group">
                  <label class="block text-sm font-medium text-gray-700 mb-1">Location</label>
                  <div class="grid grid-cols-2 gap-2">
                    <input 
                      v-model="formData.lat" 
                      type="text"
                      placeholder="Latitude"
                      class="text-base p-2 bg-gray-50 rounded-md border border-gray-200"
                      disabled
                    />
                    <input 
                      v-model="formData.long" 
                      type="text"
                      placeholder="Longitude"
                      class="text-base p-2 bg-gray-50 rounded-md border border-gray-200"
                      disabled
                    />
                  </div>
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex justify-end gap-4 mt-8 pt-6 border-t border-gray-200">
              <button 
                @click="$emit('close')" 
                type="button" 
                class="px-4 py-2 text-gray-700 bg-white rounded-md border border-gray-300 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-gray-200 cursor-pointer"
              >
                Cancel
              </button>
              <button 
                type="submit" 
                class="px-4 py-2 text-white bg-[#FA812F] rounded-md hover:bg-[#E57229] focus:outline-none focus:ring-2 focus:ring-[#FA812F]"
              >
                Save Changes
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import MapComponent from '@/components/MapComponent.vue'


export default {
    components:{
        MapComponent
    },

    props: {
        vehicleDetail: {
            type: Object,
            required: true
        },
    },
    data() {
        return {
            formData: {
                distance: this.vehicleDetail.distance,
                battery: this.vehicleDetail.battery,
                password: this.vehicleDetail.password,
                lat: this.vehicleDetail.lat,
                long: this.vehicleDetail.long,
                status: this.vehicleDetail.status,
                inOut: this.vehicleDetail.inOut,
                vehicleID:this.vehicleDetail.vehicleID
            }
        }
    },
    methods: {
        handleSubmit() {
            event.preventDefault()

            this.$emit('save', this.formData)
            this.$emit('close')
        }
    },
    mounted() {
        document.body.style.overflow = 'hidden' // Disable Scrolling
    },
    beforeUnmount(){
        document.body.style.overflow = 'auto' // Enable Scrolling
    }
}
</script>
