<template>
  <div class="relative" ref="dropdown">
    <button
      type="button"
      :class="[
        'border cursor-pointer flex justify-center items-center gap-1 h-full rounded-md p-1 px-2 text-black transition-colors duration-200 min-w-[120px] whitespace-nowrap',
        modelValue
          ? 'bg-orange-50 border-[#FA812F] text-[#FA812F] border-2' /* Styles when a sort option is selected */
          : 'bg-white border-gray-400' /* Default styles when no sort option is selected */
      ]"
      @click="toggleDropdown"
    >
      <!-- Sort icon, dynamically colored based on selection state. -->
      <SortArrowSVG :class="modelValue ? 'text-[#FA812F]' : 'text-gray-500'" class="flex-shrink-0" />
      <span class="flex items-center text-sm truncate gap-1">
        Sort by:
        <span class="font-bold">
          {{ modelValue || 'None' }}
        </span>
      </span>
      <DropdownArrow :class="{ 'rotate-180': isOpen }" class="flex-shrink-0 ml-1"/>
    </button>
    <div
      v-if="isOpen"
      class="absolute left-0 top-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg z-50 min-w-full overflow-hidden"
    >
      <!-- Unordered list to display the sort options, with dividers between items. -->
      <ul class="divide-y divide-gray-100">
        <!-- Loop through each 'option' to create a selectable list item. -->
        <li
          v-for="option in options"
          :key="option"
          @click="selectOption(option)"
          class="flex items-center justify-between px-4 py-2.5 cursor-pointer text-sm transition-colors duration-150"
          :class="[
            option === modelValue
              ? 'bg-orange-50 text-[#FA812F] font-medium' /* Styles for the currently selected option */
              : 'hover:bg-gray-50' /* Hover styles for other options */
          ]"
        >
          <span>{{ option }}</span>
          <!-- Checkmark icon, displayed only next to the currently selected option. -->
          <CheckSVG v-if="option === modelValue"/>
        </li>
        <!-- "Clear Sort" option, visible only if a sort option is currently selected. -->
        <li
          v-if="modelValue"
          @click="clearSort"
          class="px-4 py-2.5 hover:bg-red-50 cursor-pointer text-red-500 transition-colors duration-150 border-t border-gray-100 flex items-center gap-2 text-md"
        >
          <!-- Delete icon for the "Clear Sort" action. -->
          <DeleteSVG />
          <span class=" font-medium">Clear Sort</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import SortArrowSVG from './svg/SortArrowSVG.vue'
import DropdownArrow from './svg/DropdownArrow.vue'
import CheckSVG from './svg/CheckmarkSVG.vue'
import DeleteSVG from './svg/DeleteSVG.vue'

export default {
  name: 'SortFunc',
  components: {
    SortArrowSVG,
    DropdownArrow,
    CheckSVG,
    DeleteSVG
  },
  props: {
    // An array of strings representing the available sorting options.
    options: { 
      type: Array,
      required: true
    },
    // The currently selected sort option. Used with `v-model` for two-way binding.
    modelValue: {
      type: String,
      default: null
    },
    // The default text displayed when no option is selected (though `modelValue` directly handles this in template).
    title: {
      type: String,
      default: 'None' // This prop is used as a fallback if modelValue is null/empty
    }
  },
  data() {
    return {
      /**
       * Controls the visibility of the dropdown menu.
       * @type {boolean}
       */
      isOpen: false
    }
  },
  /**
   * Lifecycle hook: Called after the component is mounted to the DOM.
   * Adds a global click event listener to handle clicks outside the dropdown, closing it.
   */
  mounted() {
    document.addEventListener('click', this.handleClickOutside)
  },
  /**
   * Lifecycle hook: Called before the component is unmounted from the DOM.
   * Removes the global click event listener to prevent memory leaks.
   */
  beforeUnmount() {
    document.removeEventListener('click', this.handleClickOutside)
  },
  methods: {
    /**
     * Toggles the visibility of the dropdown menu.
     */
    toggleDropdown() {
      this.isOpen = !this.isOpen
    },
    /**
     * Selects a given option, updates the `modelValue` via emit, and closes the dropdown.
     * @param {string} value - The value of the selected option.
     */
    selectOption(value) {
      this.$emit('update:modelValue', value)
      this.isOpen = false
    },
    /**
     * Clears the currently selected sort option, setting `modelValue` to null, and closes the dropdown.
     */
    clearSort() {
      this.$emit('update:modelValue', null)
      this.isOpen = false
    },
    /**
     * Handles clicks outside the dropdown component. If a click occurs outside, it closes the dropdown.
     * @param {Event} event - The DOM click event object.
     */
    handleClickOutside(event) {
      // Check if the click event target is outside the dropdown's root element
      if (this.$refs.dropdown && !this.$refs.dropdown.contains(event.target)) {
        this.isOpen = false
      }
    }
  }
}
</script>

