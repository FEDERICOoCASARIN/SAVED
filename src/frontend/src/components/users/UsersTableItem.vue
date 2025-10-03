<template>
  <div class="flex items-center border-b border-gray-100 hover:bg-gray-50 px-5" @click="handleRowClick">
    <div class="basis-0 grow-[3] p-4 text-gray-600 flex">
      {{ userDetail.name }}
    </div>
    <div class="basis-0 grow-[3] p-4 text-gray-600 flex">
      {{ userDetail.userType }}
    </div>
    <div class="basis-0 grow-[3] p-4 text-gray-600 flex">
      {{ userDetail.username }}
    </div>
    <div class="basis-0 grow-[3] p-4 text-gray-600 flex">
      {{ formatCreatedAt(userDetail.created_at) }}
    </div>
    <div class="basis-0 grow-[2] p-4 text-gray-600 flex justify-end">
      <div v-if="!(authStore.isAdmin && userDetail.userType === 'ADMIN')" class="relative" @click.stop>
        <button @click="toggleDropdown" ref="dropdownButton"
          class="p-1.5 hover:text-[#FA812F] hover:bg-orange-50 rounded-md cursor-pointer">
          <OptionsSVG />
        </button>
      </div>
    </div>

    <teleport to="body">
      <transition name="dropdown">
        <div v-if="isDropdownOpen" class="fixed bg-white rounded-md shadow-lg py-1 z-[100] border border-gray-200 w-48"
          :style="dropdownPosition">
          <button v-if="userDetail.userType === 'COMPANY'" @click="
            showViewUserPopup = true;
          isDropdownOpen = false;
          "
            class="w-full px-4 py-2 text-sm text-gray-700 hover:bg-orange-50 hover:text-[#FA812F] flex items-center gap-2  cursor-pointer">
            <ExpandViewSVG class="h-4 w-4" />
            View Details
          </button>
          <button v-if="userDetail.userType === 'COMPANY'" @click="
            showEditUserPopup = true;
          isDropdownOpen = false;
          "
            class="w-full px-4 py-2 text-sm text-gray-700 hover:bg-orange-50 hover:text-[#FA812F] flex items-center gap-2  cursor-pointer">
            <EditSVG class="h-4 w-4" />
            Edit User
          </button>
          <button v-if="!(authStore.isAdmin && userDetail.userType === 'ADMIN')" @click="
            showResetPasswordPopup = true;
          isDropdownOpen = false;
          "
            class="w-full px-4 py-2 text-sm text-gray-700 hover:bg-orange-50 hover:text-[#FA812F] flex items-center gap-2  cursor-pointer">
            <QuestionMarkSVG class="h-4 w-4" />
            Reset Password
          </button>
          <button v-if="!(authStore.isAdmin && userDetail.userType === 'ADMIN')" @click="
            showUserConfirmation = true;
          isDropdownOpen = false;
          "
            class="w-full px-4 py-2 text-sm text-gray-700 hover:bg-red-50 hover:text-red-500 flex items-center gap-2  cursor-pointer">
            <DeleteSVG class="h-4 w-4" />
            Delete User
          </button>
        </div>
      </transition>
    </teleport>

    <teleport to="body">
      <UserConfirmationPopup :show="showUserConfirmation" :text="'to delete this user'"
        :caption="'This action cannot be undone'" @confirm="handleDelete" @click="showUserConfirmation = false"
        @cancel="showUserConfirmation = false" />
    </teleport>

    <teleport to="body">
      <SuccessConfirmationPopup :show="showSuccessfulConfirmation" :title="successTitle" :caption="successCaption"
        @close="showSuccessfulConfirmation = false" />
    </teleport>

    <teleport to="body">
      <EditUserPopup v-if="showEditUserPopup" :show="showEditUserPopup" :userDetail="userDetail"
        @close="showEditUserPopup = false" @save="editUser" />
    </teleport>

    <teleport to="body">
      <ViewUserPopup v-if="showViewUserPopup" :show="showViewUserPopup" :userDetail="userDetail"
        @close="showViewUserPopup = false" />
    </teleport>

    <teleport to="body">
      <ResetPasswordPopup v-if="showResetPasswordPopup" :show="showResetPasswordPopup"
        @close="showResetPasswordPopup = false" />
    </teleport>
  </div>
