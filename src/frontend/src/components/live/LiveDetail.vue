<template>
    <div class="flex relative flex-col h-full bg-white rounded-xl ">
       <div class="absolute -top-8 left-4 flex z-10 items-center justify-center">
            <div
            v-for="(order, index) in orderDetails.orders"
            :key="index"
            @click="updateSelectedIndex(index)"
            class="flex"
            :class="[
                'cursor-pointer px-4 py-1 min-w-40 rounded-t-lg shadow p-2 transition-colors duration-300',
                selectedIndex === index
                ? 'bg-[#F97316] text-white font-bold'
                : 'bg-gray-200 hover:bg-gray-300'
            ]"
            >
            Order #{{ orderDetails.orders[index].orderId }}
            </div>
        </div>

        <!-- header -->
        <div class="flex flex-col  h-full p-3  border-3 border-gray-300 rounded-lg">
            <div class="flex justify-between items-center px-2 ">
                <h2 class="text-2xl text-gray-800">Order Details <span class="font-bold"> #{{ orderDetails.orders[selectedIndex].orderId }} </span></h2>
                <div class="px-4 py-1 rounded-full">
                     <span class="text-sm text-white uppercase font-bold p-2 px-4 rounded-lg" 
                         :class="{
                        'bg-[#6B7280]': orderDetails.orders[selectedIndex].status === 'created',
                        'bg-[#3B82F6]': orderDetails.orders[selectedIndex].status === 'scheduled',
                        'bg-[#F97316]': orderDetails.orders[selectedIndex].status === 'undergoing',
                        'bg-[#10B981]': orderDetails.orders[selectedIndex].status === 'completed',
                        'bg-[#EF4444]': orderDetails.orders[selectedIndex].status === 'canceled',
    
                      }">
                      {{ orderDetails.orders[selectedIndex].status }}
                    </span> 
                </div>
            </div>
            <div class="w-full mx-auto p-4 flex  gap-[10%] flex-row flex justify-between">

                <!-- Right column -->
                <div class=" min-w-25 flex flex-col justify-between">
                <div class="detail-group">
                    <label class="text-sm text-gray-500">Container ID</label>
                    <p class="text-lg font-medium">{{ orderDetails.orders[selectedIndex].containerId ?  orderDetails.orders[selectedIndex].containerId : 'TBD' }}</p>
                </div>
                <div class="detail-group">
                    <label class="text-sm text-gray-500">Opt. Type</label>
                    <p class="text-lg font-medium uppercase">{{ orderDetails.orders[selectedIndex].operationType }}</p>
                </div>
                <div class="detail-group">
                    <label class="text-sm text-gray-500">Weight</label>
                    <p class="text-lg font-medium">{{ orderDetails.orders[selectedIndex].freightWeight || 0 }} kg</p>
                </div>
                </div>

                <!-- Middle column -->
                <div class="space-y-4 flex-1 flex flex-col min-w-[20%]">
                <!-- Updated Time Window section with separate slots for Start and End -->
                <div class="detail-group">
                    <label class="text-sm text-gray-500 mb-2">Time Window</label>
                    <div class="flex flex-col space-y-2">
                        <div>
                            <label class="text-xs text-gray-400">Start Time</label>
                            <p class="text-lg font-medium">{{ convertDateToTime(orderDetails.orders[selectedIndex].twStart )|| 'TBD' }}</p>
                        </div>
                        <div>
                            <label class="text-xs text-gray-400">End Time</label>
                            <p class="text-lg font-medium">{{ convertDateToTime(orderDetails.orders[selectedIndex].twEnd) || 'TBD' }}</p>
                        </div>
                    </div>
                </div>
                <!-- End -->
    
                <div class="detail-group">
                    <label class="text-sm text-gray-500">Freight Value</label>
                    <p class="text-lg font-medium">$ {{ orderDetails.orders[selectedIndex].freightValue || 0}}</p>
                </div>

                </div>

                <!-- Container image -->
                <div class="relative flex flex-col justify-between max-w-[500px]">
                    <div class="flex relative w-full  items-center">
                        <img src="../../assets/container.png" alt="Container image" class="object-contain" />
                        <div
                            class="absolute inset-0 flex justify-center items-center transition-all duration-700 ease-out"
                            :class="weightProportion !== 0 ? 'bg-green-500/70' : 'bg-red-500/70'"
                            :style="{ width: weightProportion !== 0 ? formattedWeightProportion : '100%' }"
                        >
                            <p class="text-white text-xl font-bold">
                                {{ weightProportion !== 0 ? formattedWeightProportion : 'EMPTY' }}
                            </p>
                        </div>
                    </div>
                    <div class="detail-group flex justify-end ">
                        <p class="text-sm text-gray-400">Created by: {{ orderDetails.orders[selectedIndex].requester || 0}}</p>
                    </div>
                </div>  
            </div>
        </div>
    </div>
</template>

<script>
export default {

    props: {
        orderDetails: {
            type: Object,
            default: null
        }, 
        selectedIndex: {
            type: Number,
            required: true,
        }
    },

    data() {
        return {
            totalWeight: 2500,
        }
    },

    computed: {
        /**
         * Formats the `date` property from `orderDetails` into a local date string.
         * Returns 'N/A' if the date is not available.
         */
        formattedDate() {
            return this.orderDetails?.date 
                ? new Date(this.orderDetails.date).toLocaleDateString()
                : 'N/A'
        },

        /**
         * Calculates the percentage of the current order's freight weight relative to the `totalWeight`.
         */
        weightProportion() {
            const currentFreightWeight = this.orderDetails?.orders?.[this.selectedIndex]?.freightWeight;

            if (currentFreightWeight === undefined || currentFreightWeight === null) {
                return 0;
            }

            return (Number(currentFreightWeight) / this.totalWeight) * 100;
        },
        /**
         * Formats the `weightProportion` as a rounded percentage string.
         */
        formattedWeightProportion() {
            return `${Math.round(this.weightProportion)}%`
        },
        normalizeList(){
            if(Array.isArray(this.orderDetails)){
                return this.orderDetails;
            }
            return [this.orderList]
        }
    },
    
    methods:{
        /**
         * Converts a date-time string (e.g., "DD/MM/YYYY, HH:MM:SS") to just the time part (e.g., "HH:MM")
         */
        convertDateToTime(dateTimeString) {
            if (!dateTimeString) return '';
            try {
                const timePart = dateTimeString.split(', ')[1];
                return timePart ? timePart.slice(0, 5) : '';
            } catch (error) {
                console.error('Error converting date to time:', error);
                return '';
            }
        },
         /**
         * Emits an event to update the `selectedIndex` in the parent component.
         */
        updateSelectedIndex(newValue) {
            this.$emit('update:selectedIndex', newValue);
        }
    },
    mounted(){
        console.log('order 1')
        console.log(this.orderDetails)
        console.log("order list")
        console.log(this.orderList)
    }

}
</script>
