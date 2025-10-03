<template>
  <div
    class="absolute right-0 top-13 z-20 w-96 max-h-140 bg-white border border-gray-200 rounded-lg shadow-lg p-4 flex flex-col"
  >
    <div class="mb-4 flex items-center space-x-2">
      <div class="bg-white rounded-md px-3 py-1 flex items-center gap-2 flex-1 border border-gray-300 focus-within:outline-solid focus-within:outline-2 focus-within:outline-[#FA812F]">
        <label for="search-notifications">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 24 24">
            <path d="M21 21l-4.35-4.35m1.1-5.4a7.5 7.5 0 1 1-15 0 7.5 7.5 0 0 1 15 0z" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </label>
        <input
          id="search-notifications"
          type="text"
          v-model="searchTerm"
          placeholder="Search notifications..."
          class="flex-1 p-1 bg-white rounded-md focus:outline-none"
          :disabled="loading"
          style="outline: none !important; box-shadow: none !important;"
        />
      </div>
    </div>

    <div class="flex items-center mb-4 gap-2">
      <select @change="handleBulkSelect($event.target.value)" class="px-2 py-1 rounded border border-gray-300 cursor-pointer text-sm" :disabled="loading">
        <option value="none">None</option>
        <option value="all">All</option>
        <option value="read">Read</option>
        <option value="unread">Unread</option>
      </select>

      <div v-if="selectedNotifications.length !== 0" class="flex flex-row">
          <button @click="markSelectedAsRead" class="bg-[#FA812F]/20 hover:bg-[#FA812F]/30 text-gray-800 px-3 py-1 rounded cursor-pointer transition-colors duration-200 text-xs" :disabled="loading || selectedNotifications.length === 0">Mark Read</button>
          <button @click="markSelectedAsUnread" class="bg-gray-100 hover:bg-gray-200 text-gray-800 px-3 py-1 rounded cursor-pointer transition-colors duration-200 text-xs" :disabled="loading || selectedNotifications.length === 0">Mark Unread</button>
          <button @click="deleteSelectedNotifications" class="bg-red-100 hover:bg-red-200 text-red-700 px-3 py-1 rounded cursor-pointer transition-colors duration-200 text-xs" :disabled="loading || selectedNotifications.length === 0">Delete Selected</button>
      </div>
      <div v-else class="flex h-10">

      </div>
    </div>

    <div v-if="loading" class="flex-1 flex items-center justify-center">
      <div class="flex flex-col items-center">
        <svg class="animate-spin h-8 w-8 text-[#FA812F]" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        <p class="text-gray-500 mt-3">Loading notifications...</p>
      </div>
    </div>

    <div v-else-if="filteredNotifications.length" class="overflow-y-auto flex-1 space-y-2">
      <div
        v-for="n in filteredNotifications"
        :key="n.id || n.notification_id"
        class="py-3 px-3 rounded-md shadow-sm hover:shadow-md transition-shadow duration-200 cursor-pointer"
        :class="{ 'bg-[#FA812F]/10': !n.read, 'bg-white': n.read }"
      >
        <label :for="`notification-checkbox-${n.id || n.notification_id}`" class="flex items-center w-full cursor-pointer">
          <input
            type="checkbox"
            class="mr-3 w-4 h-4 accent-[#FA812F]"
            v-model="selectedNotifications"
            :value="n.id || n.notification_id"
            :id="`notification-checkbox-${n.id || n.notification_id}`"
            :disabled="loading"
            @click.stop
          />
          <div class="flex items-start justify-between flex-1">
            <div class="flex flex-col">
                <p class="text-sm font-semibold text-gray-800">{{ n.title || 'New Notification' }}</p>
                <p class="text-sm text-gray-600 mt-1 line-clamp-2">{{ n.message || n.data }}</p>
            </div>
            <div class="relative flex flex-col items-end text-xs text-gray-500 mt-0 ml-4 whitespace-nowrap">
                <span>{{ formatTimestamp(n.timestamp) }}</span>
                <span v-if="!n.read" class="text-xs text-[#FA812F] ml-2 font-bold mt-1">New</span>
                <button v-if="!n.read" @click.stop="markAsRead(n.id || n.notification_id)" class="text-[#FA812F] hover:underline mt-1">Mark as Read</button>
            </div>
          </div>
        </label>
      </div>
    </div>

    <div v-else class="text-gray-400 text-sm text-center py-6 flex-1 flex items-center justify-center">
      No notifications found.
    </div>

    <div v-if="notifications.length && !loading" class="pt-4 border-t mt-4 text-center">
      <button @click="markAllAsRead" class="text-[#FA812F] hover:text-[#FA812F]/80 text-sm font-medium">
        Mark all as read
      </button>
    </div>
  </div>
</template>

<script>
import { useAuthStore } from "@/stores/auth.js";

