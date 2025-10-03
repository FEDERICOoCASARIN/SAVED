<template>
  <!-- Interactive button component for a navigation item. -->
  <!-- Its styling dynamically changes based on whether it is the 'currentTab'. -->
  <button
    @click="() => handleClick(desc)"
    class="flex w-full items-center p-1.5 transition duration-50 rounded-md"
    :class="[
      desc === currentTab
        ? 'bg-[#F7DCC1] text-[#BD4E3C] shadow-xl' // Styles for the active tab
        : 'text-white hover:text-[#BD4E3C] hover:bg-[#FFAC59]', // Styles for inactive tabs on hover
    ]"
  >
    <!-- Slot for placing an icon or other content before the text. -->
    <slot></slot>
    <!-- Displays the description (name) of the navigation item. -->
    <span class="ms-3">{{ desc }}</span>
  </button>
</template>

<script>
export default {
  props: {
    desc: {
      type: String, // The description or name of the navigation item (e.g., 'Orders', 'Calendar').
      required: true,
    },
    handleClick: { // A function passed from the parent to be called when the item is clicked, typically to set the current tab.
      type: Function,
      required: true,
    },
    currentTab: { // The name of the currently active tab from the parent component, used to determine active styling.
      type: String,
      required: true,
    },
  },
};
</script>

<style scoped>
/* Basic styling for the button, making it a flex container to align its content. */
.flex {
  display: flex;
}
.w-full {
  width: 100%;
}
.items-center {
  align-items: center;
}
.p-1\.5 {
  padding: 0.375rem;
}
.rounded-md {
  border-radius: 0.375rem;
}

/* Transitions for smooth hover and active state changes. */
.transition {
  transition-property: background-color, color, box-shadow;
}
.duration-50 {
  transition-duration: 50ms;
}

/* Styles specific to the active navigation tab. */
.bg-\[\#F7DCC1\] {
  background-color: #F7DCC1; /* Light orange background */
}
.text-\[\#BD4E3C\] {
  color: #BD4E3C; /* Darker orange text */
}
.shadow-xl {
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1); /* Large shadow for emphasis */
}

/* Styles for inactive tabs, including hover effects. */
.text-white {
  color: white;
}
.hover\:text-\[\#BD4E3C\]:hover {
  color: #BD4E3C; /* Darker orange text on hover */
}
.hover\:bg-\[\#FFAC59\]:hover {
  background-color: #FFAC59; /* Lighter orange background on hover */
}

/* Margin for the text next to the icon. */
.ms-3 {
  margin-inline-start: 0.75rem;
}
</style>
