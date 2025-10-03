<template>
  <div class="bg-white rounded-xl shadow-sm hover:shadow-md transition-all duration-300 p-5 flex flex-col gap-4 mb-3 group border border-transparent hover:border-orange-100 " @click="$emit('select')"
  :class="{
            'bg-[#f7f7f7]': !isActive,         
            'bg-[#f7f7f7] shadow-xl ring-3 ring-[#FA812F] transform scale-102': isActive
        }"
        tabindex="0"
        role="button"
  >
    <!-- id -->
    <div class="flex justify-between items-start">
      <div class="flex items-center gap-3">
        <div class="p-3 bg-orange-50 rounded-xl group-hover:bg-orange-100 transition-colors">
         <TruckSVG />
        </div>
        <div class="flex flex-col">
          <span class="text-lg font-bold text-gray-800">#{{ vehicleID }}</span>
          <span class="text-xs text-gray-500">Vehicle ID</span>
        </div>
      </div>

      <!-- action -->
      <div class="flex gap-1">
        <button
          @click="showViewVehiclePopup = true"
          class="p-1.5 text-gray-500 hover:text-[#FA812F] hover:bg-orange-50 rounded-lg transition-colors cursor-pointer"
          title="View Details"
        >
          <ExpandViewSVG />
        </button>
        <button
          @click="showEditVehiclePopup = true"
          class="p-1.5 text-gray-500 hover:text-[#FA812F] hover:bg-orange-50 rounded-lg transition-colors cursor-pointer"
          title="Edit Vehicle"
        >
          <EditSVG />
        </button>
        <button 
          @click="showUserConfirmation = true"
          class="p-1.5 text-gray-500 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors cursor-pointer"
          title="Delete Vehicle"
        >
          <DeleteSVG />
        </button>
      </div>
    </div>

    <!-- status -->
    <div class="px-4 py-2 rounded-full text-sm font-medium transition-colors w-fit"
      :class="{
        'bg-red-100 text-red-700': vehicleDetail.status === 'OUT OF ORDER',
        'bg-blue-100 text-blue-700': vehicleDetail.status === 'IN-USE',
        'bg-green-100 text-green-700': vehicleDetail.status === 'AVAILABLE',
      }">
      {{ vehicleDetail.status }}
    </div>

    <!-- battery -->
    <div class="flex flex-col gap-2">
      <span class="text-sm font-medium text-gray-600">Battery Status</span>
      <div class="flex items-center gap-3">
        <div class="flex-1 h-2.5 bg-gray-100 rounded-full overflow-hidden">
          <div class="h-full rounded-full transition-all duration-500"
            :style="{
              width: `${vehicleDetail.battery}%`,
              backgroundColor: getBatteryColor(vehicleDetail.battery)
            }"
          ></div>
        </div>
        <span class="font-medium text-sm w-12">{{ vehicleDetail.battery }}%</span>
      </div>
    </div>

    <!--  actions -->
    

    <!-- Keep existing teleport sections -->
    <teleport to="body">
      <transition name="fade">
        <EditVehiclePopup
          v-if="showEditVehiclePopup"
          :vehicleDetail="this.vehicleDetail"
          @close="showEditVehiclePopup = false"
          @save="editVehicle"
        />
      </transition>
  </teleport>
    
    <teleport to="body">
      <transition name="fade">
        <ViewVehiclePopup
          v-if="showViewVehiclePopup"
          :vehicleDetail="this.vehicleDetail"
          @close="showViewVehiclePopup = false"
        />
      </transition>
    </teleport>

    <teleport to="body"> 
      <UserConfirmationPopup 
      :show="showUserConfirmation"
      :text="'to delete this vehicle'"
      :caption="'This action cannot be undone'"
      @confirm="handleDelete"
      @cancel="showUserConfirmation = false"
      />
      </teleport>

     
  </div>
</template>

<script>
import EditVehiclePopup from "@/components/vehicle/EditVehiclePopup.vue";
import ViewVehiclePopup from "@/components/vehicle/ViewVehiclePopup.vue";
import ArrowIncomingSVG from "@/components/svg/ArrowIncomingSVG.vue";
import ArrowOutgoingSVG from "@/components/svg/ArrowOutgoingSVG.vue";
import ExpandViewSVG from "@/components/svg/ExpandViewSVG.vue";
import EditSVG from "@/components/svg/EditSVG.vue";
import DeleteSVG from "@/components/svg/DeleteSVG.vue";
import UserConfirmationPopup from "../UserConfirmationPopup.vue";
import SuccessConfirmationPopup from "../SuccessfulConfirmationPopup.vue";
import TruckSVG from "@/components/svg/TruckSVG.vue";
export default {
  components: {
    EditVehiclePopup,
    ViewVehiclePopup,
    ArrowIncomingSVG,
    ArrowOutgoingSVG,
    ExpandViewSVG,
    EditSVG,
    DeleteSVG,
    UserConfirmationPopup,
    SuccessConfirmationPopup,
    TruckSVG

  },
  data() {
    return {
      showEditVehiclePopup: false,
      showViewVehiclePopup: false,
      showUserConfirmation: false,
      showSuccessfulConfirmation: false,
    };
  },
  props: {
    vehicleID: {
      type: Number,
      required: true,
    },
    vehicleDetail: {
      type: Object,
      required: true,
    },
      isActive: {
            type: Boolean,
            default: false
        }
  },
  methods: {
    /**
     * Handles the confirmation of a delete action.
     * Displays a success message and then emits the 'deleteVehicle' event to the parent
     */
    handleDelete(){
            this.deleteVehicle();
            
            setTimeout(() => {
                this.$emit('deleteVehicle', this.vehicleID);
            }, 3000);
        },
    
  
    deleteVehicle() {
      this.showSuccessfulConfirmation = true;
      // Change the popup title/caption temporarily for delete
      this.successTitle = "Vehicle Removed Successfully!";
      this.successCaption = "The vehicle was removed from the system.";

      setTimeout(() => {
          this.showSuccessfulConfirmation = false;
      }, 3000);
      this.showUserConfirmation = false;
    },

      /**
     * Emits the 'editVehicle' event to the parent component with the updated vehicle details.
     * Displays a temporary success message.
     */
    editVehicle(newVehicleDetails) {
      this.$emit("editVehicle", this.vehicleID, newVehicleDetails);

      this.showSuccessfulConfirmation = true;

      setTimeout(() => {
        this.showSuccessfulConfirmation = false;
      }, 3000);
    },

    /**
     * Determines the appropriate color for the battery level display.
     */
    getBatteryColor(level) {
      level = Number(level);
      if (level > 60) return '#22c55e'; // Green
      if (level > 30) return '#f59e0b'; // Yellow
      return '#ef4444'; // Red
    }
  },
  computed: {
    animationDelay() {
      return this.index *1000; // Delay each row by 100ms
    },
  },
};
</script>

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-enter-to {
  opacity: 1;
  transform: translateY(0);
}

/* Improve hover state smoothness */
.group:hover {
  transform: translateY(-1px);
}
</style>