<style scoped>
/* Positioning context for absolute dropdown content. */
.relative {
  position: relative;
}

/* Basic styling for the dropdown button, enabling flex layout and initial appearance. */
.flex { display: flex; }
.justify-center { justify-content: center; }
.items-center { align-items: center; }
.gap-1 { gap: 0.25rem; }
.h-full { height: 100%; }
.rounded-md { border-radius: 0.375rem; }
.p-1 { padding: 0.25rem; }
.px-2 { padding-left: 0.5rem; padding-right: 0.5rem; }
.text-black { color: #000; }
.transition-colors { transition-property: background-color, border-color, color; }
.duration-200 { transition-duration: 200ms; }
.min-w-\[120px\] { min-width: 120px; }
.whitespace-nowrap { white-space: nowrap; }

/* Styles for the button when a value is selected. */
.bg-orange-50 { background-color: #fff7ed; }
.border-\[\#FA812F\] { border-color: #FA812F; }
.text-\[\#FA812F\] { color: #FA812F; }
.border-2 { border-width: 2px; }

/* Default styles for the button when no value is selected. */
.bg-white { background-color: #fff; }
.border-gray-400 { border-color: #9ca3af; }

/* Styling for SVG icons within the button. */
.flex-shrink-0 { flex-shrink: 0; }
.text-gray-500 { color: #6b7280; }
.ml-1 { margin-left: 0.25rem; }

/* Transform for rotating the dropdown arrow. */
.rotate-180 { transform: rotate(180deg); }

/* Styling for the dropdown content panel when open. */
.absolute { position: absolute; }
.top-full { top: 100%; }
.mt-1 { margin-top: 0.25rem; }
.bg-white { background-color: #fff; }
.border-gray-200 { border-color: #e5e7eb; }
.shadow-lg { box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1); }
.z-50 { z-index: 50; }
.min-w-full { min-width: 100%; }
.overflow-hidden { overflow: hidden; }

/* Styling for the list of options inside the dropdown. */
.divide-y > :not([hidden]) ~ :not([hidden]) { border-top-width: 1px; }
.divide-gray-100 { border-color: #f3f4f6; }

/* Styling for individual list items (options). */
.px-4 { padding-left: 1rem; padding-right: 1rem; }
.py-2\.5 { padding-top: 0.625rem; padding-bottom: 0.625rem; }
.cursor-pointer { cursor: pointer; }
.text-sm { font-size: 0.875rem; line-height: 1.25rem; }
.duration-150 { transition-duration: 150ms; }
.font-medium { font-weight: 500; }

/* Hover effect for list items. */
.hover\:bg-gray-50:hover { background-color: #f9fafb; }

/* Styling for the "Clear Sort" option. */
.hover\:bg-red-50:hover { background-color: #fef2f2; }
.text-red-500 { color: #ef4444; }
.border-t { border-top-width: 1px; }
.text-md { font-size: 1rem; line-height: 1.5rem; }
</style>
