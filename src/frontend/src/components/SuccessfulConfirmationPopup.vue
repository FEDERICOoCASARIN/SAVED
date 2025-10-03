<template>
  <div
    v-if="show"
    class="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
  >
    <!-- The actual confirmation popup content. -->
    <div class="bg-white rounded-xl shadow-xl w-[90%] max-w-md p-6 text-center space-y-4 animate-fade-in">
      <!-- Title of the confirmation message, typically indicating success. -->
      <h2 class="text-xl font-semibold text-green-600">{{ title }}</h2>
      <!-- Optional caption providing additional details, visible only if the 'caption' prop has content. -->
      <p v-if="caption" class="text-sm text-gray-600">{{ caption }}</p>
      <!-- Close button for the popup. Emits a 'close' event when clicked. -->
      <button
        class="mt-4 px-4 py-2 bg-green-500 hover:bg-green-600 text-white rounded-md transition cursor-pointer"
        @click="$emit('close')"
      >
        Close
      </button>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    // Controls the visibility of the confirmation popup.
    show: Boolean,
    // The main title or heading of the confirmation message. Required.
    title: {
      type: String,
      required: true
    },
    // An optional, more detailed message to display below the title. Defaults to an empty string.
    caption: {
      type: String,
      default: ''
    }
  }
};
</script>

<style scoped>

/* Keyframe animation for the fade-in effect when the popup appears. */
@keyframes fade-in {
  from {
    opacity: 0; /* Starts fully transparent */
    transform: scale(0.95); /* Starts slightly scaled down */
  }
  to {
    opacity: 1; /* Ends fully opaque */
    transform: scale(1); /* Ends at normal size */
  }
}

/* Applies the fade-in animation to the popup content. */
.animate-fade-in {
  animation: fade-in 0.2s ease-out; /* 0.2s duration, ease-out timing function */
}

</style>
