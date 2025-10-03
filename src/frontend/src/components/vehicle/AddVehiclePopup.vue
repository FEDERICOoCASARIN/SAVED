<template>
  <div class="fixed inset-0 flex items-center justify-center bg-opacity-50 z-[100]">
    <div class="fixed inset-0 bg-orange-100/70"></div>
    <div class="p-5 bg-white rounded-md relative z-[101]">
      <div class="bg-gray-100 bg-opacity-50 rounded-md w-[600px]">
        <div class="flex justify-between items-center p-6">
          <h2 class="text-5xl font-bold text-[#E57229]">Add Vehicle</h2>
          <button @click="$emit('close')" class="text-gray-500 hover:text-gray-700 cursor-pointer">&times;</button>
        </div>
        
        <div class="p-6">
          <form @submit.prevent="handleSubmit" class="w-full">
            <div class="flex flex-col gap-4"> 
              <!-- Vehicle ID Field -->
              <div class="flex flex-col px-10">
                <label class="text-sm font-medium text-gray-700 mb-1">
                  Vehicle ID <span class="text-gray-500/50 text-xs">(optional)</span>
                </label>
                <input 
                  v-model="formData.vehicleID" 
                  type="text" 
                  class="text-base p-2 bg-white rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#FA812F]"
                  
                />
              </div>

              <!-- Distance Field -->
              <div class="flex flex-col px-10">
                <label class="text-sm font-medium text-gray-700 mb-1">
                  Distance <span class="text-red-500">*</span>
                </label>
                <input 
                  v-model="formData.distance" 
                  type="text" 
                  class="text-base p-2 bg-white rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#FA812F]"
                  :class="{'border-red-500': errors.distance, 'border-gray-300': !errors.distance}"
                  @blur = "validateField('distance')"
                />
                <span v-if="errors.distance" class="text-red-500 text-xs mt-1">{{ errors.distance }}</span>
              </div>

              <!-- Battery Field -->
              <div class="flex flex-col px-10">
                <label class="text-sm font-medium text-gray-700 mb-1">
                  Battery Level <span class="text-red-500">*</span>
                </label>
                <input 
                  v-model="formData.battery" 
                  type="text" 
                  class="text-base p-2 bg-white rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#FA812F]"
                  :class="{'border-red-500': errors.battery, 'border-gray-300': !errors.battery}"
                  @blur = "validateField('battery')"
                />
                <span v-if="errors.battery" class="text-red-500 text-xs mt-1">{{ errors.battery }}</span>

              </div>

              <!-- Status Field -->
              <div class="flex flex-col px-10">
                <label class="text-sm font-medium text-gray-700 mb-1">
                  Status <span class="text-red-500">*</span>
                </label>
                <select
                  v-model="formData.status"
                  class="text-base p-2 bg-white rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#FA812F]"
                  
                >
                  <option value="" disabled>Select a status</option>
                  <option value="AVAILABLE">AVAILABLE</option>
                  <option value="OUT OF ORDER">OUT OF ORDER</option>
                </select>
              </div>
            </div>

            <div class="flex justify-end gap-4 p-5 mr-5 mt-8">
              <button 
                @click="$emit('close')" 
                type="button" 
                class="px-4 py-2 rounded-md hover:bg-gray-50 border border-gray-300 cursor-pointer"
              >
                Cancel
              </button>
              <button 
                type="submit" 
                class="px-4 py-2 bg-[#FA812F] text-white rounded-md hover:bg-[#E57229] cursor-pointer"
              >
                Save
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  export default {
   
    data(){
        return{
            formData: {
                vehicleID: '',
                distance: '',
                battery: '',
                status: '',
                long: '',
                lat: '',
                inOut: '',
            },
             errors: {
              vehicleID: '',
              distance: '',
              battery: '',
              status: ''
            },
            formError: ''
        }
    },
    methods: {
      /**
       * Validates a specific form field based on its name.
       * Sets an error message in the `errors` object if validation fails.
       */
       validateField(field){
        this.errors[field] = ''
        switch(field){
          case 'distance':
          if (!this.formData.distance) {
            this.errors.distance = 'Distance is required'
          } else if (isNaN(this.formData.distance) || Number(this.formData.distance) < 0) {
            this.errors.distance = 'Please enter a valid positive number'
          }
          break
          
        case 'battery':
          if (!this.formData.battery) {
            this.errors.battery = 'Battery level is required'
          } else if (isNaN(this.formData.battery) || Number(this.formData.battery) < 0 || Number(this.formData.battery) > 100) {
            this.errors.battery = 'Battery must be between 0 and 100'
          }
          break
          
        case 'status':
          if (!this.formData.status) {
            this.errors.status = 'Status is required'
          }
          break
        }
       },

       /**
       * Validates all required fields in the form.
       */
       validateForm() {
        ['distance', 'battery', 'status'].forEach(field => {
          this.validateField(field)
        })

        return !Object.values(this.errors).some(error => error !== '')
      },
      
      /**
       * Handles the form submission. Performs validation and, if successful,
       * emits the 'save' event with the formatted data and closes the popup.
       */
      handleSubmit() {
          this.formError = ''

          if (this.validateForm()) {
            const formattedData = {
              ...this.formData,
              distance: Number(this.formData.distance),
              battery: Number(this.formData.battery)
            }

            this.$emit('save', formattedData)
            this.resetForm()
            this.$emit('close')
          } else {
            this.formError = 'Please fix the errors before submitting'
          }
        },

        /**
        * Resets all form fields and error messages to their initial empty states.
        */
        resetForm() {
          this.formData = {
            vehicleID: '',
            distance: '',
            battery: '',
            status: '',
            long: '5',
            lat: '5',
            inOut: ''
          }
          this.errors = {
            vehicleID: '',
            distance: '',
            battery: '',
            status: ''
          }
          this.formError = ''
        }
    },
    mounted(){
      document.body.style.overflow = 'hidden'; // Disable scrolling
    },
    beforeUnmount(){
      document.body.style.overflow = 'auto'; //Enable scrolling
    }
  }

</script>
