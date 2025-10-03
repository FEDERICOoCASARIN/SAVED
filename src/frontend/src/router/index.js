// src/router/index.js
import LoginForm from "@/components/LoginForm.vue";
import DashboardPage from "@/views/DashboardPage.vue";
import RegistrationRouter from "@/components/RegistrationRouter.vue";
import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const routes = [
  {
    path: "/login",
    name: "Login",
    component: LoginForm,
    meta: { requiresAuth: false },
  },
  {
    path: "/",
    name: "Dashboard",
    component: DashboardPage,
    meta: { requiresAuth: true },
  },
  {
    path: "/register/:token",
    name: "Registration",
    component: RegistrationRouter,
    meta: { requiresAuth: false },
  },
  // // You might want a 404 catch-all
  // {
  //     path: '/:pathMatch(.*)*',
  //     name: 'NotFound',
  //     component: () => import('@/views/NotFoundView.vue'),
  // }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      next({ name: "Login" });
    } else {
      next();
    }
  } else {
    if (to.name === "Login" && authStore.isAuthenticated) {
      next({ name: "Dashboard" });
    } else {
      next();
    }
  }
});

export default router;
