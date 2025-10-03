<template>
  <div class="bg-white shadow-md rounded-xl p-6 mb-4 border"
       :class="{ 'border-orange-500 border-2': isUnread, 'border-gray-200': !isUnread }">
    <div class="flex justify-between items-center mb-1">
      <h3 class="text-2xl font-bold text-gray-800">
        Order #{{ logDetail.orderId }}
      </h3>
      <span class="text-gray-500 text-sm">#{{ logDetail.changeId }}</span>
    </div>

    <div class="text-sm text-gray-500 mb-4">
      by {{ logDetail.OrderOwner }}
    </div>

    <div class="mb-4">
      <p class="text-sm text-gray-600">
        <strong>Modified at:</strong> {{ logDetail.modifiedAt }}
      </p>
      <p class="text-sm text-gray-600">
        <strong>Modified by:</strong> {{ logDetail.ModifiedBy }}
      </p>
    </div>

    <div class="flex flex-wrap gap-2 mb-4">
      <span
        v-for="status in getChangeStatuses()"
        :key="status"
        class="px-3 py-1 rounded-full text-white text-sm font-semibold border border-gray-300"
        :class="{
          'bg-red-600': status === 'REMOVED',
          'bg-green-600': status === 'RESCHEDULED',
          'bg-blue-600': status === 'EDITED',
          'bg-gray-500': !['REMOVED', 'RESCHEDULED', 'EDITED'].includes(status)
        }"
      >
        {{ status }}
      </span>
    </div>

    <div v-if="logDetail.Changes && logDetail.Changes.length > 0" class="space-y-3">
      <div
        v-for="(change, idx) in logDetail.Changes"
        :key="idx"
        class="bg-gray-100 p-4 rounded-md shadow-sm"
      >
        <p class="text-sm font-semibold text-gray-700">
          {{ formatFieldName(change.key) }}
        </p>
        <p class="text-sm text-gray-600 break-words">
          <span :class="{'text-red-600 line-through': change.old !== change.new}">{{ formatValue(change.old, change.key) }}</span>
          →
          <span :class="{'text-green-600': change.old !== change.new}">{{ formatValue(change.new, change.key) }}</span>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "ChangeLogTableItem",
  props: {
    logId: Number,
    logDetail: Object,
    isUnread: { // NEW PROP
      type: Boolean,
      default: false
    }
  },
  methods: {
    /**
     * Splits the comma-separated ChangeType string into an array of individual statuses.
     * @returns {Array<string>} An array of change status strings.
     */
    getChangeStatuses() {
      if (this.logDetail && this.logDetail.ChangeType) {
        return this.logDetail.ChangeType.split(',').map(s => s.trim()).filter(s => s.length > 0);
      }
      return [];
    },
    /**
     * Formats a field name for better readability (e.g., converts camelCase to "Camel Case").
     * @param {string} key The original field name (e.g., "freightWeight").
     * @returns {string} The formatted field name (e.g., "Freight Weight").
     */
    formatFieldName(key) {
      return key.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase());
    },
    /**
     * Formats a value for display, handling dates, numbers, and nulls.
     * @param {any} value The value to format.
     * @param {string} key The key associated with the value, used for context (e.g., to detect date fields).
     * @returns {string|any} The formatted value or the original value if no specific formatting applies.
     */
    formatValue(value, key) {
        if (value === null) return "empty";

        // Attempt to parse dates that look like ISO strings (e.g., from Backend)
        if (typeof value === 'string' && (key.includes('At') || key.includes('tw') || key.includes('eta'))) {
            try {
                const date = new Date(value);
                if (!isNaN(date.getTime())) {
                    return date.toLocaleString('en-GB', {
                        year: 'numeric',
                        month: '2-digit',
                        day: '2-digit',
                        hour: '2-digit',
                        minute: '2-digit',
                        second: '2-digit',
                        hour12: false
                    });
                }
            } catch (e) {
                // Fall through to default return if parsing fails
            }
        }
        // For numbers, especially BigDecimal strings
        if ((key.includes('Value') || key.includes('Weight')) && typeof value === 'string') {
            try {
                const num = parseFloat(value);
                if (!isNaN(num)) {
                    return num.toLocaleString('en-GB', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
                }
            } catch (e) {
                // Fall through to default return if parsing fails
            }
        }
        return value;
    }
  }
};
</script>