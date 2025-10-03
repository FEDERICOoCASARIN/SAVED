<template>
    <div class="fixed inset-0 flex items-center justify-center bg-opacity-50 z-[100]">
        <div class="fixed inset-0 bg-orange-100/70"></div>
        <div class="p-8 bg-white rounded-xl relative z-[101] max-w-3xl mx-auto my-6 shadow-lg">
            <div v-if="!showConfirmation" class="h-full">
                <div class="flex justify-between items-center pb-5 border-b border-gray-200 mb-6">
                    <h2 class="text-4xl font-bold text-[#E57229]">Add New Order</h2>

                    <div class="flex items-center gap-2">
                        <div v-if="isAdmin" class="flex items-center gap-2">
                            <p class="text-sm">Order for<span class="text-red-500"> *</span> :</p>
                            <div class="relative">
                                <button
                                type="button"
                                @click="showOrderForDropdown = !showOrderForDropdown"
                                class="w-full text-left text-base p-2 rounded-md text-gray-700 border  focus:outline-none focus:ring-2 focus:ring-[#FA812F] focus:border-transparent flex justify-between min-w-60 items-center uppercase"
                                :class="{'border-red-500 border-2': showErrors && !orderData.orderFor, 'border-gray-300': !(showErrors && !orderData.orderFor)}">
                                <span> {{ orderData.orderFor || 'Select recipient' }}</span>
                                 <svg
                                                class="w-4 h-4 text-gray-400 transition-transform duration-200"
                                                :class="{ 'rotate-180': showFreightTypeDropdown }"
                                                fill="none"
                                                stroke="currentColor"
                                                viewBox="0 0 24 24"
                                            >
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                                     </svg>
                                </button>

                                <div v-if="showOrderForDropdown" class="absolute z-50 w-full mt-1 bg-white border border-gray-200 rounded-md shadow-lg max-h-40 overflow-y-auto">
                                    <div
                                    v-for="company in companyList"
                                    :key="company" 
                                    @click="selectOrderFor(company)"
                                    class="px-4 py-2 text-gray-700 hover:bg-orange-50 hover:text-[#FA812F] cursor-pointer uppercase text-base"
                                    >
                                        {{ company }}
                                    </div>
                                   
                                </div>
                                 <div v-if="showErrors && !orderData.orderFor" class="absolute bottom-full left-0 mb-1 text-red-500 text-xs whitespace-nowrap z-50">
                                    Please select a recipient.
                                </div> 
                            </div>
                        </div>

                        <button @click="$emit('close')" class="text-gray-500 hover:text-gray-700 cursor-pointer text-3xl leading-none">&times;</button>
                    </div>
                </div>

                <div class="py-2">
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6 mb-6">
                        <div class="flex flex-col">
                            <label class="text-sm font-semibold text-gray-700 mb-2">Request or Send Container <span class="text-red-500">*</span></label>
                            <div class="relative flex bg-[#EFEEF5] rounded-xl items-center gap-1 overflow-hidden text-base h-11">
                                <div
                                    class="absolute top-0 bottom-0 w-1/2 bg-[#FA812F] rounded-xl transition-left duration-200 ease-in-out"
                                    :style="{ left: sliderPosition1 }"
                                ></div>
                                <button
                                    class="flex-1 flex justify-center rounded-xl py-2 font-bold cursor-pointer z-10 select-none"
                                    :class="sendOrRequestOption === 'Unloading' ? 'text-white' : 'text-gray-700'"
                                    @click="sendOrRequestOption = 'Unloading'"
                                >
                                    Request
                                </button>
                                <button
                                    class="flex-1 flex justify-center rounded-xl py-2 font-bold cursor-pointer z-10 select-none"
                                    :class="sendOrRequestOption === 'Loading' ? 'text-white' : 'text-gray-700'"
                                    @click="sendOrRequestOption = 'Loading'"
                                >
                                    Send
                                </button>
                            </div>
                        </div>

                        <div class="flex flex-col">
                            <label class="text-sm font-semibold text-gray-700 mb-2">Empty, Partial, or Full Container <span class="text-red-500">*</span></label>
                            <div class="relative flex bg-[#EFEEF5] rounded-xl items-center gap-1 overflow-hidden text-base h-11">
                                <div
                                    class="absolute top-0 bottom-0 w-1/2 bg-[#FA812F] rounded-xl transition-left duration-200 ease-in-out"
                                    :style="{ left: sliderPosition2 }"
                                ></div>
                                <button
                                    class="flex-1 flex justify-center rounded-xl py-2 font-bold cursor-pointer z-10 select-none"
                                    :class="emptyOrFullContainer === 'Empty' ? 'text-white' : 'text-gray-700'"
                                    @click="emptyOrFullContainer = 'Empty'"
                                >
                                    Empty
                                </button>
                                <button
                                    class="flex-1 flex justify-center rounded-xl py-2 font-bold cursor-pointer z-10 select-none"
                                    :class="emptyOrFullContainer === 'Full' ? 'text-white' : 'text-gray-700'"
                                    @click="emptyOrFullContainer = 'Full'"
                                >
                                    Full
                                </button>
                            </div>
                        </div>
                    </div>

                    <form @submit.prevent="handleSubmitOrder" class="mt-4">
                        
                     

                        <div class="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-2 mb-6">
                            <div class="space-y-2">
                                <label class="block text-sm font-semibold text-gray-700">
                                    Start Window<span class="text-red-500"> *</span>
                                </label>
                                <div class="flex gap-3">
                                    <input
                                        v-model="orderData.twStartDate"
                                        type="date"
                                        class="flex-1 p-3 bg-white/50 rounded-md border border-gray-200 focus:ring-2 focus:ring-[#FA812F] focus:border-transparent text-center text-base"
                                    />
                                    <input
                                        v-model="orderData.twStartTime"
                                        type="time"
                                        class="w-28 p-3 bg-white/50 rounded-md border border-gray-200 focus:ring-2 focus:ring-[#FA812F] focus:border-transparent text-center text-base"
                                        :min="currentTime"
                                        @input="validateTimeInput($event, 'twStart')"
                                    />
                                </div>
                                 <p v-if="formErrors.twStartMinute" class="text-red-500 text-xs mt-1">{{ formErrors.twStartMinute }}</p>
                                 <p v-if="formErrors.twStartPast" class="text-red-500 text-xs mt-1">{{ formErrors.twStartPast }}</p>
                            </div>

                            <div class="space-y-2">
                                <label class="block text-sm font-semibold text-gray-700">
                                    End Window<span class="text-red-500"> *</span>
                                </label>
                                <div class="flex gap-3">
                                    <input
                                        v-model="orderData.twEndDate"
                                        type="date"
                                        class="flex-1 p-3 bg-white/50 rounded-md border border-gray-200 focus:ring-2 focus:ring-[#FA812F] focus:border-transparent text-center text-base"
                                    />
                                    <input
                                        v-model="orderData.twEndTime"
                                        type="time"
                                        class="w-28 p-3 bg-white/50 rounded-md border border-gray-200 focus:ring-2 focus:ring-[#FA812F] focus:border-transparent text-center text-base"
                                        :min="currentTime"
                                        @input="validateTimeInput($event, 'twEnd')"
                                    />
                                </div>
                                 <p v-if="formErrors.twEndMinute" class="text-red-500 text-xs mt-1">{{ formErrors.twEndMinute }}</p>
                                 <p v-if="formErrors.twEndPast" class="text-red-500 text-xs mt-1">{{ formErrors.twEndPast }}</p>
                                </div>
                                <p v-if="formErrors.twEndBeforeStart" class="text-red-500 text-xs mt-1">{{ formErrors.twEndBeforeStart }}</p>
                                <p v-if="formErrors.twDurationTooShort" class="text-red-500 text-xs mt-1">{{ formErrors.twDurationTooShort }}</p>
                        </div>

                        <div>
                            <div class="flex items-center gap-3 mb-4 mt-6">
                                <span class="text-sm font-medium text-gray-600">Freight Information (Excluding EMPTY containers)</span>
                                <hr class="flex-1 border-t border-gray-200 ml-2" />
                            </div>
                        <div class="flex items-center mt-2 mb-6">
                            <input
                                type="checkbox"
                                v-model="orderData.preferredShared"
                                id="shareContainer"
                                :disabled="isDisabled"
                                class="mr-3 w-5 h-5 accent-[#FA812F]"
                            />
                            <label
                                for="shareContainer"
                                :class="{ 'opacity-30 transition-opacity duration-300': isDisabled }"
                                class="text-sm font-semibold text-gray-700"
                            >
                                Prefer to share containers? Tick if apply 
                            </label>
                        </div>
                            <div class="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6 mb-6">
                                <div class="flex flex-col">
                                    <div class="flex justify-between gap-2">
                                        <label class="block text-sm font-semibold text-gray-700 mb-2">
                                            Freight Type <span class="text-sm text-gray-300"> (opt.)</span>
                                        </label> 
                                        <span v-if="formErrors.orderFor" class="text-sm text-red-500">{{ formErrors.freightType }}</span>
                                    </div>

                                    <div class="relative">
                                        <button
                                            type="button"
                                            @click="showFreightTypeDropdown = !showFreightTypeDropdown"
                                            :disabled="emptyOrFullContainer === 'Empty'"
                                            class="w-full text-left text-base rounded-md text-gray-700 border border-gray-200 focus:outline-none px-1 focus:ring-2 focus:ring-[#FA812F] focus:border-transparent flex justify-between items-center uppercase "
                                            :class="[
                                                emptyOrFullContainer === 'Empty' ? 'bg-gray-200 cursor-not-allowed' : 'bg-white',
                                                formErrors.freightType ? ' outline-2 outline-red-500' : '',
                                                
                                            ]"
                                        >
                                        <span class="flex flex-row items-center w-full ">
                                          <input type="text" class="w-full h-full p-3.5 uppercase focus:outline-none" v-model="orderData.freightType" :disabled="emptyOrFullContainer === 'Empty'" placeholder="SELECT FREIGHT TYPE" :class="emptyOrFullContainer === 'Empty' ? 'bg-gray-200 cursor-not-allowed' : 'bg-white'">
                                              <svg
                                                class="w-4 h-4 text-gray-400 transition-transform duration-200"
                                                :class="{ 'rotate-180': showFreightTypeDropdown }"
                                                fill="none"
                                                stroke="currentColor"
                                                
                                                viewBox="0 0 24 24"
                                            >
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                                            </svg>
                                        </span>
                                        
                                        </button>

                                        <div
                                            v-if="showFreightTypeDropdown && emptyOrFullContainer !== 'Empty'"
                                            class="absolute z-50 w-full mt-1 bg-white border border-gray-200 rounded-md shadow-lg max-h-40 overflow-y-auto"
                                            >
                                            <div
                                                v-if="searchFreightType.length"
                                                v-for="type in searchFreightType"
                                                :key="type"
                                                @click="selectFreightType(type)"
                                                class="px-4 py-2 text-gray-700 hover:bg-orange-50 hover:text-[#FA812F] cursor-pointer uppercase text-base"
                                                
                                            >
                                                {{ type }}
                                            </div>

                                            <div v-else class="px-4 py-2 text-gray-500">
                                                No options found
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div class="flex flex-col sm:flex-col">
                                    <div class="flex gap-6">
                                        <div class="flex-1">
                                            <label class="block text-sm font-semibold text-gray-700 mb-2">
                                                Freight Value (€) <span class="text-sm text-gray-300"> (opt.)</span>
                                            </label>
                                            <input
                                                type="number"
                                                class="w-full text-base p-3 rounded-md text-gray-700 border border-gray-200 focus:outline-none focus:ring-2 focus:ring-[#FA812F] focus:border-transparent"
                                                :class="emptyOrFullContainer === 'Empty' ? 'bg-gray-200 cursor-not-allowed' : 'bg-gray-50'"
                                                v-model="orderData.freightValue"
                                                :disabled="emptyOrFullContainer === 'Empty'"
                                            />
                                        </div>
                                        <div class="flex-1">
                                            <label class="block text-sm font-semibold text-gray-700 mb-2">
                                                Freight Weight (kg) 
                                            </label>
                                            <input
                                                type="number"
                                                class="w-full text-base p-3 rounded-md text-gray-700 border border-gray-200 focus:outline-none focus:ring-2 focus:ring-[#FA812F] focus:border-transparent"
                                                :class="emptyOrFullContainer === 'Empty' ? 'bg-gray-200 cursor-not-allowed' : 'bg-gray-50'"
                                                v-model="orderData.freightWeight"
                                                :disabled="emptyOrFullContainer === 'Empty'"
                                            />
                                        </div>
                                    </div>
                                    <p v-if="formErrors.freightWeight" class="text-red-500 text-xs mt-1">{{ formErrors.freightWeight }}</p>

                                </div>
                            </div>
                        </div>
                        <div class="flex justify-end gap-4 pt-6 border-t border-gray-200 mt-8">
                            <button @click="$emit('close')" type="button" class="px-6 py-2.5 rounded-md hover:bg-gray-100 cursor-pointer text-base border border-gray-300">Cancel</button>
                            <button type="submit" class="px-6 py-2.5 bg-[#FA812F] text-white rounded-md hover:bg-[#E57229] cursor-pointer text-base">Submit Order</button>
                        </div>
                    </form>
                </div>
            </div>

            <div v-else class="text-center p-8">
                <svg class="mx-auto h-20 w-20 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                <h2 class="text-3xl font-bold text-[#E57229] mt-4 mb-2">Order Summary</h2>
                <p class="text-lg text-gray-700 mb-6">Please review the details before confirming.</p>

                <div class="text-left bg-gray-50 p-6 rounded-lg mb-8 shadow-inner">
                    <div class="grid grid-cols-2 gap-4">
                        <div v-if="isAdmin">
                            <p class="text-sm font-semibold text-gray-600">Order For:</p>
                            <p class="text-base text-gray-800 uppercase">{{ orderData.orderFor }}</p>
                        </div>
                        <div>
                            <p class="text-sm font-semibold text-gray-600">Operation Type:</p>
                            <p class="text-base text-gray-800 capitalize">{{ orderData.operationType }}</p>
                        </div>
                        <div>
                            <p class="text-sm font-semibold text-gray-600">Container Type:</p>
                            <p class="text-base text-gray-800">{{ emptyOrFullContainer }}</p>
                        </div>
                        <div>
                            <p class="text-sm font-semibold text-gray-600">Pick-up Point:</p>
                            <p class="text-base text-gray-800 uppercase">{{ orderData.source }}</p>
                        </div>
                        <div>
                            <p class="text-sm font-semibold text-gray-600">Destination Point:</p>
                            <p class="text-base text-gray-800 uppercase">{{ isAdmin ?  orderData.destination : this.loggedInUsername }}</p>
                        </div>
                        <div>
                            <p class="text-sm font-semibold text-gray-600">Start Window:</p>
                            <p class="text-base text-gray-800">{{ orderData.twStartDate }} at {{ orderData.twStartTime }}</p>
                        </div>
                        <div>
                            <p class="text-sm font-semibold text-gray-600">End Window:</p>
                            <p class="text-base text-gray-800">{{ orderData.twEndDate }} at {{ orderData.twEndTime }}</p>
                        </div>
                        <div v-if="emptyOrFullContainer !== 'Empty'">
                            <p class="text-sm font-semibold text-gray-600">Freight Type:</p>
                            <p class="text-base text-gray-800 capitalize">{{ orderData.freightType || 'N/A' }}</p>
                        </div>
                        <div v-if="emptyOrFullContainer !== 'Empty'">
                            <p class="text-sm font-semibold text-gray-600">Freight Value:</p>
                            <p class="text-base text-gray-800">€{{ orderData.freightValue || 0 }}</p>
                        </div>
                        <div v-if="emptyOrFullContainer !== 'Empty'">
                            <p class="text-sm font-semibold text-gray-600">Freight Weight:</p>
                            <p class="text-base text-gray-800">{{ orderData.freightWeight || 0 }} kg</p>
                        </div>
                        <div>
                            <p class="text-sm font-semibold text-gray-600">Preferred Shared:</p>
                            <p class="text-base text-gray-800">{{ orderData.preferredShared ? 'Yes' : 'No' }}</p>
                        </div>
                    </div>
                </div>

                <div class="flex justify-center gap-4">
                    <button @click="editOrder" type="button" class="px-6 py-2.5 rounded-md hover:bg-gray-100 cursor-pointer text-base border border-gray-300">Edit Order</button>
                    <button @click="confirmOrder" type="button" class="px-6 py-2.5 bg-[#FA812F] text-white rounded-md hover:bg-[#E57229] cursor-pointer text-base">Confirm Order</button>
                </div>
            </div>
        </div>
    </div>
</template>


<script>
import DropdownSVG from "@/components/svg/DropdownSVG.vue";
import DropdownArrow from "@/components/svg/DropdownArrow.vue"
import { useAuthStore } from '@/stores/auth'

export default {
    components:{
        DropdownSVG,
        DropdownArrow
    },
    props: {
        companyList: {
            type: Array,
            required: true
        },
        loggedInUsername: {
            type: String,
            required: true
        }
    },
     data(){
        return{
            showErrors: false,
            isDropDownOpen: false,
            currentTime: '',
            sendOrRequestOption: 'Unloading',
            emptyOrFullContainer: 'Empty',
            intervalId: null,
            formErrors: {},
            searchFreightTypeQuery: '',
            orderData: {
                madeAt: '',
                operationType: null,
                containerId: '',
                source: null,
                destination: null,
                twStartDate: '',
                twStartTime: '',
                twEndDate: '',
                twEndTime: '',
                freightValue: 0,
                freightWeight: 0,
                status: '',
                freightType: null,
                preferredShared: false,
                orderFor: ''
    
            },
            freightTypes: [
                'food',
                'electronic',
                'textile',
                'machinery',
                'chemical',
                'other'
            ],
            containerOptions: [
                ['#A1B2C3D4'],
                ['#E5F6G7H8'],
                ['#I9J0K1L2'],
                ['#M3N4O5P6'],
                ['#Q7R8S9T0'],
                ['#U1V2W3X4'],
                ['#Y5Z6A7B8'],
                ['#C9D0E1F2'],
                ['#G3H4I5J6'],
                ['#K7L8M9N0'],
                ['#O1P2Q3R4'],
                ['#S5T6U7V8'],
                ['#W9X0Y1Z2'],
                ['#A3B4C5D6'],
                ['#E7F8G9H0'],
                ['#A1B2C3D4'],
                ['#E5F6G7H8'],
                ['#I9J0K1L2'],
                ['#M3N4O5P6'],
                ['#Q7R8S9T0'],
                ['#U1V2W3X4'],
                ['#Y5Z6A7B8'],
                ['#C9D0E1F2'],
                ['#G3H4I5J6'],
                ['#K7L8M9N0'],
                ['#O1P2Q3R4'],
                ['#S5T6U7V8'],
                ['#W9X0Y1Z2'],
                ['#A3B4C5D6'],
                ['#E7F8G9H0'],
                ['#A1B2C3D4'],
                ['#E5F6G7H8'],
                ['#I9J0K1L2'],
                ['#M3N4O5P6'],
                ['#Q7R8S9T0'],
                ['#U1V2W3X4'],
                ['#Y5Z6A7B8'],
                ['#C9D0E1F2'],
                ['#G3H4I5J6'],
                ['#K7L8M9N0'],
                ['#O1P2Q3R4'],
                ['#S5T6U7V8'],
                ['#W9X0Y1Z2'],
                ['#A3B4C5D6'],
                ['#E7F8G9H0']
            ],
            showFreightTypeDropdown: false,
            showOrderForDropdown: false,
            // New data property for confirmation
            showConfirmation: false, 
        }
    },
    computed: {
        isAdmin() {
            const authStore = useAuthStore();
            return authStore.isAdmin; // Replace with your actual admin check
        },
         sliderPosition1() {
            return this.sendOrRequestOption === 'Unloading' ? '0%' : '50%';
        },
        sliderPosition2() {
            return this.emptyOrFullContainer === 'Empty' ? '0%' : '50%';
        },
        searchFreightType() {
            // Your existing logic for filtering freight types
            if (!this.orderData.freightType) {
                return this.freightTypes;
            }
            const query = this.orderData.freightType.toLowerCase();
            return this.freightTypes.filter(type => type.includes(query));
        },
         isDisabled(){
            return  this.emptyOrFullContainer === 'Empty'
        }
    },
    watch: {
        emptyOrFullContainer(newVal) {
            if (newVal === 'Empty') {
                this.orderData.freightType = null;
                this.orderData.freightValue = 0;
                this.orderData.freightWeight = 0;
                this.orderData.preferredShared = false; // Disable sharing for empty containers
                this.formErrors.freightWeight = ''; // Clear any weight errors
            }
        },
        'orderData.freightWeight'(newVal) {
            // Auto-uncheck preferredShared if weight is >= 2400
            if (newVal >= 2400) {
                this.orderData.preferredShared = false;
            }
        },
    },
    mounted() {
        this.getCurrentFormattedTime();
    },
    methods: {
        containerDropDown(){
            this.isDropDownOpen = !this.isDropDownOpen;
        },
        selectOption(option){
            this.orderData.operationType = option;

            if(this.orderData.operationType === 'REQUEST FULL CONTAINER'){
                this.orderData.source = "PORT"
                this.orderData.destination = this.orderData.orderFor
            } else if (this.orderData.operationType === 'REQUEST EMPTY CONTAINER') {
                this.orderData.source = "PORT"
                this.orderData.destination = this.orderData.orderFor
            } else{
                this.orderData.source = ""
                this.orderData.destination = ""
            }
            this.isDropDownOpen = false;
            
        },
        handleOutsideDropdown(event){
           const dropdownEl = this.$refs.dropdown
            if (dropdownEl && !dropdownEl.contains(event.target)) {
                this.isDropDownOpen = false
            }
        },
        
        attachCurrentDate() {
            const pad = n => String(n).padStart(2, '0');
            const date = new Date();

            const year = date.getFullYear();
            const month = pad(date.getMonth() + 1);
            const day = pad(date.getDate());
            const hours = pad(date.getHours());
            const minutes = pad(date.getMinutes());
            const seconds = pad(date.getSeconds());

            return this.buildTimeStamp(year, month, day, hours, minutes, seconds, date, pad);
        },
        // to timestamp

        buildTimeStamp(year, month, day, hours, minutes, seconds, date, pad) {
            const tzOffset = -date.getTimezoneOffset();
            const sign = tzOffset >= 0 ? '+' : '-';
            const absOffset = Math.abs(tzOffset);
            const tzHours = pad(Math.floor(absOffset / 60));
            const tzMinutes = pad(absOffset % 60);

            return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}${sign}${tzHours}:${tzMinutes}`;
        }, 


        combineDateTime(dateStr, timeStr) {
            const pad = n => String(n).padStart(2, '0');
            
            const [year, month, day] = dateStr.split('-');
            const [hours, minutes] = timeStr.split(':');

            const date = new Date(year, month - 1, day, hours, minutes, 0);

            return this.buildTimeStamp(
                year,
                pad(month),
                pad(day),
                pad(hours),
                pad(minutes),
                '00',
                date,
                pad
            );
            },
        validateTime(){
            this.formErrors = {};

            if (!this.orderData.twStartDate || !this.orderData.twStartTime) {
                this.formErrors.twStart = 'Start Window is required';
            }
            if (!this.orderData.twEndDate || !this.orderData.twEndTime) {
                this.formErrors.twEnd = 'End Window is required';
            }
              if (this.orderData.twStartDate && this.orderData.twStartTime && this.orderData.twEndDate && this.orderData.twEndTime) {
                const startDateTime = new Date(`${this.orderData.twStartDate}T${this.orderData.twStartTime}`);
                const endDateTime = new Date(`${this.orderData.twEndDate}T${this.orderData.twEndTime}`);
                const now = new Date();

                // Validate minutes for start time
                if (this.orderData.twStartTime) {
                    const startMinutes = parseInt(this.orderData.twStartTime.split(':')[1]);
                    if (startMinutes % 30 !== 0) {
                        this.formErrors.twStartMinute = 'Start time minutes must be 00 or 30.';
                    }
                }
                // Validate minutes for end time
                if (this.orderData.twEndTime) {
                    const endMinutes = parseInt(this.orderData.twEndTime.split(':')[1]);
                    if (endMinutes % 30 !== 0) {
                        this.formErrors.twEndMinute = 'End time minutes must be 00 or 30.';
                    }
                }
                
                if (startDateTime < now) {
                    this.formErrors.twStartPast = 'Start window cannot be in the past.';
                }
                if (endDateTime < now) {
                    this.formErrors.twEndPast = 'End window cannot be in the past.';
                }

                if (endDateTime < startDateTime) {
                    this.formErrors.twEndBeforeStart = 'End window cannot be before start window.';
                }

                const durationMinutes = (endDateTime.getTime() - startDateTime.getTime()) / (1000 * 60);
                if (durationMinutes < 30) {
                    this.formErrors.twDurationTooShort = 'Time window must be at least 30 minutes.';
                }
            }
        },
        validateForm() {
            this.formErrors = {};

            this.validateTime()


            // Admin specific validation for "Order for"
             if (this.isAdmin && !this.orderData.orderFor) {
                this.formErrors.orderFor = 'Recipient is required';
            }
            
                
            if (this.orderData.freightType){
                if(!this.freightTypes.includes(this.orderData.freightType.toLowerCase())) {
                this.formErrors.freightType = 'Invalid Freight Type';
                }
            }

            if (this.orderData.freightWeight > 2500) {
                this.formErrors.freightWeight = 'Weight cannot exceed 2500 kg.';
            }

            if ((this.orderData.freightWeight == 0 || this.orderData.freightWeight == null) && this.sendOrRequestOption == 'Loading' && this.emptyOrFullContainer == 'Full') {
                this.formErrors.freightWeight = 'Weight cannot be empty for sending containers.';
            }

            if (this.orderData.freightWeight >= 2400 && this.orderData.preferredShared) {
                this.formErrors.freightWeight = 'Weight must be less than 2400 kg to enable container-sharing.';
            }

              if (this.orderData.freightValue < 0) {
                this.formErrors.freightWeight = 'Freight value cannot be a negative number.';
            }

            // Validate time windows:
          

            console.log(this.formErrors)

            return Object.keys(this.formErrors).length === 0;
        },

        handleSubmitOrder(event){
            event.preventDefault();

            this.showErrors = true
            this.formErrors = {}

            if(!this.validateForm()){
                console.log(this.formErrors)
                return;
            }

            // Prepare orderData for confirmation/submission
            this.orderData.madeAt = this.attachCurrentDate();
            this.orderData.status = 'created';
            this.orderData.operationType = `${this.sendOrRequestOption}`.toLowerCase();
            this.orderData.source = this.sendOrRequestOption === 'Loading' ? 'PORT' : this.orderData.orderFor;
            this.orderData.destination = this.sendOrRequestOption === 'Loading' ? this.orderData.orderFor : 'PORT';
            
            // Set freight values to null if empty container
            if (this.emptyOrFullContainer === 'Empty') {
                this.orderData.freightType = null;
                this.orderData.freightValue = 0;
                this.orderData.freightWeight = 0;
                this.orderData.preferredShared = false;
            }

            // Show confirmation overlay
            this.showConfirmation = true;
        },

        confirmOrder() {
            // This method is called when the user clicks "Confirm Order"
            const authStore = useAuthStore();
         
            const finalOrderData = {
                ...this.orderData,
                // combine date and time for time windows
                twStart: this.combineDateTime(this.orderData.twStartDate, this.orderData.twStartTime),
                twEnd: this.combineDateTime(this.orderData.twEndDate, this.orderData.twEndTime),
                orderFor: authStore.isAdmin ? this.orderData.orderFor : this.loggedInUsername
            };

            delete finalOrderData.twStartDate;
            delete finalOrderData.twStartTime;
            delete finalOrderData.twEndDate;
            delete finalOrderData.twEndTime;

            console.log(finalOrderData)
            this.$emit('save', finalOrderData);
            
            // Optionally reset form and close modal
            this.resetForm();
            this.$emit('close');
        },

        editOrder() {
            // This method is called when the user clicks "Edit Order"
            // Hide the confirmation and show the form again
            this.showConfirmation = false;
        },

        resetForm() {
            this.orderData = {
                madeAt: '',
                operationType: null,
                containerId: '',
                source: null,
                destination: null,
                twStartDate: '',
                twStartTime: '',
                twEndDate: '',
                twEndTime: '',
                freightValue: 0,
                freightWeight: 0,
                status: '',
                freightType: null,
                preferredShared: false,
                orderFor: ''
            };
            this.sendOrRequestOption = 'Unloading';
            this.emptyOrFullContainer = 'Empty';
            this.showErrors = false;
            this.formErrors = {};
            this.showConfirmation = false; // Ensure confirmation is hidden
            this.getCurrentFormattedTime(); // Reinitialize time fields
        },

        getCurrentFormattedTime() {
            const now = new Date();

            const year = now.getFullYear();
            const month = String(now.getMonth() + 1).padStart(2, '0');
            const day = String(now.getDate()).padStart(2, '0');
            const isoDate = `${year}-${month}-${day}`;

            const adjustedStartTime = this.roundToNearest30Minutes(now.getHours(), now.getMinutes());

            this.orderData.twStartDate = isoDate;
            this.orderData.twStartTime = `${adjustedStartTime.hour}:${adjustedStartTime.minute}`;

         
            const adjustedStartDateObj = new Date(
                year,
                Number(month) - 1, 
                Number(day),
                Number(adjustedStartTime.hour),
                Number(adjustedStartTime.minute)
            );

            adjustedStartDateObj.setMinutes(adjustedStartDateObj.getMinutes() + 30); 

            const endHours = String(adjustedStartDateObj.getHours()).padStart(2, '0');
            const endMinutes = String(adjustedStartDateObj.getMinutes()).padStart(2, '0');
            const adjustedEndTimeString = `${endHours}:${endMinutes}`;

            this.orderData.twEndDate = isoDate; 
            this.orderData.twEndTime = adjustedEndTimeString;
        },


        roundToNearest30Minutes(hours, minutes) {
            hours = Number(hours);
            minutes = Number(minutes);

            let newMinute = Math.ceil(minutes / 30) * 30;
            let newHour = hours;

            if (newMinute >= 60) {
                newMinute -= 60; 
                newHour = (newHour + 1) % 24; 
            }

            const paddedHour = String(newHour).padStart(2, '0');
            const paddedMinute = String(newMinute).padStart(2, '0');

            return { hour: paddedHour, minute: paddedMinute };
        },


        selectContainerOption(id){
            this.orderData.containerId = id;
            this.isDropDownOpen = false;
        },
        selectFreightType(type) {
            this.orderData.freightType = type;
            this.showFreightTypeDropdown = false;
        },
        selectOrderFor(company) {
            this.orderData.orderFor = company;
            this.showOrderForDropdown = false;
        },
        validateTimeInput(event, field) {
            const now = new Date();
            const inputTime = event.target.value;
            const [inputHours, inputMinutes] = inputTime.split(':').map(Number);
            const inputDate = new Date();
            inputDate.setHours(inputHours, inputMinutes, 0, 0);

            const isStartField = field === 'twStart';

            if (inputMinutes % 30 !== 0) {
                if (isStartField) {
                    this.formErrors.twStartMinute = 'Minutes must be 00 or 30.';
                } else {
                    this.formErrors.twEndMinute = 'Minutes must be 00 or 30.';
                }
            } else {
                if (isStartField) {
                    delete this.formErrors.twStartMinute;
                } else {
                    delete this.formErrors.twEndMinute;
                }
            }

            // Re-validate all time-related fields after any time input changes
            this.validateTime();
        }
    }
}
</script>