<template>
  <div
    class="fixed inset-0 flex items-center justify-center bg-opacity-50 z-[100]"
  >
    <div class="fixed inset-0 bg-orange-100/90"></div>
    <div class="pt-2 bg-[#E57229] rounded-md relative z-[101]">
      <div
        class="bg-white bg-opacity-50 rounded-md min-w-[800px] min-h-[320px]"
      >
        <div
          class="flex justify-between items-center p-6 border-b border-gray-200"
        >
         
           <div class="flex text-xs gap-1 flex-col">
             <h3 class="text-4xl font-bold">
            User Info:
            <span class="text-[#E57229]">{{ userDetail.name }}</span>
             </h3>
             <div>
                Modified at:
                       <span :class="userDetail.modified_at ? '' : 'italic'">
                         {{ this.formatTimestamp(userDetail.modified_at) || 'No Information' }}
                    </span> 
             </div>
                    
            </div>
          <button
            @click="$emit('close')"
            class="text-gray-500 hover:text-gray-700 cursor-pointer"
          >
            &times;
          </button>
        </div>
      

        <div class="p-4">
          <form class="w-full">
            <div class="flex items-start px-10 mb-5 gap-8">
              <div
                :class="
                  userDetail.userType === 'ADMIN'
                    ? 'flex flex-col w-full gap-4'
                    : 'flex flex-col min-w-[320px] gap-4'
                "
              >
               <h3 class="text-lg font-bold text-gray-700 mb-2 mt-1">
                    User Details
                  </h3>
                <div class="flex items-center">
                  <label class="text-sm font-medium text-gray-700 w-32"
                    >Name</label
                  >
                  <span class="text-base pt-2 px-1 pb-1.5 border-b border-gray-300  flex-1">{{
                    userDetail.name
                  }}</span>
                </div>
                <div class="flex items-center">
                  <label class="text-sm font-medium text-gray-700 w-32"
                    >Username</label
                  >
                  <span class="text-base pt-2 px-1 pb-1.5 border-b bg-white  border-gray-300  flex-1">{{
                    userDetail.username
                  }}</span>
                </div>
                <div class="flex items-center">
                  <label class="text-sm font-medium text-gray-700 w-32"
                    >Acc. Type</label
                  >
                  <span class="text-base pt-2 px-1 pb-1.5 border-b bg-white border-gray-300  flex-1">{{
                    userDetail.userType
                  }}</span>
                </div>


                <template v-if="userDetail.userType === 'COMPANY'">
                  <div class="flex items-center gap-4">
                    <div class="flex items-center flex-1">
                      <label class="text-sm font-medium text-gray-700 w-32"
                        >Opening Time</label
                      >
                      <span class="text-base p-2 bg-white rounded-md flex-1 border-gray-200 border-1">{{
                        userDetail.openingTime
                      }}</span>
                    </div>
                    <div class="flex items-center flex-1">
                      <label class="text-sm font-medium text-gray-700 w-32"
                        >Closing Time</label
                      >
                      <span class="text-base p-2 bg-white rounded-md flex-1 border-gray-200 border-1">{{
                        userDetail.closingTime
                      }}
                      </span>
                    </div>
                  </div>
                  <div class="flex items-center">
                    <label class="text-sm font-medium text-gray-700 w-32"
                      >Email</label
                    >
                    <span class="text-base pt-2 px-1 pb-1.5 border-b border-gray-300 bg-white  flex-1">{{
                      userDetail.email
                    }}</span>
                  </div>
                </template>
              </div>
              <template
                v-if="
                  userDetail.userType === 'PORT' ||
                  userDetail.userType === 'COMPANY'
                "
              >
                <div
                  class="flex flex-1 flex-col px-0 mt-1 rounded-md overflow-hidden min-w-[300px] max-w-[200px] max-h-[320px]"
                >
                  <h3 class="text-lg font-semibold text-gray-700 mb-4">
                    User Location
                  </h3>
                  <div
                    class="h-[400px] w-full rounded-lg overflow-hidden border border-gray-200"
                  >
                    <MapComponent
                      :markers="[
                        {
                          lat: Number(userDetail.lat),
                          lng: Number(userDetail.long),
                          icon: 'redIcon',
                          name: userDetail.name,
                        },
                      ]"
                      :zoomable="true"
                      :zoom="14"
                      class="w-full h-full"
                    />
                  </div>
                </div>
              </template>
            </div>
          </form>
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
    userDetail: {
      type: Object,
      required: true,
    },
  },
  methods: {
    formatTimestamp(timestamp) {
        if (!timestamp) {
          return "N/A";
        }

        const date = new Date(timestamp);

        // Check if the date is valid
        if (isNaN(date.getTime())) {
          return "Invalid Date";
        }

        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0'); // Months are 0-indexed
        const year = date.getFullYear();
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');

        return `${day}/${month}/${year} ${hours}:${minutes}:${seconds}`;
    }
  }
};
</script>
