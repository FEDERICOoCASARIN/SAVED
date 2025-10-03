<template>
  <div class="flex flex-col" name="nav-container">
    <aside
      id="logo-sidebar"
      class="fixed top-0 left-0 z-40 w-64 h-screen transition-transform -translate-x-full sm:translate-x-0"
      aria-label="Sidebar"
    >
      <!-- Inner container for the sidebar content.  -->
        <div
        class="h-full px-3 py-4 overflow-y-auto bg-[url(src/assets/navbar.svg)] flex flex-col w-52"
      >
        <!-- Logo link at the top of the sidebar. -->
        <a href="localhost:5173/" class="flex m-3 ml-1 mb-10">
          <img src="../assets/icons/saved_logo.svg" />
        </a>
        <!-- Section title for the main menu items. -->
        <div class="mt-10 text-sm border-b-2 border-b-white-500/20 py-2 text-white ">
          Menu
        </div>
        <!-- Navigation links. Each NavbarItem represents a clickable menu item. -->
        <nav class="space-y-1 font-medium text-sm mt-2">
          <!-- Orders link. -->
          <NavbarItem :handle-click="setCurrentTab" :desc="'Orders'" :currentTab="currentTab" class="cursor-pointer">
            <OrdersSVG />
          </NavbarItem>
          <!-- Live Tracking link. -->
          <NavbarItem :handle-click="setCurrentTab" :desc="'Live'" :currentTab="currentTab" class="cursor-pointer">
            <LiveTrackingSVG />
          </NavbarItem>
          <!-- Calendar link. -->
          <NavbarItem :handle-click="setCurrentTab" :desc="'Calendar'" :currentTab="currentTab" class="cursor-pointer">
            <CalendarSVG />
          </NavbarItem>
          <!-- Users link, visible only if the user is an admin. -->
          <NavbarItem v-if="isAdmin" :handle-click="setCurrentTab" :desc="'Users'" :currentTab="currentTab" class="cursor-pointer">
            <UsersSVG />
          </NavbarItem>
          <!-- Vehicles link, visible only if the user is an admin. -->
          <NavbarItem v-if="isAdmin" :handle-click="setCurrentTab" :desc="'Vehicles'" :currentTab="currentTab" class="cursor-pointer">
            <VehiclesSVG />
          </NavbarItem>
          <!-- Analytics link, visible only if the user is an admin. -->
          <NavbarItem v-if="isAdmin" :handle-click="setCurrentTab" :desc="'Analytics'" :currentTab="currentTab" class="cursor-pointer">
            <AnalyticsSVG />
          </NavbarItem>
          <!-- Chat link. -->
          <NavbarItem :handle-click="setCurrentTab" :desc="'Chat'" :currentTab="currentTab" class="cursor-pointer">
            <ChatSVG />
          </NavbarItem>
        </nav>
        <!-- Notifications button (without label), placed separately below main nav) -->
        
        <!-- Bottom section of the navbar, containing only user profile links. -->
        <div class="mt-auto flex flex-col items-center gap-3 mb-4">
				<button
          @click="() => setCurrentTab('Notifications')"
          class="flex items-center p-1.5 transition duration-50 rounded-md w-10 justify-center"
          :class="[
            currentTab === 'Notifications'
              ? 'bg-[#F7DCC1] text-[#BD4E3C] shadow-xl'
              : 'text-white hover:text-[#BD4E3C] hover:bg-[#FFAC59]',
          ]">
          <NotificationsSVG class="w-7 h-7" />
        </button>
          <!-- User Profile link. -->
          <div
            name="navbar-profile"
            @click="setCurrentTab('UserProfile')"
            class="flex flex-col items-center justify-center text-white text-sm cursor-pointer hover:text-[#BD4E3C]"
          >
            <UserSVG class="size-10" />
            <span>User</span>
          </div>
        </div>
      </div>
    </aside>
  </div>
</template>

<script>
import NavbarItem from "./NavbarItem.vue";
// import NotificationsNavbarItem from "./notifications/NotificationsNavbarItem.vue";
import CalendarSVG from "./svg/CalendarSVG.vue";
import ChatSVG from "./svg/ChatSVG.vue";
import NotificationsSVG from "./svg/NotificationsSVG.vue";
import OrdersSVG from "./svg/OrdersSVG.vue";
import AnalyticsSVG from "./svg/StatisticsSVG.vue";
import UserSVG from "./svg/UserSVG.vue";
import UsersSVG from "./svg/UsersSVG.vue";
import VehiclesSVG from "./svg/VehiclesSVG.vue";
import LiveTrackingSVG from "./svg/LiveSVG.vue";
import NotificationButton from "./notifications/NotificationButton.vue";


export default {
  components: {
    CalendarSVG,
    ChatSVG,
    OrdersSVG,
    AnalyticsSVG,
    UserSVG,
    UsersSVG,
    VehiclesSVG,
    NotificationsSVG,
    NavbarItem,
    LiveTrackingSVG,
    NotificationButton
  },
  props: {
    currentTab: { // The currently active tab, used to highlight the corresponding navigation item.
      type: String,
      required: true,
    },
    setCurrentTab: { // A function passed from the parent to update the active tab.
      type: Function,
      required: true,
    },
    isAdmin: { // A boolean indicating if the current user has admin privileges, controlling visibility of certain menu items.
      type: Boolean,
      required: true,
    },
    isUser: { // A boolean indicating if the current user has regular user privileges (kept for consistency, but not directly used for conditional rendering in this template).
      type: Boolean,
      required: true
    }
  },
};
</script>

<style scoped>
/* CSS variable for the active sidebar item color. */
:root {
  --sidebar-item-active: rgb(137, 30, 5);
}

/* Base styles for the sidebar when hidden and visible on small vs. large screens */
/* The fixed positioning, z-index, width, height, and transition are key for the sliding effect. */
#logo-sidebar {
  /* No specific styles needed here as Tailwind classes handle translation and fixed positioning */
}

/* Styles for the inner content container of the sidebar, including its background image. */
.bg-\[url\(src\/assets\/navbar\.svg\)\] {
  /* This sets the background image for the sidebar. */
}

/* Basic styling for text elements within the sidebar, like menu titles and links. */
.text-white {
  color: white;
}
.text-sm {
  font-size: 0.875rem;
}
.font-medium {
  font-weight: 500;
}

/* Styles for hover and active states of navigation items. */
.transition-colors {
  transition-property: background-color, color;
}
.rounded-xl {
  border-radius: 0.75rem;
}
.cursor-pointer {
  cursor: pointer;
}
.hover\:bg-white:hover {
  background-color: white;
}
.hover\:text-\[\#BD4E3C\]:hover {
  color: #BD4E3C;
}

/* Ensures icons and text are properly aligned within flex containers. */
.flex {
  display: flex;
}
.items-center {
  align-items: center;
}
.justify-center {
  justify-content: center;
}
.gap-3 {
  gap: 0.75rem;
}
</style>
