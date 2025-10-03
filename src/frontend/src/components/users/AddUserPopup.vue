<template>
  <div
    class="fixed inset-0 flex items-center justify-center bg-opacity-50 z-[100]"
  >
    <div class="fixed inset-0 bg-orange-100/70"></div>
    <div class="p-5 bg-white rounded-md relative z-[101]">
      <div
        class="bg-gray-50 bg-opacity-50 rounded-md flex flex-col min-w-[800px]"
      >
        <!-- Header -->
        <div class="flex justify-between items-center p-6">
          <h2 class="text-4xl font-bold text-[#E57229]">
            Add {{ formType }} Account
          </h2>
          <button
            @click="$emit('close')"
            class="text-gray-500 hover:text-gray-700 cursor-pointer"
          >
            <span class="text-2xl">&times;</span>
          </button>
        </div>

        <div class="p-6">
          <!-- Form -->
          <form @submit.prevent="handleSubmit" class="w-full">
            <div class="flex flex-col space-y-5 py-3 px-10">
              <div class="space-y-4">
                <div class="flex items-center">
                  <label
                    class="text-sm font-medium text-gray-700 w-32 flex items-center gap-1"
                  >
                    Name
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
                  <label
                    class="text-sm font-medium text-gray-700 w-32 flex items-center gap-1"
                  >
                    Username
                    <span class="text-red-500">*</span>
                  </label>
                  <input
                    v-model="formData.username"
                    type="text"
                    class="text-base p-2 bg-white rounded-md flex-1 border border-gray-200 focus:outline-none focus:ring-2 focus:ring-[#FA812F] transition-all duration-200"
                    required
                  />
                </div>

                <!-- Only show email for Company -->
                <div v-if="formType === 'Company'" class="flex items-center">
                  <label
                    class="text-sm font-medium text-gray-700 w-32 flex items-center gap-1"
                  >
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

                <div class="flex items-center">
                  <label
                    class="text-sm font-medium text-gray-700 w-32 flex items-center gap-1"
                  >
                    Password
                    <span class="text-red-500">*</span>
                  </label>
                  <div class="relative flex-1">
                    <input
                      :type="showPassword ? 'text' : 'password'"
                      v-model="formData.password"
                      class="text-base p-2 bg-white rounded-md w-full pr-10 border border-gray-200 focus:outline-none focus:ring-2 focus:ring-[#FA812F] transition-all duration-200"
                      required
                    />
                    <button
                      type="button"
                      class="absolute top-1/2 right-3 -translate-y-1/2 text-gray-500 hover:text-gray-700 transition-colors cursor-pointer p-1"
                      @click="showPassword = !showPassword"
                    >
                      <component
                        :is="showPassword ? 'EyeOpen' : 'EyeClose'"
                        class="h-5 w-5"
                      />
                    </button>
                  </div>
                </div>
              </div>

              <div
                v-if="formType === 'Company'"
                class="space-y-4 pt-2 border-t border-gray-200"
              >
                <div class="grid grid-cols-2 gap-6">
                  <div class="flex items-center">
                    <label
                      class="text-sm font-medium text-gray-700 w-32 flex items-center gap-1"
                    >
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
                    <label
                      class="text-sm font-medium text-gray-700 w-32 flex items-center gap-1"
                    >
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
                    <label
                      class="text-sm font-medium text-gray-700 w-32 flex items-center gap-1"
                    >
                      Opening
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
                    <label
                      class="text-sm font-medium text-gray-700 w-32 flex items-center gap-1"
                    >
                      Closing
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

              <!-- Add required fields note -->
              <div class="text-sm text-gray-500 flex items-center gap-1">
                <span class="text-red-500">*</span> Required fields
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex justify-end gap-4 px-10 pt-6">
              <button
                type="button"
                @click="$emit('close')"
                class="px-6 py-2 rounded-md border border-gray-300 hover:bg-gray-50 cursor-pointer transition-colors duration-200"
              >
                Cancel
              </button>
              <button
                type="submit"
                class="px-6 py-2 bg-[#FA812F] text-white rounded-md hover:bg-[#E57229] cursor-pointer transition-colors duration-200"
              >
                Create
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import EyeOpen from "@/components/svg/EyeOpen.vue";
import EyeClose from "@/components/svg/EyeClose.vue";

export default {
  components: {
    EyeOpen,
    EyeClose,
  },
  data() {
    return {
      showPassword: false,
      newPassword: "",
      selectedAccountType: "Admin",
      formData: {
        name: "",
        username: "",
        password: "",
        location: "",
        long: "",
        lat: "",
        accountType: "",
        openingTime: "09:00",
        closingTime: "17:00",
        email: "", // only used for company
      },
    };
  },
  methods: {
    /**
     * Submit data
     * Emit to parent's method
     */
    handleSubmit() {
      event.preventDefault();

      this.formData.accountType = this.formType;

      this.$emit("save", this.formData);

      this.formData = {
        name: "",
        username: "",
        password: "",
        location: "",
        long: "",
        lat: "",
        accountType: "",
        openingTime: "09:00",
        closingTime: "17:00",
      };

      this.$emit("close");
    },
  },

  computed: {
    sliderPosition() {
      switch (this.selectedAccountType) {
        case "Admin":
          return "0%";
        case "Company":
          return "50%";
      }
    },
  },

  props: {
    formType: {
      type: String,
      required: true,
    },
  },
};
</script>
