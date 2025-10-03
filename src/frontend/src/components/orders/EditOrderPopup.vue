<template>
  <div class="fixed inset-0 flex items-center justify-center bg-opacity-50 z-[100]">
    <div class="fixed inset-0 bg-orange-100/90"></div>
    <div class="pt-2 bg-[#E57229] rounded-md relative z-[101]">
      <div class="bg-white bg-opacity-50 rounded-md w-[700px]">
        <div class="flex justify-between items-center p-8">
          <div class="flex flex-col gap-1">
            <h2 class="text-4xl font-bold">
              Edit Order <span class="text-[#E57229]">#{{ orderDetail.orderId }}</span>
            </h2>
            <span class="text-sm text-gray-500">
              Requested by: <span class="font-medium uppercase">{{ orderDetail.requester }}</span>
            </span>
          </div>
          <button @click="$emit('close')" class="text-gray-500 hover:text-gray-700 cursor-pointer">&times;</button>
        </div>

        <div class="px-6 pb-2">
          <div class="grid grid-cols-2 gap-6">
            <div class="space-y-4">
              <div class="form-group">
                <label class="block text-sm font-medium text-gray-700 mb-2">Date</label>
                <span class="block w-full p-3 bg-gray-50 rounded-lg text-gray-700">{{ orderDetail.madeAt }}</span>
              </div>
              <div class="form-group">
                <label class="block text-sm font-medium text-gray-700 mb-2">Pick up</label>
                <span class="block w-full p-3 bg-gray-50 rounded-lg text-gray-700 uppercase">{{ orderDetail.source }}</span>
              </div>
              
            </div>

            <div class="space-y-4">
              <div class="grid grid-cols-2 gap-4">
                <div class="form-group">
                  <label class="block text-sm font-medium text-gray-700 mb-2">Status</label>
                  <span class="block w-full p-3 bg-gray-50 rounded-lg text-gray-700 uppercase">{{ orderDetail.status }}</span>
                </div>
                <div class="form-group">
                  <label class="block text-sm font-medium text-gray-700 mb-2">In/Out</label>
                  <span class="block w-full p-3 bg-gray-50 rounded-lg text-gray-700">{{ orderDetail.inOut }}</span>
                </div>
              </div>
              <div class="form-group">
                <label class="block text-sm font-medium text-gray-700 mb-2">Destination</label>
                <span class="block w-full p-3 bg-gray-50 rounded-lg text-gray-700 uppercase">{{ orderDetail.destination }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="px-6">
          <hr class="my-4 border-gray-300" />
        </div>

        <div class="p-6 pt-0">
          <form @submit.prevent="handleSubmit" class="space-y-6">
            <div class="grid grid-cols-2 gap-6">
              <div class="space-y-4">
                <div class="form-group border-1 border-gray-300 p-2 rounded-xl">
                  <label class="block text-sm font-medium text-gray-700 mb-2">Time Window</label>
                  <div class="space-y-4">
                    <div class="space-y-2">
                      <span class="text-xs text-gray-500">Start Window</span>
                      <div class="flex gap-2">
                        <input
                          v-model="formData.twStartDate"
                          type="date"
                          class="flex-1 p-3 bg-white/50 rounded-lg border border-gray-200 focus:ring-2 focus:ring-[#FA812F] focus:border-transparent text-center"
                          :class="{
                            'opacity-50 cursor-not-allowed': formData.status === 'undergoing',
                          }"
                          :disabled="formData.status === 'undergoing'"
                        />
                        <input
                          v-model="formData.twStartTime"
                          type="time"
                          class="w-32 p-3 bg-white/50 rounded-lg border border-gray-200 focus:ring-2 focus:ring-[#FA812F] focus:border-transparent text-center"
                          :class="{
                            'opacity-50 cursor-not-allowed': formData.status === 'undergoing',
                          }"
                          :disabled="formData.status === 'undergoing'"
                        />
                      </div>
                    </div>

                    <div class="space-y-2">
                      <span class="text-xs text-gray-500">End Window</span>
                      <div class="flex gap-2">
                        <input
                          v-model="formData.twEndDate"
                          type="date"
                          class="flex-1 p-3 bg-white/50 rounded-lg border border-gray-200 focus:ring-2 focus:ring-[#FA812F] focus:border-transparent text-center"
                          :class="{
                            'opacity-50 cursor-not-allowed': formData.status === 'undergoing',
                          }"
                          :disabled="formData.status === 'undergoing'"
                        />
                        <input
                          v-model="formData.twEndTime"
                          type="time"
                          class="w-32 p-3 bg-white/50 rounded-lg border border-gray-200 focus:ring-2 focus:ring-[#FA812F] focus:border-transparent text-center"
                          :class="{
                            'opacity-50 cursor-not-allowed': formData.status === 'undergoing',
                          }"
                          :disabled="formData.status === 'undergoing'"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="space-y-4 border-1 rounded-xl p-2 border-gray-199">
                <div class="form-group">
                  <label class="block text-sm font-medium text-gray-700 mb-2">Freight Type</label>
                  <div class="relative">
                    <button
                      type="button"
                      @click="showFreightTypeDropdown = !showFreightTypeDropdown"
                      class="w-full p-3 bg-white/50 rounded-lg border border-gray-200 focus:ring-2 focus:ring-[#FA812F] focus:border-transparent flex justify-between items-center uppercase"
                    >
                      <span>{{ formData.freightType || 'Select freight type' }}</span>
                      <svg
                        class="w-4 h-4 text-gray-400 transition-transform duration-200"
                        :class="{ 'rotate-180': showFreightTypeDropdown }"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                      </svg>
                    </button>
                    <div
                      v-if="showFreightTypeDropdown"
                      class="absolute z-50 w-full mt-1 bg-white rounded-lg shadow-xl border border-gray-200 max-h-48 overflow-y-auto"
                    >
                      <div
                        v-for="type in availableFreightTypes"
                        :key="type"
                        @click="selectFreightType(type)"
                        class="p-3 hover:bg-orange-50 hover:text-[#FA812F] cursor-pointer uppercase"
                      >
                        {{ type }}
                      </div>
                    </div>
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-4">
                  <div class="form-group">
                    <label class="block text-sm font-medium text-gray-700 mb-2">Weight (kg)</label>
                    <input
                      v-model="formData.freightWeight"
                      type="number" step="0.01"
                      class="w-full p-3 bg-white/50 rounded-lg border border-gray-200 focus:ring-2 focus:ring-[#FA812F] focus:border-transparent"
                      :class="{ 'border-red-500': weightError }"

                      />
                  </div>
                  
                  <div class="form-group">
                    <label class="block text-sm font-medium text-gray-700 mb-2">Value (€)</label>
                    <input
                    v-model="formData.freightValue"
                    type="number"
                    step="0.01"
                    class="w-full p-3 bg-white/50 rounded-lg border border-gray-200 focus:ring-2 focus:ring-[#FA812F] focus:border-transparent"
                    />
                  </div>
                </div>
                <p v-if="weightError" class="text-red-500 text-sm mt-1">{{ weightError }}</p>
              </div>
            </div>

            <div class="flex justify-end gap-4 pt-6 border-t border-gray-200">
              <button
                @click="$emit('close')"
                type="button"
                class="px-6 py-2.5 rounded-lg hover:bg-gray-100 text-gray-700 font-medium transition-colors cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="submit"
                class="px-6 py-2.5 bg-[#FA812F] text-white rounded-lg hover:bg-[#E57229] font-medium transition-colors cursor-pointer"
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
export default {
    props: {
        orderDetail: {
            type: Object,
            required: true
        },
    },
    data() {
        return {
            showFreightTypeDropdown: false,
            formData: {
                // Map all incoming properties
                orderId: this.orderDetail.orderId,
                containerId: this.orderDetail.containerId,
                requester: this.orderDetail.requester,
                madeAt: this.orderDetail.madeAt,
                source: this.orderDetail.source,
                sourcePosition: this.orderDetail.sourcePosition,
                destination: this.orderDetail.destination,
                destinationPosition: this.orderDetail.destinationPosition,
                twStartDate: '',  // Will be set in mounted
                twStartTime: '',  // Will be set in mounted
                twEndDate: '',    // Will be set in mounted
                twEndTime: '',    // Will be set in mounted
                status: this.orderDetail.status,
                inOut: this.orderDetail.inOut,
                freightWeight: this.orderDetail.freightWeight,
                freightType: this.orderDetail.freightType,
                freightValue: this.orderDetail.freightValue,
                operationType: this.orderDetail.operationType,
                vehicleId: this.orderDetail.vehicleId,
                routeId: this.orderDetail.routeId,
                shared: this.orderDetail.shared,
                eta: this.orderDetail.eta
            },
            freightTypes: [
                'food',
                'electronic',
                'textile',
                'machinery',
                'chemical',
                'other'
            ],
            weightError: null
        }
    },
    computed: {
      /**
         * Filters the `freightTypes` array to exclude the currently selected freight type.
         * To prevent the current type from appearing in the dropdown list when it's already selected.
         */
      availableFreightTypes() {
          return this.freightTypes.filter(type => type !== this.formData.freightType);
      },

      /**
         * Checks if the start time window of the order has already passed.
      */
      isTimePassed() {
        const now = new Date();
        
        const parts = this.orderDetail.twStart.split(', ');
        const dateParts = parts[0].split('/'); 
        const timeParts = parts[1].split(':'); 

        const target = new Date(
            Number(dateParts[2]),          
            Number(dateParts[1]) - 1,      
            Number(dateParts[0]),          
            Number(timeParts[0]),          
            Number(timeParts[1]),          
            Number(timeParts[2]) || 0      
        );
        return now >= target;
      }
    },
    methods: {
      validateForm() {
          this.weightError = null; // Reset previous errors

          if (this.formData.freightWeight > 2500) {
              this.weightError = 'Weight cannot exceed 2500 kg.';
              return false;
          }
          return true;
      },
        /**
         * Splits a combined date-time string (e.g., "DD/MM/YYYY, HH:MM:SS") into separate
         * date and time strings in formats suitable for  input fields
         */
        splitDateTime(dateTimeString) {
            if (!dateTimeString) return { date: '', time: '' };
            
            try {
                const [datePart, timePart] = dateTimeString.split(', ');
                const [day, month, year] = datePart.split('/');
                const [hours, minutes] = timePart.split(':');
                
                return {
                    date: `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`,
                    time: `${hours.padStart(2, '0')}:${minutes.padStart(2, '0')}`
                };
            } catch (error) {
                console.error('Error splitting datetime:', error);
                return { date: '', time: '' };
            }
        },
          /**
         * Combines a date string and a time string into
         * the specific "DD/MM/YYYY, HH:MM:00" format required by the backend.
         */
        combineDateTime(date, time) {
            return `${date.split('-').reverse().join('/')}, ${time}:00`;
        },


        /**
         * Handles the form submission event.
         */
        handleSubmit() {
            if (!this.validateForm()) {
              // If validation fails, stop the submission
              return;
          }
            const submissionData = {
                freightType: this.formData.freightType,

                twStart: this.combineDateTime(this.formData.twStartDate, this.formData.twStartTime),
                twEnd: this.combineDateTime(this.formData.twEndDate, this.formData.twEndTime),
                freightWeight: Number(this.formData.freightWeight),
                freightValue: Number(parseFloat(this.formData.freightValue).toFixed(2)),
            };

            console.log('popup')
            console.log(submissionData)
            // Remove temporary fields
            delete submissionData.twStartDate;
            delete submissionData.twStartTime;
            delete submissionData.twEndDate;
            delete submissionData.twEndTime;

            this.$emit('save', submissionData);
            this.$emit('close');
        },

        /**
         * Selects a freight type from the dropdown and closes the dropdown.
         */
        selectFreightType(type) {
            this.formData.freightType = type; // Changed from orderData to formData
            this.showFreightTypeDropdown = false;
        },
        
     
    },
    mounted() {
        // initialize time windows
        const startWindow = this.splitDateTime(this.orderDetail.twStart);
        const endWindow = this.splitDateTime(this.orderDetail.twEnd);
        
        this.formData = {
            ...this.formData,
            twStartDate: startWindow.date,
            twStartTime: startWindow.time,
            twEndDate: endWindow.date,
            twEndTime: endWindow.time
        };

         document.body.style.overflow = 'hidden'; // Disable scrolling
    },
   
    beforeUnmount() {
        document.body.style.overflow = 'auto';  // Enable scrolling back
        document.removeEventListener('click', this.handleOutsideDropdown);
    },
}
</script>