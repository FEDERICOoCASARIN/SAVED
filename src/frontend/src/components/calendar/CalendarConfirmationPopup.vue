<template>
  <div v-if="showModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
    <div class="bg-white rounded-lg p-8 max-w-md w-full shadow-2xl text-center relative">
      <button @click="$emit('cancel')" class="absolute top-4 right-4 text-gray-400 hover:text-gray-600 focus:outline-none">
        <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
        </svg>
      </button>

      <!-- Modal title -->
      <h3 class="text-2xl font-bold mb-6 text-gray-800">Review and Confirm Event Change</h3>
      <div class="text-left mb-8 space-y-5">
        <div class="bg-gray-50 p-4 rounded-lg border border-gray-200">
          <p class="text-gray-600 font-semibold mb-2 flex items-center">
            <svg class="h-5 w-5 mr-2 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
            <span class="text-lg">Before Change:</span>
          </p>
          <!-- Display of original start and end times, formatted using 'formatDate' method. -->
          <div class="ml-7 text-gray-700 leading-relaxed">
            <p><strong class="font-medium">Start:</strong> {{ formatDate(pendingChange.oldStart) }}</p>
            <p><strong class="font-medium">End:</strong> {{ formatDate(pendingChange.oldEnd) }}</p>
          </div>
        </div>

        <!-- Box displaying the "After Change" details. Styled with a light blue background and border. -->
        <div class="bg-blue-50 p-4 rounded-lg border border-blue-200">
          <!-- Header for "After Change" section, including an SVG document icon. -->
          <p class="text-blue-700 font-semibold mb-2 flex items-center">
            <svg class="h-5 w-5 mr-2 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"/>
            </svg>
            <span class="text-lg">After Change:</span>
          </p>
          <!-- Display of new start and end times, formatted using 'formatDate' method. -->
          <div class="ml-7 text-gray-800 leading-relaxed">
            <p><strong class="font-medium">Start:</strong> {{ formatDate(pendingChange.newStart) }}</p>
            <p><strong class="font-medium">End:</strong> {{ formatDate(pendingChange.newEnd) }}</p>
          </div>
        </div>

        <!-- Informational note about vehicle reassignment possibility. -->
        <div>
          <p class="text-sm text-gray-500 italic mt-4">
            Note: Your vehicle may be reassigned as a result of this schedule change.
          </p>
        </div>
      </div>

      <!-- Action buttons for confirming or undoing the change. -->
      <div class="flex justify-center space-x-4">
        <button
          @click="$emit('confirm')"
          class="bg-[#FA812F] hover:bg-[#E57229] text-white font-semibold py-3 px-8 rounded-lg cursor-pointer text-lg shadow-md hover:shadow-lg transition duration-200"
        >
          Confirm Change
        </button>
        <!-- Undo button. Emits a 'cancel' event when clicked. -->
        <button
          @click="$emit('cancel')"
          class="bg-red-600 hover:bg-red-700 text-white font-semibold py-3 px-8 rounded-lg cursor-pointer text-lg shadow-md hover:shadow-lg transition duration-200"
        >
          Undo
        </button>
      </div>
    </div>
  </div>
</template>


<script>
export default {
  /**
   * Props received by this component.
   * @property {boolean} showModal - Controls the visibility of the modal.
   * @property {Object} pendingChange - An object containing details of the event change (old and new start/end times, event data).
   */
  props: {
    showModal: Boolean,
    pendingChange: Object
  },
  methods: {
    /**
     * Formats a given date object or string into a localized date and time string.
     * @param {Date|string} date - The date to format.
     * @returns {string} The formatted date and time string, or '-' if the date is null/invalid.
     */
    formatDate(date) {
      return date ? new Date(date).toLocaleString() : '-'
    }
  }
}
</script>

<style scoped>
/* Scoped styles ensure these CSS rules only apply to this component */

/* Styles for the main modal overlay */
.fixed {
  position: fixed;
}

.inset-0 {
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
}

.bg-black\/50 {
  background-color: rgba(0, 0, 0, 0.5); /* Semi-transparent black background */
}

.flex {
  display: flex;
}

.items-center {
  align-items: center; /* Vertically center content */
}

.justify-center {
  justify-content: center; /* Horizontally center content */
}

.z-50 {
  z-index: 50; /* High z-index to ensure it's on top of other content */
}

.p-4 {
  padding: 1rem; /* Padding around the modal content */
}

/* Styles for the modal content container */
.bg-white {
  background-color: #fff;
}

