<template>
  <img :src="logo" alt="Logo" class="logo" />
  <div class="login-page">
    <div class="login-container">
      <div class="w-full px-6 py-6">
        <!-- Login section title -->
        <div>
          <h2
            class="text-center text-6xl/9 font-bold tracking-tight text-gray-900"
          >
            Log in
          </h2>
        </div>

        <!-- Error message display, visible if 'error' data property has content -->
        <div v-if="error" class="mt-12 text-sm text-red-600 text-center p-2">
          <span class="items-center text-center bg-gray-100 p-3 rounded-xl">
            {{ error }}
          </span>
        </div>
        <div class="mt-6 sm:mx-auto sm:w-full sm:max-w-sm">
          <form @submit.prevent="login">
            <!-- Username input field -->
            <div>
              <label
                for="username"
                class="block text-sm/6 font-medium text-gray-900"
                >Username</label
              >
              <div class="mt-2">
                <input
                  v-model="username"
                  id="username"
                  required
                  class="block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-[#FA812F] sm:text-sm/6"
                />
              </div>
            </div>

            <!-- Password input field with toggle visibility feature -->
            <div>
              <div class="flex items-center justify-between">
                <label
                  for="password"
                  class="block text-sm/6 font-medium text-gray-900"
                  >Password</label
                >
              </div>
              <div class="relative flex-1">
                <input
                  :type="showPassword ? 'text' : 'password'"
                  class="text-base p-2 bg-white rounded-md w-full pr-10 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-[#FA812F] sm:text-sm/6"
                  v-model="password"
                />
                <!-- Button to toggle password visibility using dynamic component rendering -->
                <button
                  type="button"
                  class="absolute top-1/3 right-3 -translate-y-1/2 text-gray-500 hover:text-gray-700 transition-colors cursor-pointer p-1"
                  @click="showPassword = !showPassword"
                >
                  <component
                    :is="showPassword ? 'EyeOpen' : 'EyeClose'"
                    class="h-5 w-5"
                  />
                </button>
              </div>
            </div>

            <!-- "Remember Me" checkbox -->
            <div class="remember-me-cont">
              <div>
                <input
                  type="checkbox"
                  v-model="rememberMe"
                  id="remember-me"
                />
              </div>
              <div>
                <label for="remember-me">Remember me</label>
              </div>
            </div>

            <!-- Sign in button -->
            <div>
              <button
                type="submit"
                class="flex w-full justify-center rounded-md px-3 py-1.5 text-sm/6 font-semibold text-white shadow-xs hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 sign-in-btn cursor-pointer"
              >
                Sign in
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Login side image container, positioned alongside the login form -->
    <div class="login-pic-container">
      <img :src="loginPic" alt="login-pic" class="login-pic" />
    </div>
  </div>
</template>

<script>
import loginPic from "@/assets/login-side-pic.png";
import logo from "@/assets/saved4-orange.png";
import router from "@/router";
import { useAuthStore } from '../stores/auth';
import EyeOpen from "@/components/svg/EyeOpen.vue"
import EyeClose from "@/components/svg/EyeClose.vue"

export default {
  name: 'LoginForm',

  components:{
    EyeOpen, // Component for the open eye icon
    EyeClose // Component for the closed eye icon
  },

  data() {
    return {
      username: "",      	// Binds to the username input field
      password: "",      	// Binds to the password input field
      rememberMe: false, 	// Binds to the "Remember me" checkbox
      loading: false,    	// Controls loading state (e.g., for showing a spinner)
      error: '',         	// Stores and displays login error messages
      showPassword: false, 	// Controls visibility of the password field
      loginPic,          	// Image URL for the login side picture
      logo               	// Image URL for the logo
    }
  },

  setup() {
    const authStore = useAuthStore() // Accesses the authentication store
    return { authStore }
  },

  methods: {
    /**
     * Handles the user login process.
     * Sets loading state, attempts to log in via auth store, and redirects on success.
     * Displays an error message on failure.
     */
    async login() {
      this.loading = true; // Set loading to true to indicate operation in progress
      this.error = null;   // Clear any previous errors

      try {
        // Attempt to log in using the authentication store
        const result = await this.authStore.login({
          username: this.username,
          password: this.password
        });

        // If login is successful, redirect to the home page
        if (result) {
          router.push("/");
        }
      } catch (err) {
        // Log the error and set a user-friendly error message
        console.error("Login failed:", err);
        this.error = "Invalid Credentials. Please check your credentials.";
      } finally {
        this.loading = false; // Reset loading state regardless of success or failure
      }
    }
  }
}
</script>

