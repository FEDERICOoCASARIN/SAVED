<template>
  <transition name="fade-slide" appear>
    <div class="w-full min-h-screen flex flex-col px-15 py-10 space-y-6">
      <div class="font-bold text-gray-800 w-full max-w-3xl">
        <div class="flex items-center space-x-3">
          <div class="p-2 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl shadow-lg">
            <NotificationsSVG class="size-6 text-white" />
          </div>
          <h1 class="text-4xl font-bold text-gray-900">
            Notifications
          </h1>
        </div>
      </div>
      <div class="flex justify-between w-full">
        <div class="bg-white rounded-md px-4 py-1 flex items-center gap-2 w-[300px] border-1 border-gray-300 focus-within:outline-solid focus-within:outline-2 focus-within:outline-[#E57229]">
          <label>
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 24 24">
              <path d="M21 21l-4.35-4.35m1.1-5.4a7.5 7.5 0 1 1-15 0 7.5 7.5 0 0 1 15 0z" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </label>
          <input
            v-model="searchQuery"
            type="text"
            class="text-base p-2 bg-white rounded-md flex-1 ml-2 focus-within:outline-none"
            placeholder="Search Notification"
            style="outline: none !important; box-shadow: none !important;"
          />
        </div>
        <FilterFunc
          v-model="selectedCategory"
          :options="[
            { label: 'ALL', value: null },
            { label: 'ORDER CREATION', value: 'ORDER_CREATION' },
            { label: 'ORDER UPDATE', value: 'ORDER_UPDATE' },
            { label: 'ORDER DELETION', value: 'ORDER_DELETION' },
            { label: 'MISCELLANEOUS', value: 'MISCELLANEOUS' }
          ]"
          class="mt-auto"
          title="MESSAGE TYPE"
        />
      </div>
      <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-200 space-y-4 w-full max-h-[calc(100vh-200px)] overflow-y-auto notification-content-wrapper">
        <div class="flex justify-between">
          <div class="flex items-center mb-4 gap-2">
            <select @change="handleBulkSelect($event.target.value)" class="px-2 py-1 rounded border border-gray-300 cursor-pointer">
              <option value="none">Select None</option>
              <option value="all">Select All</option>
              <option value="read">Select Read</option>
              <option value="unread">Select Unread</option>
            </select>
            <div v-if="selectedNotifications.length !== 0"class="flex flex-row gap-2">
              <button @click="markSelectedAsRead" class="bg-green-100 hover:bg-green-200 px-2 py-1 rounded cursor-pointer">Mark Read</button>
              <button @click="markSelectedAsUnread" class="bg-yellow-100 hover:bg-yellow-200 px-2 py-1 rounded cursor-pointer">Mark Unread</button>
              <button @click="deleteSelectedNotifications" class="bg-red-100 hover:bg-red-200 px-2 py-1 rounded cursor-pointer">Delete Selected</button>
            </div>
          </div>

        </div>

        <template v-if="loading">
          <div class="flex items-center justify-center py-10">
            <svg class="animate-spin h-8 w-8 text-orange-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span class="ml-3 text-lg text-gray-600">Loading notifications...</span>
          </div>
        </template>
        <template v-else-if="filteredNotifications.length === 0">
          <div class="text-center text-gray-500 py-10">
            No notifications found.
          </div>
        </template>
        <template v-else>
          <div
            v-for="(notification, index) in filteredNotifications"
            :key="notification.id || notification.notification_id || index"
            :class="['rounded-md p-4 flex items-center transition-all duration-200 cursor-pointer', getBgColor(notification.type), 'hover:shadow-lg active:shadow-xl']"
          >
            <label :for="`notification-checkbox-${notification.id || notification.notification_id}`" class="flex items-center w-full cursor-pointer">
              <input
                type="checkbox"
                class="mr-4"
                v-model="selectedNotifications"
                :value="notification.id || notification.notification_id"
                :id="`notification-checkbox-${notification.id || notification.notification_id}`"
              />
              <div class="flex justify-between items-start w-full notification-content">
               <div class="flex flex-col">
                  <strong :class="getTitleColorClass(notification.title)">{{ notification.title }}</strong>
                  <p class="text-sm mt-1">{{ notification.data }}</p>
                </div>
                <div class="relative flex flex-col items-end text-sm text-gray-700 whitespace-nowrap ml-4">
                  <span>{{ formatTimestamp(notification.timestamp) }}</span>
                  <div v-if="!notification.read"
                       class="absolute w-6 h-6 bg-red-500 rounded-full"
                       style="bottom: -32px; right: 0;"></div>
                </div>
              </div>
            </label>
          </div>
        </template>
      </div>
    </div>
  </transition>
