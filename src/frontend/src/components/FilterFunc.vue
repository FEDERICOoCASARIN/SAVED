<template>
  <div class="relative" ref="dropdown">
    <!-- The dropdown's main button. Its styling dynamically changes based on whether an option is selected. -->
    <button
      type="button"
      :class="[
        'border cursor-pointer flex justify-center items-center gap-1 h-full rounded-md p-2 text-black transition-colors duration-200 ',
        modelValue
          ? 'bg-orange-50 border-[#FA812F] text-[#FA812F] border-2' // Styles when an option is selected
          : 'bg-white border-gray-400' // Default styles when no option is selected
      ]"
      @click="toggleDropdown"
    >
      <FilterSVG :class="modelValue ? 'text-[#FA812F]' : 'text-gray-500'" />
      <span class="flex items-center text-sm">
        {{ title }}
      </span>
      <DropdownArrow :class="{ 'rotate-180': isOpen }"/>
    </button>
    <!-- Dropdown content panel, visible only when 'isOpen' is true. -->
    <div
      v-if="isOpen && normalizedOptions.length > 0" class="absolute left-0 top-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg z-50 w-full overflow-hidden"
    >
      <!-- List of selectable options, with a scrollbar if content overflows. -->
      <ul class="divide-y divide-gray-100 max-h-60 overflow-y-auto">
        <li
          v-for="option in normalizedOptions"
          :key="option.value"
          @click="selectOption(option.value)"
          class="flex items-center justify-between px-4 py-2.5 cursor-pointer text-sm transition-colors duration-150"
          :class="[
            option.value === modelValue // Styles for the currently selected option
              ? 'bg-orange-50 text-[#FA812F] font-medium'
              : 'hover:bg-gray-50' // Hover styles for other options
          ]"
        >
          <span>{{ option.label }}</span>
          <!-- Checkmark icon, visible only for the selected option. -->
          <CheckSVG
          v-if="option.value === modelValue"
          />
        </li>
        <!-- "Clear Filter" option, visible only if a filter is currently applied (modelValue exists). -->
        <li
          v-if="modelValue"
          @click="clearFilter"
          class="px-4 py-2.5 hover:bg-red-50 text-red-500 text-sm cursor-pointer transition-colors duration-150 border-t border-gray-100 flex items-center gap-2 text-sm"
        >
          <DeleteSVG />
          <span class="text-red-500 font-medium">Clear Filter</span>
        </li>
      </ul>
    </div>
        <div
      v-else-if="isOpen && normalizedOptions.length === 0" class="absolute left-0 top-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg z-50 w-full overflow-hidden"
        >
      <div class="px-4 py-2.5 text-sm text-gray-500 text-center">
        No options available
      </div>
    </div>

  </div>
</template>

<script>
import FilterSVG from './svg/FilterSVG.vue'
import DropdownArrow from '@/components/svg/DropdownArrow.vue'
import DeleteSVG from './svg/DeleteSVG.vue'
import CheckSVG from './svg/CheckmarkSVG.vue'

