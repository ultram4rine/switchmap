<template>
  <v-dialog :value="form" persistent max-width="500px">
    <v-card dark>
      <v-toolbar>
        <v-toolbar-title>{{ title }}</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn icon @click="close">
          <v-icon>{{ mdiClose }}</v-icon>
        </v-btn>
      </v-toolbar>

      <ValidationObserver ref="observer" v-slot="{ invalid }">
        <v-card-text>
          <v-form ref="form">
            <ValidationProvider v-slot="{ errors }" name="Name" rules="required">
              <v-text-field
                v-model="inputBuild.name"
                :error-messages="errors"
                label="Name"
                required
                color="orange accent-2"
              ></v-text-field>
            </ValidationProvider>

            <ValidationProvider v-slot="{ errors }" name="Short name" rules="required">
              <v-text-field
                v-model="inputBuild.shortName"
                :error-messages="errors"
                label="Short name"
                required
                color="orange accent-2"
              ></v-text-field>
            </ValidationProvider>
          </v-form>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="orange darken-1" :disabled="invalid" @click="submit">{{ action }}</v-btn>
        </v-card-actions>
      </ValidationObserver>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue, { PropType } from "vue";
import { mdiClose } from "@mdi/js";

import { Build } from "@/interfaces";

import { ValidationObserver, ValidationProvider, extend } from "vee-validate";
import { required } from "vee-validate/dist/rules";

extend("required", {
  ...required,
  message: "{_field_} is required"
});

export default Vue.extend({
  props: {
    form: { type: Boolean, required: true },
    action: { type: String, required: true },

    build: { type: Object as PropType<Build>, required: true }
  },

  components: { ValidationObserver, ValidationProvider },

  data() {
    return {
      mdiClose: mdiClose,

      inputBuild: this.build
    };
  },

  computed: {
    title: function() {
      if (this.action == "Add") return "New build";
      else if (this.action == "Change") return "Change build";
    }
  },

  watch: {
    build: function(newBuild: Build) {
      this.inputBuild = newBuild;
    }
  },

  methods: {
    submit() {
      this.$emit("submit", this.inputBuild);
    },
    close() {
      this.$emit("close");
    }
  }
});
</script>