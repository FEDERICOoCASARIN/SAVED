<template>
  <div class="fixed inset-0 flex items-center justify-center bg-opacity-50 z-[100]">
    <div class="fixed inset-0 bg-orange-100/90"></div>
    <div class="p-2 bg-[#E57229] rounded-md relative z-[101]">
      <div class="p-x-2 bg-white rounded-md w-[600px] ">
        <div class="flex justify-between items-center p-6">
          <!-- Title of the reset password popup. -->
          <h2 class="text-5xl font-bold text-[#E57229]">Reset Password</h2>
          <button @click="$emit('close')" class="text-gray-500 hover:text-gray-70 cursor-pointer">&times;</button>
        </div>

        <!-- Main content area for the password reset form. -->
        <div class="p-6 ">
          <div>
            <!-- Form for submitting new and confirmed passwords. -->
            <form @submit.prevent="handleSubmit" class="w-full">
              <div class="flex flex-col">
                <!-- Error message display -->
                <div v-if="error" class="px-10 mb-5 text-red-600 text-sm">
                  {{ error }}
                </div>

                <!-- New Password input field with a label and an eye icon to toggle visibility. -->
                <div class="flex items-center px-10 mb-5 gap-3 ">
                  <label class="text-sm ml-auto font-medium text-gray-700 w-32 ">New Password:</label>
                  <div class="relative flex-1 ">
                    <input :type="showPassword1 ? 'text' : 'password'"
                      class="text-base p-2 bg-white rounded-md w-full pr-10" v-model="newPassword" />
                    <!-- Button to toggle visibility of the new password. -->
                    <button type="button" class="absolute top-1/4 right-3 text-gray-500 cursor-pointer"
                      @click="showPassword1 = !showPassword1">
                      <EyeOpen class="h-5 w-5" v-if="showPassword1" />
                      <EyeClose class="h-5 w-5" v-else />
                    </button>
                  </div>
                </div>

                <!-- Confirm Password input field with a label and an eye icon to toggle visibility. -->
                <div class="flex items-center px-10 mb-5 gap-3">
                  <label class="text-sm font-medium text-gray-700 w-32 ">Confirm Password:</label>
                  <div class="relative flex-1">
                    <input :type="showPassword2 ? 'text' : 'password'"
                      class="text-base p-2 bg-white rounded-md w-full pr-10" v-model="confirmPassword" />
                    <!-- Button to toggle visibility of the confirmed password. -->
                    <button type="button" class="absolute top-1/4 right-3 text-gray-500 cursor-pointer"
                      @click="showPassword2 = !showPassword2">
                      <EyeOpen class="h-5 w-5" v-if="showPassword2" />
                      <EyeClose class="h-5 w-5" v-else />
                    </button>
                  </div>
                </div>

                <!-- Action buttons for canceling or confirming the password reset. -->
                <div class="flex justify-end gap-4 p-5 mr-5 mt-4">
                  <!-- Cancel button. Emits a 'close' event. -->
                  <button @click="$emit('close')" type="button"
                    class="px-4 py-2 rounded-md hover:bg-gray-50 cursor-pointer" :disabled="isLoading">
                    Cancel
                  </button>
                  <!-- Confirm button. Submits the form. -->
                  <button type="submit"
                    class="px-4 py-2 bg-[#FA812F] text-white rounded-md hover:bg-[#E57229] cursor-pointer relative"
                    :disabled="isLoading">
                    <span v-if="isLoading">Resetting...</span>
                    <span v-else>Confirm</span>
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import EyeOpen from '@/components/svg/EyeOpen.vue'
import EyeClose from '@/components/svg/EyeClose.vue'

export default {
  components: {
    EyeOpen, // Component for the open eye icon
    EyeClose // Component for the closed eye icon
  },
  data() {
    return {
      showPassword1: false, // Controls visibility of the first password field
      showPassword2: false, // Controls visibility of the second password field
      newPassword: '',      // Binds to the new password input
      confirmPassword: '',  // Binds to the confirm password input
      error: '',            // Error message for password validation
      isLoading: false      // Loading state for the confirm button
    }
  },
  methods: {
    /**
     * Validates the new password against common security requirements.
     * Requirements: At least 8 characters, including uppercase, lowercase, and numbers.
     */
    validatePassword(password) {
      const minLength = password.length >= 8;
      const hasUpperCase = /[A-Z]/.test(password);
      const hasLowerCase = /[a-z]/.test(password);
      const hasNumber = /[0-9]/.test(password);

      if (!minLength) return "Password must be at least 8 characters long";
      if (!hasUpperCase) return "Password must contain at least one uppercase letter";
      if (!hasLowerCase) return "Password must contain at least one lowercase letter";
      if (!hasNumber) return "Password must contain at least one number";

      return null;
    },

    /**
     * Handles the form submission for password reset.
     * Validates password strength and matches, then calls the API to reset the password.
     */
    async handleSubmit() {
      event.preventDefault();
      this.error = '';
      this.isLoading = true;

      try {
        // Validate password requirements
        const passwordError = this.validatePassword(this.newPassword);
        if (passwordError) {
          this.error = passwordError;
          return;
        }

        // Check if passwords match
        if (this.newPassword !== this.confirmPassword) {
          this.error = "Passwords do not match";
          return;
        }

        // Make API call to reset password
        const response = await fetch('/api/auth/password', {
          method: 'PATCH',
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include',
          body: JSON.stringify({
            newPassword: this.newPassword
          })
        });

        if (!response.ok) {
          const data = await response.json();
          throw new Error(data.message || 'Failed to reset password');
        }

        // Emit success event and close popup
        this.$emit('success', 'Password has been successfully reset');
        this.$emit('close');
      } catch (err) {
        this.error = err.message || 'An error occurred while resetting password';
      } finally {
        this.isLoading = false;
      }
    }
  }
}
</script>