</template>

<script>
import EditUserPopup from "@/components/users/EditUserPopup.vue";
import ViewUserPopup from "@/components/users/ViewUserPopup.vue";
import ExpandViewSVG from "@/components/svg/ExpandViewSVG.vue";
import EditSVG from "@/components/svg/EditSVG.vue";
import DeleteSVG from "@/components/svg/DeleteSVG.vue";
import UserConfirmationPopup from "../UserConfirmationPopup.vue";
import SuccessConfirmationPopup from "../SuccessfulConfirmationPopup.vue";
import OptionsSVG from "@/components/svg/OptionsSVG.vue";
import QuestionMarkSVG from "@/components/svg/QuestionMarkSVG.vue";
import ResetPasswordPopup from "../profile/ResetPasswordPopup.vue";
import { useAuthStore } from "@/stores/auth.js";

export default {
  components: {
    EditUserPopup,
    ViewUserPopup,
    ExpandViewSVG,
    EditSVG,
    DeleteSVG,
    UserConfirmationPopup,
    SuccessConfirmationPopup,
    OptionsSVG,
    ResetPasswordPopup,
    QuestionMarkSVG,
  },
  data() {
    return {
      showEditUserPopup: false,
      showViewUserPopup: false,
      showUserConfirmation: false,
      showSuccessfulConfirmation: false,
      showResetPasswordPopup: false,
      isDropdownOpen: false,
      successTitle: "User Updated Successfully!",
      successCaption: "User details were updated successfully.",
      dropdownPosition: {
        top: "0px",
        left: "0px",
      },
      authStore: useAuthStore(),
    };
  },
  props: {
    userDetail: {
      type: Object,
      required: true,
    },
  },
  watch: {
    isDropdownOpen(newValue) {
      this.$emit('dropdown-open-state-changed', newValue);
    }
  },
  methods: {
    /**
     * Handles the click event on the table row.
     * Emits a 'rowClick' event to the parent if the user is not an 'Admin' and the dropdown is not open.
     */
    handleRowClick(event) {
      if (this.userDetail.userType !== "Admin" && !this.isDropdownOpen) {
        this.$emit("rowClick", this.userDetail);
      }
    },

    /**
     * Toggles the visibility of the options dropdown menu.
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


    closeDropdown(event) {
      if (!this.$refs.dropdownButton.contains(event.target)) {
        this.isDropdownOpen = false;
      }
    },

    /**
    * Handles the confirmation of a delete action.
    * Emits the 'deleteUser' event to the parent component with the user details.
    */
    handleDelete() {
      this.deleteUser();
      setTimeout(() => {
        this.$emit("deleteUser", this.userId);
      }, 3000);
    },
    deleteUser() {
      this.$emit("deleteUser", this.userDetail);
    },
    editUser(newUserDetails) {
      this.$emit("editUser", newUserDetails);
    },

    /**
     * Handles clicks outside the dropdown menu to close it.
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
    },
    formatCreatedAt(timestamp) {
      if (!timestamp) {
        return "N/A";
      }

      const dateObj = new Date(timestamp);

      if (isNaN(dateObj.getTime())) {
        return "Invalid Date";
      }

      const isoTime = dateObj.toISOString().split("T")[1].slice(0, 8);
      const dateStr = dateObj.toLocaleDateString("en-GB");
      const formatted = `${dateStr} ${isoTime}`;

      return formatted;
    },
  },
  mounted() {
    document.addEventListener("click", this.handleClickOutside);
  },
  beforeUnmount() {
    document.removeEventListener("click", this.handleClickOutside);
  },
};
</script>
<style lang=""></style>

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

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>