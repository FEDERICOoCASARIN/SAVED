<template>
   <div class="flex flex-1 p-4 flex-col">
    <!-- Filter section for companies and vehicles, visible only if the authenticated user is an admin -->
    <div v-if="authStore.isAdmin" class="flex flex-row justify-center gap-5">
      <!-- Company filter dropdown -->
      <div class="flex mb-4 items-center">
        <label for="companyFilter" class="mr-2 text-gray-700 font-medium">Filter by Company:</label>
        <select
          id="companyFilter"
          v-model="selectedCompany"
          @change="filterOrdersByCompany"
          class="block w-auto py-2 px-3 border border-gray-300 bg-white rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm uppercase"
        >
          <option value="">All Companies</option>
          <option v-for="company in companyList" :key="company" :value="company">
            {{ company }}
          </option>
        </select>
      </div>

      <!-- Vehicle filter dropdown -->
      <div class="flex mb-4 items-center">
        <label for="vehicleFilter" class="mr-2 text-gray-700 font-medium">Filter by Vehicle:</label>
        <select
          id="vehicleFilter"
          v-model="selectedVehicle"
          @change="filterOrdersByVehicle"
          class="block w-auto py-2 px-3 border border-gray-300 bg-white rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm uppercase"
        >
          <option value="">All Vehicles</option>
          <option v-for="vehicle in vehicleList" :key="vehicle" :value="vehicle">
            {{ vehicle }}
          </option>
        </select>
      </div>
    </div>

    <!-- FullCalendar component, occupying remaining space and applying a slide-up animation -->
    <div class="flex-1 slide-up">
      <FullCalendar :options="calendarOptions" />
    </div>

    <!-- Transition wrapper for the CalendarEventPopup, providing fade-in/out animations -->
    <Transition
      enter-active-class="animate-fade-in"
      leave-active-class="animate-fade-out"
    >
      <!-- Popup for displaying details of a selected calendar event -->
      <CalendarEventPopup
        v-if="selectedEvent"
        :show="!!selectedEvent"
        :event="selectedEvent"
        @close="closePopupAndDeselect"
        :x="selectedX" :y="selectedY" :useRight="useRight"
      />
    </Transition>

    <!-- Confirmation popup for changes to calendar orders (e.g., drag/drop, resize) -->
    <CalendarConfirmationPopup
      :showModal="showChangeOrderConfirmationPopup"
      :pendingChange="pendingChange"
      @confirm="applyChange"
      @cancel="cancelChange(this.currentInfo)"
    />
    <!-- Popup displayed when a calendar action (e.g., dragging to past) is not allowed -->
    <CalendarNotAllowed :showPopup="showCalendarNotAllowedPopup" @close="showCalendarNotAllowedPopup = false" :errorCaption="this.errorCaption" />

  </div>
</template>

<script>
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import timeGridPlugin from '@fullcalendar/timegrid'
import listPlugin from '@fullcalendar/list'
import CalendarEventPopup from './CalendarEventPopup.vue'
import { useAuthStore } from '@/stores/auth'
import CalendarConfirmationPopup from './CalendarConfirmationPopup.vue'
import CalendarNotAllowed from './CalendarNotAllowed.vue'