export default {
  data() {
    return {
      searchTerm: '',
      filterType: 'all',
      notifications: [],
      loading: true,
      selectedNotifications: [],
    };
  },
  props:{
    loggedInUsername: {
        type: String,
        default: null
    }
  },
  computed: {
    filteredNotifications() {
      let tempNotifications = this.notifications;

      if (this.searchTerm) {
        const lowerSearchTerm = this.searchTerm.toLowerCase();
        tempNotifications = tempNotifications.filter(n =>
          (n.message && n.message.toLowerCase().includes(lowerSearchTerm)) ||
          (n.data && n.data.toLowerCase().includes(lowerSearchTerm)) ||
          (n.title && n.title.toLowerCase().includes(lowerSearchTerm))
        );
      }

      if (this.filterType !== 'all') {
        if (this.filterType === 'unread') {
          tempNotifications = tempNotifications.filter(n => !n.read);
        } else if (this.filterType === 'read') {
          tempNotifications = tempNotifications.filter(n => n.read);
        } else {
          tempNotifications = tempNotifications.filter(n => n.type === this.filterType);
        }
      }

      tempNotifications.sort((a, b) => {
        const dateA = new Date(a.timestamp);
        const dateB = new Date(b.timestamp);
        return dateB - dateA;
      });

      return tempNotifications;
    },
  },
  methods: {
    formatTimestamp(ts) {
      if (!ts) return '';

      let date;
      if (typeof ts === 'string') {
        const cleaned = ts.replace(' ', 'T').split('.')[0];
        date = new Date(cleaned.endsWith('Z') ? cleaned : cleaned + 'Z');
      } else {
        date = new Date(ts);
      }

      if (!(date instanceof Date) || isNaN(date.getTime())) return '';

      const now = new Date();
      const diffMs = now - date;
      const diffMinutes = Math.round(diffMs / (1000 * 60));
      const diffHours = Math.round(diffMs / (1000 * 60 * 60));
      const diffDays = Math.round(diffMs / (1000 * 60 * 60 * 24));

      if (diffMinutes < 1) return 'Just now';
      if (diffMinutes < 60) return `${diffMinutes}m ago`;
      if (diffHours < 24) return `${diffHours}h ago`;
      if (diffDays < 7) return `${diffDays}d ago`;

      return date.toLocaleString('en-GB', {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    },
    markAsRead(id) {
      const notification = this.notifications.find(n => (n.id || n.notification_id) === id);
      if (notification && !notification.read) {
        console.log(`Marking notification ${id} as read (simulated API call)`);
        notification.read = true;
        this.selectedNotifications = this.selectedNotifications.filter(selectedId => selectedId !== id);
      }
    },
    markAllAsRead() {
      console.log('Marking all notifications as read (simulated API call)');
      this.notifications.forEach(n => {
        n.read = true;
      });
      this.selectedNotifications = [];
    },
    handleBulkSelect(type) {
      this.selectedNotifications = [];
      if (type === 'all') {
        this.selectedNotifications = this.filteredNotifications.map(n => n.id || n.notification_id);
      } else if (type === 'read') {
        this.selectedNotifications = this.filteredNotifications.filter(n => n.read).map(n => n.id || n.notification_id);
      } else if (type === 'unread') {
        this.selectedNotifications = this.filteredNotifications.filter(n => !n.read).map(n => n.id || n.notification_id);
      }
    },
    async markSelectedAsRead() {
      for (const id of [...this.selectedNotifications]) {
        await fetch(`api/notifications/${id}/read`, { method: 'POST' });
      }
      this.notifications.forEach(n => {
        if (this.selectedNotifications.includes(n.id || n.notification_id)) n.read = true;
      });
    },
    async markSelectedAsUnread() {
      for (const id of [...this.selectedNotifications]) {
        await fetch(`api/notifications/${id}/unread`, { method: 'POST' });
      }
      this.notifications.forEach(n => {
        if (this.selectedNotifications.includes(n.id || n.notification_id)) n.read = false;
      });
    },
    async deleteSelectedNotifications() {
      console.log("Attempting to delete IDs:", this.selectedNotifications);
      for (const id of [...this.selectedNotifications]) {
        await fetch(`api/notifications/${id}`, { method: 'DELETE' });
      }
      this.notifications = this.notifications.filter(n => !this.selectedNotifications.includes(n.id || n.notification_id));
      this.selectedNotifications = [];
    },
    fetchNotifications() {
      this.loading = true;

      let apiUrl = 'api/notifications';

       if (this.authStore.isUser && this.loggedInUsername) {
        apiUrl += `?recipient=${encodeURIComponent(this.loggedInUsername)}`;
      }
        console.log(apiUrl)
      fetch(apiUrl)
        .then(async res => {
          const text = await res.text();
          if (!res.ok) {
            throw new Error('Server responded with ' + res.status + ': ' + text);
          }
          return JSON.parse(text);
        })
        .then(data => {
          this.notifications = data;
        })
        .catch(err => {
          console.error('Failed to fetch notifications:', err);
        })
        .finally(() => {
          this.loading = false;
        });
    }
  },
  created() {
    console.log(this.logged_in_username)
    this.fetchNotifications();
  },
  setup(){
    const authStore = useAuthStore();
    return { authStore }
  }
};
</script>

<style scoped>
input[type="checkbox"].accent-\[\#FA812F\] {
  accent-color: #FA812F;
}
</style>