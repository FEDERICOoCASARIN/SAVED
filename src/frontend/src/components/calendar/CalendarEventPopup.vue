<template>
  <div v-if="show" class="absolute inset-0 z-50" :style="popupStyle">
    <div class="fixed inset-0" @click="$emit('close')"></div>
    
    <div class="bg-white rounded-lg w-[400px] relative z-10 shadow-2xl border border-gray-200">
      <!-- Color strip -->
      <div class="h-1.5 rounded-t-lg" :style="{ backgroundColor: `${event.backgroundColor}`}"></div>
      
      <!-- Header -->
      <div class="p-4 border-b border-gray-100">
        <div class="flex justify-between items-start">
          <h2 class="text-xl font-semibold text-gray-900">{{ event.title }}</h2>
          <button @click="$emit('close')" class="p-2 hover:bg-gray-100 rounded-full">
            <svg class="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>

      <!-- Content -->
      <div class="p-4 space-y-4">
        <!-- Time section -->
        <div class="flex items-start space-x-4">
          <div class="mt-1">
            <svg class="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <div>
            <p class="font-medium text-gray-900">
              {{ formatDateRange(event.start, event.end) }}
            </p>
            <p class="text-sm text-gray-500">
              {{ formatTime(event.start) }} - {{ formatTime(event.end) }}
            </p>
          </div>
        </div>

        <!-- Status section -->
        <div class="flex items-start space-x-4">
          <div class="mt-1">
            <svg class="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
          </div>
          <div>
            <p class="font-medium text-gray-900">Status</p>
            <div class="mt-1">
              <span class="px-2 py-1 text-sm rounded-lg text-white uppercase" 
                :class="{
                    'bg-[#6B7280]': event.status === 'created',
                    'bg-[#3B82F6]': event.status === 'scheduled',
                    'bg-[#F97316]': event.status === 'undergoing',
                    'bg-[#10B981]': event.status === 'completed',
                    'bg-[#EF4444]': event.status === 'canceled',

                }">
                {{ event.status }}
              </span>
            </div>
          </div>
        </div>

        <!-- Order ID section -->
        <div class="flex items-start space-x-4">
          <div class="mt-1">
            <svg class="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                d="M7 20l4-16m2 16l4-16M6 9h14M4 15h14" />
            </svg>
          </div>
          <div>
            <p class="font-medium text-gray-900">Requested By</p>
            <p class="text-sm text-gray-600">
              <span class="uppercase">
                {{ event.requester }}

              </span>
            
            </p>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="px-4 py-3 bg-gray-50 rounded-b-lg flex justify-end space-x-2">
      
        <button 
          @click="$emit('close')"
          class="px-4 py-2 bg-[#FA812F] text-white hover:bg-[#E57229] rounded-md text-sm font-medium">
          Close
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    show: {
      type: Boolean, // Controls the visibility of the popup.
      default: false
    },
    event: {
      type: Object, // The event object containing details to be displayed (title, start, end, color, status, requester).
      required: true
    },
    x: {
      type: Number, // The x-coordinate for positioning the popup.
      required: true
    },
    y: {
      type: Number, // The y-coordinate for positioning the popup.
      required: true
    },
    useRight: {
      type: Boolean, // A boolean indicating if the popup should align to the right of the x coordinate.
      required: true
    }
  },
  methods: {
    /**
     * Formats a date range into a readable string (e.g., "Monday, July 1 - Tuesday, July 2").
     * If the start and end dates are the same, it returns only the date.
     * @param {Date|string} start - The start date of the event.
     * @param {Date|string} end - The end date of the event.
     * @returns {string} The formatted date range string.
     */
    formatDateRange(start, end) {
      const startDate = new Date(start);
      const endDate = new Date(end);

      if (startDate.toDateString() === endDate.toDateString()) {
        return startDate.toLocaleDateString('en-US', {
          weekday: 'long',
          month: 'long',
          day: 'numeric',
        });
      }

      return `${startDate.toLocaleDateString('en-US', {
        weekday: 'short',
        month: 'long',
        day: 'numeric',
      })} - ${endDate.toLocaleDateString('en-US', {
        weekday: 'short',
        month: 'long',
        day: 'numeric',
      })}`;
    },

    /**
     * Formats a given date object or string into a localized time string.
     * @param {Date|string} date - The date to format.
     * @returns {string} The formatted time string (e.g., "10:00 AM").
     */
    formatTime(date) {
      return new Date(date).toLocaleTimeString('en-US', {
        hour: 'numeric',
        minute: '2-digit',
        hour12: true
      });
    }
  },
  computed: {
    /**
     * Computes the dynamic inline style for positioning the popup based on x, y coordinates and 'useRight' prop.
     * @returns {object} An object containing CSS style properties for the popup.
     */
    popupStyle() {
      const style = {
        top: `${this.y + 10}px` // Position slightly below the click point
      };
      // Determine if the popup should align its left or right edge with the given x coordinate
      if (this.useRight) {
        style.right = `calc(100% - ${this.x}px)`; // Position from right
      } else {
        style.left = `${this.x}px`; // Position from left
      }
      return style;
    }
  },
}
</script>

<style scoped>
/* Scoped styles for the CalendarEventPopup component, ensuring they only apply within this component */

/* Styles for the main popup container and its overlay */
.absolute {
  position: absolute; /* Positions the popup relative to its parent container */
}

.fixed {
  position: fixed; /* Ensures the overlay covers the entire viewport */
}

