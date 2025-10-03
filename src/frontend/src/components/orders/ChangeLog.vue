<template>
  <div class="fixed inset-0 flex items-center justify-center bg-opacity-50 z-[100]">
    <div class="fixed inset-0 bg-orange-100/70"></div>

    <div class="bg-[#E57229] p-1 rounded-md relative z-[101] flex flex-col max-w-4xl w-full max-h-[95vh] h-full">
      <div class="bg-white p-4 bg-opacity-50 rounded-md flex flex-col flex-1 overflow-hidden">

        <div class="flex justify-between items-center p-6 border-b border-gray-300 flex-shrink-0">
          <h2 class="text-4xl font-bold text-[#E57229]">Change Log</h2>
          <button @click="$emit('close')" class="text-gray-500 hover:text-gray-700 cursor-pointer text-4xl leading-none">&times;</button>
        </div>

        <span class="px-6 text-xs text-gray-500 mt-2">Press Enter to Search</span>
        <div class="flex mb-5 mt-2 px-6 justify-between items-center flex-shrink-0 gap-4">
          <div
            class="border border-gray-300 rounded-md px-4 py-1 flex items-center focus-within:outline-[#FA812F] focus-within:outline focus-within:outline-2 w-full sm:w-auto">
            <span class="flex items-center w-full">
              <label>
                <SearchSVG />
              </label>
              <input
                type="text"
                v-model="searchQuery"
                @keyup.enter="applySearch"
                class="text-base flex-1 p-2 rounded-md ml-2 focus:outline-none w-full"
                placeholder="Search by Order ID"
                style="outline: none !important; box-shadow: none !important;"
              />
              <CancelSVG
                v-if="searchQuery || isSearchActive" @click="clearSearch" class="w-5 h-5 text-red-500 cursor-pointer" />
              <div
                v-else class="w-5 h-5 cursor-pointer text-gray-500 hover:text-gray-700"/>
            </span>
          </div>
          <div class="flex gap-2 flex-shrink-0 w-full sm:w-auto justify-end">
            <FilterDropdown
              v-model="filterOption"
              :options="['EDITED', 'REMOVED', 'RESCHEDULED']"
              @dropdownToggled="handleDropdownToggle"
              title="CHANGE TYPE"
            />
            <div>
              <SortFunc
                v-model="sortDirection"
                :options="['DESC', 'ASC']"
                title="Sort Order"
                class="w-40"
              />
            </div>
          </div>
        </div>

        <div class="flex-1 px-6 pt-3 min-h-0 relative">
          <div v-if="isLoading" class="absolute inset-0 flex items-center justify-center bg-white bg-opacity-80 z-40 rounded-md">
            <div class="animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-orange-500"></div>
          </div>

          <div v-else class="bg-white h-full overflow-y-auto space-y-4 p-4 rounded-md shadow-inner">
            <transition-group name="stagger-fade" tag="div">
              <ChangeLogTableItem
                v-for="(log, i) in filteredAndSortedLogs"
                :key="log.changeId" :logId="i + 1"
                :logDetail="log"
                :isUnread="!log.isReadByCurrentUser" :style="{ animationDelay: (i * 100) + 'ms' }"
                class="animated-row"
              />
            </transition-group>
            <div v-if="filteredAndSortedLogs.length === 0" class="flex justify-center items-center h-full text-gray-500 text-lg">
              <p v-if="searchQuery">No changelog found for Order "{{ searchQuery }}". Try a different ID.</p>
              <p v-else>No changelog to display.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ChangeLogTableItem from "@/components/orders/ChangeLogTableItem.vue";
import SearchSVG from "@/components/svg/SearchSVG.vue";
import FilterDropdown from "@/components/FilterFunc.vue";
import SortFunc from "../SortFunc.vue";
import CancelSVG from "@/components/svg/CancelSVG.vue";
import { useAuthStore } from '@/stores/auth.js';

