<template>
  <transition name="fade-slide" appear>
    <div class="flex flex-col h-screen overflow-hidden">
      <!-- Header section with a logout button. -->
      <div class="flex bg-[#efefee] p-3 md:p-4 items-center">
        <button
          class="bg-[#FA812F] ml-auto text-white rounded-lg p-2 cursor-pointer text-sm md:text-base hover:shadow-xl hover:translate-Y-2 transition-colors"
          @click="showConfirmLogout = true">
          <span class="flex items-center">
            <LogOutSVG />
          </span>
        </button>
      </div>

      <!-- Main content area for user profile -->
      <div class="flex flex-col md:flex-row flex-1 w-full bg-[#f7f7f7] overflow-y-auto">
        <div v-if="isLoading" class="absolute inset-0 flex items-center justify-center bg-white bg-opacity-80 z-40">
          <div class="animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-orange-500"></div>
        </div>

        <div v-else class="w-full md:w-2/5 xl:w-1/3 p-4 md:p-6 lg:p-8 flex-shrink-0">
          <div class="bg-white rounded-md shadow-sm p-6 lg:p-8">
            <div class="mb-6">
              <h1 class="text-3xl lg:text-4xl font-bold">Hey,</h1>
              <h1 class="text-4xl lg:text-5xl font-bold text-orange-500">
                {{ this.userDetail.username }}!
              </h1>
            </div>
            <!-- User's name display field. -->
            <div class="flex flex-col space-y-4">

              <div class="flex flex-col sm:flex-row items-start sm:items-center">
                <label class="text-sm font-medium text-gray-700 w-32 flex-shrink-0 mb-1 sm:mb-0">Name:</label>
                <span class="text-base p-2 bg-gray-50 rounded-md flex-1 ml-0 sm:ml-4">{{ this.userDetail.name }}</span>
              </div>
              <!-- Section title for extra details. -->
              <div class="text-sm pt-4 pb-2 border-b border-b-black/20 font-semibold text-gray-600">
                Details
              </div>

              <!-- Additional user details like username and account type. -->
              <div class="flex flex-col space-y-4 pt-2">
                <div class="flex flex-col sm:flex-row items-start sm:items-center">
                  <label class="text-sm font-medium text-gray-700 w-32 flex-shrink-0 mb-1 sm:mb-0">Username:</label>
                  <span
                    class="text-base p-2 bg-gray-50 rounded-md flex-1 ml-0 sm:ml-4">{{ this.userDetail.username }}</span>
                </div>
                <div class="flex flex-col sm:flex-row items-start sm:items-center">
                  <label class="text-sm font-medium text-gray-700 w-32 flex-shrink-0 mb-1 sm:mb-0">Account Type:</label>
                  <span class="text-base p-2 bg-gray-50 rounded-md flex-1 ml-0 sm:ml-4">{{ authStore.isAdmin ? 'Admin' :
                    'Company'}}</span>
                </div>

                <!-- Company-specific details (opening/closing time), visible only for 'User' type (which implies company). -->
                <div v-if="userType === 'User'" class="flex flex-col sm:flex-row items-start sm:items-center">
                  <label class="text-sm font-medium text-gray-700 w-32 flex-shrink-0 mb-1 sm:mb-0">Opening Time:</label>
                  <span
                    class="text-base p-2 bg-gray-50 rounded-md flex-1 ml-0 sm:ml-4">{{ this.userDetail.opening_time }}</span>
                </div>

                <div v-if="userType === 'User'" class="flex flex-col sm:flex-row items-start sm:items-center">
                  <label class="text-sm font-medium text-gray-700 w-32 flex-shrink-0 mb-1 sm:mb-0">Closing Time:</label>
                  <span class="text-base p-2 bg-gray-50 rounded-md flex-1 ml-0 sm:ml-4">{{ this.userDetail.closing_time
                    }}</span>
                </div>
              </div>

              <!-- Section title for extra settings. -->
              <div class="text-sm pt-4 pb-2 border-b border-b-black/20 font-semibold text-gray-600">
                Extra Settings
              </div>

              <!-- Reset Password button. -->
              <div class="pt-2">
                <button @click="showResetPasswordPopup = true"
                  class="cursor-pointer w-full p-3 text-white bg-[#FA812F] rounded-md hover:bg-orange-600 transition-colors">
                  Reset Password
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Map component section, occupying the remaining width and managing its height. -->
        <div class="flex-1 w-full p-4 md:p-6 lg:p-8 min-h-[300px] md:min-h-0">
          <!-- Container for the map, ensuring it fills available space and has a shadow. -->
          <div class="w-full h-full bg-white rounded-md shadow-sm overflow-hidden pointer-events-non">
            <MapComponent :lat="51.8830" :lng="4.435" class="w-full h-full z-0" :zoomable="false" />
          </div>
        </div>
      </div>

      <!-- Popup for editing user details. -->
      <transition name="fade">
        <EditUserPopup v-if="showEditUserPopup" :type="'Profile'" :userDetail="this.userDetail"
          @close="this.showEditUserPopup = false"
          class="fixed inset-0 z-50 bg-black/30 flex items-center justify-center" @save="updateUser" />
      </transition>

      <!-- Popup for admin registration. -->
      <transition name="fade">
        <AdminRegistrationPopup v-if="showAdminRegistrationPopup" @close="this.showAdminRegistrationPopup = false"
          class="fixed inset-0 z-50 bg-black/30 flex items-center justify-center"
          @tokenGenerated="handleAdminTokenGenerated" />
      </transition>

      <!-- Popup for resetting user password. -->
      <transition name="fade">
        <ResetPasswordPopup v-if="showResetPasswordPopup" @close="this.showResetPasswordPopup = false"
          class="fixed inset-0 z-50 bg-black/30 flex items-center justify-center" />
      </transition>

      <!-- Confirmation popup for logout. -->
      <transition>
        <UserConfirmationPopup :show="showConfirmLogout" @cancel="showConfirmLogout = false" @confirm="handleLogout"
          :text="'to log out'" />
      </transition>

      <!-- Popup for displaying successful confirmation messages. -->
      <transition name="fade">
        <SuccessfulConfirmationPopup :show="showSuccessfulConfirmation" :title="this.successConfirmationTitle"
          :caption="this.successConfirmationCaption" @close="showSuccessfulConfirmation = false" />
      </transition>
    </div>
  </transition>
