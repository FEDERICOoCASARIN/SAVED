<script>
import Calendar from "@/components/calendar/Calendar.vue";
import Chat from "@/components/Chat/Chat.vue";
import Navbar from "@/components/Navbar.vue";
import Orders from "@/components/orders/Orders.vue";
import Users from "@/components/users/Users.vue";
import UserProfile from "@/components/profile/UserProfile.vue";
import AddUserPopup from "@/components/users/AddUserPopup.vue";
import EditUserProfilePopup from "@/components/users/EditUserPopup.vue";
import ViewUserPopup from "@/components/users/ViewUserPopup.vue";
import Vehicles from "@/components/vehicle/Vehicles.vue";
import Analytics from "@/components/Statistics.vue"
import Notifications from "@/components/notifications/Notifications.vue"
import Live from "@/components/live/Live.vue"
import { useAuthStore } from "@/stores/auth";
import { computed } from 'vue';
import { check } from "prettier";



export default {
	components: {
		Navbar,
		Calendar,
		Chat,
		Orders,
		Analytics,
		Users,
		UserProfile,
		AddUserPopup,
		EditUserProfilePopup,
		ViewUserPopup,
		Vehicles,
    	Notifications,
		Live
	},
	data() {
		return {
			currentTab: "Orders", 
			hasAccessToCurrentTab: true,
            // limit: 1270, real dimension for mobile view
            limit: 1000,
            isMobile: window.innerWidth < this.limit,
            currentTime: '',
            timeUpdateInterval: null
		};
	},
	setup() {
        const authStore = useAuthStore(); 
        const isAdmin = computed(() => authStore.isAdmin);
        const isUser = computed(() => authStore.isUser); 

        return {
            authStore, 
            isAdmin,
            isUser,
        };
	},
	watch: {
        currentTab(newTab) {
            this.setCurrentTabWithAccessCheck(newTab);
        },
    
    },
   
    beforeUnmount() {
    window.removeEventListener('resize', this.checkMobile)
    },
	methods: {
        checkMobile(){
            this.isMobile = window.innerWidth < this.limit
        },
        setCurrentTabWithAccessCheck(newTab) {
            const authStore = useAuthStore(); 

            let allowed = true;
            switch (newTab) {        
				case 'AddUserPopup': 
                case 'ViewUserPopup':
                case 'Vehicles':
				case 'Users':
                case 'Analytics':
					allowed = authStore.isAdmin;
                    break;
                default: 
                    allowed = true; 
                    break;
            }

            if (allowed) {
                this.currentTab = newTab;
                this.hasAccessToCurrentTab = true;
            } else {
                console.warn(`User ${authStore.getUser?.username} does not have access to ${newTab}.`);
                this.hasAccessToCurrentTab = false;
            }
        },
    // easy polling
        updateCurrentTime() {
            const now = new Date();
            this.currentTime = now.toLocaleTimeString();        
        },

        async updateOrder(){
            try {
                const result = await fetch("api/orders/activate");

                if (!result.ok) {
                    const errorBody = await result.text();
                    throw new Error(
                        `HTTP error! Status: ${result.status} - ${errorBody}`
                    );
                }
        
            } catch (err) {
                console.log("error update activate order (AddUserFunction)");
            }
        }
    },
    mounted() {
        this.setCurrentTabWithAccessCheck(this.currentTab);
        this.checkMobile()
        window.addEventListener('resize', this.checkMobile)

        this.updateCurrentTime();
        this.timeUpdateInterval = setInterval(this.updateOrder, 10000);
    },
    
};

</script>

<template>
    <main class="flex size-full">
    <div v-if="isMobile" class="w-full flex items-center justify-center text-center p-4 bg-[#E57229] text-white">
        <p class="text-lg font-semibold">Currently our mobile mode is not ready.</p>
    </div>
        <template v-else>
            <Navbar
            :currentTab="currentTab"
            :setCurrentTab="setCurrentTabWithAccessCheck"
            :isAdmin="isAdmin"       
            :isUser="isUser"
            :authStore="authStore" 
            />
            <div class="flex size-full ml-52">
                <component
                v-if="hasAccessToCurrentTab"
            :is="currentTab"
            class="flex size-full"
            ></component>
            </div>
        </template>
    </main>
</template>