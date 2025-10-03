<template>
  <div class="fixed inset-0 flex items-center justify-center bg-opacity-50 z-[100]">
    <div class="fixed inset-0 bg-orange-100/70"></div>
    <div class="p-5 bg-white rounded-md relative z-[101]">
      <div class="bg-gray-50 bg-opacity-50 rounded-md flex flex-col min-w-[800px]">
        <!-- Header -->
        <div class="flex justify-between items-center p-6">
          <h2 class="text-4xl font-bold text-[#E57229]">
            Generate Company Registration Token
          </h2>
          <button
            @click="$emit('close')"
            class="text-gray-500 hover:text-gray-700 cursor-pointer"
          >
            <span class="text-2xl">&times;</span>
          </button>
        </div>

        <div class="p-6">
          <!-- Step 1: Company Details Form -->
          <div v-if="step === 1">
            <h3 class="text-xl font-semibold mb-4 text-gray-800">Company Information</h3>
            <form @submit.prevent="generateToken" class="w-full">
              <div class="flex flex-col space-y-5 py-3 px-10">
                <div class="space-y-4">
                  <div class="flex items-center">
                    <label class="text-sm font-medium text-gray-700 w-32 flex items-center gap-1">
                      Company Name
                      <span class="text-red-500">*</span>
                    </label>
                    <input
                      v-model="formData.name"
                      type="text"
                      class="text-base p-2 bg-white rounded-md flex-1 border border-gray-200 focus:outline-none focus:ring-2 focus:ring-[#FA812F] transition-all duration-200"
                      required
                    />
                  </div>

                  <div class="flex items-center">
                    <label class="text-sm font-medium text-gray-700 w-32 flex items-center gap-1">
                      Email
                      <span class="text-red-500">*</span>
                    </label>
                    <input
                      v-model="formData.email"
                      type="email"
                      class="text-base p-2 bg-white rounded-md flex-1 border border-gray-200 focus:outline-none focus:ring-2 focus:ring-[#FA812F] transition-all duration-200"
                      required
                    />
                  </div>

                  <div class="grid grid-cols-2 gap-6">
                    <div class="flex items-center">
                      <label class="text-sm font-medium text-gray-700 w-32 flex items-center gap-1">
                        Latitude
                        <span class="text-red-500">*</span>
                      </label>
                      <input
                        v-model="formData.lat"
                        type="number"
                        step="0.000001"
                        class="text-base p-2 bg-white rounded-md flex-1 text-center border border-gray-200 focus:outline-none focus:ring-2 focus:ring-[#FA812F] transition-all duration-200"
                        required
                      />
                    </div>
                    <div class="flex items-center">
                      <label class="text-sm font-medium text-gray-700 w-32 flex items-center gap-1">
                        Longitude
                        <span class="text-red-500">*</span>
                      </label>
                      <input
                        v-model="formData.long"
                        type="number"
                        step="0.000001"
                        class="text-base p-2 bg-white rounded-md flex-1 text-center border border-gray-200 focus:outline-none focus:ring-2 focus:ring-[#FA812F] transition-all duration-200"
                        required
                      />
                    </div>
                  </div>

                  <div class="grid grid-cols-2 gap-6">
                    <div class="flex items-center">
                      <label class="text-sm font-medium text-gray-700 w-32 flex items-center gap-1">
                        Opening Time
                        <span class="text-red-500">*</span>
                      </label>
                      <input
                        v-model="formData.openingTime"
                        type="time"
                        class="text-base p-2 bg-white rounded-md flex-1 text-center border border-gray-200 focus:outline-none focus:ring-2 focus:ring-[#FA812F] transition-all duration-200"
                        required
                      />
                    </div>
                    <div class="flex items-center">
                      <label class="text-sm font-medium text-gray-700 w-32 flex items-center gap-1">
                        Closing Time
                        <span class="text-red-500">*</span>
                      </label>
                      <input
                        v-model="formData.closingTime"
                        type="time"
                        class="text-base p-2 bg-white rounded-md flex-1 text-center border border-gray-200 focus:outline-none focus:ring-2 focus:ring-[#FA812F] transition-all duration-200"
                        required
                      />
                    </div>
                  </div>
                </div>

                <!-- Required fields note -->
                <div class="text-sm text-gray-500 flex items-center gap-1">
                  <span class="text-red-500">*</span> Required fields
                </div>
              </div>

              <!-- Action Buttons -->
              <div class="flex justify-end gap-4 px-10 pt-6">
                <button
                  type="button"
                  @click="$emit('close')"
                  :disabled="isLoading"
                  class="px-6 py-2 rounded-md border border-gray-300 hover:bg-gray-50 cursor-pointer transition-colors duration-200 disabled:opacity-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  :disabled="isLoading"
                  class="px-6 py-2 bg-[#FA812F] text-white rounded-md hover:bg-[#E57229] cursor-pointer transition-colors duration-200 disabled:opacity-50 flex items-center gap-2"
                >
                  <span v-if="isLoading" class="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></span>
                  Generate Registration Token
                </button>
              </div>
            </form>
          </div>

          <!-- Step 2: Token Generated -->
          <div v-if="step === 2" class="text-center py-8">
            <div class="mb-6">
              <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg class="w-8 h-8 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                </svg>
              </div>
              <h3 class="text-2xl font-bold text-gray-900 mb-2">Registration Token Generated!</h3>
              <p class="text-gray-600 mb-6">Share this information with the company to complete their registration.</p>
            </div>

            <!-- Token Display -->
            <div class="bg-gray-50 border border-gray-200 rounded-lg p-6 mb-6">
              <h4 class="text-lg font-semibold text-gray-900 mb-4">Registration Details</h4>
              
              <div class="space-y-3 text-left">
                <div class="flex justify-between items-center py-2 border-b border-gray-200">
                  <span class="font-medium text-gray-700">Company:</span>
                  <span class="text-gray-900">{{ formData.name }}</span>
                </div>
                <div class="flex justify-between items-center py-2 border-b border-gray-200">
                  <span class="font-medium text-gray-700">Email:</span>
                  <span class="text-gray-900">{{ formData.email }}</span>
                </div>
                <div class="flex justify-between items-center py-2 border-b border-gray-200">
                  <span class="font-medium text-gray-700">Registration URL:</span>
                  <div class="flex items-center gap-2">
                    <code class="bg-gray-100 px-2 py-1 rounded text-sm">{{ registrationUrl }}</code>
                    <button 
                      @click="copyToClipboard(registrationUrl)"
                      class="px-2 py-1 bg-blue-500 text-white text-xs rounded hover:bg-blue-600 transition-colors"
                    >
                      Copy
                    </button>
                  </div>
                </div>
                <div class="flex justify-between items-center py-2">
                  <span class="font-medium text-gray-700">Token Expires:</span>
                  <span class="text-gray-900">{{ tokenExpiry }}</span>
                </div>
              </div>
            </div>

            <!-- Instructions -->
            <div class="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
              <h4 class="text-sm font-semibold text-blue-900 mb-2">Instructions for the company:</h4>
              <ol class="text-sm text-blue-800 text-left space-y-1">
                <li>1. Visit the registration URL above</li>
                <li>2. Fill out their username and password</li>
                <li>3. Complete the registration process</li>
                <li>4. They can then login to the system</li>
              </ol>
            </div>

            <div class="flex justify-end gap-4">
              <button
                @click="resetForm"
                class="px-6 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600 cursor-pointer transition-colors duration-200"
              >
                Generate Another Token
              </button>
              <button
                @click="$emit('close')"
                class="px-6 py-2 bg-[#FA812F] text-white rounded-md hover:bg-[#E57229] cursor-pointer transition-colors duration-200"
              >
                Done
              </button>
            </div>
          </div>

          <!-- Error Display -->
          <div v-if="error" class="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg">
            <div class="flex">
              <div class="flex-shrink-0">
                <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                </svg>
              </div>
              <div class="ml-3">
                <h3 class="text-sm font-medium text-red-800">Error</h3>
                <div class="mt-2 text-sm text-red-700">{{ error }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      step: 1,
      isLoading: false,
      error: null,
      generatedToken: '',
      tokenExpiry: '',
      formData: {
        name: '',
        email: '',
        lat: '',
        long: '',
        openingTime: '09:00',
        closingTime: '17:00',
      },
    };
  },
  computed: {
    registrationUrl() {
      return `${window.location.origin}/register/${this.generatedToken}`;
    }
  },
  methods: {
    async generateToken() {
      this.isLoading = true;
      this.error = null;

      try {
        // Convert time format and create location object for PGpoint
        const location = `(${this.formData.long},${this.formData.lat})`;

        // Parse time strings to create proper Time objects for backend
        const openingTime = this.formData.openingTime + ':00';
        const closingTime = this.formData.closingTime + ':00';

        const payload = {
          userType: 'COMPANY',
          email: this.formData.email,
          name: this.formData.name,
          location: location,
          openingTime: openingTime,
          closingTime: closingTime
        };

        console.log('Sending payload:', payload);

        const response = await fetch('/api/register', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(payload),
          credentials: 'include'
        });

        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(`Failed to generate token: ${response.status} ${errorText}`);
        }

        this.generatedToken = await response.text();
        
        // Set token expiry (15 minutes from now as per backend)
        const expiryTime = new Date();
        expiryTime.setMinutes(expiryTime.getMinutes() + 15);
        this.tokenExpiry = expiryTime.toLocaleString();
        
        this.step = 2;
        this.$emit('tokenGenerated', {
          company: this.formData.name,
          email: this.formData.email,
          token: this.generatedToken,
          url: this.registrationUrl
        });

      } catch (err) {
        console.error('Error generating token:', err);
        this.error = err.message || 'Failed to generate registration token';
      } finally {
        this.isLoading = false;
      }
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

    resetForm() {
      this.step = 1;
      this.error = null;
      this.generatedToken = '';
      this.tokenExpiry = '';
      this.formData = {
        name: '',
        email: '',
        lat: '',
        long: '',
        openingTime: '09:00',
        closingTime: '17:00',
      };
    }
  },

  mounted() {
    document.body.style.overflow = 'hidden';
  },

  beforeUnmount() {
    document.body.style.overflow = 'auto';
  }
};
</script>
