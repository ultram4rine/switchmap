import { ActionContext } from "vuex";

import {
  AUTH_LOGIN,
  AUTH_LOGOUT,
  AUTH_SUCCESS,
  AUTH_ERROR,
} from "@/store/actions";
import { login, logout } from "@/api/auth";
import { User } from "@/interfaces/user";

type State = {
  token: string;
  username: string;
  status: string;
  hasLoadedOnce: boolean;
};

export const state: State = {
  token: localStorage.getItem("token") || "",
  username: localStorage.getItem("username") || "",
  status: "",
  hasLoadedOnce: false,
};

export const getters = {
  getLoggedIn: (state: State): boolean => !!state.token,
  getUsername: (state: State): string => state.username,
  getStatus: (state: State): string => state.status,
};

export const actions = {
  [AUTH_LOGIN]: (
    context: ActionContext<State, State>,
    user: User
  ): Promise<void> => {
    return new Promise((resolve, reject) => {
      context.commit(AUTH_LOGIN);
      try {
        login(user.username, user.password, user.rememberMe).then((resp) => {
          localStorage.setItem("username", user.username);
          context.commit(AUTH_SUCCESS, user);
          resolve(resp);
        });
      } catch (err) {
        context.commit(AUTH_ERROR, err);
        localStorage.removeItem("token");
        reject(err);
      }
    });
  },
  [AUTH_LOGOUT]: (context: ActionContext<State, State>): Promise<void> => {
    return new Promise((resolve) => {
      context.commit(AUTH_LOGOUT);
      localStorage.removeItem("username");
      logout().then(() => resolve());
    });
  },
};

export const mutations = {
  [AUTH_LOGIN]: (state: State): void => {
    state.status = "loading";
  },
  [AUTH_SUCCESS]: (state: State, username: string): void => {
    state.status = "success";
    state.token = localStorage.getItem("token") || "";
    state.username = username;
    state.hasLoadedOnce = true;
  },
  [AUTH_ERROR]: (state: State): void => {
    state.status = "error";
    state.hasLoadedOnce = true;
  },
  [AUTH_LOGOUT]: (state: State): void => {
    state.token = "";
  },
};
