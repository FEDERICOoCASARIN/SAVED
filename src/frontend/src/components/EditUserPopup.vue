<template>
  <div class="fixed inset-0 flex items-center justify-center bg-opacity-50 z-[100]">
    <div class="fixed inset-0 bg-orange-100/70"></div>
    <div class="p-5 bg-white rounded-md relative z-[101]">
      <div class="bg-[#f7f7f7] bg-opacity-50 rounded-md w-[800px]">
        <div class="flex justify-between items-center p-6">
          <h2 v-if="type==='User'" class="text-5xl font-bold text-[#E57229]">Edit User</h2>
          <h2 v-else class="text-5xl font-bold ">Edit Profile <span class="text-[#E57229]">#{{ userDetail.username }}</span></h2>
          <button @click="$emit('close')" class="text-gray-500 hover:text-gray-70 cursor-pointer">&times;</button>
        </div>

        <!-- Main content area for the user edit form. -->
        <div class="p-6 ">
          <div>
            <!-- Form for updating user details. Prevents default submission to handle with Vue. -->
            <form @submit.prevent="handleSubmit" class="w-full">
              <div class="flex flex-col">
                <!-- Name input field. -->
                <div class="flex items-center px-10 mb-5">
                  <label class="text-sm font-medium text-gray-700 w-32">Name</label>
                  <input v-model="formData.name" type="text" class="text-base p-2 bg-white rounded-md flex-1"></input>
                </div>

                <!-- Username input field. -->
                <div class="flex items-center px-10 mb-5">
                  <label class="text-sm font-medium text-gray-700 w-32">Username</label>
                  <input v-model="formData.username" type="text" class="text-base p-2 bg-white rounded-md flex-1"></input>
                </div>

                <!-- Latitude and Longitude input fields, visible only if the account type is not 'Admin'. -->
                <div v-if="formData.accountType !== 'Admin'" class="flex items-center px-10 gap-5 mb-5">
                  <div class="flex items-center flex-1">
                    <label class="text-sm font-medium text-gray-700 w-32">Latitude</label>
                    <input
                      v-model="formData.lat"
                      type="number"
                      class="text-base p-2 bg-white rounded-md flex-1 text-center border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#FA812F]"
                      step="0.000001"
                    />
                  </div>
                  <div class="flex items-center flex-1">
                    <label class="text-sm font-medium text-gray-700 w-32">Longitude</label>
                    <input
                      v-model="formData.long"
                      type="number"
                      class="text-base p-2 bg-white rounded-md flex-1 text-center border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#FA812F]"
                      step="0.000001"
                    />
                  </div>
                </div>

                <!-- Opening and Closing Time input fields, visible only if the account type is not 'Admin'. -->
                <div v-if="formData.accountType !== 'Admin'" class="flex items-center px-10 gap-5">
                  <div class="flex items-center flex-1">
                    <label class="text-sm font-medium text-gray-700 w-32">Opening Time</label>
                    <input
                      v-model="formData.openingTime"
                      type="time"
                      class="text-base p-2 bg-white rounded-md flex-1 text-center border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#FA812F]"
                      value="09:00"
                    />
                  </div>
                  <div class="flex items-center flex-1">
                    <label class="text-sm font-medium text-gray-700 w-32">Closing Time</label>
                    <input
                      v-model="formData.closingTime"
                      type="time"
                      class="text-base p-2 bg-white rounded-md flex-1 text-center border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#FA812F]"
                      value="17:00"
                    />
                  </div>
                </div>
              </div>
              <!-- Action buttons for canceling or saving the changes. -->
              <div class="flex justify-end gap-4 p-5 mr-5 mt-4">
                <button @click="$emit('close')" type="button" class="px-4 py-2 rounded-md hover:bg-gray-50 cursor-pointer">Cancel</button>
                <button type="submit" class="px-4 py-2 bg-[#FA812F] text-white rounded-md hover:bg-[#E57229] cursor-pointer">Save</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    userDetail: { // The current user details object used to pre-populate the form.
      type: Object, 
      required: true
    },
    type: { // Indicates the context of the popup, either 'User' (for general user editing) or 'Profile' (for current user's profile).
      type: String,
      required: true
    },
  },
  data() {
    return {
      /**
       * Reactive data object for the form fields, initialized with 'userDetail' prop values.
       * This allows local modification of form data without directly mutating props.
       * @type {object}
       */
      formData: {
        name: this.userDetail.name,
        username: this.userDetail.username,
        location: this.userDetail.location, // Keeping this if the backend expects it, even if lat/long are separate inputs
        openingTime: this.userDetail.openingTime,
        closingTime: this.userDetail.closingTime,
        accountType: this.userDetail.accountType,
        lat: this.userDetail.lat,
        long:this.userDetail.long
      }
    }
  },
  methods: {
    /**
     * Handles the form submission.
     * Prevents default form submission, emits a 'save' event with the form data,
     * and then emits a 'close' event to hide the popup.
     */
    handleSubmit() {
      // Prevents the default form submission behavior (page reload)
      event.preventDefault();

      // Emit 'save' event to the parent component, passing the current form data.
      this.$emit('save', this.formData);

      // Emit 'close' event to the parent component, signaling to hide the popup.
      this.$emit('close');
    }
  }
}
</script>

