<template>
    <div 
        :class="{
            'bg-[#f7f7f7]': !isActive,         
            'bg-[#f7f7f7] shadow-xl ring-3 ring-[#FA812F] transform scale-102': isActive
        }"
        class="flex min-h-[25vh] rounded-md justify-center font-bold cursor-pointer
               transition-all duration-300 ease-in-out hover:scale-101 hover:shadow-lg p-4"
        @click="$emit('select')"
        tabindex="0"
        role="button"
    >
    <!-- Shared orders -->
        <div v-if="order.orders.length > 1" class="flex flex-col gap-2 w-full ju">
            <div class="flex flex-row justify-between items-center">
                <div class="flex">
                    <span class="text-2xl">Shared Order 
                        <span class="text-gray-400">
                            ({{ order.orders.length }})
                        </span>
                    </span>
                </div>
                <div class="flex text-green-600">
                     <EarthSVG />
                </div>
            </div>
            <div class="flex border-1 rounded-xl border-gray-400 p-1" v-for="(order, index) in order.orders">
                    <span class="text-xl">Order #{{ order.orderId }}</span>
                    
            </div>
            
        </div>
        <!-- Non-shared orders -->
        <div v-else class="flex flex-col gap-2 w-full justify-center">
            <div class="flex justify-between items-center">
                <span class="text-2xl font-bold">Order #{{ order.orders[0].orderId }}</span>
                <span class="text-sm text-gray-500">{{ order.orders[0].time }}</span>
            </div>
            <div class="flex flex-col gap-1">
                <div class="text-sm text-gray-600">From: 
                    <span class="font-bold uppercase">
                    {{ order.orders[0].source }}
                    </span></div>
                <div class="text-sm text-gray-600">To: 
                    <span class="font-bold uppercase">
                        {{ order.orders[0].destination }}
                    </span></div>
            </div>
            <div class="flex justify-between items-center mt-10">
                <span class="text-sm text-white uppercase font-bold p-2 px-3 rounded-lg" 
                     :class="{
                    'bg-[#6B7280]': order.orders[0].status === 'created',
                    'bg-[#3B82F6]': order.orders[0].status === 'scheduled',
                    'bg-[#F97316]': order.orders[0].status === 'undergoing',
                    'bg-[#10B981]': order.orders[0].status === 'completed',
                    'bg-[#EF4444]': order.orders[0].status === 'canceled',

                  }">
                    {{ order.orders[0].status }}
                </span>
                <span class="text-sm text-gray-500">ETA: {{ convertDateToTime(order.orders[0].eta) || 'N/A'}}</span>
            </div>
            <div v-if=order.orders[0].shared class="flex gap-2 border-1 rounded-xl border-gray-200 p-1">
                    <span class="flex text-xl text-green-600 items-center gap-1"> 
                        <EarthSVG />
                        Shared
                    </span>
                       
            </div>
        </div>
    </div>
</template>

<script>
import EarthSVG from "@/components/svg/EarthSVG.vue";

export default {
    name: 'LiveList',
    props: {
        order: {
            type: Object,
            required: true
        },
        isActive: {
            type: Boolean,
            default: false
        }
    },
    methods:{
        /**
         * Converts a date-time string to just the time part (e.g., "HH:MM").     .
         */
        convertDateToTime(dateTimeString) {
            if (!dateTimeString) return '';
            console.log(dateTimeString)
            try {
                const timePart = dateTimeString.split(', ')[1];
                return timePart ? timePart.slice(0, 5) : '';
            } catch (error) {
                console.error('Error converting date to time:', error);
                return '';
            }
        }
    },
    components: {
        EarthSVG
    }
}
</script>