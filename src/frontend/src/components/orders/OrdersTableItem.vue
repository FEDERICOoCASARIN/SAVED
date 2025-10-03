<template>
  <div class="flex items-center border-b border-gray-100 hover:bg-gray-50">
    <!-- Order ID -->
    <div class="basis-0 grow-[1.5] p-4 font-medium text-gray-700 flex justify-center">{{ orderDetail.orderId }}</div>

    <!-- Created At Date  -->
    <div class="basis-0 grow-[1.5] p-4 font-medium text-gray-700 flex justify-center">{{ orderDetail.madeAt }}</div>

    <!-- Pickup location  -->
    <div class="basis-0 grow-[3] p-4 font-medium text-gray-700 flex justify-center uppercase">{{ orderDetail.source }}</div>

    <!-- ETD r -->
    <div v-if="orderDetail.departureTime" class="basis-0 grow-[1] p-4 font-medium text-gray-700 flex justify-center">{{  convertDateToTime(orderDetail.departureTime)}}</div>
    <div v-else class="basis-0 grow-[1] p-4 flex justify-center font-bold text-gray-700">N/A</div>

    <!-- ETA  -->
    <div v-if="orderDetail.eta" class="basis-0 grow-[1] p-4 font-medium text-gray-700 flex justify-center">{{ convertDateToTime(orderDetail.eta) }}</div>
    <div v-else class="basis-0 grow-[1] p-4 flex justify-center font-bold text-gray-700">N/A</div>

    <!-- Status badge -->
    <div class="basis-0 grow-[2] p-4 font-medium text-gray-700">
      <div
        class="flex p-2 rounded-xl text-white text-xs justify-center uppercase"
        :class="{
                    'bg-[#6B7280]/80': orderDetail.status === 'created',
                    'bg-[#3B82F6]/80': orderDetail.status === 'scheduled',
                    'bg-[#F97316]/80': orderDetail.status === 'undergoing',
                    'bg-[#10B981]/80': orderDetail.status === 'completed',
                    'bg-[#EF4444]/80 ': orderDetail.status=== 'canceled',

                  }"
      >
        {{ orderDetail.status }}
      </div>
    </div>

    <div
      class="basis-0 grow-[1.5] p-4 font-medium flex justify-center"
      :class="{
        'text-[#FF0004]': orderDetail.inOut === 'Outbound',
        'text-[#57BA00]': orderDetail.inOut === 'Inbound',
      }"
    >
      <span class="flex items-center gap-1 flex justify-center uppercase text-sm">
        <template v-if="orderDetail.inOut === 'Inbound'">
          <ArrowIncomingSVG />
        </template>
        <template v-else-if="orderDetail.inOut === 'Outbound'">
          <ArrowOutgoingSVG />
        </template>
        {{ orderDetail.inOut }}
      </span>
    </div>

    <!-- Action buttons  -->
    <div class="basis-0 grow-[1.5] p-4 text-gray-600 flex justify-center">
      <div class="relative" @click.stop>
        <button
          @click="toggleDropdown"
          ref="dropdownButton"
          class="p-1.5 hover:text-[#FA812F] hover:bg-orange-50 rounded-md cursor-pointer"
        >
          <OptionsSVG />
        </button>
      </div>
    </div>

    <!-- Action options -->

    <teleport to="body">
      <transition name="dropdown">
        <div
          v-if="isDropdownOpen"
          class="fixed bg-white rounded-md shadow-lg py-1 z-[100] border border-gray-200 w-48"
          :style="dropdownPosition"
        >
        <!-- View Order button -->
          <button
            @click="
              showViewOrderPopup = true;
              isDropdownOpen = false;
            "
            class="w-full px-4 py-2 text-sm text-gray-700 hover:bg-orange-50 hover:text-[#FA812F] flex items-center gap-2  cursor-pointer"
          >
            <ExpandViewSVG class="h-4 w-4" />
            View Order
          </button>
          <!-- Edit Order button -->
          <button 
            v-if="(authStore.isUser & (orderDetail.status === 'scheduled' || orderDetail.status === 'created')) || (authStore.isAdmin & orderDetail.status !== 'completed')"
            @click="
              showEditOrderPopup = true;
              isDropdownOpen = false;
            "
            class="w-full px-4 py-2 text-sm text-gray-700 hover:bg-orange-50 hover:text-[#FA812F] flex items-center gap-2  cursor-pointer"
          >
            <EditSVG class="h-4 w-4" />
            Edit Order
          </button>
          <!-- Cancel Order button -->
          <button
            v-if="orderDetail.status === 'scheduled' || orderDetail.status === 'created'"
            @click="openConfirmation('cancel')"
            class="w-full px-4 py-2 text-sm text-gray-700 hover:bg-orange-50 hover:text-[#FA812F] flex items-center gap-2  cursor-pointer"
          >
            <CancelSVG class="h-4 w-4" />
            Cancel Order
          </button>

            <!-- Delete Order button -->
           <button
            @click="openConfirmation('delete')"

            v-if="authStore.isAdmin"
            class="w-full px-4 py-2 text-sm text-gray-700 hover:bg-orange-50 hover:text-[#FA812F] flex items-center gap-2  cursor-pointer "
          >
            <DeleteSVG class="h-4 w-4" />
            Delete Order
          </button>
        </div>
      </transition>
    </teleport>

    <teleport to="body">
      <transition name="fade">
        <EditOrderPopup
      v-if="showEditOrderPopup"
      @close="showEditOrderPopup = false"
      @save="updateOrder"
      :orderDetail="orderDetail"
    />
      </transition>
    </teleport>
    
    <teleport to="body">
      <transition name="fade">
        <OrderViewPopup
      v-if="showViewOrderPopup"
      @close="showViewOrderPopup = false"
      :order="orderDetail"
    />
      </transition>
    </teleport>
  
    <teleport to="body" >
      <UserConfirmationPopup 
    :show="showUserConfirmation"
    @cancel="showUserConfirmation = false"
    @confirm="confirmAction"
    :text="actionType === 'cancel' ? 'to cancel this order' : 'to delete this order'"
    :caption="'This action cannot be undone.'"
    />
    </teleport>

    <teleport to="body">
      <SuccessConfirmationPopup 
      :show="showSuccessfulConfirmation"
      :title="successTitle"
      :caption="successCaption"
      @close="showSuccessfulConfirmation = false"
    />
    </teleport>
    
  </div>