<style scoped>
/* Styles for the main modal overlay, covering the entire viewport. */
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
  background-color: rgba(0, 0, 0, 0.5); /* Semi-transparent black background */
}
.z-\[100\] {
  z-index: 100; /* High z-index to ensure it overlays other content */
}

/* Styles for the subtle orange background layer behind the main popup content. */
.bg-orange-100\/70 {
  background-color: rgba(255, 237, 213, 0.7); /* Light orange with transparency */
}

/* Styles for the primary popup content box. */
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
  z-index: 101; /* Higher z-index to sit above the inner overlay */
}

/* Styles for the inner container wrapping the form content. */
.bg-\[\#f7f7f7\] {
  background-color: #f7f7f7; /* Light gray background */
}
.bg-opacity-50 {
  background-color: rgba(247, 247, 247, 0.5); /* Semi-transparent light gray */
}
.w-\[800px\] {
  width: 800px; /* Fixed width for the form container */
}

/* Styles for the header section of the popup. */
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
  color: #E57229; /* Custom orange text color */
}
.text-gray-500 {
  color: #6b7280;
}
.hover\:text-gray-70:hover { /* This class name seems incorrect, should be hover:text-gray-700 */
  color: #374151; /* Darker gray on hover */
}
.cursor-pointer {
  cursor: pointer;
}

/* Styles for the input fields and their labels within the form. */
.px-10 {
  padding-left: 2.5rem;
  padding-right: 2.5rem;
}
.mb-5 {
  margin-bottom: 1.25rem;
}
.gap-3 {
  gap: 0.75rem; /* Spacing between label and input */
}
.text-sm {
  font-size: 0.875rem;
  line-height: 1.25rem;
}
.font-medium {
  font-weight: 500;
}
.text-gray-700 {
  color: #374151;
}
.w-32 {
  width: 8rem; /* Fixed width for labels */
}
.flex-1 {
  flex: 1 1 0%; /* Allows input field to grow and shrink */
}
.text-base {
  font-size: 1rem;
  line-height: 1.5rem;
}
.p-2 {
  padding: 0.5rem;
}
.border {
  border-width: 1px;
}
.border-gray-300 {
  border-color: #d1d5db;
}
.focus\:outline-none:focus {
  outline: 2px solid transparent;
  outline-offset: 2px;
}
.focus\:ring-2:focus {
  box-shadow: 0 0 0 2px var(--tw-ring-color);
}
.focus\:ring-\[\#FA812F\]:focus {
  --tw-ring-color: #FA812F; /* Focus ring color */
}

/* Styles for the form's action buttons (Cancel and Save). */
.justify-end {
  justify-content: flex-end;
}
.gap-4 {
  gap: 1rem;
}
.mr-5 {
  margin-right: 1.25rem;
}
.mt-4 {
  margin-top: 1rem;
}
.bg-\[\#FA812F\] {
  background-color: #FA812F; /* Custom orange for Save button */
}
.text-white {
  color: #fff;
}
.hover\:bg-\[\#E57229\]:hover {
  background-color: #E57229; /* Darker orange on hover */
}
</style>
