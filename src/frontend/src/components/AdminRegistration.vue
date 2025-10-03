<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <img
        class="mx-auto h-12 w-auto"
        :src="logo"
        alt="SAVED4 Logo"
      />
      <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
        Complete Admin Registration
      </h2>
      <p class="mt-2 text-center text-sm text-gray-600">
        Create your admin account to get started
      </p>
    </div>

    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
        <!-- Token Validation Status -->
        <div v-if="tokenStatus === 'validating'" class="text-center">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-orange-500 mx-auto mb-4"></div>
          <p class="text-gray-600">Validating registration token...</p>
        </div>

        <div v-else-if="tokenStatus === 'invalid'" class="text-center">
          <div class="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </div>
          <h3 class="text-lg font-medium text-red-900 mb-2">Invalid Registration Token</h3>
          <p class="text-red-700 mb-4">{{ errorMessage }}</p>
          <router-link
            to="/login"
            class="text-orange-600 hover:text-orange-500"
          >
            Return to Login
          </router-link>
        </div>

        <!-- Registration Form -->
        <div v-else-if="tokenStatus === 'valid'">
          <!-- Admin Info Display -->
          <div class="mb-6 p-4 bg-gray-50 rounded-lg">
            <h3 class="text-lg font-medium text-gray-900 mb-2">Admin Information</h3>
            <div class="text-sm text-gray-600 space-y-1">
              <p><span class="font-medium">Email:</span> {{ adminInfo.email }}</p>
              <p><span class="font-medium">Account Type:</span> Administrator</p>
            </div>
          </div>

          <form @submit.prevent="completeRegistration" class="space-y-6">
            <div>
              <label for="name" class="block text-sm font-medium text-gray-700">
                Full Name
              </label>
              <div class="mt-1">
                <input
                  id="name"
                  name="name"
                  type="text"
                  autocomplete="name"
                  required
                  v-model="registrationForm.name"
                  class="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-orange-500 focus:border-orange-500 sm:text-sm"
                  placeholder="Enter your full name"
                />
              </div>
            </div>

            <div>
              <label for="username" class="block text-sm font-medium text-gray-700">
                Username
              </label>
              <div class="mt-1">
                <input
                  id="username"
                  name="username"
                  type="text"
                  autocomplete="username"
                  required
                  v-model="registrationForm.username"
                  class="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-orange-500 focus:border-orange-500 sm:text-sm"
                  placeholder="Enter your username"
                />
              </div>
            </div>

            <div>
              <label for="password" class="block text-sm font-medium text-gray-700">
                Password
              </label>
              <div class="mt-1 relative">
                <input
                  id="password"
                  name="password"
                  :type="showPassword ? 'text' : 'password'"
                  autocomplete="new-password"
                  required
                  v-model="registrationForm.password"
                  class="appearance-none block w-full px-3 py-2 pr-10 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-orange-500 focus:border-orange-500 sm:text-sm"
                  placeholder="Enter your password"
                />
                <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute inset-y-0 right-0 pr-3 flex items-center"
                >
                  <component
                    :is="showPassword ? 'EyeOpen' : 'EyeClose'"
                    class="h-5 w-5 text-gray-400 hover:text-gray-500"
                  />
                </button>
              </div>
            </div>

            <div>
              <label for="confirmPassword" class="block text-sm font-medium text-gray-700">
                Confirm Password
              </label>
              <div class="mt-1 relative">
                <input
                  id="confirmPassword"
                  name="confirmPassword"
                  :type="showConfirmPassword ? 'text' : 'password'"
                  autocomplete="new-password"
                  required
                  v-model="registrationForm.confirmPassword"
                  class="appearance-none block w-full px-3 py-2 pr-10 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-orange-500 focus:border-orange-500 sm:text-sm"
                  placeholder="Confirm your password"
                />
                <button
                  type="button"
                  @click="showConfirmPassword = !showConfirmPassword"
                  class="absolute inset-y-0 right-0 pr-3 flex items-center"
                >
                  <component
                    :is="showConfirmPassword ? 'EyeOpen' : 'EyeClose'"
                    class="h-5 w-5 text-gray-400 hover:text-gray-500"
                  />
                </button>
              </div>
            </div>

            <!-- Error Display -->
            <div v-if="error" class="rounded-md bg-red-50 p-4">
              <div class="flex">
                <div class="flex-shrink-0">
                  <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                  </svg>
                </div>
                <div class="ml-3">
                  <h3 class="text-sm font-medium text-red-800">Registration Error</h3>
                  <div class="mt-2 text-sm text-red-700">{{ error }}</div>
                </div>
              </div>
            </div>

            <div>
              <button
                type="submit"
                :disabled="isSubmitting || !isFormValid"
                class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-orange-600 hover:bg-orange-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <span v-if="isSubmitting" class="absolute left-0 inset-y-0 flex items-center pl-3">
                  <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                </span>
                {{ isSubmitting ? 'Creating Account...' : 'Complete Registration' }}
              </button>
            </div>
          </form>
        </div>

        <!-- Success State -->
        <div v-else-if="tokenStatus === 'registered'" class="text-center">
          <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
            </svg>
          </div>
          <h3 class="text-lg font-medium text-green-900 mb-2">Registration Complete!</h3>
          <p class="text-green-700 mb-4">Your admin account has been created successfully.</p>
          <router-link
            to="/login"
            class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700"
          >
            Go to Login
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import logo from "@/assets/saved4-orange.png";
import EyeOpen from "@/components/svg/EyeOpen.vue";
import EyeClose from "@/components/svg/EyeClose.vue";

