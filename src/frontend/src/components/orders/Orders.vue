<template>
  <transition name="fade-slide" appear>
    
    <div class="flex flex-col">
      <div class="flex-row max-h-10px">
        <div class="flex mb-5 justify-between px-10 pt-10">
          <div class="flex items-center space-x-3">
            <div class="p-2 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl shadow-lg">
              <OrdersSVG class="size-6 text-white" />
            </div>
            <div class="flex flex-col">
              <h1 class="text-4xl font-bold">
                Orders
                <span class="text-xl text-gray-300">({{ orders.length }})</span>
              </h1>
              <p class="text-gray-400 mt-1">
                Manage
                <span>
                  {{ authStore.isAdmin ? "all" : "your" }}
                </span>
                orders
              </p>
            </div>
          </div>
          <div class="flex gap-5 max-h-12 align-center">
            <div class="relative flex">
              <NotificationButton
                class="border-1 border-gray-200 shadow-sm hover:bg-gray-100"
                @click="showNotificationList = !showNotificationList"
              />
              <span v-if="hasNotification" class="absolute top-0 right-0 block h-2.5 w-2.5 rounded-full bg-red-500 ring-2 ring-white"></span>

              <!-- Notification dropdown -->
              <NotificationList 
              v-if="showNotificationList"
               :loggedInUsername="this.logged_in_username"
              />
            </div>
            <button
              type="submit"
              class="border-1 border-gray-200 text-sm rounded-md p-2 text-black cursor-pointer shadow-sm hover:bg-gray-100"
            >
              <span
                @click="showChangeLog = true"
                class="flex items-center gap-2 px-2"
              >
                <ChangeLogSVG />
                Change Log
              </span>
            </button>

            <div class="flex gap-5 relative">
              <button
                type="button"
                @click="toggleExportOrdersButton"
                class="border-1 border-gray-200 text-sm rounded-md p-2 text-black cursor-pointer shadow-sm hover:bg-gray-100"
              >
                <span class="flex items-center gap-2 px-2">
                  <ExportArrowSVG />
                  Export
                </span>
              </button>

              <transition name="dropdown">
                <div
                  v-if="showExportDropdown"
                  ref="exportDropdown"
                  class="absolute top-[105%] right-0 z-10 bg-white border border-gray-300 rounded-md shadow-lg w-48"
                >
                  <ul class="text-sm text-gray-700">
                    <li
                      @click="selectExportType('xlsx')"
                      class="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                    >
                      Export as XLSX
                    </li>
                    <li
                      @click="selectExportType('csv')"
                      class="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                    >
                      Export as CSV
                    </li>
                  </ul>
                </div>
              </transition>
            </div>

            <button
              @click="showAddOrderPopup = true"
              type="submit"
              class="bg-[#FA812F] hover:bg-orange-500 rounded-xl px-2 py-1 text-white cursor-pointer"
            >
              <span class="flex items-center gap-1">
                <AddSVG />
                Create Order
              </span>
            </button>
          </div>
        </div>

        <transition name="slide-fade" appear>
          <div
            v-if="showUserStats && authStore.isUser"
            class="flex flex-1 flex-col px-10 pb-8 pt-3 gap-4"
          >
            <div class="flex flex-1 rounded-lg flex-row gap-4 flex-wrap">
              <!-- Card 1: Total Orders -->
              <div
                class="flex-1 min-w-[200px] flex rounded-xl p-4 flex-col shadow-md bg-white"
              >
                <div class="flex">
                  <h2 class="ml-1 text-xl font-semibold text-gray-700">
                    Total Orders
                  </h2>
                </div>
                <div
                  class="flex-1 text-[3.5rem] flex items-end ml-1 text-[#FA812F]"
                >
                  <AnimatedNumber :value="userStats.totalOrders" />
                </div>
              </div>

              <!-- Card 2: Completion Rate -->
              <div
                class="flex-1 min-w-[200px] flex rounded-xl p-4 flex-col shadow-md bg-white"
              >
                <div class="flex">
                  <h2 class="ml-1 text-xl font-semibold text-gray-700">
                    Completion Rate
                  </h2>
                </div>
                <div
                  class="flex-1 text-[3.5rem] flex items-end ml-1 text-[#FA812F]"
                >
                  <AnimatedNumber
                    :value="userStats.completionRate"
                    :fixed="1"
                  />
                  <div class="text-xl ml-1 mb-2">%</div>
                </div>
              </div>

              <!-- Card 3: Total Freight Value -->
              <div
                class="flex-1 min-w-[200px] flex rounded-xl p-4 flex-col shadow-md bg-white"
              >
                <div class="flex">
                  <h2 class="ml-1 text-xl font-semibold text-gray-700">
                    Total Freight Value
                  </h2>
                </div>
                <div
                  class="flex-1 text-[3.5rem] flex items-end ml-1 text-[#FA812F]"
                >
                  <span class="text-2xl ml-1 mb-2">$</span>
                  <AnimatedNumber :value="userStats.totalFreightValue" />
                </div>
              </div>

              <!-- Card 4: Average Weight -->
              <div
                class="flex-1 min-w-[200px] flex rounded-xl p-4 flex-col shadow-md bg-white"
              >
                <div class="flex">
                  <h2 class="ml-1 text-xl font-semibold text-gray-700">
                    Average Weight
                  </h2>
                </div>
                <div
                  class="flex-1 text-[3.5rem] flex items-end ml-1 text-[#FA812F]"
                >
                  <AnimatedNumber :value="userStats.averageWeight" :fixed="1" />
                  <div class="text-xl ml-1 mb-2">kg</div>
                </div>
              </div>
            </div>
          </div>
        </transition>

        <span class="px-10 text-xs text-gray-500">Press Enter to Search</span>
        <div class="flex mb-5 px-10 justify-between">
          <div
            class="border-1 border-gray-300 rounded-md px-4 py-1 items-center flex focus-within:outline-[#FA812F] focus-within:outline focus-within:outline-2"
          >
            <span class="flex items-center justify-content w-full">
              <label>
                <SearchSVG />
              </label>
              <input
                type="text"
                v-model="searchQuery"
                @keyup.enter="getOrderById"
                class="text-base flex flex-1 p-2 rounded-md flex-1 ml-2 w-50 focus:outline-none"
                placeholder="Search Order by ID"
                style="outline: none !important; box-shadow: none !important;"

              />
              <CancelSVG
                v-if="searchQuery || isSearchActive"
                @click="clearSearch"
                class="w-5 h-5 text-red-500 cursor-pointer"
              />
              <div
                v-else
                class="w-5 h-5 cursor-pointer text-gray-500 hover:text-gray-700"
              />
            </span>
          </div>

          <!-- Filters -->
          <div class="flex gap-2 border-1 rounded-lg p-2 border-gray-200">
            <div class="flex mr-2" v-if="authStore.isAdmin">
              <FilterDropdown
                v-model="vehicleFilter"
                :options="vehicleList"
                title="VEHICLE ID"
              />
            </div>
            <div v-if="authStore.isAdmin" class="flex mr-2">
              <FilterDropdown
                v-model="requesterFilter"
                :options="companyList"
                title="RECIPIENT"
                class="uppercase"
              />
            </div>
            <div class="flex mr-2">
              <FilterDropdown
                v-model="statusFilter"
                :options="[
                  'created',
                  'scheduled',
                  'undergoing',
                  'completed',
                  'canceled',
                ]"
                title="ORDER STATUS"
                class="uppercase"
              />
            </div>

            <button
              type="submit"
              @click="getAllOrders"
              class="bg-[#FA812F] hover:bg-orange-500 rounded-lg px-3 py-1 text-white cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="!hasFilters"
            >
              <span class="flex items-center gap-1"> Apply </span>
            </button>
            <button
              v-if="isFilterActive"
              type="submit"
              class="rounded-lg text-red-500 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="!hasFilters || !isFilterActive"
              @click="clearFilter"
            >
              <span class="flex items-center gap-1">
                <CancelSVG class="w-4 h-4" />
              </span>
            </button>
            <div v-else class="w-4 h-4"></div>
          </div>

          <!-- Sort  -->
          <div class="flex">
            <SortFunc
              v-model="sortOption"
              :options="['DATE', 'ETD', 'ETA']"
              class="w-40"
            />
          </div>
        </div>
      </div>

      <!-- Table section -->
      <div class="flex-1 px-10 pt-3 pb-32">
        <div
          class="flex-1 bg-[#ADABCF] rounded-t-md h-[calc(100vh-250px)] shadow-xl border-t border-gray-200"
        >
          <!-- Table header -->
          <div
            class="flex items-center bg-white border-b border-gray-200 rounded-t-md"
          >
            <div
              class="basis-0 grow-[1.5] p-4 font-medium text-gray-500 flex justify-center"
            >
              Order ID.
            </div>
            <div
              class="basis-0 grow-[1.5] p-4 font-medium text-gray-500 flex justify-center"
            >
              Created at
            </div>
            <div
              class="basis-0 grow-[3] p-4 font-medium text-gray-500 flex justify-center"
            >
              Pick-up
            </div>
            <div
              class="basis-0 grow-[1] p-4 font-medium text-gray-500 flex justify-center"
            >
              ETD
            </div>
            <div
              class="basis-0 grow-[1] p-4 font-medium text-gray-500 flex justify-center"
            >
              ETA
            </div>

            <div
              class="basis-0 grow-[2] p-4 font-medium text-gray-500 flex justify-center"
            >
              Status
            </div>

            <div
              class="basis-0 grow-[1.5] p-4 font-medium text-gray-500 flex justify-center"
            >
              In/Out
            </div>
            <div
              class="basis-0 grow-[1.5] p-4 font-medium text-gray-500 flex justify-center"
            >
              Actions
            </div>
          </div>
          <div class=" bg-white h-full"
		      :class="{
            'overflow-hidden': isChildDropdownOpen,
            'overflow-auto': !isChildDropdownOpen
          }">
            <transition-group name="stagger-fade" tag="div">
              <OrdersTableItem
                v-for="(order, i) in filteredAndSortedOrders"
                :key="i"
                :orderDetail="order"
                :style="{ animationDelay: i * 100 + 'ms' }"
                class="animated-row"
                @deleteOrder="deleteOrderFromDB"
				@cancelOrder="cancelOrder"
                @updateOrder="updateAnOrder"
                @viewOrder="viewOrderFunction"
				@dropdown-open-state-changed = "handleDropdownStateChange"
        
              />
            </transition-group>

            <div
              v-if="filteredAndSortedOrders.length === 0"
              class="flex justify-center items-center h-full text-gray-500 text-lg"
            >
              <p v-if="searchQuery">
                No orders found for "{{ searchQuery }}". Try a different ID.
              </p>
              <p v-else-if="isFilterActive">
                No orders match the current filters.
              </p>
              <p v-else>No orders to display.</p>
            </div>
          </div>

          <transition name="fade">
            <AddOrderPopup
              v-if="showAddOrderPopup"
              @close="showAddOrderPopup = false"
              @save="addAnOrderToDB"
              :companyList="this.companyList"
              :loggedInUsername="logged_in_username"
            />
          </transition>

          <transition name="fade">
            <ChangeLog v-if="showChangeLog" @close="showChangeLog = false" />
          </transition>

          <teleport to="body">
            <SuccessConfirmationPopup
              :show="showSuccessfulConfirmation"
              :title="this.successTitle"
              :caption="this.successCaption"
              @close="showSuccessfulConfirmation = false"
            />
          </teleport>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