<style>
/* Keyframes for the hover up and down animation */
@keyframes hoverUpDown {
  0% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px); /* Moves up 10px */
  }
  100% {
    transform: translateY(0);
  }
}

/* Base styles for the login page */
.login-page {
  display: flex;
  min-height: 100vh;
  justify-content: center; /* Centers the login form and image container horizontally */
  align-items: center; /* Centers them vertically */
  background-color: #f0f2f5; /* Light background for the page */
  gap: 250px; /* Add space between the login form and the image container */
}

.login-container {
  flex: none; /* Prevents it from growing/shrinking with flex, allowing max-width to control size */
  max-width: 500px; /* Original max width for the login form container */
  padding: 2rem;
  background-color: #ffffff; /* White background for the form itself */
  border-radius: 0.5rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.login-pic-container {
  flex: none; /* Prevents it from growing/shrinking with flex */
  width: 600px; /* Specific width to match prototype's larger size */
  height: 650px; /* Specific height to match prototype's aspect ratio */
  display: none; /* Hidden by default on small screens */
  border-radius: 1rem; /* More rounded corners like the prototype */
  justify-content: center;
  align-items: center;
  overflow: hidden; /* Ensures the image doesn't overflow rounded corners */

  /* Apply the animation */
  animation: hoverUpDown 3s ease-in-out infinite;
}

.login-pic {
  width: 100%; /* Image fills the container width */
  height: 100%; /* Image fills the container height */
  object-fit: cover; /* Covers the area while maintaining aspect ratio */
  border-radius: 1rem; /* Match the container's border-radius */
}

.logo {
  position: absolute;
  top: 1rem;
  left: 1rem; /* Original logo position */
  height: 60px; /* Size of the logo */
}

@media (min-width: 768px) {
  /* Medium screens and up */
  .login-page {
    flex-direction: row; /* Arrange form and image side-by-side */
  }
  .login-pic-container {
    display: flex; /* Show the image container on larger screens */
  }
}

/* Styles for the "Remember Me" checkbox */
.remember-me-cont {
  display: flex;
  align-items: center;
  margin-top: 1rem;
  margin-bottom: 1rem;
  gap: 0.5rem;
}

/* Styles for the Sign In button */
.sign-in-btn {
  background-color: #fa812f; /* Orange button background */
  transition: background-color 0.2s ease-in-out;
  /* Original button styles (from first provided code) */
  border-radius: 0.25rem;
  font-size: 0.875rem; /* text-sm/6 */
  padding: 0.375rem 0.75rem; /* px-3 py-1.5 */
}

.sign-in-btn:hover {
  background-color: #e57229; /* Darker orange on hover */
}

/* Base styles for input fields with focus effects */
input {
  outline: 1px solid #d1d5db; /* Light gray outline */
  outline-offset: -1px;
}

input:focus {
  outline: 2px solid #fa812f; /* Orange outline on focus */
  outline-offset: -2px;
  box-shadow: 0 0 0 2px #fa812f; /* Matching focus ring */
}

/* Utilities for general transitions and animations */
.transition-colors {
  transition-property: background-color, border-color, color;
  transition-duration: 150ms;
}
</style>