export default {
  components: {
    FullCalendar,
    CalendarEventPopup,
    CalendarConfirmationPopup,
    CalendarNotAllowed
  },
  data() {
    return {
      selectedEvent: null,
      prevSelectedElement: null,
      selectedY: null,
      selectedX: null,
      useRight: false,
      activeOrders: [],
      events: [],
      showChangeOrderConfirmationPopup: false,
      showCalendarNotAllowedPopup: false,
      currentInfo: null,
      pendingChange: null,
      errorCaption: null,
      logged_in_username: '',
      selectedCompany: '',
      selectedVehicle: '',
      companyList: [],
      vehicleList: []
    }
  },
  computed: {
    /**
     * Configures and returns the options for the FullCalendar component.
     * Includes plugins, toolbar settings, event handling, and editability based on user roles.
     * @returns {object} FullCalendar options object.
     */
    calendarOptions() {
      return {
        plugins: [dayGridPlugin, interactionPlugin, timeGridPlugin, listPlugin],
        initialView: 'dayGridMonth',
        headerToolbar: {
          left: 'prev,next today',
          center: 'title',
          right: 'dayGridMonth,timeGridWeek,timeGridDay'
        },
        titleFormat: {
          month: 'long',
          year: 'numeric',
        },
        buttonText: {
          today: 'Today',
          month: 'Month',
          week: 'Week',
          day: 'Day',
          list: 'List'
        },
        buttonIcons: {
          prev: 'chevron-left',
          next: 'chevron-right'
        },
        validRange: {
          start: new Date()
        },
        events: this.events,
        eventClick: this.handleEventClick,
        editable: this.isCalendarEditable,
        eventStartEditable: this.isCalendarEditable,
        eventDurationEditable: this.isCalendarEditable,
        droppable: this.isCalendarEditable,
        eventDrop: this.handleEventDrop,
        eventResize: this.handleEventResize,
        now: new Date(),
        nowIndicator: true,
        slotDuration: '00:15:00',
        dayMaxEvents: 5,
      }
    },
    /**
     * Determines if the calendar events are editable based on the user's admin status.
     * @returns {boolean} True if the user is an admin, false otherwise.
     */
    isCalendarEditable() {
      const authStore = useAuthStore();
      return authStore.isAdmin;
    }
  },
  methods: {
    /**
     * Closes the event popup and deselects any previously selected event.
     */
    closePopupAndDeselect() {
      this.selectedEvent = null;
      if (this.prevSelectedElement) {
        this.prevSelectedElement.classList.remove('selected-event');
        this.prevSelectedElement = null;
      }
    },
    /**
     * Handles the click event on a calendar event.
     * Opens a popup displaying event details and highlights the clicked event.
     * @param {object} info - FullCalendar event click info object.
     */
    handleEventClick(info) {
    this.selectedEvent = {
      title: info.event.title,
      start: info.event.start,
      end: info.event.end,
      color: info.event.color,
      status: info.event.extendedProps.status,
      requester: info.event.extendedProps.requester
    };

    // Remove previous selection and add to current
    if (this.prevSelectedElement) {
      this.prevSelectedElement.classList.remove('selected-event');
    }
    info.el.classList.add('selected-event');
    this.prevSelectedElement = info.el;

    const eventRect = info.el.getBoundingClientRect();
    const popupWidth = 400; // Define popup width as a constant
    const popupHeight = 350; // Estimate popup height or get it dynamically if needed
    const padding = 20; // Padding from viewport edges

    let newX, newY;
    let useRight = false;

    // Calculate vertical position (Y)
    // Try to center the popup vertically with the event, or adjust if it goes off screen
    newY = eventRect.top + (eventRect.height / 2) - (popupHeight / 2);

    // Ensure the popup doesn't go above the top of the viewport
    if (newY < padding) {
      newY = padding;
    }
    // Ensure the popup doesn't go below the bottom of the viewport
    if (newY + popupHeight > window.innerHeight - padding) {
      newY = window.innerHeight - popupHeight - padding;
    }

    // Calculate horizontal position (X)
    // Option 1: Position to the right of the event
    const rightPositionX = eventRect.right + 10; // 10px spacing from event

    // Option 2: Position to the left of the event
    const leftPositionX = eventRect.left - popupWidth - 10; // 10px spacing from event

    // Check if there's enough space to the right
    if (rightPositionX + popupWidth <= window.innerWidth - padding) {
      newX = rightPositionX;
      useRight = false; // Popup is positioned from the left edge
    }
    // If not enough space to the right, check if there's enough space to the left
    else if (leftPositionX >= padding) {
      newX = leftPositionX;
      useRight = false; // Popup is positioned from the left edge
    }
    // If neither side has enough space, center it horizontally
    else {
      newX = (window.innerWidth / 2) - (popupWidth / 2);
      useRight = false;
    }

    this.selectedX = newX;
    this.selectedY = newY;
    this.useRight = useRight; // Set based on the logic above
  },
    /**
     * Handles the event drop (drag-and-drop) action on a calendar event.
     * Validates the new time range and opens a confirmation popup.
     * @param {object} info - FullCalendar event drop info object.
     */
    handleEventDrop(info) {
      let now = new Date();

      if (!this.isCalendarEditable) {
        info.revert();
        return;
      }
      
      if (info.event.extendedProps.status === 'undergoing') {
        this.showCalendarNotAllowedPopup = true;
        this.errorCaption = "You can't drag events that are currently undergoing."
        info.revert();
        return;
      }

      if (info.event.start < now) {
        this.showCalendarNotAllowedPopup = true;
        this.errorCaption = "You can't drag and drop events past the current time."
        info.revert();
        return;
      }

      const newStart = info.event.start;
      const newEnd = info.event.end;

      this.currentInfo = info; // Store current info for revert

      this.pendingChange = {
        event: info.event,
        newStart,
        newEnd,
        oldStart: new Date(info.oldEvent.start),
        oldEnd: new Date(info.oldEvent.end),
        color: info.event.color,
        orderId: info.event.extendedProps.orderId
      }
      this.showChangeOrderConfirmationPopup = true;
    },
    /**
     * Handles the event resize action on a calendar event.
     * Validates the new time range and opens a confirmation popup.
     * @param {object} info - FullCalendar event resize info object.
     */
    handleEventResize(info) {
      let now = new Date();

      if (!this.isCalendarEditable) {
        info.revert();
        return;
      }

      if (info.event.start < now) {
        this.showCalendarNotAllowedPopup = true;
        this.errorCaption = "You can't resize events past the current time."
        info.revert();
        return;
      }

      const newStart = info.event.start;
      const newEnd = info.event.end;

      this.currentInfo = info; // Store current info for revert

      this.pendingChange = {
        event: info.event,
        newStart,
        newEnd,
        oldStart: new Date(info.oldEvent.start),
        oldEnd: new Date(info.oldEvent.end),
        color: info.event.color,
        orderId: info.event.extendedProps.orderId
      }
      this.showChangeOrderConfirmationPopup = true;
    },
    /**
     * Applies the pending event change (drag or resize) and updates the backend.
     */
    applyChange() {
      const e = this.pendingChange.event;
      e.setStart(this.pendingChange.newStart);
      e.setEnd(this.pendingChange.newEnd);

      this.updateOrderTimeToDB(this.pendingChange);
      this.showChangeOrderConfirmationPopup = false;
      this.pendingChange = null;
    },
    /**
     * Cancels the pending event change, reverting the event to its original state.
     * @param {object} info - FullCalendar event info object (for revert).
     */
    cancelChange(info) {
      info.revert();
      this.showChangeOrderConfirmationPopup = false;
      this.pendingChange = null;
    },
    /**
     * Fetches all orders from the backend based on current filters (company and vehicle).
     * Populates the calendar events.
     */
    async getAllOrders() {
      this.events = [];

      let statusFilter = ['created','scheduled', 'undergoing']
      try {
        const queryParams = new URLSearchParams();

         for(const status of statusFilter){
                queryParams.append("status", status)
          }

        if (this.authStore.isAdmin && this.selectedCompany !== '') {
          queryParams.append('order_for', this.selectedCompany);
        } else if (this.authStore.isUser) {
          queryParams.append('order_for', this.logged_in_username);
        }

        if (this.authStore.isAdmin && this.selectedVehicle !== '') {
          queryParams.append('vehicle_id', this.selectedVehicle);
        }

        const result = await fetch(`api/orders?${queryParams.toString()}`);

        if (!result.ok) {
          const errorBody = await result.text();
          throw new Error(`HTTP error! Status: ${result.status} - ${errorBody}`);
        }

        let data = await result.json();
        this.activeOrders = data;
        this.events = data.map((order) => this.mapBackendToFrontend(order));
      } catch (err) {
        console.error('Error when retrieving all orders from database (calendar): ' + err);
      }
    },
    /**
     * Updates the time range of an order in the backend.
     * @param {object} newData - Object containing the order ID and new start/end times.
     */
    async updateOrderTimeToDB(newData) {
      const id = newData.orderId;
      const data = {
        twStart: newData.newStart,
        twEnd: newData.newEnd
      };

      try {
        const result = await fetch(`api/orders/${id}`, {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify(data)
        });

        if (!result.ok) {
          const errorBody = await result.text();
          throw new Error(`HTTP error! Status: ${result.status} - ${errorBody}`);
        }

        await this.getAllOrders(); // Refresh orders after update
      } catch (err) {
        console.error("Error updating order (calendar): ", err);
      }
    },
    /**
     * Maps backend order data to a format suitable for FullCalendar events.
     * @param {object} order - The order object from the backend.
     * @returns {object} A FullCalendar event object.
     */
    mapBackendToFrontend(order) {
      return {
        title: `Order #${order.orderId} - ${order.order_for}`,
        start: order.twStart,
        end: order.twEnd,
        color: this.getStatusColor(order.status),
        extendedProps: {
          requester: order.requester,
          status: order.status,
          orderId: order.orderId,
          order_for: order.order_for
        },
      }
    },
    /**
     * Returns a color code based on the order status.
     * @param {string} status - The status of the order.
     * @returns {string} Hexadecimal color code.
     */
    getStatusColor(status) {
      switch (status) {
        case 'created': return '#6B7280';
        case 'scheduled': return '#3B82F6';
        case 'undergoing': return '#F97316';
        case 'completed': return '#10B981';
        case 'canceled': return '#EF4444';
        default: return '#6B7280'; // Default color for unknown status
      }
    },
    /**
     * Fetches the list of companies if the logged-in user is an admin.
     */
    async getCompanyList() {
      if (this.authStore.isAdmin) {
        try {
          const result = await fetch('api/companies?limit=100');
          if (!result.ok) {
            const errorBody = await result.text();
            throw new Error(`HTTP error! Status: ${result.status} - ${errorBody}`);
          }
          let data = await result.json();
          this.companyList = data.map((cur) => cur.username);
        } catch (err) {
          console.error("Error getting all companies:", err);
        }
      }
    },
    /**
     * Fetches the list of vehicles if the logged-in user is an admin.
     */
    async getAllVehicles() {
      if (this.authStore.isAdmin) {
        this.vehicleList = [];
        try {
          const result = await fetch('api/vehicle');
          if (!result.ok) {
            const errorBody = await result.text();
            throw new Error(`HTTP error! Status: ${result.status} - ${errorBody}`);
          }
          let data = await result.json();
          this.vehicleList = data.map(vehicle => String(vehicle.vehicleId));
        } catch (err) {
          console.error("Error getting all vehicles: ", err);
        }
      }
    },
    /**
     * Triggers fetching of all orders when the company filter changes.
     */
    filterOrdersByCompany() {
      this.getAllOrders();
    },
    /**
     * Triggers fetching of all orders when the vehicle filter changes.
     */
    filterOrdersByVehicle() {
      this.getAllOrders();
    }
  },
  /**
   * Lifecycle hook: Called after the instance is created.
   * Initializes logged-in username, fetches company list, all orders, and all vehicles.
   */
  created() {
    this.logged_in_username = this.authStore.getUser.username.string;
    this.getCompanyList();
    this.getAllOrders();
    this.getAllVehicles();
  },
  /**
   * Vue 3 Composition API setup function for accessing the auth store.
   * @returns {object} An object containing the authStore.
   */
  setup() {
    const authStore = useAuthStore();
    return { authStore };
  }
}
</script>