.inset-0 {
  top: 0;
  right: 0;
  bottom: 0;
  left: 0; /* Stretches the element to cover the entire parent/viewport */
}

.z-50 {
  z-index: 50; /* High z-index to ensure the popup is on top of other content */
}

/* Styles for the background overlay when the popup is open */
.bg-black\/50 {
  background-color: rgba(0, 0, 0, 0.5); /* Semi-transparent black for a dimmed background effect */
}

.flex {
  display: flex; /* Enables flexbox layout */
}

.items-center {
  align-items: center; /* Vertically centers items in a flex container */
}

.justify-center {
  justify-content: center; /* Horizontally centers items in a flex container */
}

.p-4 {
  padding: 1rem; /* Padding around elements */
}

/* Styles for the popup's main content box */
.bg-white {
  background-color: #fff; /* White background for the popup */
}

.rounded-lg {
  border-radius: 0.5rem; /* Rounded corners for the popup */
}

.w-\[400px\] {
  width: 400px; /* Fixed width for the popup */
}

.relative {
  position: relative; /* Allows for absolute positioning of children within this container */
}

.z-10 {
  z-index: 10; /* Ensures the popup content is above the overlay */
}

.shadow-2xl {
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25); /* Large shadow for a lifted appearance */
}

.border {
  border-width: 1px; /* Adds a border */
}

.border-gray-200 {
  border-color: #e5e7eb; /* Light gray border color */
}

/* Styling for the colored strip at the top of the popup */
.h-1\.5 {
  height: 0.375rem; /* Small height for the color strip */
}

.rounded-t-lg {
  border-top-left-radius: 0.5rem;
  border-top-right-radius: 0.5rem; /* Rounds only the top corners */
}

/* Styling for the popup header section */
.p-4 {
  padding: 1rem;
}

.border-b {
  border-bottom-width: 1px; /* Bottom border for the header */
}

.border-gray-100 {
  border-color: #f3f4f6; /* Very light gray border color */
}

.justify-between {
  justify-content: space-between; /* Spaces items evenly with space between them */
}

.items-start {
  align-items: flex-start; /* Aligns items to the start of the cross axis */
}

.text-xl {
  font-size: 1.25rem;
  line-height: 1.75rem;
}

.font-semibold {
  font-weight: 600;
}

.text-gray-900 {
  color: #111827; /* Darkest gray text color */
}

/* Styling for the close button within the header */
.p-2 {
  padding: 0.5rem;
}

.hover\:bg-gray-100:hover {
  background-color: #f3f4f6; /* Light gray background on hover */
}

.rounded-full {
  border-radius: 9999px; /* Makes the button perfectly round */
}

.w-5 {
  width: 1.25rem;
}

.h-5 {
  height: 1.25rem;
}

.text-gray-500 {
  color: #6b7280; /* Medium gray text/icon color */
}

/* Styling for the main content body of the popup */
.space-y-4 > :not([hidden]) ~ :not([hidden]) {
  --tw-space-y-reverse: 0;
  margin-top: calc(1rem * calc(1 - var(--tw-space-y-reverse)));
  margin-bottom: calc(1rem * var(--tw-space-y-reverse)); /* Vertical spacing between content sections */
}

.space-x-4 > :not([hidden]) ~ :not([hidden]) {
  --tw-space-x-reverse: 0;
  margin-right: calc(1rem * var(--tw-space-x-reverse));
  margin-left: calc(1rem * calc(1 - var(--tw-space-x-reverse))); /* Horizontal spacing for icons and text */
}

.mt-1 {
  margin-top: 0.25rem;
}

.font-medium {
  font-weight: 500;
}

.text-gray-900 {
  color: #111827;
}

.text-sm {
  font-size: 0.875rem;
  line-height: 1.25rem;
}

/* Styling for the status badge */
.px-2 {
  padding-left: 0.5rem;
  padding-right: 0.5rem;
}

.py-1 {
  padding-top: 0.25rem;
  padding-bottom: 0.25rem;
}

.uppercase {
  text-transform: uppercase; /* Makes status text all caps */
}

/* Specific background colors for different statuses */
.bg-\[\#6B7280\] {
  background-color: #6B7280; /* Gray for 'created' */
}
.bg-\[\#3B82F6\] {
  background-color: #3B82F6; /* Blue for 'scheduled' */
}
.bg-\[\#F97316\] {
  background-color: #F97316; /* Orange for 'undergoing' */
}
.bg-\[\#10B981\] {
  background-color: #10B981; /* Green for 'completed' */
}
.bg-\[\#EF4444\] {
  background-color: #EF4444; /* Red for 'canceled' */
}

/* Styling for the footer section of the popup */
.bg-gray-50 {
  background-color: #f9fafb; /* Light gray background */
}

.rounded-b-lg {
  border-bottom-left-radius: 0.5rem;
  border-bottom-right-radius: 0.5rem; /* Rounds only the bottom corners */
}

.justify-end {
  justify-content: flex-end; /* Aligns items to the end of the flex container */
}

/* Styling for the footer button */
.bg-\[\#FA812F\] {
  background-color: #FA812F; /* Custom orange color */
}

.text-white {
  color: #fff;
}

.hover\:bg-\[\#E57229\]:hover {
  background-color: #E57229; /* Darker orange on hover */
}

.rounded-md {
  border-radius: 0.375rem; /* Slightly rounded corners */
}
</style>
