import { defineStore } from "pinia";

import { login, logout } from "@/api/auth";
import type { User } from "@/interfaces/user";

export const useAuth = defineStore("auth", {
  state: () => {
    return {
      token: localStorage.getItem("token") || "",
      username: localStorage.getItem("username") || "",
    };
  },

  getters: {
    getLoggedIn: (state) => !!state.token,
    getUsername: (state) => state.username,
  },

  actions: {
    async login(user: User) {
      try {
        await login(user.username, user.password, user.rememberMe);
        this.token = localStorage.getItem("token") || "";
        this.username = user.username;
        localStorage.setItem("username", user.username);
      } catch (err) {
        localStorage.removeItem("token");
      }
    },

    async logout() {
      localStorage.removeItem("username");
      await logout();
    },
  },
});