<style scoped>
/* Scoped styles for the Calendar component, ensuring they only apply within this component */

/* Transition for fading elements */
.fade-in {
  animation: fadeIn 0.5s ease-in;
}

/* Transition for sliding elements up */
.slide-up {
  animation: slideUp 0.4s ease-out;
}

/* Keyframe for fadeIn animation */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* Keyframe for slideUp animation */
@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* Popup entrance animation */
.animate-fade-in {
  animation: popIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Popup exit animation */
.animate-fade-out {
  animation: popOut 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Keyframe for popIn animation (scale and fade in) */
@keyframes popIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* Keyframe for popOut animation (scale and fade out) */
@keyframes popOut {
  from {
    opacity: 1;
    transform: scale(1);
  }
  to {
    opacity: 0;
    transform: scale(0.95);
  }
}

/* Deep selector to target FullCalendar's internal elements for custom styling */
:deep(.fc) {
  /* Styling for primary buttons within FullCalendar (e.g., prev, next, view buttons) */
  .fc-button-primary {
    background-color: #FA812F; /* Orange background */
    border: none;
    color: white;
    font-weight: 500;
    border-radius: 0.375rem; /* Rounded corners */
    box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); /* Subtle shadow */
    padding: 0.5rem 1rem;
  }

  /* Hover effect for primary buttons */
  .fc-button-primary:hover {
    background-color: #E57229; /* Darker orange on hover */
    box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1); /* Larger shadow on hover */
    transform: translateY(-0.5px); /* Slight lift effect */
  }

  /* Active (clicked) effect for primary buttons */
  .fc-button-primary:active {
    background-color: #D4631E; /* Even darker orange when active */
    transform: translateY(0); /* Reset lift effect */
  }

  /* Style for the "Today" button specifically */
  .fc-today-button {
    background-color: white;
    color: #FA812F; /* Orange text */
    border: 1px solid #FA812F; /* Orange border */
    border-radius: 250px; /* Pill shape */
  }

  /* Hover effect for the "Today" button */
  .fc-today-button:hover {
    background-color: #FA812F; /* Orange background on hover */
    color: white;
  }

  /* Style for the calendar toolbar title (e.g., "June 2025") */
  .fc-toolbar-title {
    font-size: 2.5rem;
    font-weight: 300;
    color: #1f2937; /* Dark gray text */
  }

  /* Style for day column headers (e.g., "Sun", "Mon") */
  .fc-col-header-cell {
    background-color: #f9fafb; /* Light gray background */
    font-weight: 600;
    color: #374151; /* Darker gray text */
    padding-top: 0.75rem;
    padding-bottom: 0.75rem;
  }

  /* Style for the current day cell in the calendar grid */
  .fc-day-today {
    background-color: rgba(255, 237, 213, 0.5); /* Light orange with transparency */
  }

  /* General style for all calendar events */
  .fc-event {
    border-radius: 0.375rem; /* Rounded corners */
    border: black; /* Border color for events */
    cursor: pointer;
    transition: transform 0.2s ease, box-shadow 0.2s ease; /* Smooth transitions for hover/selection */
    background-color: #FA812F; /* Default event background color (orange) */
    color: white;
  }

  /* Style for a selected event, indicating it's currently active or clicked */
  .selected-event {
    transform: translateY(-1.5px); /* Lift effect */
    z-index: 5; /* Ensure it stays above other events */
    box-shadow: 0 6px 6px -1px rgb(0 0 0 / 0.1); /* More prominent shadow */
  }

  /* Hover effect for calendar events */
  .fc-event:hover {
    transform: translateY(-1.5px); /* Lift effect on hover */
    background-color: #ffae78; /* Lighter orange on hover */
    box-shadow: 6px 6px 6px -1px rgb(0 0 0 / 0.1); /* Larger shadow on hover */
  }

  /* Spacing for FullCalendar button groups */
  .fc-button-group {
    gap: 0.25rem; /* Small gap between buttons in a group */
  }

  /* Style for disabled FullCalendar primary buttons */
  .fc-button-primary:disabled {
    opacity: 0.5; /* Reduce opacity */
    cursor: not-allowed; /* Change cursor to indicate non-interactiveness */
  }

  /* Uppercase text for time slot labels in timeGrid views */
  .fc-timegrid-slot-label {
    text-transform: uppercase;
  }

  /* Uppercase text for time display in list view items */
  .fc-list-item-time {
    text-transform: uppercase;
  }
}
</style>
