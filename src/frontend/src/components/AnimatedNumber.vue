<template>
  <span>{{ animatedValue.toFixed(fixed) }}</span>
</template>

<script>
import { gsap } from 'gsap';

export default {
  props: {
    value: {
      type: Number, //The target numerical value to animate to. Required.
      required: true,
    },
    duration: {
      type: Number, // The duration of the animation in seconds. Defaults to 1.
      default: 1, // Animation duration in seconds
    },
    fixed: {
      type: Number, // The number of decimal places to display. Defaults to 0.
      default: 0, // Number of decimal places
    }
  },
  data() {
    return {
      animatedValue: 0,
    };
  },
  computed: {
    formatValue() {
      return (value) => {
        // Ensure value is a number and handle null/undefined cases
        const numValue = typeof value === 'number' && !isNaN(value) ? value : 0;
        return numValue.toFixed(this.fixed);
      };
    }
  },
  watch: {
    /**
     * Watches for changes in the 'value' prop and animates 'animatedValue' to the new value.
     * @param {number} newValue - The new target value for the animation.
     * @param {number} oldValue - The previous value before the change.
     */
    value(newValue, oldValue) {
      // Ensure we have a valid number to animate to
      const targetValue = typeof newValue === 'number' && !isNaN(newValue) ? newValue : 0;
      gsap.to(this.$data, {
        duration: this.duration,
        animatedValue: targetValue,
        ease: 'power1.out', // You can experiment with different eases
      });
    },
  },
  mounted() {
    // Initial animation when component is mounted
    const targetValue = typeof this.value === 'number' && !isNaN(this.value) ? this.value : 0;
    gsap.to(this.$data, {
      duration: this.duration,
      animatedValue: targetValue,
      ease: 'power1.out',
    });
  },
};
</script>