<template>
  <transition name="fade-slide" appear>
    <div class="flex flex-col min-h-screen">
      <div class="flex-none px-20 pt-10 pb-5">
        <div class="flex flex-col gap-4">
          <div class="flex items-center space-x-3">
            <div class="p-2 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl shadow-lg">
              <UsersSVG class="size-6 text-white" />
            </div>
            <h1 class="text-4xl font-semibold">User management</h1>
          </div>

          <div class="flex justify-between items-center">
            <div class="flex gap-5 items-center">
              <div class="text-lg font-bold">{{ userType }}</div>
              <div class="text-lg text-gray-500/60">
                {{ users.length }}
              </div>
            </div>
            <div class="flex gap-5 relative">
              <button
                ref="addUserButton"
                @click="toggleUserDropdown"
                type="button"
                class="bg-[#FA812F] px-4 py-2 h-[6vh] rounded-md text-white transition-all duration-300 ease-out hover:bg-[#ff933f] hover:shadow-lg hover:-translate-y-0.5 active:translate-y-0 active:shadow-none cursor-pointer font-semibold max-h-12"
              >
                + Add User
              </button>

              <transition name="dropdown">
                <div v-if="showUserDropdown" ref="userDropdown"
                  class="absolute top-[105%] right-0 z-10 bg-white border border-gray-300 rounded-md shadow-lg w-48">
                  <ul class="text-sm text-gray-700">
                    <li @click="selectUserType('admin')" class="px-4 py-2 hover:bg-gray-100 cursor-pointer">
                      Generate Admin Registration
                    </li>
                    <li @click="selectUserType('company')" class="px-4 py-2 hover:bg-gray-100 cursor-pointer">
                      Generate Company Registration
                    </li>
                  </ul>
                </div>
              </transition>
            </div>
          </div>
        </div>
      </div>

      <div class="flex-1 px-20 pb-6 min-h-0">
        <div class="flex flex-col h-full border rounded-lg border-gray-200">
          <div class="flex justify-between items-center p-6">
            <div class="flex-none text-2xl font-semibold">User List</div>
            <div class="flex gap-5 max-h-">
              <div class="relative flex bg-gray-250 rounded-xl items-center gap-1 overflow-hidden w-[30vh]">
                <div class="absolute inset-[2px] rounded-lg bg-[#f4f4f4]"></div>
                <div
                  class="absolute top-0 bottom-0 bg-[#FA812F] rounded-xl transition-left duration-200 ease-in-out"
                  :style="{ left: sliderPositionUserType, width: sliderWidth }"
                ></div>
                <button
                  class="flex-1 flex justify-center rounded-xl p-2 font-bold cursor-pointer z-10 text-center select-none"
                  :class="userType === 'All' ? 'text-white' : 'text-gray-700'" @click="handleUserTypeChange('All')">
                  All
                </button>
                <button
                  class="flex-1 flex justify-center rounded-xl p-2 font-bold cursor-pointer z-10 text-center select-none"
                  :class="userType === 'Admin' ? 'text-white' : 'text-gray-700'" @click="handleUserTypeChange('Admin')">
                  Admin
                </button>
                <button
                  class="flex-1 flex justify-center rounded-xl p-2 font-bold cursor-pointer z-10 text-center select-none mr-1"
                  :class="userType === 'Company' ? 'text-white' : 'text-gray-700'" @click="handleUserTypeChange('Company')">
                  Company
                </button>
              </div>
              <div class="flex flex-col">
                <span class="text-xs text-gray-500">Press Enter to Search</span>
                <div class="flex rounded-md px-4 py-0 border border-gray-200 focus-within:outline-[#FA812F] focus-within:outline focus-within:outline-2">
                  <span class="flex items-center gap-2">
                    <label><SearchSVG /></label>
                    <input
                      type="text"
                      v-model="searchQuery"
                      @keyup.enter="searchUsersFromBackend"
                      @input="onSearchInput"
                      class="text-base p-2 rounded-md flex w-50 h-[6vh] focus:outline-none focus:ring-0 cursor-text"
                      placeholder="Search user by username"
                      style="outline: none !important; box-shadow: none !important;"
                    />
                    <CancelSVG v-if="searchQuery && isSearchActive" @click="clearSearch" class="w-5 h-5 text-red-500 cursor-pointer" />
                    <div v-else-if="!searchQuery && !isSearchActive" class="w-5 h-5 cursor-pointer text-gray-500 hover:text-gray-700"/>
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div class="flex-1 flex flex-col min-h-0">
            <div class="flex-none flex items-center border-gray-200 border-t border-1 p-1 px-5">
              <div class="basis-0 grow-[3] p-4 font-medium text-gray-700 flex">
                Name
              </div>
              <div class="basis-0 grow-[3] p-4 font-medium text-gray-700 flex">
                Account Type
              </div>
              <div class="basis-0 grow-[3] p-4 font-medium text-gray-700 flex">
                Username
              </div>
              <div class="basis-0 grow-[3] p-4 font-medium text-gray-700 flex">
                Created At
              </div>
              <div class="basis-0 grow-[2] p-4 font-medium text-gray-700 flex"></div>
            </div>

            <div class="flex-1 bg-white h-full"
            :class="{
              'overflow-hidden': isChildDropdownOpen,
              'overflow-auto': !isChildDropdownOpen
            }">
              <transition name="stagger-fade" mode="out-in">
                <template v-if="filteredUsers.length === 0">
                  <div
                    class="flex items-center justify-center h-full text-gray-400 text-xl font-semibold"
                  >
                    <p v-if="isSearchActive && !isFiltering">No users found for "{{ searchQuery }}".</p>
                    <p v-else-if="userType !== 'All' && !isFiltering">No {{ userType.toLowerCase() }} users to display.</p>
                    <p v-else>No users to display.</p>
                  </div>
                </template>
                <transition-group
                  v-else
                  name="stagger-fade"
                  tag="div"
                  :class="{ 'opacity-50': isFiltering }"
                  :key="usersListKey"
                >
                  <UsersTableItem
                    v-for="(user, i) in filteredUsers" :key="user.username"
                    :userDetail="user"
                    @deleteUser="deleteUserFunction"
                    @editUser="editUserFunction"
                    class="animated-row"
                    @rowClick="
                      user.userType !== 'ADMIN'
                        ? openViewUserPopup(user)
                        : null
                      " 
                      @dropdown-open-state-changed="handleDropdownStateChange"
                    :style="{ animationDelay: (i * 100) + 'ms', cursor: user.userType !== 'ADMIN' ? 'pointer' : 'default' }" />
                </transition-group>
              </transition>
            </div>
          </div>
        </div>
      </div>

      <transition name="fade">
        <AddUserPopup v-if="showAddUserPopup" @close="showAddUserPopup = false" @save="AddUserFunction"
          :formType="selectedAddForm" />
      </transition>
      <transition name="fade">
        <AdminRegistrationPopup v-if="showAdminRegistrationPopup" @close="showAdminRegistrationPopup = false"
          @tokenGenerated="handleAdminTokenGenerated" />
      </transition>
      <transition name="fade">
        <CompanyRegistrationPopup v-if="showCompanyRegistrationPopup" @close="showCompanyRegistrationPopup = false"
          @tokenGenerated="handleTokenGenerated" />
      </transition>
      <transition name="fade">
        <ViewUserPopup v-if="showViewUserPopup" :userDetail="selectedUserDetail" @close="showViewUserPopup = false" />
      </transition>

      <teleport to="body">
        <SuccessConfirmationPopup :show="showSuccessfulConfirmation" :title="successConfirmationTitle"
          :caption="successConfirmationCaption" @close="showSuccessfulConfirmation = false" />
      </teleport>
    </div>
  </transition>