<style scoped>
/* Styles for the main modal overlay, covering the entire viewport */
.fixed {
  position: fixed;
}

.inset-0 {
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
}

.flex {
  display: flex;
}

.items-center {
  align-items: center;
}

.justify-center {
  justify-content: center;
}

.bg-opacity-50 {
  background-color: rgba(0, 0, 0, 0.5);
  /* Semi-transparent black background */
}

.z-\[100\] {
  z-index: 100;
  /* High z-index to ensure it overlays other content */
}

/* Styles for the subtle orange background layer behind the main popup content */
.bg-orange-100\/70 {
  background-color: rgba(255, 237, 213, 0.7);
  /* Light orange with transparency */
}

/* Styles for the primary popup content box */
.p-5 {
  padding: 1.25rem;
}

.bg-white {
  background-color: #fff;
}

.rounded-md {
  border-radius: 0.375rem;
}

.relative {
  position: relative;
}

.z-\[101\] {
  z-index: 101;
  /* Higher z-index to sit above the inner overlay */
}

/* Styles for the inner container wrapping the form content */
.bg-\[\#f7f7f7\] {
  background-color: #f7f7f7;
  /* Light gray background */
}

.bg-opacity-50 {
  background-color: rgba(247, 247, 247, 0.5);
  /* Semi-transparent light gray */
}

.w-\[700px\] {
  width: 700px;
  /* Fixed width for the form container */
}

/* Styles for the header section of the popup */
.justify-between {
  justify-content: space-between;
}

.p-6 {
  padding: 1.5rem;
}

.text-5xl {
  font-size: 3rem;
  line-height: 1;
}

.font-bold {
  font-weight: 700;
}

.text-\[\#E57229\] {
  color: #E57229;
  /* Custom orange text color */
}

.text-gray-500 {
  color: #6b7280;
}

.hover\:text-gray-70:hover {
  /* This class name seems incorrect, should be hover:text-gray-700 */
  color: #374151;
  /* Darker gray on hover */
}

.cursor-pointer {
  cursor: pointer;
}

/* Styles for the input fields and their labels within the form */
.px-10 {
  padding-left: 2.5rem;
  padding-right: 2.5rem;
}

.mb-5 {
  margin-bottom: 1.25rem;
}

.gap-3 {
  gap: 0.75rem;
  /* Spacing between label and input */
}

.text-sm {
  font-size: 0.875rem;
  line-height: 1.25rem;
}

.ml-auto {
  margin-left: auto;
  /* Pushes the label to the left */
}

.font-medium {
  font-weight: 500;
}

.text-gray-700 {
  color: #374151;
}

.w-32 {
  width: 8rem;
  /* Fixed width for labels */
}

.relative {
  position: relative;
}

.flex-1 {
  flex: 1 1 0%;
  /* Allows input field to grow and shrink */
}

.text-base {
  font-size: 1rem;
  line-height: 1.5rem;
}

.p-2 {
  padding: 0.5rem;
}

.pr-10 {
  padding-right: 2.5rem;
  /* Space for the eye icon */
}

.absolute {
  position: absolute;
}

.top-1\/2 {
  top: 50%;
}

.-translate-y-1\/2 {
  transform: translateY(-50%);
  /* Vertically centers the eye icon */
}

/* Styles for the form's action buttons (Cancel and Confirm) */
.justify-end {
  justify-content: flex-end;
  /* Aligns buttons to the right */
}

.gap-4 {
  gap: 1rem;
  /* Spacing between buttons */
}

.mr-5 {
  margin-right: 1.25rem;
}

.mt-4 {
  margin-top: 1rem;
}

.bg-\[\#FA812F\] {
  background-color: #FA812F;
  /* Custom orange for Confirm button */
}

.text-white {
  color: #fff;
}

.hover\:bg-\[\#E57229\]:hover {
  background-color: #E57229;
  /* Darker orange on hover */
}

/* Styles for the error message box */
.text-red-500 {
  color: #f56565;
  /* Red color for error messages */
}

.bg-red-100 {
  background-color: #fee2e2;
  /* Light red background for error messages */
}

.rounded-md {
  border-radius: 0.375rem;
  /* Consistent border radius */
}

.p-4 {
  padding: 1rem;
  /* Padding for the error message box */
}
</style>
