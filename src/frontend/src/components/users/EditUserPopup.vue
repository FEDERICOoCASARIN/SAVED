<template>
  <div class="fixed inset-0 flex items-center justify-center bg-opacity-50 z-[100]">
    <div class="fixed inset-0 bg-orange-100/85"></div>
    <div class="p-1 bg-[#E57229] rounded-lg relative z-[101] max-w-2xl min-w-150 mx-auto">
      <div class="bg-white rounded-lg shadow-lg">
        <div class="flex justify-between items-center p-6 border-b border-gray-200">
          <h2 v-if="formData.userType === 'Company'" class="text-4xl font-extrabold text-[#E57229]">Edit Company User</h2>
          <h2 v-else class="text-4xl font-extrabold text-gray-800">
            Edit Profile <span class="text-[#E57229]">#{{  }}123131</span>
          </h2>
          <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600 text-4xl cursor-pointer transition-colors duration-200">
            &times;
          </button>
        </div>

        <div class="p-8">
          <form @submit.prevent="handleSubmit" class="w-full">
            <div class="grid grid-cols-1 gap-y-6">
              <div class="flex items-center">
                <label for="name" class="text-sm font-medium text-gray-700 w-32 min-w-[128px]">Name</label>
                <input
                  id="name"
                  v-model="formData.name"
                  type="text"
                  class="flex-1 p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#FA812F] transition-all duration-200"
                  autocomplete="given-name"
                />
              </div>

            

              <template v-if="formData.userType === 'PORT'">
                <div class="grid grid-cols-2 gap-x-6">
                  <div class="flex items-center">
                    <label for="port-lat" class="text-sm font-medium text-gray-700 w-32 min-w-[128px]">Latitude</label>
                    <input
                      id="port-lat"
                      v-model="formData.lat"
                      type="number"
                      class="flex-1 p-2 border border-gray-300 rounded-md text-center focus:outline-none focus:ring-2 focus:ring-[#FA812F] transition-all duration-200"
                      step="0.000001"
                      autocomplete="off"
                    />
                  </div>
                  <div class="flex items-center">
                    <label for="port-long" class="text-sm font-medium text-gray-700 w-32 min-w-[128px]">Longitude</label>
                    <input
                      id="port-long"
                      v-model="formData.long"
                      type="number"
                      class="flex-1 p-2 border border-gray-300 rounded-md text-center focus:outline-none focus:ring-2 focus:ring-[#FA812F] transition-all duration-200"
                      step="0.000001"
                      autocomplete="off"
                    />
                  </div>
                </div>
              </template>

              <template v-if="formData.userType === 'COMPANY'">
                  <!-- <div class="flex items-center">
                <label for="username" class="text-sm font-medium text-gray-700 w-32 min-w-[128px]">Username</label>
                <input
                  id="username"
                  v-model="formData.username"
                  type="text"
                  class="flex-1 p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#FA812F] transition-all duration-200"
                  autocomplete="username"
                />
                </div> -->
                <div class="flex items-center">
                  <label for="email" class="text-sm font-medium text-gray-700 w-32 min-w-[128px]">Email</label>
                  <input
                    id="email"
                    v-model="formData.email"
                    type="email"
                    class="flex-1 p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#FA812F] transition-all duration-200"
                    autocomplete="email"
                  />
                </div>

                <div class="grid grid-cols-2 gap-x-6">
                  <div class="flex items-center">
                    <label for="company-lat" class="text-sm font-medium text-gray-700 w-32 ">Latitude</label>
                    <input
                      id="company-lat"
                      v-model="formData.lat"
                      type="number"
                      class="flex-1 p-2 border border-gray-300 rounded-md text-center focus:outline-none focus:ring-2 focus:ring-[#FA812F] transition-all duration-200"
                      step="0.0001"
                      autocomplete="off"
                    />
                  </div>
                  <div class="flex items-center">
                    <label for="company-long" class="text-sm font-medium text-gray-700 w-32 ">Longitude</label>
                    <input
                      id="company-long"
                      v-model="formData.long"
                      type="number"
                      class="flex-1 p-2 border border-gray-300 rounded-md text-center focus:outline-none focus:ring-2 focus:ring-[#FA812F] transition-all duration-200"
                      step="0.0001"
                      autocomplete="off"
                    />
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-x-6">
                  <div class="flex items-center">
                    <label for="opening-time" class="text-sm font-medium text-gray-700 w-32 ">Opening Time</label>
                    <input
                      id="opening-time"
                      v-model="formData.openingTime"
                      type="time"
                      class="flex-1 p-2 border border-gray-300 rounded-md text-center focus:outline-none focus:ring-2 focus:ring-[#FA812F] transition-all duration-200"
                      value="09:00"
                      autocomplete="off"
                    />
                  </div>
                  <div class="flex items-center">
                    <label for="closing-time" class="text-sm font-medium text-gray-700 w-32 ">Closing Time</label>
                    <input
                      id="closing-time"
                      v-model="formData.closingTime"
                      type="time"
                      class="flex-1 p-2 border border-gray-300 rounded-md text-center focus:outline-none focus:ring-2 focus:ring-[#FA812F] transition-all duration-200"
                      value="17:00"
                      autocomplete="off"
                    />
                  </div>
                </div>
              </template>
            </div>


            <div class="flex justify-between border-t border-gray-200 mt-8 pt-4">

                <div class="flex text-xs gap-1">
                    Last modified at:
                       <span :class="formData.modified_at ? '' : 'italic'">
                         {{ new Date(formData.modified_at).toLocaleString() || 'No Information' }}
                    </span>
                </div>
               
                <div class="flex justify-end justify-between gap-4">
                
                  <button
                    @click="$emit('close')"
                    type="button"
                    class="px-6 py-2 rounded-md text-gray-600 hover:bg-gray-100 transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-gray-300 cursor-pointer"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    class="px-6 py-2 bg-[#FA812F] text-white rounded-md hover:bg-[#E57229] transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-[#FA812F] cursor-pointer"
                  >
                    Save Changes
                  </button>
                </div>
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
        userDetail: {
            type: Object,
            required: true
        },
    
    },
    data() {
        return {
            formData: {
                name: this.userDetail.name,
                username: this.userDetail.username,
                location: this.userDetail.location,
                openingTime: this.userDetail.openingTime,
                closingTime: this.userDetail.closingTime,
                userType: this.userDetail.userType,
                lat: this.userDetail.lat,
                long:this.userDetail.long,
                email: this.userDetail.email,
                oldUsername: this.userDetail.username,
                modified_at: this.userDetail.modified_at,
                created_at: this.userDetail.created_at
            }
        }
    },
    methods: {
        /**
         * Submit data
         * Emit to parent's component
         */
        handleSubmit() {
            event.preventDefault()
        

            this.formData.modified_at = this.getCurrentTimestampWithTimezone();

            this.$emit('save', this.formData)
            this.$emit('close')
        },

        /**
         * Get the current time in a timestamp (with timezone) format
         */
        getCurrentTimestampWithTimezone() {
              const now = new Date(); 
                           
              return now.toISOString(); 
            }
    },
    created(){
        console.log(this.userDetail)
    }
}
</script>