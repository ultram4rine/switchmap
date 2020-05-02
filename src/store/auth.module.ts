import { ActionContext, Commit, Module } from "Vuex";
import axios from "axios";

import { AUTH_LOGIN, AUTH_LOGOUT, AUTH_SUCCESS, AUTH_ERROR } from "./actions";

interface User {
  username: string;
  password: string;
}

interface AuthResp {
  token: string;
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
  isAuthenticated: (state: State) => !!state.token,
  authStatus: (state: State) => state.status,
};

const actions = {
  [AUTH_LOGIN]: (context: ActionContext<State, any>, user: User) => {
    return new Promise((resolve, reject) => {
      context.commit(AUTH_LOGIN);
      axios
        .post("http://localhost:8080/auth", user)
        .then((resp: any) => {
          localStorage.setItem("user-token", resp.data.token);
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
    return new Promise((resolve) => {
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
    state.token = resp.token;
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
  state,
  getters,
  actions,
  mutations,
};

export default auth;