</template>

<script>
import MapComponent from '@/components/MapComponent.vue'
import ResetPasswordPopup from './ResetPasswordPopup.vue';
import EditUserPopup from "@/components/users/EditUserPopup.vue";
import AdminRegistrationPopup from "@/components/users/AdminRegistrationPopup.vue";
import UserConfirmationPopup from '../UserConfirmationPopup.vue';
import { useRouter } from 'vue-router'
import LogOutSVG from '@/components/svg/LogOutSVG.vue';
import { useAuthStore } from '@/stores/auth';
import SuccessfulConfirmationPopup from '../SuccessfulConfirmationPopup.vue';

export default {
  components: {
    MapComponent,
    EditUserPopup,
    AdminRegistrationPopup,
    ResetPasswordPopup,
    UserConfirmationPopup,
    LogOutSVG,
    SuccessfulConfirmationPopup
  },
  data() {
    return {
      showEditUserPopup: false,
      showAdminRegistrationPopup: false,
      showResetPasswordPopup: false,
      showConfirmLogout: false,
      logged_in_username: '',
      userDetail: {},
      showSuccessfulConfirmation: false,
      successConfirmationTitle: '',
      successConfirmationCaption: '',
      isLoading: true,
    }
  },
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()

    /**
     * Handles the user logout process.
     * Clears authentication state and redirects to the login page.
     */
    const handleLogout = () => {
      authStore.logout()
      router.push('/login')
    };

    return {
      handleLogout,
      authStore
    }
  },
  computed: {
    /**
     * Determines the user type ('Admin' or 'User') based on authentication store's isAdmin flag.
     * @returns {string} 'Admin' if the user is an admin, otherwise 'User'.
     */
    userType() {
      return this.authStore.isAdmin ? 'Admin' : 'Company'
    }
  },
  methods: {
    /**
     * Updates the user's profile information by sending a PATCH request to the backend.
     * Handles both admin and company user types.
     */
    // CURRENTLY NOT BEING USED.
    // async updateUser(formData){
    //   this.isLoading = true; // Set loading state

    //   console.log(formData)
    //   const long = formData.long;
    //   const lat = formData.lat;

    //   // Format location for backend (PGpoint format)
    //   const location = (long && lat) ? `(${long}, ${lat})` : null;


    //   const data = {
    //     modified_at: formData.modified_at,
    //     userType: formData.userType,
    //     username: String(formData.username),
    //     name: String(formData.name),
    //     email: formData.email,
    //     created_at: formData.created_at
    //   };

    //    const isCompany = formData.accountType === "Company" ;

    //   // Only add location-related fields for Company users
    //   if (isCompany) {
    //     if (formData.long && formData.lat) {
    //       data.x = String(formData.long);
    //       data.y = String(formData.lat);
    //     }
    //     data.location = location;
    //     if (formData.openingTime) {
    //       data.opening_time = `${formData.openingTime}:00`;
    //     }
    //     if (formData.closingTime) {
    //       data.closing_time = `${formData.closingTime}:00`;
    //     }
    //   }

    //   console.log("to backend");
    //   console.log(data);

    //   try {
    //     const result = await fetch(`api/users/${this.logged_in_username}`, {
    //       method: "PATCH",
    //       headers: {
    //         "Content-Type": "application/json",
    //       },
    //       body: JSON.stringify(data),
    //     });

    //     if (!result.ok) {
    //       const errorBody = await result.text();
    //       throw new Error(
    //         `HTTP error! Status: ${result.status} - ${errorBody}`
    //       );
    //     }

    //     this.successConfirmationTitle = "Update Success!";
    //     this.successConfirmationCaption = "New user profile has been saved";
    //     this.showSuccessfulConfirmation = true;

    //     await this.getUserByUsername(); // Refresh user data after successful update
    //   } catch (err) {
    //     console.error("Failed to update user profile:", err);
    //     // This.error is not defined in data(), consider adding it if you want to display errors
    //     // this.error = `Could not update profile: ${err.message}`;
    //   } finally {
    //     this.isLoading = false; // Reset loading state
    //     this.showEditUserPopup = false; // Close the edit popup
    //   }
    // },
    handleAdminTokenGenerated(tokenData) {
      console.log("Admin registration token generated:", tokenData);
      this.successConfirmationTitle = "Admin Registration Token Generated!";
      this.successConfirmationCaption = `A registration token has been generated for ${tokenData.email}. The registration URL has been copied to your clipboard.`;
      this.showSuccessfulConfirmation = true;
      this.showAdminRegistrationPopup = false;
    },
    /**
     * Fetches the logged-in user's details from the backend.
     * Populates the 'userDetail' data property.
     */
    async getUserByUsername() {
      this.isLoading = true; // Set loading state
      try {
        const result = await fetch(`/api/users/${this.logged_in_username}`)

        if (!result.ok) {
          const errorBody = await result.text()
          throw new Error(`HTTP error! Status: ${result.status} - ${errorBody}`);
        }

        let data = await result.json();
        // Format modified_at timestamp for display
        data = {
          ...data,
          modified_at: this.formatModifiedAt(data.modified_at)
        }
        this.userDetail = data;
        console.log(this.userDetail);
      } catch (err) {
        console.error("Error fetching user details:", err);
        // This.error is not defined in data(), consider adding it if you want to display errors
        // this.error = `Could not load user profile: ${err.message}`;
      } finally {
        this.isLoading = false; // Reset loading state
      }
    },
    /**
     * Formats a timestamp string into a readable date and time string.
     * Handles null, undefined, or invalid timestamp inputs.
     * @param {string|null|undefined} timestamp - The timestamp string (e.g., from database).
     * @returns {string} The formatted date and time string, or "N/A" if invalid.
     */
    formatModifiedAt(timestamp) {
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
  /**
   * Lifecycle hook: Called after the instance is created.
   * Initializes the logged-in username and fetches user details.
   */
  created() {
    this.logged_in_username = this.authStore.getUser.username.string;
    this.getUserByUsername();
  }
};
</script>

<style>
/* Keyframe animation for a spinning effect (used for loading indicator) */
@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

/* Applies the spin animation */
.animate-spin {
  animation: spin 1s linear infinite;
}

/* Fade transition for elements (opacity change) */
.fade-enter-active,
.fade-leave-active {
  transition-property: opacity;
  transition-duration: 200ms;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Scale and fade transition for elements */
.scale-fade-enter-active,
.scale-fade-leave-active {
  transition-property: transform, opacity;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 300ms;
}

.scale-fade-enter-from,
.scale-fade-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

/* Fade and slide transition for elements (opacity and vertical movement) */
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