</template>

<script>
import ArrowIncomingSVG from "@/components/svg/ArrowIncomingSVG.vue";
import ArrowOutgoingSVG from "@/components/svg/ArrowOutgoingSVG.vue";
import ExpandViewSVG from "@/components/svg/ExpandViewSVG.vue";
import EditSVG from "@/components/svg/EditSVG.vue";
import DeleteSVG from "@/components/svg/DeleteSVG.vue";
import CancelSVG from "@/components/svg/CancelSVG.vue";
import OptionsSVG from "@/components/svg/OptionsSVG.vue";
import EditOrderPopup from "@/components/orders/EditOrderPopup.vue";
import UserConfirmationPopup from "../UserConfirmationPopup.vue";
import OrderViewPopup from "./OrderViewPopup.vue";
import SuccessConfirmationPopup from "../SuccessfulConfirmationPopup.vue";
import { useAuthStore } from '@/stores/auth';


export default {
  components: {
    ArrowIncomingSVG,
    ArrowOutgoingSVG,
    ExpandViewSVG,
    EditSVG,
    DeleteSVG,
    EditOrderPopup,
    UserConfirmationPopup,
    OrderViewPopup,
    SuccessConfirmationPopup,
    CancelSVG,
    OptionsSVG,
    UserConfirmationPopup
  },
  props: {

    orderDetail: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      showEditOrderPopup: false,
      showUserConfirmation: false,
      showViewOrderPopup: false,
      showSuccessfulConfirmation: false,
      isDropdownOpen: false,
      actionType: null,
      successTitle: "Order Updated Successfully!",
      successCaption: "The order has been successfully updated."
    };
  },
  methods: {
     /**
     * Opens the user confirmation dialog for a specific action type.
     */
    openConfirmation(type) {
      this.actionType = type;
      this.showUserConfirmation = true;
      this.isDropdownOpen = false;
    },

    /**
     * Confirms the pending action (cancel or delete order) and emits the corresponding event.
     * Closes the confirmation dialog and resets the action type.
     */
    confirmAction() {
      this.showUserConfirmation = false;

      if (this.actionType === 'cancel') {
        this.$emit("cancelOrder", this.orderDetail.orderId);
      } else if (this.actionType === 'delete') {
        this.$emit("deleteOrder", this.orderDetail.orderId);
      }

      this.actionType = null;
    },
 
    /**
     * Emits an event to update an order with new data.
     */
    updateOrder(updatedOrder) {
      this.$emit("updateOrder", { id: this.orderDetail.orderId, dataFromUser: updatedOrder });
    },

    /**
     * Converts a full date-time string (e.g., "DD/MM/YYYY, HH:MM:SS") to just the time part (e.g., "HH:MM").
     */
    convertDateToTime(dateTimeString) {
      if (!dateTimeString) return '';
        try {
            // "18/06/2025, 12:00:00" -> "12:00"
            const timePart = dateTimeString.split(', ')[1];
            return timePart ? timePart.slice(0, 5) : '';
        } catch (error) {
            console.error('Error converting date to time:', error);
            return '';
        }
    },

    /**
     * Toggles the visibility of a dropdown menu and calculates its optimal position
     * to ensure it stays within the viewport.
     */
    toggleDropdown(event) {
      if (!this.isDropdownOpen) {
        this.$nextTick(() => {
          const button = this.$refs.dropdownButton;
          if (button) { 
            const rect = button.getBoundingClientRect();
            const dropdownHeight = 160; // Approximate height of your dropdown (4 buttons * 40px each)
            const dropdownWidth = 192; 
            const viewportHeight = window.innerHeight || document.documentElement.clientHeight;
            const spaceBelow = viewportHeight - rect.bottom;
            const spaceAbove = rect.top;

            let topPosition;
            let leftPosition = `${rect.right - dropdownWidth}px`; 

            if (spaceBelow < dropdownHeight && spaceAbove >= dropdownHeight) {
              topPosition = `${rect.top - dropdownHeight}px`; 
            } else {
              topPosition = `${rect.bottom + 8}px`;
            }

            this.dropdownPosition = {
              top: topPosition,
              left: leftPosition,
            };
            this.isDropdownOpen = true; 
          }
        });
      } else {
        this.isDropdownOpen = false;
      }
      if (event) event.stopPropagation();
    },
    
    /**
       * Handles clicks outside the dropdown and its toggle button to close the dropdown.
       */
    handleClickOutside(event) {
      if (
        this.isDropdownOpen &&
        this.$refs.dropdownButton &&
        !this.$refs.dropdownButton.contains(event.target)
      ) {
        const dropdownMenu = document.querySelector(
          ".fixed.bg-white.rounded-md.shadow-lg"
        );
        if (!dropdownMenu || !dropdownMenu.contains(event.target)) {
          this.isDropdownOpen = false;
        }
      }
    }
  },
  watch: {
    /**
     * Watches for changes in `isDropdownOpen` and emits an event when its state changes.
     */
    isDropdownOpen(newValue) {
      this.$emit('dropdown-open-state-changed', newValue);
    }
  },
  setup() {
    const authStore = useAuthStore();
    return { authStore }
  },
  mounted() {
      document.addEventListener("click", this.handleClickOutside);
  },
  beforeUnmount() {
    document.removeEventListener("click", this.handleClickOutside);
  },
};

</script>

<style>

.fade-slide-enter-active,
.fade-slide-leave-active {
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

</style>