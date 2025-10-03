<template>
  <div>
    <div v-if="isLoading" class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div class="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-orange-500 mx-auto mb-4"></div>
        <p class="text-gray-600">Determining registration type...</p>
      </div>
    </div>
    
    <CompanyRegistration v-else-if="registrationType === 'COMPANY'" />
    <AdminRegistration v-else-if="registrationType === 'ADMIN'" />
    
    <div v-else class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div class="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div class="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg class="w-8 h-8 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </div>
        <h3 class="text-lg font-medium text-red-900 mb-2">Invalid Registration Token</h3>
        <p class="text-red-700 mb-4">The registration token is invalid or expired.</p>
        <router-link
          to="/login"
          class="text-orange-600 hover:text-orange-500"
        >
          Return to Login
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import CompanyRegistration from '@/components/CompanyRegistration.vue';
import AdminRegistration from '@/components/AdminRegistration.vue';

export default {
  name: 'RegistrationRouter',
  components: {
    CompanyRegistration,
    AdminRegistration
  },
  data() {
    return {
      isLoading: true,
      registrationType: null
    };
  },
  async mounted() {
    await this.determineRegistrationType();
  },
  methods: {
    async determineRegistrationType() {
      const token = this.$route.params.token;
      
      if (!token) {
        this.isLoading = false;
        return;
      }

      try {
        // First try to validate token with backend
        const response = await fetch(`/api/register/validate/${token}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          }
        });

        if (response.ok) {
          const data = await response.json();
          this.registrationType = data.userType;
          this.isLoading = false;
          return;
        }
      } catch (err) {
        console.error('Backend validation failed:', err);
      }

      // Fallback: try to decode JWT client-side
      try {
        const payload = this.decodeJWT(token);
        this.registrationType = payload.userType;
      } catch (err) {
        console.error('JWT decode error:', err);
        this.registrationType = null;
      }
      
      this.isLoading = false;
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
    }
  }
};
</script>
