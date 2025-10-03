<template>
  <div
    v-if="show"
    class="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
  >
    <!-- The actual confirmation popup content. -->
    <div class="bg-white rounded-xl shadow-xl w-[20%] px-7 py-10 text-center space-y-4 animate-fade-in flex flex-col">
      <!-- Confirmation message title. Displays dynamic text based on the 'text' prop. -->
      <h2 class="text-2xl font-semibold text-black">
        Are you sure<br>
        you want {{ text }}?
      </h2>
      <!-- Optional caption providing additional context, visible only if the 'caption' prop has content. -->
      <p v-if="caption" class="text-sm text-gray-600">{{ caption }}</p>
      <!-- Container for the action buttons. -->
      <div class="flex-1 pt-8">
        <!-- Confirm button. Emits a 'confirm' event when clicked. -->
        <button
          class="px-4 py-2 bg-[#FA812F] hover:bg-red-600 text-white rounded-full transition cursor-pointer min-w-full"
          @click="$emit('confirm')"
        >
          Confirm
        </button>
      </div>
      <div class="flex-1 justify-center">
        <!-- Cancel button. Emits a 'cancel' event when clicked. -->
        <button
          class="px-4 py-2 bg-white hover:bg-gray-200 text-gray-800 rounded-full border-1 transition cursor-pointer min-w-full"
          @click="$emit('cancel')"
        >
          Cancel
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    //Controls the visibility of the confirmation popup.
    show: Boolean,
    //The main text of the confirmation message, typically completing the phrase "Are you sure you want...". Required.
    text: {
      type: String,
      required: true
    },
    // An optional, more detailed message to display.
    caption: {
      type: String,
      required: false
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