import AddOrderPopup from "@/components/orders/AddOrderPopup.vue";
import EditOrderPopup from "@/components/orders/EditOrderPopup.vue";
import OrderViewPopup from "@/components/orders/OrderViewPopup.vue";
import OrdersTableItem from "@/components/orders/OrdersTableItem.vue";
import ChangeLogSVG from "@/components/svg/ChangeLogSVG.vue";
import ExportArrowSVG from "@/components/svg/ExportArrowSVG.vue";
import FilterSVG from "@/components/svg/FilterSVG.vue";
import SearchSVG from "@/components/svg/SearchSVG.vue";
import FilterDropdown from "@/components/FilterFunc.vue";
import SortArrowSVG from "@/components/svg/SortArrowSVG.vue";
import AddSVG from "@/components/svg/AddSVG.vue";
import CancelSVG from "@/components/svg/CancelSVG.vue";
import ChangeLog from "@/components/orders/ChangeLog.vue";
import SuccessConfirmationPopup from "../SuccessfulConfirmationPopup.vue";
import SortFunc from "../SortFunc.vue";
import AnimatedNumber from "../AnimatedNumber.vue";
import { ref } from "vue";
import { useAuthStore } from "@/stores/auth.js";
import OrdersSVG from "../svg/OrdersSVG.vue";
import NotificationButton from "../notifications/NotificationButton.vue";
import NotificationList from "@/components/notifications/NotificationList.vue";


