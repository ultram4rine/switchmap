import { ActionContext, Module } from "Vuex";

interface State {
  token: string;
}

const state: State = {
  token: "",
};

const getters = {
  getToken: (state: State) => {
    return state.token;
  },
};

const actions = {
  setToken: (context: ActionContext<State, any>, token: string) => {
    context.commit("setToken", token);
  },
};

const mutations = {
  setToken: (state: State, token: string) => {
    state.token = token;
  },
};

const csrf: Module<State, any> = {
  namespaced: true,
  state,
  getters,
  actions,
  mutations,
};

export default csrf;
