import { defineStore } from "pinia";
import router from "@/router";

async function authFetch(url, options = {}) {
  const authStore = useAuthStore();

  const headers = {
    "Content-Type": "application/json",
    ...options.headers,
  };

  const response = await fetch(`${API_BASE_URL}${url}`, {
    ...options,
    headers: headers,
    credentials: "include",
  });

  if (response.status === 401 || response.status === 403) {
    if (authStore.isAuthenticated) {
      console.warn("Unauthorized or Forbidden. Logging out current user.");
      authStore.logout();
      await router.push("/login");
    } else if (!router.currentRoute.value.path.includes("/login")) {
      console.warn(
        "Attempted to access protected route without authentication. Redirecting to login."
      );
      await router.push("/login");
    }
    throw new Error("Authentication  error.");
  }

  if (!response.ok) {
    const errorBody = await response
      .json()
      .catch(() => ({ message: "Unknown error" }));
    throw new Error(
      errorBody.message ||
        `API error: ${response.status} ${response.statusText}`
    );
  }

  return response;
}

export const useAuthStore = defineStore("auth", {
  state: () => ({
    user: (() => {
      try {
        const rawUser = sessionStorage.getItem("user_data");
        return rawUser && rawUser !== "undefined" ? JSON.parse(rawUser) : null;
      } catch (e) {
        console.warn("Failed to parse user_data:", e);
        return null;
      }
    })(),
  }),
  getters: {
    isAuthenticated: (state) => state.user,
    getUser: (state) => state.user,
    hasRole: (state) => (role) => {
      return state.user && state.user.roles && state.user.role.includes(role);
    },
    isAdmin: (state) =>
      state.user && state.user.role && state.user.role.string === "ADMIN",
    isUser: (state) =>
      state.user && state.user.role && state.user.role.string === "COMPANY",
  },
  actions: {
    async login(credentials) {
      try {
        const response = await fetch(`api/auth/login`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(credentials),
          credentials: "include",
        });

        const data = await response.json();

        if (!response.ok) {
          throw new Error(data.error || "Login failed due to server error.");
        }

        this.user = data;
        sessionStorage.setItem("user_data", JSON.stringify(this.user));
        return true;
      } catch (error) {
        this.logout();
        throw error;
      }
    },
    logout() {
      this.token = null;
      this.user = null;
      sessionStorage.removeItem("jwt_token");
      sessionStorage.removeItem("user_data");
    },
    initializeAuth() {
      if (this.token && this.user) {
        console.log(
          "Auth store initialized with existing token and user data."
        );
      }
    },
  },
});

// Export the authFetch helper for other components/stores to use
export { authFetch };