</template>

<script>
import FilterFunc from '../FilterFunc.vue';
import { useAuthStore } from "@/stores/auth.js";
import NotificationsSVG from "@/components/svg/NotificationsSVG.vue";

export default {
  name: 'Notifications',
  components: {
    FilterFunc,
    NotificationsSVG
  },
  data() {
    return {
      notifications: [],
      searchQuery: '',
      selectedCategory: null,
      selectedNotifications: [],
      logged_in_username: '',
      loading: true, 
    };
  },

  computed: {
    filteredNotifications() {
      let filtered = this.notifications.filter(note => {
        const matchesSearch = (note.title || '').toLowerCase().includes(this.searchQuery.toLowerCase());
        const matchesCategory = !this.selectedCategory || note.type === this.selectedCategory;
        return matchesSearch && matchesCategory;
      });
      return filtered;
    }
  },

  methods: {
    getBgColor(type) {
      if (type === 'message') return 'bg-purple-200';
      if (type === 'order_creation') return 'bg-green-100';
      if (type === 'order_update') return 'bg-yellow-100';
      if (type === 'order_deletion') return 'bg-red-100';
      return 'bg-gray-100';
    },
    getTitleColorClass(title) {
      if (!title) return 'text-gray-800'; 

      const lowerCaseTitle = title.toLowerCase();
      if (lowerCaseTitle.includes('created')) {
        return 'text-green-700';
      } else if (lowerCaseTitle.includes('updated')) {
        return 'text-blue-700';
      } else if (lowerCaseTitle.includes('deleted')) {
        return 'text-red-700';
      } else if (lowerCaseTitle.includes('completed')) {
        return 'text-purple-700';
      }
      return 'text-gray-800'; 
    },
    formatTimestamp(ts) {
      if (!ts) return '';

      let date;
      if (typeof ts === 'string') {
        const cleaned = ts.replace(' ', 'T').split('.')[0];
        date = new Date(cleaned);
      } else {
        date = ts;
      }

      if (!(date instanceof Date) || isNaN(date.getTime())) return '';
      return date.toLocaleString('en-GB', {
        day: 'numeric',
        month: 'long',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    },
    handleBulkSelect(type) {
      if (type === 'all') {
        this.selectedNotifications = this.filteredNotifications.map(n => n.id || n.notification_id);
      } else if (type === 'none') {
        this.selectedNotifications = [];
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
    }
  },
  setup(){
    const authStore = useAuthStore();
    return { authStore }
  },
  created(){
    this.logged_in_username = this.authStore.getUser.username.string
  },
  mounted() {
    let apiUrl = 'api/notifications';

    if (this.authStore.isUser && this.logged_in_username) {
      apiUrl += `?recipient=${encodeURIComponent(this.logged_in_username)}`;
    }

    // Set loading to true before the fetch request
    this.loading = true; 

    fetch(apiUrl)
      .then(async res => {
        const text = await res.text();
        if (!res.ok) {
          throw new Error('Server responded with ' + res.status + ': ' + text);
        }
        return JSON.parse(text);
      })
      .then(data => {
        console.log('Parsed data:', data);
        this.notifications = data;
        console.log(this.notifications);
      })
      .catch(err => {
        console.error('Failed to fetch notifications:', err);
      })
      .finally(() => {
        // Set loading to false after the fetch request completes, regardless of success or failure
        this.loading = false; 
      });
  },
};
</script>

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}
.fade-slide-enter-to {
  opacity: 1;
  transform: translateY(0);
}

input[type="checkbox"] {
  width: 20px;
  height: 20px;
}

.notification-content-wrapper {
  flex-grow: 1;
  overflow-y: auto;
  max-height: calc(100vh - 200px);
}
</style>