</template>

<script>
import UsersTableItem from "@/components/users/UsersTableItem.vue";
import AddUserPopup from "@/components/users/AddUserPopup.vue";
import AdminRegistrationPopup from "@/components/users/AdminRegistrationPopup.vue";
import CompanyRegistrationPopup from "@/components/users/CompanyRegistrationPopup.vue";
import SearchSVG from "@/components/svg/SearchSVG.vue";
import SuccessConfirmationPopup from "../SuccessfulConfirmationPopup.vue";
import ViewUserPopup from "@/components/users/ViewUserPopup.vue";
import UsersSVG from "../svg/UsersSVG.vue";
import CancelSVG from "@/components/svg/CancelSVG.vue";
import { useAuthStore } from "@/stores/auth.js";

export default {
  components: {
    UsersTableItem,
    AddUserPopup,
    AdminRegistrationPopup,
    CompanyRegistrationPopup,
    SearchSVG,
    SuccessConfirmationPopup,
    ViewUserPopup,
    UsersSVG,
    CancelSVG
  },
  data() {
    return {
      showAddUserPopup: false,
      showAdminRegistrationPopup: false,
      showCompanyRegistrationPopup: false,
      showSuccessfulConfirmation: false,
      showUserDropdown: false,
      showViewUserPopup: false,
      isChildDropdownOpen: false, 

      users: [],
      selectedUserDetail: null,
      userType: "All",
      searchQuery: "",
      isSearchActive: false,
      isFiltering: false,

      successConfirmationTitle: "",
      successConfirmationCaption: "",

      usersListKey: 0, 
      authStore: useAuthStore(),
    };
  },
  watch: {
    // Watchers to close main user dropdown when any other popup opens
    showAddUserPopup(val) {
      if (val) this.showUserDropdown = false;
    },
    showAdminRegistrationPopup(val) {
      if (val) this.showUserDropdown = false;
    },
    showCompanyRegistrationPopup(val) {
      if (val) this.showUserDropdown = false;
    },
    showViewUserPopup(val) {
      if (val) this.showUserDropdown = false;
    },
    showSuccessfulConfirmation(val) {
      if (val) this.showUserDropdown = false;
    },
    // Dynamically add/remove click outside listener for the user dropdown
    showUserDropdown(isOpen) {
      if (isOpen) {
        document.addEventListener('click', this.handleClickOutside);
      } else {
        document.removeEventListener('click', this.handleClickOutside);
      }
    }
  },
  methods: {
    /**
     * Clears the search query and, if a search was active, reloads all users.
     */
    clearSearch() {
      if (this.isSearchActive) {
        this.getAllUsers();
      }
      this.searchQuery = "";
      this.isSearchActive = false;
    },
    /**
     * Handles changes in the open/close state of child dropdowns (e.g., action dropdown within a table row).
     * Controls body overflow to prevent scrolling when a child dropdown is open.
     * @param {boolean} isOpen - True if a child dropdown is open, false otherwise.
     */
    handleDropdownStateChange(isOpen) {
      this.isChildDropdownOpen = isOpen;
      // This part for body overflow is usually handled by a global overlay
      // or by the popup/modal components themselves. If you have multiple
      // popups/dropdowns that could open simultaneously, you might need
      // a more robust state management for body overflow (e.g., a counter).
      if (isOpen) {
        document.body.style.overflow = 'hidden';
      } else {
        document.body.style.overflow = 'auto';
      }
    },

    /**
     * Toggles the visibility of the "Add User" type dropdown menu.
     */
    toggleUserDropdown() {
      this.showUserDropdown = !this.showUserDropdown;
    },

    /**
     * Sets the type of user to be added (admin or company) and opens the corresponding registration popup.
     * @param {string} type - The type of user to add ('admin' or 'company').
     */
    selectUserType(type) {
      this.showUserDropdown = false;
      if (type === "admin") {
        this.showAdminRegistrationPopup = true;
      } else if (type === "company") {
        this.showCompanyRegistrationPopup = true;
      }
    },

    /**
     * Handles the event when a company registration token is generated.
     * Displays a success confirmation message and closes the company registration popup.
     * @param {object} tokenData - Data related to the generated token, including company name.
     */
    handleTokenGenerated(tokenData) {
      console.log("Registration token generated:", tokenData);
      this.successConfirmationTitle = "Registration Token Generated!";
      this.successConfirmationCaption = `A registration token has been generated for ${tokenData.company}. The registration URL has been copied to your clipboard.`;
      this.copyToClipboard(tokenData.url)
      this.showSuccessfulConfirmation = true;
      this.showCompanyRegistrationPopup = false;
    },

     async copyToClipboard(text) {
      try {
        await navigator.clipboard.writeText(text);
        // You could add a toast notification here
        console.log('Copied to clipboard');
      } catch (err) {
        console.error('Failed to copy text: ', err);
      }
    },

    /**
     * Handles the event when an admin registration token is generated.
     * Displays a success confirmation message and closes the admin registration popup.
     * @param {object} tokenData - Data related to the generated token, including admin email.
     */
    handleAdminTokenGenerated(tokenData) {
      console.log("Registration token generated:", tokenData);
      this.successConfirmationTitle = "Admin Registration Token Generated!";
      this.successConfirmationCaption = `A registration token has been generated for ${tokenData.email}. The registration URL has been copied to your clipboard.`;
      this.copyToClipboard(tokenData.url)
      this.showSuccessfulConfirmation = true;
      this.showAdminRegistrationPopup = false;
    },

    /**
     * Deletes a user from the backend based on their username.
     * Displays a success message after successful deletion and refreshes the user list.
     * @param {object} user - The user object to be deleted.
     */
    async deleteUserFunction(user) {
      // Prevent admins from deleting other admins
      if (this.authStore.isAdmin && user.userType === 'ADMIN') {
        console.warn("Attempt to delete an admin user by another admin was blocked.");
        this.successConfirmationTitle = "Action Not Allowed";
        this.successConfirmationCaption = "Admins cannot delete other admin users.";
        this.showSuccessfulConfirmation = true;
        return;
      }

      try {
        // First, cancel all orders for the user that haven't started
        if (user.userType === 'COMPANY') {
          await this.cancelUserOrders(user.username);
        }

        const result = await fetch(`api/users/${user.username}`, {
          method: "DELETE",
        });
        if (!result.ok) {
          const errorBody = await result.text();
          throw new Error(
            `HTTP error! Status: ${result.status} - ${errorBody}`
          );
        }

        this.successConfirmationTitle = "User Removed Successfully!";
        this.successConfirmationCaption = "The user account has been removed and their pending orders have been canceled.";
        this.showSuccessfulConfirmation = true;

        await this.getAllUsers();
      } catch (err) {
        console.error("Error deleting user:", err);
        this.successConfirmationTitle = "Error Deleting User";
        this.successConfirmationCaption = `Failed to delete user: ${err.message}`;
        this.showSuccessfulConfirmation = true;
      }
    },

    /**
     * Cancels all orders for a user that haven't started yet
     */
    async cancelUserOrders(username) {
      try {
        const result = await fetch(`api/orders/cancel-user-orders/${username}`, {
          method: "PATCH",
        });
        if (!result.ok) {
          console.warn("Failed to cancel user orders:", result.status);
          // Don't throw error here as we still want to proceed with user deletion
        }
      } catch (err) {
        console.warn("Error canceling user orders:", err);
        // Don't throw error here as we still want to proceed with user deletion
      }
    },

    /**
     * Initiates a search for users in the backend based on the `searchQuery`.
     * Updates the `users` array with filtered results and refreshes map markers.
     */
    async searchUsersFromBackend() {
      const query = this.searchQuery.trim();

      if (!query) {
        await this.getAllUsers();
        return;
      }
      this.isLoading = true; // Assuming you have an isLoading data property
      this.error = null;

      try {
        const backendUrl = `/api/companies/${query}`;

        const response = await fetch(backendUrl, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
          },
        });

        if (!response.ok) {
          const errorData = await response
            .json()
            .catch(() => ({ message: "Unknown error" }));
        }

        const data = await response.json();

        // Assuming backend returns an array of users for search results
        if (Array.isArray(data)) {
          this.users = data.map(this.convertBackendToFrontend);
        } else {
          // If for some reason it returns a single object but should be an array
          this.users = [this.convertBackendToFrontend(data)];
        }

        console.log(this.users)
        this.usersListKey++; // Force Vue to re-render the list

        if (this.users.length === 0) {
          console.log(`No users found for query: "${query}"`);
        } else {
          console.log(`Found ${this.users.length} user(s) for query: "${query}"`);
        }


        if (this.users.length === 0) {
          console.log(`No users found for query: "${query}"`);
        } else {
          console.log(
            `Found ${this.users.length} user(s) for query: "${query}"`
          );
        }

      } catch (err) {
        console.error("Error fetching users from backend:", err);
        this.error = "Failed to search users: " + err.message;
        this.users = []; // Clear users on error
        this.usersListKey++; // Trigger re-render even on error
      } finally {
        this.isLoading = false; // Assuming you have an isLoading data property
      }
    },

    /**
     * Handles input in the search bar. Sets `isSearchActive` based on the query and
     * reloads all users if the search query is cleared.
     */
    onSearchInput() {
      this.isSearchActive = this.searchQuery.trim() !== "";
      if (!this.isSearchActive) {
        // If search query is cleared, fetch all users again
        this.getAllUsers();
      }
    },

    /**
     * Adds a new user to the backend.
     * Displays a success message after successful addition and refreshes the user list.
     * @param {object} userData - The data for the new user to be added.
     */
    async AddUserFunction(userData) {
      this.showAddUserPopup = false;

      const isCompany = userData.userType === "COMPANY";
  

      const data = {
        userType: userData.userType,
        username: String(userData.username),
        name: String(userData.name),
        password: userData.password || "defaultPassword", // Consider better password handling
        email: isCompany && userData.email ? userData.email : "hello1@gmail.com", // Default email needs review
      };

      if (isCompany) {
        if (userData.long && userData.lat) {
          data.x = String(userData.long);
          data.y = String(userData.lat);
        }
        // Ensure location is only set if both long and lat are present
        data.location = (userData.long && userData.lat) ? `(${userData.long}, ${userData.lat})` : null;
        if (userData.openingTime) {
          data.opening_time = `${userData.openingTime}:00`;
        }
        if (userData.closingTime) {
          data.closing_time = `${userData.closingTime}:00`;
        }
      }

      this.error = null;
      try {
        const result = await fetch("api/register", {
          method: "POST",
          headers: {
            "Content-type": "application/json",
          },
          body: JSON.stringify(data),
        });

        if (!result.ok) {
          const errorBody = await result.text();
          throw new Error(
            `HTTP error! Status: ${result.status} - ${errorBody}`
          );
        }

        this.successConfirmationTitle = "User Added Successfully!";
        this.successConfirmationCaption =
          "The new user has been successfully registered.";
        this.showSuccessfulConfirmation = true;

        await this.getAllUsers();
      } catch (err) {
        console.error("Error saving new user:", err);
        this.successConfirmationTitle = "Error Adding User";
        this.successConfirmationCaption = `Failed to add user: ${err.message}`;
        this.showSuccessfulConfirmation = true;
      }
    },

    /**
     * Edits an existing user in the backend.
     * Displays a success message after successful edit and refreshes the user list.
     * @param {object} newUserData - The updated data for the user.
     */
    async editUserFunction(newUserData) {
      // Prevent admins from editing other admins
      if (this.authStore.isAdmin && newUserData.userType === 'ADMIN') {
        console.warn("Attempt to edit an admin user by another admin was blocked.");
        this.successConfirmationTitle = "Action Not Allowed";
        this.successConfirmationCaption = "Admins cannot edit other admin users.";
        this.showSuccessfulConfirmation = true;
        return;
      }

      console.log("Editing user:", newUserData); // Better logging
      const isCompany = newUserData.userType === "COMPANY";
  

      const long = newUserData.long;
      const lat = newUserData.lat;

      const location = long && lat ? `(${long}, ${lat})` : null;

      const data = {
        ...newUserData,
        modified_at: newUserData.modified_at,
        userType: String(newUserData.userType).toUpperCase(),
        username: String(newUserData.username),
        name: String(newUserData.name),
        email: isCompany && newUserData.email ? newUserData.email : "hello@gmail.com", // Default email needs review
      };

      

      if (isCompany) {
        if (newUserData.long && newUserData.lat) {
          data.x = String(newUserData.long);
          data.y = String(newUserData.lat);
        }
        data.location = location;
        newUserData.openingTime ? data.openingTime = `${newUserData.openingTime}:00` : data.openingTime = null;
        newUserData.closingTime ? data.closingTime = `${newUserData.closingTime}:00` : data.closingTime = null;
      }

      console.log("to backend");
      console.log(data);

      delete data.lat
      delete data.long
      delete data.oldUsername
      delete data.x
      delete data.y

      this.error = null;
      try {
        const result = await fetch(`api/companies/${newUserData.oldUsername}`, {
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

        this.successConfirmationTitle = "User Edited Successfully!";
        this.successConfirmationCaption = "User changes have been saved.";
        this.showSuccessfulConfirmation = true;

        await this.getAllUsers();
      } catch (err) {
        console.error("Failed to update user:", err);
        this.successConfirmationTitle = "Error Editing User";
        this.successConfirmationCaption = `Failed to update user: ${err.message}`;
        this.showSuccessfulConfirmation = true;
      }
    },

    /**
     * Fetches all users from the backend, optionally filtered by `userType`.
     * Updates the `users` array and map markers.
     */
    async getAllUsers() {
      this.error = null;
      let type = null;

      if (this.userType !== "All") {
        const typeMap = {
          "Admin": "ADMIN",
          "Company": "COMPANY",
          "Port": "PORT"
        };
        type = typeMap[this.userType]; // Use mapped type directly, no need for toLowerCase()
      }
      
      try {
        let url = `api/users?limit=100`;
        if (type) {
          url += `&type=${type}`;
        }

    
        const result = await fetch(url);
        if (!result.ok) {
          const errorBody = await result.text();
          throw new Error(
            `HTTP error! Status: ${result.status} - ${errorBody}`
          );
        }
        const data = await result.json();
        this.users = [];
         console.log(this.users = data)
        this.users = data.map(this.convertBackendToFrontend);
      } catch (err) {
        console.error("Failed to fetch the users:", err);
        this.error = `Could not fetch users: ${err.message}`;
      }
    },

    /**
     * Converts a backend user data object to the frontend's expected format.
     * Handles type mapping, date formatting, and null checks for location/times.
     * @param {object} dbData - The user data object from the backend.
     * @returns {object} The formatted user data object for the frontend.
     */
    convertBackendToFrontend(dbData) {
      const typeMap = {
        company: "Company",
        admin: "Admin",
        port: "Port",
      };

      return {
        ...dbData,
        created_at: dbData.created_at,
        userType: String(dbData.userType) || dbData.type,
        username: dbData.username,
        name: dbData.name,
        // Ensure lat/long are strings for consistency, handle null/undefined
        lat:
          dbData.location && dbData.location.y != null
            ? String(dbData.location.y)
            : "",
        long:
          dbData.location && dbData.location.x != null
            ? String(dbData.location.x)
            : "",
        openingTime: dbData.openingTime ? dbData.openingTime.slice(0, 5) : "",
        closingTime: dbData.closingTime ? dbData.closingTime.slice(0, 5) : "",
        email: dbData.email ? String(dbData.email) : "",
      };
    },

    /**
     * Formats a timestamp string into "DD/MM/YYYY HH:MM:SS" format.
     * Handles invalid or null timestamps.
     * @param {string} timestamp - The timestamp string to format.
     * @returns {string} The formatted date and time string, or "N/A" / "Invalid Date".
     */
    
      

    /**
     * Handles the change in user type selection (All, Admin, Company).
     * Triggers a re-fetch of users based on the selected type after a short delay.
     * @param {string} type - The selected user type.
     */
    async handleUserTypeChange(type) {
      this.isFiltering = true;
      this.userType = type;

      await new Promise((resolve) => setTimeout(resolve, 300)); // Simulate loading delay

      await this.getAllUsers(); // Ensure this completes before setting isFiltering to false
      this.usersListKey++; // Force re-render after filtering for staggered animation
      this.isFiltering = false;
    },

    /**
     * Opens the View User popup and sets the selected user's details.
     * @param {object} user - The user object to be displayed in the popup.
     */
    openViewUserPopup(user) {
      this.selectedUserDetail = user;
      this.showViewUserPopup = true;
    },

    /**
     * Closes the user type dropdown if a click occurs outside of it or its trigger button.
     * @param {Event} event - The click event.
     */
    handleClickOutside(event) {
      const dropdownElement = this.$refs.userDropdown;
      const addUserButton = this.$refs.addUserButton;

      // Only close if the click is outside both the dropdown and the button
      if (
        dropdownElement &&
        !dropdownElement.contains(event.target) &&
        addUserButton &&
        !addUserButton.contains(event.target)
      ) {
        this.showUserDropdown = false;
      }
    },
  },
  computed: {
    /**
     * Computes the list of users to be displayed, applying filters based on `userType` and `searchQuery`.
     * @returns {Array} The filtered list of users.
     */
    filteredUsers() {
      let tempUsers = this.users;

      // Apply search query filter if active
      if (this.isSearchActive && this.searchQuery.trim() !== "") {
        const query = this.searchQuery.trim().toLowerCase();
        tempUsers = tempUsers.filter(user =>
          // Check against properties that contain searchable text
          user.name.toLowerCase().includes(query) ||
          user.username.toLowerCase().includes(query) ||
          (user.email && user.email.toLowerCase().includes(query)) // Include email if it exists
        );
      }
      return tempUsers;
    },
    /**
     * Calculates the 'left' CSS property for the slider indicating the active user type filter.
     * @returns {string} The left position percentage.
     */
    sliderPositionUserType() {
      switch (this.userType) {
        case "All":
          return "0%";
        case "Admin":
          return "33.3%";
        case "Company":
          return "66%";
        default:
          return "0%";
      }
    },
    /**
     * Calculates the 'width' CSS property for the slider indicating the active user type filter.
     * @returns {string} The width percentage.
     */
    sliderWidth() {
      switch (this.userType) {
        case "All":
          return "30%";
        case "Admin":
          return "30%";
        case "Company":
          return "40%"; // This might need adjustment if visual alignment is off
        default:
          return "30%";
      }
    },
  },
  // Lifecycle hooks
  async created() {
    // Fetch all users from the database when the component is created
    await this.getAllUsers();
  },
  // Removed mounted/beforeUnmount as handleClickOutside is now managed by watcher
};
</script>

<style scoped>
/* Scoped styles for the component */

/* Staggered animation for table rows */
.animated-row {
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.4s ease forwards;
  position: relative;
  z-index: 1;
}

/* Fade transition for popups and general elements */
.fade-enter-active,
.fade-leave-active {
  @apply transition-opacity duration-300;
}

.fade-enter-from,
.fade-leave-to {
  @apply opacity-0;
}

/* Scale and fade transition */
.scale-fade-enter-active,
.scale-fade-leave-active {
  @apply transition transform duration-300;
}

.scale-fade-enter-from,
.scale-fade-leave-to {
  @apply opacity-0 scale-90;
}

/* Fade and slide transition for main container */
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

/* Staggered fade transitions for list items */
.stagger-fade-leave-active {
  transition: all 0.4s ease;
  position: absolute;
  opacity: 0;
}

.stagger-fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
.stagger-fade-move {
  transition: transform 0.4s ease;
}

/* Keyframe animation for fading in and sliding up */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Dropdown specific transition */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease-in-out;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>