export default {
  name: 'AdminRegistration',
  components: {
    EyeOpen,
    EyeClose
  },
  data() {
    return {
      logo,
      tokenStatus: 'validating', // validating, valid, invalid, registered
      errorMessage: '',
      error: '',
      isSubmitting: false,
      showPassword: false,
      showConfirmPassword: false,
      adminInfo: {
        email: '',
      },
      registrationForm: {
        name: '',
        username: '',
        password: '',
        confirmPassword: ''
      }
    };
  },
  computed: {
    token() {
      return this.$route.params.token;
    },
    isFormValid() {
      return (
        this.registrationForm.name.trim() &&
        this.registrationForm.username.trim() &&
        this.registrationForm.password.length >= 6 &&
        this.registrationForm.password === this.registrationForm.confirmPassword
      );
    }
  },
  async mounted() {
    await this.validateToken();
  },
  methods: {
    async validateToken() {
      if (!this.token) {
        this.tokenStatus = 'invalid';
        this.errorMessage = 'No registration token provided';
        return;
      }

      try {
        // Call backend to validate token and get admin info
        const response = await fetch(`/api/register/validate/${this.token}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          }
        });

        if (!response.ok) {
          throw new Error('Token validation failed');
        }

        const adminData = await response.json();
        
        // Check if this is an admin token
        if (adminData.userType !== 'ADMIN') {
          throw new Error('Invalid token type for admin registration');
        }
        
        this.tokenStatus = 'valid';
        this.adminInfo = adminData;
        
      } catch (err) {
        console.error('Token validation error:', err);
        
        // Fallback: try to decode JWT client-side (basic implementation)
        try {
          const payload = this.decodeJWT(this.token);
          
          // Check if this is an admin token
          if (payload.userType !== 'ADMIN') {
            throw new Error('Invalid token type for admin registration');
          }
          
          this.tokenStatus = 'valid';
          this.adminInfo = {
            email: payload.sub || 'admin@example.com',
          };
        } catch (decodeErr) {
          console.error('JWT decode error:', decodeErr);
          this.tokenStatus = 'invalid';
          this.errorMessage = 'Invalid or expired registration token';
        }
      }
    },

    decodeJWT(token) {
      try {
        const parts = token.split('.');
        if (parts.length !== 3) {
          throw new Error('Invalid JWT format');
        }
        
        const payload = parts[1];
        const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
        return JSON.parse(decoded);
      } catch (err) {
        throw new Error('Failed to decode JWT');
      }
    },

    async completeRegistration() {
      this.isSubmitting = true;
      this.error = '';

      if (this.registrationForm.password !== this.registrationForm.confirmPassword) {
        this.error = 'Passwords do not match';
        this.isSubmitting = false;
        return;
      }

      try {
        const payload = {
          name: this.registrationForm.name,
          username: this.registrationForm.username,
          password: this.registrationForm.password,
          email: this.adminInfo.email, // This should come from the token
          userType: "ADMIN"
        };

        const response = await fetch(`/api/register/${this.token}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(payload)
        });

        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(errorText || 'Registration failed');
        }

        this.tokenStatus = 'registered';

      } catch (err) {
        console.error('Registration error:', err);
        this.error = err.message || 'Registration failed. Please try again.';
      } finally {
        this.isSubmitting = false;
      }
    }
  }
};
</script>
