import { ActionContext, Module } from "vuex";
import axios from "axios";

import { config } from "@/config";
import {
  AUTH_LOGIN,
  AUTH_LOGOUT,
  AUTH_SUCCESS,
  AUTH_ERROR,
} from "@/store/actions";

interface User {
  username: string;
  password: string;
}

interface State {
  token: string;
  status: string;
  hasLoadedOnce: boolean;
}

const state: State = {
  token: localStorage.getItem("user-token") || "",
  status: "",
  hasLoadedOnce: false,
};

const getters = {
  isAuthenticated: (state: State) => {
    return !!state.token;
  },
  authStatus: (state: State) => state.status,
};

const actions = {
  [AUTH_LOGIN]: (context: ActionContext<State, any>, user: User) => {
    return new Promise((resolve, reject) => {
      context.commit(AUTH_LOGIN);
      axios
        .post(`${config.apiURL}/auth`, user)
        .then((resp: any) => {
          localStorage.setItem("user-token", resp.data);
          context.commit(AUTH_SUCCESS, resp);
          resolve(resp);
        })
        .catch((err: any) => {
          context.commit(AUTH_ERROR, err);
          localStorage.removeItem("user-token");
          reject(err);
        });
    });
  },
  [AUTH_LOGOUT]: (context: ActionContext<State, any>) => {
    return new Promise(resolve => {
      context.commit(AUTH_LOGOUT);
      localStorage.removeItem("user-token");
      resolve();
    });
  },
};

const mutations = {
  [AUTH_LOGIN]: (state: State) => {
    state.status = "loading";
  },
  [AUTH_SUCCESS]: (state: State, resp: any) => {
    state.status = "success";
    state.token = resp.data;
    state.hasLoadedOnce = true;
  },
  [AUTH_ERROR]: (state: State) => {
    state.status = "error";
    state.hasLoadedOnce = true;
  },
  [AUTH_LOGOUT]: (state: State) => {
    state.token = "";
  },
};

const auth: Module<State, any> = {
  namespaced: true,
  state,
  getters,
  actions,
  mutations,
};

export default auth;