export default {
  name: 'FilterDropdown',
  components: {
    FilterSVG,
    DropdownArrow,
    DeleteSVG,
    CheckSVG
  },
  props: {
    options: { // The list of options to display in the dropdown. Can be an array of strings (e.g., ['Option1', 'Option2']) or objects with 'value' and 'label' properties.
      type: String,
      type: Array,
      required: true
    },
    modelValue: { //The currently selected value. Used with v-model for two-way binding.
      default: null
    },
    title:{ // The text displayed on the dropdown button.
      type: String, 
      default: 'OPTIONS'
    }
  },
  data() {
    return {
      /**
       * Controls the visibility state of the dropdown menu.
       * @type {boolean}
       */
      isOpen: false
    }
  },
  computed: {
    /**
     * Normalizes the 'options' prop into an array of objects with 'value' and 'label' properties.
     * This allows flexible input for the 'options' prop (strings or objects).
     * @returns {Array<object>} An array of normalized option objects.
     */
    normalizedOptions() {
      return this.options.map(opt => {
        return typeof opt === 'string'
          ? { value: opt, label: opt }
          : opt
      })
    },
  },
  methods: {
    /**
     * Toggles the visibility of the dropdown menu.
     * Emits a 'dropdownToggled' event with the new state (true for open, false for closed).
     */
    toggleDropdown() {
      this.isOpen = !this.isOpen
      this.$emit('dropdownToggled', this.isOpen)
    },
    /**
     * Closes the dropdown menu.
     */
    closeDropdown() {
      this.isOpen = false
    },
    /**
     * Selects an option from the dropdown.
     * Emits an 'update:modelValue' event to update the v-model binding with the selected value,
     * then closes the dropdown.
     * @param {string} value - The value of the selected option.
     */
    selectOption(value) {
      this.$emit('update:modelValue', value)
      this.closeDropdown()
    },
    /**
     * Clears the currently selected filter.
     * Emits an 'update:modelValue' event with null, then closes the dropdown.
     */
    clearFilter() {
      this.$emit('update:modelValue', null)
      this.closeDropdown()
    },
    /**
     * Handles clicks outside the dropdown component to close it.
     * @param {Event} event - The DOM click event.
     */
    handleClickOutside(event) {
      const dropdown = this.$refs.dropdown
      if (dropdown && !dropdown.contains(event.target)) {
        this.closeDropdown()
      }
    }
  },
  /**
   * Lifecycle hook: Called after the component is mounted to the DOM.
   * Adds a global click event listener to handle clicks outside the dropdown.
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
  }
}
</script>

<style scoped>
/* Basic flex and positioning for the dropdown. */
.relative {
  position: relative;
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
.gap-1 {
  gap: 0.25rem;
}

/* Styling for the dropdown button. */
.border {
  border-width: 1px;
}
.rounded-md {
  border-radius: 0.375rem;
}
.p-2 {
  padding: 0.5rem;
}
.text-sm {
  font-size: 0.875rem;
  line-height: 1.25rem;
}
.transition-colors {
  transition-property: background-color, border-color, color;
}
.duration-200 {
  transition-duration: 200ms;
}

/* Specific styles for the dropdown button when an option is selected. */
.bg-orange-50 {
  background-color: #fff7ed;
}
.border-\[\#FA812F\] {
  border-color: #FA812F;
}
.text-\[\#FA812F\] {
  color: #FA812F;
}
.border-2 {
  border-width: 2px;
}

/* Styles for the dropdown content panel. */
.absolute {
  position: absolute;
}
.top-full {
  top: 100%;
}
.mt-1 {
  margin-top: 0.25rem;
}
.bg-white {
  background-color: #fff;
}
.border-gray-200 {
  border-color: #e5e7eb;
}
.shadow-lg {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.1);
}
.z-50 {
  z-index: 50;
}
.w-full {
  width: 100%;
}
.overflow-hidden {
  overflow: hidden;
}

/* Styles for the list of options within the dropdown. */

.divide-gray-100 {
  border-color: #f3f4f6;
}
.max-h-60 {
  max-height: 15rem; /* Max height to enable scrolling */
}
.overflow-y-auto {
  overflow-y: auto; /* Enables vertical scrolling */
}

/* Styles for individual list items (options). */
.px-4 {
  padding-left: 1rem;
  padding-right: 1rem;
}
.py-2\.5 {
  padding-top: 0.625rem;
  padding-bottom: 0.625rem;
}
.cursor-pointer {
  cursor: pointer;
}
.transition-colors {
  transition-property: background-color, border-color, color;
}
.duration-150 {
  transition-duration: 150ms;
}
.font-medium {
  font-weight: 500;
}
.hover\:bg-gray-50:hover {
  background-color: #f9fafb;
}

/* Specific styles for the "Clear Filter" option. */
.hover\:bg-red-50:hover {
  background-color: #fef2f2;
}
.text-red-500 {
  color: #ef4444;
}
.border-t {
  border-top-width: 1px;
}
</style>