export default {
  components: {
    OrdersTableItem,
    ExportArrowSVG,
    FilterSVG,
    SearchSVG,
    SortArrowSVG,
    ChangeLogSVG,
    AddOrderPopup,
    EditOrderPopup,
    FilterDropdown,
    OrderViewPopup,
    ChangeLog,
    AddSVG,
    SuccessConfirmationPopup,
    SortFunc,
    CancelSVG,
    AnimatedNumber,
    OrdersSVG,
    NotificationButton,
    NotificationList
  },
  data() {
    return {
      showAddOrderPopup: false,
      showOrderViewPopup: false,
      statusFilter: null,
      requesterFilter: null,
      vehicleFilter: null,
      sortOption: null,
      showChangeLog: false,
      showSuccessfulConfirmation: false,
      successTitle: "",
      successCaption: "",
      searchQuery: "",
      companyList: [],
      vehicleList: [],
      isFilterActive: false,
      isSearchActive: false,
      routeTime: null,
      logged_in_username: "", 
      source: "",
      destination: "",
      showUserStats: false, // Control visibility with animation
      userStats: {
        totalOrders: 0,
        completedOrders: 0,
        completionRate: 0.0,
        totalFreightValue: 0,
        averageWeight: 0.0,
      },
      hasNotification: true,
      orders: [],
      isFiltering: false,
      showExportDropdown: false,
	    isChildDropdownOpen: false,
      showNotificationList: false,
      
    };
  },
  methods: {
    handleDropdownStateChange(isOpen) {
      this.isChildDropdownOpen = isOpen;
      if (isOpen) {
        document.body.style.overflow = 'hidden';
      } else {
        document.body.style.overflow = 'auto';
      }
    },
    async fetchUserStats() {
      if (this.authStore.isUser) {
        try {
          // Construct the URL with the dynamic companyName
          const url = `/api/analytics/user-analytics?companyName=${this.logged_in_username}`; 
          const res = await fetch(url);
          if (!res.ok) {
            const errorBody = await res.text();
            throw new Error(`HTTP error! Status: ${res.status} - ${errorBody}`);
          }
          const data = await res.json();

          // Update the userStats data property
          this.userStats.totalOrders = data.totalOrders || 0;
          this.userStats.completedOrders = data.completedOrders || 0;
          // Calculate completion rate locally if not provided by backend, or use backend's value
          this.userStats.completionRate =
            data.completionRate !== undefined
              ? data.completionRate
              : this.userStats.totalOrders > 0
                ? (this.userStats.completedOrders /
                    this.userStats.totalOrders) *
                  100.0
                : 0.0;
          this.userStats.totalFreightValue = data.totalFreightValue || 0;
          this.userStats.averageWeight = data.averageWeight || 0.0;
        } catch (err) {
          console.error("Failed to fetch user stats:", err);
          // Optionally, reset stats to 0 or display an error state
          this.userStats = {
            totalOrders: 0,
            completedOrders: 0,
            completionRate: 0.0,
            totalFreightValue: 0,
            averageWeight: 0.0,
          };
        }
      }
    },

    // Clear search
    clearSearch() {
      if (this.isSearchActive) {
        this.getAllOrders();
      }
      this.searchQuery = "";
      this.isSearchActive = false;
    },

    // Modified parseDDMMYYYY to be more explicit in how dates are constructed
    parseDDMMYYYY(dateString) {
      if (!dateString) return null;
      const parts = dateString.split("/");
      // Ensure month is 0-indexed for Date constructor
      return new Date(Number(parts[2]), Number(parts[1]) - 1, Number(parts[0]));
    },

    //Parse datetime string into date object
    parseLocalDateTimeString(dateTimeString) {
      if (!dateTimeString) return null;
      const date = new Date(dateTimeString);
      return isNaN(date.getTime()) ? null : date;
    },

    //Open view order popup, upon clicking
    viewOrderFunction(order, orderId) {
      this.selectedOrder = { ...order, orderId };
      this.showOrderViewPopup = true;
    },

    //Open change log popup, upon clicking
    viewChangeLog() {
      this.showChangeLog = true;
    },

    // Format from datetime string into timestamp to be sent back to backend
    formatToTimestamp(dateTimeStr) {
      if (!dateTimeStr) return null;

      // Split by comma-space first, then by space if not found
      let parts = dateTimeStr.split(", ");
      if (parts.length === 1) {
        parts = dateTimeStr.split(" ");
      }

      let datePart = parts[0];
      let timePart = parts.length > 1 ? parts.slice(1).join(" ") : "00:00:00"; // Default to midnight if no time

      let dateComponents;
      if (datePart.includes("/")) {
        dateComponents = datePart.split("/");
      } else if (datePart.includes("-")) {
        dateComponents = datePart.split("-");
      } else {
        console.warn(
          "Could not determine date separator (/, -) for:",
          datePart
        );
        return null; // Cannot parse date format
      }

      // Assuming DD/MM/YYYY or DD-MM-YYYY from user's explicit example
      const day = Number(dateComponents[0]);
      const month = Number(dateComponents[1]);
      const year = Number(dateComponents[2]);

      const timeComponents = timePart.split(":");
      const hours = Number(timeComponents[0] || "00");
      const minutes = Number(timeComponents[1] || "00");
      const seconds = Number(timeComponents[2] || "00");

      const localDate = new Date(
        year,
        month - 1, 
        day,
        hours,
        minutes,
        seconds
      );

      // If the constructed date is invalid, return null
      if (isNaN(localDate.getTime())) {
        console.error("Invalid date constructed from:", dateTimeStr);
        return null;
      }

      return localDate.toISOString();
    },

    // Apply filter
    applyFilter() {
      const filterMap = {};

      if (this.statusFilter) {
        filterMap.status = this.statusFilter;
        this.isFilterActive = true;
      }
      if (this.requesterFilter) {
        filterMap.order_for = this.requesterFilter;
        this.isFilterActive = true;
      }
      if (this.vehicleFilter) {
        filterMap.vehicle_id = this.vehicleFilter;
        this.isFilterActive = true;
      }
      return filterMap;
    },

    // Clear filter
    clearFilter() {
      this.statusFilter = null;
      this.requesterFilter = null;
      this.vehicleFilter = null;
      this.isFilterActive = false;

      this.getAllOrders();
    },

    // DB METHOD: cancel an order by changing order status to 'canceled'
    async cancelOrder(orderId) {
      try {
        // Instead of DELETE, we call updateAnOrder with status 'canceled'
        const dataToCancel = { status: "canceled" };
        await this.updateAnOrder({ id: orderId, dataFromUser: dataToCancel });

        this.successTitle = "Order canceled successfully!";
        this.successCaption = "The order status has been updated to canceled.";
        this.showSuccessfulConfirmation = true;

        await this.getAllOrders(); 
      } catch (err) {
        console.error("Error canceling order:", err);
        this.successTitle = "Error canceling order!";
        this.successCaption = "Please try again later.";
        this.showSuccessfulConfirmation = true; // Show error message
      }
    },

    // DB METHOD: Add an order to the database
    async addAnOrderToDB(orderData) {
      if (orderData.operationType === "loading") {
        this.source = orderData.orderFor;
        this.destination = "port";
      } else {
        this.source = "port"; // DONT HARDCODE THIS <--------------------------------
        this.destination = orderData.orderFor;
      }

      const data = {
        ...orderData,
        requester: this.logged_in_username,
        destination: this.destination,
        source: this.source,
        order_for: orderData.orderFor,
      };

      delete data.orderFor;

      
  
      try {
        const result = await fetch("api/orders", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(data),
        });

        if (!result.ok) {
          const errorBody = await result.text();
          throw new Error(
            `HTTP error! Status: ${result.status} - ${errorBody}`
          );
        }

        this.successTitle = "Order Submitted!";
        this.successCaption = "The order has been added.";
        this.showSuccessfulConfirmation = true;

        await this.getAllOrders();
      } catch (err) {
        console.error("Error add", err);
      }
    },

    // DB METHOD: delete an order from the database of the given order identifier
    async deleteOrderFromDB(orderId){
      try{
        const result = await fetch(`api/orders/${orderId}`, {
          method: "DELETE"
        }
        )
        if (!result.ok) {
          const errorBody = await result.text();
          throw new Error(
            `HTTP error! Status: ${result.status} - ${errorBody}`
          );
        }

        this.successTitle = "Order deleted successfully!";
        this.successCaption = "The order has been deleted.";
        this.showSuccessfulConfirmation = true;

        await this.getAllOrders();
        
      } catch(err){
        console.error('Error deleting user')
        console.error(err)
      }
    },

    // DB METHOD: update an order on the database for the given order identifier 
    // and the edited fields as json object.
    async updateAnOrder({ id, dataFromUser }) {
      const data = {
        ...dataFromUser,
        twStart: this.formatToTimestamp(dataFromUser.twStart),
        twEnd: this.formatToTimestamp(dataFromUser.twEnd),
      };

      delete data.inOut;
      try {
        const result = await fetch(`api/orders/${id}`, {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(data),
        });

        if (!result.ok) {
          const errorBody = await result.text();
          throw new Error(
            `HTTP error! Status: ${result.status} - ${errorBody}`
          );
        }

        this.successTitle = "Order updated successfully!";
        this.successCaption = "The order has been updated.";
        this.showSuccessfulConfirmation = true;

        await this.getAllOrders();
      } catch (err) {
        console.error("Error updating order:", err);
      }
    },

      // DB METHOD: Retrieve all orders from database.
      // If the current session is logged in by a user,
      // additional query parameter is added so that only
      // orders that are intended to the logged in user should be retrieved.
      async getAllOrders() {
        this.error = null;

        let filters = this.applyFilter();

        try {
          const queryParams = new URLSearchParams();

          for (const [key, value] of Object.entries(filters)) {
            if (value != null && value !== "") {
              queryParams.append(key, value);
            }
          }

          // requester and order for
          if (this.authStore.isUser) {
            queryParams.append("order_for", this.logged_in_username);
          }
          
          const result = await fetch(`api/orders?${queryParams.toString()}`, {
            method: "GET",
            credentials: "include",
          });

          if (!result.ok) {
            const errorBody = await result.text();
            throw new Error(
              `HTTP error! Status: ${result.status} - ${errorBody}`
            );
          }

          let data = await result.json();
          this.orders = [];
          

          this.orders = data.map((order) => this.mapFrontendToBackend(order));
          console.log(this.orders)
        } catch (err) {
          console.error("Error getting all orders:", err);
        }
      },

      // Map frontend to backend so data is in the desired format 
      mapFrontendToBackend(order) {
        //In/Out
        // const isPort = String(order.source).toLowerCase() === "port";
        const inOut = order.operationType == 'unloading' ? "Inbound" : "Outbound";

        const dateOptions = { year: "numeric", month: "2-digit", day: "2-digit" };
        const dateTimeOptions = {
          year: "numeric",
          month: "2-digit",
          day: "2-digit",
          hour: "2-digit",
          minute: "2-digit",
          second: "2-digit",
        };

        return {
          ...order,
          departureTime: order.departureTime ? new Date(order.departureTime).toLocaleDateString("en-GB", dateTimeOptions) : null,
          madeAt: order.madeAt
            ? new Date(order.madeAt).toLocaleDateString("en-GB", dateOptions)
            : null,
          twStart: order.twStart
            ? new Date(order.twStart).toLocaleString("en-GB", dateTimeOptions)
            : null,
          twEnd: order.twEnd
            ? new Date(order.twEnd).toLocaleString("en-GB", dateTimeOptions)
            : null,
          eta: order.eta
            ? new Date(order.eta).toLocaleString("en-GB", dateTimeOptions)
            : null,
          inOut: inOut,
        };
      },

      // DB METHOD: Retrieve an order with the given identifier from database
      // This method is currently only used for the search feature.
      async getOrderById() {
        this.orders = [];
        this.error = null;
        this.isSearchActive = true;

        try {
          const queryParams = new URLSearchParams();
          const idQuery = Number(this.searchQuery);
          if (idQuery === 0) {
            this.getAllOrders();
            return;
          }

          if (isNaN(idQuery) || !this.searchQuery) {
            this.error = "Please enter a valid number for the Order ID.";
            this.orders = [];
            return;
          }

          if (this.authStore.isUser) {
            queryParams.append("order_for", this.logged_in_username);
          }

          const result = await fetch(
            `api/orders/${idQuery}?${queryParams.toString()}`
          );

          if (!result.ok) {
            const errorBody = await result.text();
            throw new Error(
              `HTTP error! Status: ${result.status} - ${errorBody}`
            );
          }

          let data = await result.json();

          if (data && typeof data === "object" && !Array.isArray(data)) {
            this.orders = [this.mapFrontendToBackend(data)];
          } else if (Array.isArray(data) && data.length > 0) {
            this.orders = data.map((order) => this.mapFrontendToBackend(order));
          } else {
            this.orders = [];
            this.error = `No order found with ID ${orderId}.`;
          }

        } catch (err) {
          console.error("Error when getting order by id (orders)", err);
        }
      },



      // DB METHOD: Retrieve all available companies to apply the 'REQUESTED BY' filter.
      async getCompanyList() {
        if (this.authStore.isAdmin) {
          try {
            const result = await fetch("api/companies?limit=100");
            if (!result.ok) {
              const errorBody = await result.text();
              throw new Error(
                `HTTP error! Status: ${result.status} - ${errorBody}`
              );
            }

            let data = await result.json();
            this.companyList = data.map((cur) => cur.username);
        
          } catch (err) {
            console.error("Error getting all orders:", err);
          }
        }
      },

      // DB METHOD: Retrieve all available vehicles to apply the 'VEHICLE ID' filter.
      async getAllVehicles() {
        this.vehicleList = [];
        try {
          const result = await fetch("api/vehicle");

          if (!result.ok) {
            const errorBody = await result.text();
            throw new Error(
              `HTTP error! Status: ${result.status} - ${errorBody}`
            );
          }

          let data = await result.json();

          this.vehicleList = data.map((vehicle) => String(vehicle.vehicleId));
        } catch (err) {
          console.error("Error getting all the vehicles: ", err);
        }
      },
      // Helper method to show the export dropdown
      toggleExportOrdersButton() {
        this.showExportDropdown = !this.showExportDropdown;
      },
      
      // DB METHOD: Export all the order data into the desired format.
      async selectExportType(type) {
        this.showExportDropdown = false;
        try {
          if (this.authStore.isUser){
                window.location.href = `/api/orders/export/${type}?order_for=${this.logged_in_username}`;
          } else{
            window.location.href = `/api/orders/export/${type}`;
          }
        } catch (err) {
          console.error("Failed to export orders:", err);
          this.error = `Could not export orders: ${err.message}`;
        }
      },
    },

  computed: {
    filteredAndSortedOrders() {
      let result = this.orders;
      // sort
      if (this.sortOption === "DATE") {
        result = [...result].sort((a, b) => {
          const dateA = this.parseDDMMYYYY(a.madeAt); //Change madeAt if we want Date value to be different
          const dateB = this.parseDDMMYYYY(b.madeAt);
          return dateA.getTime() - dateB.getTime(); // Sorts in ascending order
        });
      } else if (this.sortOption === "ETD") {
        result = [...result].sort((a, b) => {
          const timeA = this.parseLocalDateTimeString(a.twStart)
            ? this.parseLocalDateTimeString(a.twStart).getTime()
            : 0;
          const timeB = this.parseLocalDateTimeString(b.twStart)
            ? this.parseLocalDateTimeString(b.twStart).getTime()
            : 0;
          return timeA - timeB; // Sorts in ascending order
        });
      } else if (this.sortOption === "ETA") {
        result = [...result].sort((a, b) => {
          const timeA = a.eta
            ? this.parseLocalDateTimeString(a.eta).getTime()
            : 0; // Converts eta to timestamp, defaults to 0 if null
          const timeB = b.eta
            ? this.parseLocalDateTimeString(b.eta).getTime()
            : 0;
          return timeA - timeB; // Sorts in ascending order
        });
      }
      return result;
    },

    // Helper method to check if the filter is active. 
    hasFilters() {
      if (this.statusFilter || this.requesterFilter || this.vehicleFilter) {
        return true;
      } else {
        this.getAllOrders();
      }
    },
  },
  created() {
    this.logged_in_username = this.authStore.getUser.username.string; // Assigning current logged in user's username

    this.getAllOrders();
    this.getCompanyList();
    this.getAllVehicles();
    this.fetchUserStats();
  },
  mounted() {
    this.showUserStats = true; // Trigger animation for stats cards
  },
  setup() {
    const authStore = useAuthStore();
    return { authStore };
  },
};
</script>

<style scoped>
/* ANIMATIONS */
.animated-row {
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.4s ease forwards;
  position: relative;
  z-index: 1;
}

.fade-enter-active {
  @apply transition-opacity duration-300;
}
.fade-enter-from {
  @apply opacity-0;
}

.scale-fade-enter-active {
  @apply transition transform duration-300;
}
.scale-fade-enter-from {
  @apply opacity-0 scale-90;
}

.fade-slide-enter-active {
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

.stagger-fade-enter-active {
  transition: all 0.4s ease;
}
.stagger-fade-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.stagger-fade-leave-active {
  opacity: 0 !important;
  position: absolute !important;
}

.stagger-fade-leave-to {
  opacity: 0;
}

.stagger-fade-move {
  transition: none;
}

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-list-item {
  transition: all 0.3s ease;
}
.fade-list-enter-active {
  transition: all 0.3s ease;
}
.fade-list-enter-from {
  opacity: 0;
  transform: translateY(30px);
}

.slide-fade-enter-active {
  transition: all 0.5s ease-out;
}
.slide-fade-leave-active {
  transition: all 0.5s cubic-bezier(1, 0.5, 0.8, 1);
}
.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(-20px);
  opacity: 0;
}
</style>