.rounded-lg {
  border-radius: 0.5rem; /* Rounded corners */
}

.p-8 {
  padding: 2rem;
}

.max-w-md {
  max-width: 28rem; /* Maximum width for the modal */
}

.w-full {
  width: 100%; /* Full width within its max-width container */
}

.shadow-2xl {
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25); /* Large shadow for depth */
}

.text-center {
  text-align: center;
}

.relative {
  position: relative; /* For absolute positioning of the close button */
}

/* Styles for the close button */
.absolute {
  position: absolute;
}

.top-4 {
  top: 1rem;
}

.right-4 {
  right: 1rem;
}

.text-gray-400 {
  color: #9ca3af; /* Light gray text color */
}

.hover\:text-gray-600:hover {
  color: #4b5563; /* Darker gray on hover */
}

.focus\:outline-none:focus {
  outline: 2px solid transparent; /* Remove default focus outline */
  outline-offset: 2px;
}

/* Styles for the modal title */
.text-2xl {
  font-size: 1.5rem;
  line-height: 2rem;
}

.font-bold {
  font-weight: 700;
}

.mb-6 {
  margin-bottom: 1.5rem;
}

.text-gray-800 {
  color: #1f2937; /* Dark gray text */
}

/* Styles for the event details section */
.text-left {
  text-align: left;
}

.mb-8 {
  margin-bottom: 2rem;
}

.space-y-5 > :not([hidden]) ~ :not([hidden]) {
  --tw-space-y-reverse: 0;
  margin-top: calc(1.25rem * calc(1 - var(--tw-space-y-reverse)));
  margin-bottom: calc(1.25rem * var(--tw-space-y-reverse)); /* Vertical spacing between children */
}

/* Styles for "Before Change" and "After Change" boxes */
.p-4 {
  padding: 1rem;
}

.rounded-lg {
  border-radius: 0.5rem;
}

.border {
  border-width: 1px;
}

.border-gray-200 {
  border-color: #e5e7eb;
}

.bg-gray-50 {
  background-color: #f9fafb;
}

.text-gray-600 {
  color: #4b5563;
}

.font-semibold {
  font-weight: 600;
}

.mb-2 {
  margin-bottom: 0.5rem;
}

.mr-2 {
  margin-right: 0.5rem;
}

.text-lg {
  font-size: 1.125rem;
  line-height: 1.75rem;
}

.ml-7 {
  margin-left: 1.75rem;
}

.leading-relaxed {
  line-height: 1.625;
}

/* Specific styling for "After Change" box */
.bg-blue-50 {
  background-color: #eff6ff;
}

.border-blue-200 {
  border-color: #bfdbfe;
}

.text-blue-700 {
  color: #1d4ed8;
}

.text-blue-600 {
  color: #2563eb;
}

.text-gray-800 {
  color: #1f2937;
}

/* Styles for the informational note */
.text-sm {
  font-size: 0.875rem;
  line-height: 1.25rem;
}

.text-gray-500 {
  color: #6b7280;
}

.italic {
  font-style: italic;
}

.mt-4 {
  margin-top: 1rem;
}

/* Styles for the action buttons */
.justify-center {
  justify-content: center;
}

.space-x-4 > :not([hidden]) ~ :not([hidden]) {
  --tw-space-x-reverse: 0;
  margin-right: calc(1rem * var(--tw-space-x-reverse));
  margin-left: calc(1rem * calc(1 - var(--tw-space-x-reverse))); /* Horizontal spacing between buttons */
}

.bg-\[\#FA812F\] {
  background-color: #FA812F; /* Custom orange color */
}

.hover\:bg-\[\#E57229\]:hover {
  background-color: #E57229; /* Darker orange on hover */
}

.text-white {
  color: #fff;
}

.py-3 {
  padding-top: 0.75rem;
  padding-bottom: 0.75rem;
}

.px-8 {
  padding-left: 2rem;
  padding-right: 2rem;
}

.rounded-lg {
  border-radius: 0.5rem;
}

.cursor-pointer {
  cursor: pointer;
}

.shadow-md {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1);
}

.hover\:shadow-lg:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.1);
}

.transition {
  transition-property: color, background-color, border-color, text-decoration-color, fill, stroke, opacity, box-shadow, transform, filter, backdrop-filter;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 150ms; 
}

.duration-200 {
  transition-duration: 200ms;
}

/* Specific style for the "Undo" button (red background) */
.bg-red-600 {
  background-color: #dc2626;
}

.hover\:bg-red-700:hover {
  background-color: #b91c1c;
}
</style>
