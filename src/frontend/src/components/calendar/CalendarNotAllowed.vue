<template>
  <div v-if="showPopup" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
    <div class="bg-white rounded-lg p-6 max-w-sm w-full shadow-lg text-center relative">
      <button @click="$emit('close')" class="absolute top-3 right-3 text-gray-400 hover:text-gray-600 focus:outline-none">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
        </svg>
      </button>

      <!-- Icon indicating an error or 'not allowed' state. Styled in red. -->
      <div class="text-red-500 mb-4">
        <svg class="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2A9 9 0 111 12a9 9 0 0118 0z"/>
        </svg>
      </div>

      <!-- Title of the popup, conveying the main message. -->
      <h3 class="text-xl font-semibold mb-3 text-gray-800">Action Not Allowed</h3>
      <!-- Dynamic caption providing more details about why the action is not allowed. -->
      <p class="text-gray-700 mb-6">
        {{errorCaption}}
      </p>

      <!-- Confirmation button to acknowledge the message and close the popup. -->
      <button
        @click="$emit('close')"
        class="bg-[#FA812F] hover:bg-[#E57229] text-white font-semibold py-2 px-5 rounded-lg cursor-pointer text-base shadow-md hover:shadow-lg transition duration-200"
      >
        Got It
      </button>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    showPopup: Boolean, //Controls the visibility of the popup.
    errorCaption: {
      type: String, //The message to be displayed in the popup, explaining why the action is not allowed.
      required: true
    }
  }
}
</script>

<style scoped>
/* Scoped styles for the CalendarNotAllowed component, ensuring they only apply within this component */

/* Styles for the main modal overlay */
.fixed {
  position: fixed; /* Positions the overlay relative to the viewport */
}

.inset-0 {
  top: 0;
  right: 0;
  bottom: 0;
  left: 0; /* Stretches the overlay to cover the entire viewport */
}

.bg-black\/50 {
  background-color: rgba(0, 0, 0, 0.5); /* Semi-transparent black background for a dimmed effect */
}

.flex {
  display: flex; /* Enables flexbox layout for centering */
}

.items-center {
  align-items: center; /* Vertically centers content within the flex container */
}

.justify-center {
  justify-content: center; /* Horizontally centers content within the flex container */
}

.z-50 {
  z-index: 50; /* Ensures the popup is on top of most other content */
}

/* Styles for the popup's content box */
.bg-white {
  background-color: #fff; /* White background for the popup */
}

.rounded-lg {
  border-radius: 0.5rem; /* Rounded corners for the popup box */
}

.p-6 {
  padding: 1.5rem; /* Padding inside the popup box */
}

.max-w-sm {
  max-width: 24rem; /* Maximum width for the popup */
}

.w-full {
  width: 100%; /* Ensures the popup takes full width up to max-w-sm */
}

.shadow-lg {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.1); /* Standard large shadow */
}

.text-center {
  text-align: center; /* Centers text content horizontally */
}

.relative {
  position: relative; /* Allows for absolute positioning of the close button inside */
}

/* Styles for the close button */
.absolute {
  position: absolute; /* Positions the button relative to its parent (the popup box) */
}

.top-3 {
  top: 0.75rem; /* Distance from the top edge */
}

.right-3 {
  right: 0.75rem; /* Distance from the right edge */
}

.text-gray-400 {
  color: #9ca3af; /* Light gray color for the icon */
}

.hover\:text-gray-600:hover {
  color: #4b5563; /* Darker gray on hover */
}

.focus\:outline-none:focus {
  outline: 2px solid transparent;
  outline-offset: 2px; /* Removes default browser outline on focus */
}

/* Styles for the error icon */
.text-red-500 {
  color: #ef4444; /* Red color for the icon */
}

.mb-4 {
  margin-bottom: 1rem; /* Margin below the icon */
}

.mx-auto {
  margin-left: auto;
  margin-right: auto; /* Centers the icon horizontally */
}

.h-12 {
  height: 3rem; /* Height of the icon */
}

.w-12 {
  width: 3rem; /* Width of the icon */
}

/* Styles for the popup title */
.text-xl {
  font-size: 1.25rem;
  line-height: 1.75rem;
}

.font-semibold {
  font-weight: 600;
}

.mb-3 {
  margin-bottom: 0.75rem; /* Margin below the title */
}

.text-gray-800 {
  color: #1f2937; /* Dark gray text color */
}

/* Styles for the error caption text */
.text-gray-700 {
  color: #374151; /* Medium dark gray text color */
}

.mb-6 {
  margin-bottom: 1.5rem; /* Margin below the caption */
}

/* Styles for the "Got It" button */
.bg-\[\#FA812F\] {
  background-color: #FA812F; /* Custom orange background */
}

.hover\:bg-\[\#E57229\]:hover {
  background-color: #E57229; /* Darker orange on hover */
}

.text-white {
  color: #fff;
}

.py-2 {
  padding-top: 0.5rem;
  padding-bottom: 0.5rem;
}

.px-5 {
  padding-left: 1.25rem;
  padding-right: 1.25rem;
}

.rounded-lg {
  border-radius: 0.5rem;
}

.cursor-pointer {
  cursor: pointer; /* Indicates the button is clickable */
}

.text-base {
  font-size: 1rem;
  line-height: 1.5rem;
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
  transition-duration: 200ms; /* Transition speed for hover/active states */
}
</style>
