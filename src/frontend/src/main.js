import { createApp } from 'vue'
import { createPinia } from 'pinia';
import App from './App.vue'
import router from './router'
import { useAuthStore } from '@/stores/auth';

const app = createApp(App)
const pinia = createPinia();

app.use(pinia);
app.use(router)


router.isReady().then(() => {
    app.mount('#app')
    const authStore = useAuthStore();
    authStore.initializeAuth();

});
// Vue.config.productionTip = false;

// new Vue({
//     el: '#app',
//     router,
//     template: '<App/>',
//     components: {App}
// })