export default {
    components: {
        SearchSVG,
        FilterDropdown,
        ChangeLogTableItem,
        SortFunc,
        CancelSVG,
    },
    data() {
        return {
            /** @type {string | null} The currently selected filter option for change type. */
            filterOption: null,
            /** @type {string} The current search query for order IDs. */
            searchQuery: '',
            /** @type {boolean} Indicates if a search operation is active. */
            isSearchActive: false,
            /** @type {'ASC' | 'DESC'} The current sort direction for change logs by modified date. */
            sortDirection: 'DESC',
            /** @type {string | null} The username of the order owner to filter logs by. Null for admin to see all logs. */
            currentOrderOwner: null,
            /** @type {Array<Object>} The raw list of change log entries fetched from the backend. */
            logs: [],
            /** @type {string} The username of the currently logged-in user. */
            logged_in_username:'',
            isLoading: true,

        };
    },
    computed: {
        /**
         * Filters and sorts the `logs` array based on `filterOption`, `searchQuery`, and `sortDirection`.
         * @returns {Array<Object>} The processed list of change log entries.
         */
        filteredAndSortedLogs() {
            let result = this.logs;

            // Apply filter by ChangeType
            if (this.filterOption) {
                result = result.filter((v) => v.ChangeType.includes(this.filterOption));
            }

            // Apply filter by search query in orderId
            if (this.searchQuery.trim() !== '') {
                const q = this.searchQuery.trim().toLowerCase();
                result = result.filter((v) => String(v.orderId).toLowerCase().includes(q));
            }

            // Apply sorting by ModifiedAt date
            result = [...result].sort(
                (a, b) => {
                    const dateA = new Date(a.modifiedAt);
                    const dateB = new Date(b.modifiedAt);
                    const diff = dateA.getTime() - dateB.getTime();
                    return this.sortDirection === 'ASC' ? diff : -diff;
                }
            );

            return result;
        },
    },
    /**
     * Lifecycle hook: Called after the instance has been created.
     * Initializes `logged_in_username`, `currentOrderOwner`, and fetches changelogs.
     */
    created() {
        this.logged_in_username = this.authStore.user ? this.authStore.user.username.string : '';
        this.currentOrderOwner = this.logged_in_username;
        this.fetchChangeLogs();
    },
    methods: {
        /**
         * Asynchronously fetches the list of change logs from the backend API.
         * Filters logs based on the current user's role (admin sees all, regular users see their own).
         * After fetching, it processes the `isRead` status for the current user and
         * immediately calls `markLogsAsRead` for all fetched logs to mark them as read in the DB.
         * @returns {Promise<void>} A promise that resolves when the changelog is successfully fetched and marked as read.
         */
        async fetchChangeLogs() {
            this.loading = true;
            let apiUrl = '/api/changelog';
            // Append orderOwner query parameter if the user is not an admin
            if (this.authStore.isUser && this.currentOrderOwner.trim() !== '') {
                apiUrl += `?orderOwner=${encodeURIComponent(this.currentOrderOwner.trim())}`;
            }

            try {
                const res = await fetch(apiUrl);
                if (!res.ok) {
                    throw new Error(`Network response was not ok: ${res.statusText}`);
                }
                const data = await res.json();

                this.logs = data.map(item => {
                    let changes = [];
                    // Extract dataInitial and dataChanged for detailed log view
                    if (item.dataInitial && item.dataChanged) {
                        const keys = Object.keys(item.dataChanged);
                        changes = keys.map(key => ({
                            key,
                            old: item.dataInitial[key],
                            new: item.dataChanged[key]
                        }));
                    }

                    // Determine if the current user has read this specific log entry
                    // `item.isRead` is a map like: { "username1": "timestamp", "username2": "timestamp" }
                    const isReadByCurrentUser = item.isRead && item.isRead.hasOwnProperty(this.logged_in_username);

                    return {
                        changeId: item.changeId.toString(),
                        orderId: item.orderId.toString(),
                        modifiedAt: this.formatCreatedAt(item.modifiedAt),
                        ChangeType: item.changeStatus && Array.isArray(item.changeStatus)
                                        ? item.changeStatus.map(s => s.toUpperCase()).join(',')
                                        : (item.changeStatus ? item.changeStatus.toString().toUpperCase() : ''),
                        ModifiedBy: item.modifiedBy,
                        OrderOwner: item.orderOwner,
                        Changes: changes,
                        isReadByCurrentUser: isReadByCurrentUser // Flag for frontend display (e.g., orange border)
                    };
                });
                console.log("ChangeLogPopup: Logs fetched and processed.");

                // Automatically mark all fetched logs as read for the current user in the database
                const logIdsToMarkAsRead = this.logs.map(log => parseInt(log.changeId));
                if (logIdsToMarkAsRead.length > 0 && this.logged_in_username) {
                    this.markLogsAsRead(logIdsToMarkAsRead, this.logged_in_username);
                }

            } catch (error) {
                console.error("ChangeLogPopup: Error fetching changelog:", error);
            } finally {
                this.isLoading = false;
             }
        },

        /**
         * Asynchronously sends a request to the backend to mark specific change logs as read for a given user.
         * The orange "unread" border will disappear on the *next* time the popup is opened.
         * @param {number[]} logIds An array of change log IDs to mark as read.
         * @param {string} username The username of the user who read the logs.
         * @returns {Promise<void>} A promise that resolves when the request is sent, regardless of success.
         */
        async markLogsAsRead(logIds, username) {
            try {
                const res = await fetch('/api/changelog/mark-as-read', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        logIds: logIds,
                        username: username
                    }),
                });
                if (!res.ok) {
                    const errorText = await res.text();
                    throw new Error(`Failed to mark logs as read: ${res.status} - ${errorText}`);
                }
                console.log(`ChangeLogPopup: Successfully sent request to mark logs as read for user: ${username}`);
            } catch (error) {
                console.error("ChangeLogPopup: Error marking logs as read:", error);
            }
        },

        /**
         * Clears the search query and re-fetches change logs if a search was active.
         */
        clearSearch(){
            if(this.isSearchActive){
                this.fetchChangeLogs(); // Re-fetch all logs if search was active
            }
            this.searchQuery = '';
            this.isSearchActive = false;
        },

        /**
         * Handles the dropdown toggle event (e.g., for filter dropdown).
         * @param {boolean} isOpen - Indicates if the dropdown is open or closed.
         */
        handleDropdownToggle(isOpen) {
            // Placeholder for any specific logic needed when a dropdown is toggled.
            // For example, you might want to prevent body scrolling when a dropdown is open.
        },

        /**
         * Formats a given timestamp string into a more readable "DD/MM/YYYY HH:MM:SS" format.
         * @param {string} timestamp The timestamp string to format.
         * @returns {string} The formatted date and time string, or "N/A" if invalid.
         */
        formatCreatedAt(timestamp) {
            if (!timestamp) {
                return "N/A";
            }
            const dateObj = new Date(timestamp);
            if (isNaN(dateObj.getTime())) {
                return "Invalid Date";
            }

            const isoTime = dateObj.toISOString().split("T")[1].slice(0, 8); // Extracts HH:MM:SS
            const dateStr = dateObj.toLocaleDateString("en-GB"); // Formats date as DD/MM/YYYY
            return `${dateStr} ${isoTime}`;
        },
    },
    /**
     * Setup function to initialize Pinia stores.
     * @returns {Object} An object containing the authStore instance.
     */
    setup(){
        const authStore = useAuthStore();
        return { authStore };
    },
    /**
     * Lifecycle hook: Called after the component is mounted to the DOM.
     * Disables body scrolling to prevent content behind the modal from moving.
     */
    mounted(){
        document.body.style.overflow = 'hidden';
    },
    /**
     * Lifecycle hook: Called right before the component is unmounted from the DOM.
     * Re-enables body scrolling.
     */
    beforeUnmount(){
        document.body.style.overflow = 'auto';
    }
};
</script>

<style>
/* Transition for fading in/out elements */
.fade-enter-active, .fade-leave-active {
    @apply transition-opacity duration-300;
}
.fade-enter-from, .fade-leave-to {
    @apply opacity-0;
}

/* Transition for scaling and fading elements */
.scale-fade-enter-active, .scale-fade-leave-active {
    @apply transition transform duration-300;
}
.scale-fade-enter-from, .scale-fade-leave-to {
    @apply opacity-0 scale-90;
}

/* Keyframe animation for staggered fade-in effect on log rows */
.animated-row {
    opacity: 0;
    transform: translateY(20px);
    animation: fadeInUp 0.4s ease forwards;
    position: relative;
    z-index: 1;
}

@keyframes fadeInUp {
    to {
        opacity: 1;
        transform: translateY(0);
    }
}


@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}
</style>