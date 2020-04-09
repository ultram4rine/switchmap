<template>
  <div id="home">
    <div v-for="(build, i) in builds" :key="i">
      <v-card class="d-inline-block mx-auto" max-width="344" outlined>
        <v-list-item two-line>
          <v-list-item-content>
            <v-list-item-title class="headline mb-1">{{ build.name }}</v-list-item-title>
            <v-list-item-subtitle>{{ build.floors }} floors, {{ build.switches }} switches</v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>

        <v-card-actions>
          <v-btn text>Go</v-btn>
        </v-card-actions>
      </v-card>
    </div>

    <v-card class="d-inline-block mx-auto" max-width="344" outlined>
      <v-list-item two-line>
        <v-list-item-content>
          <v-list-item-title class="headline mb-1">New build</v-list-item-title>
        </v-list-item-content>
      </v-list-item>
    </v-card>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      builds: null,
      endpoint: "http://localhost:8080/builds"
    };
  },

  created() {
    this.getAllBuilds();
  },

  methods: {
    getAllBuilds() {
      axios
        .get(this.endpoint, { crossDomain: true })
        .then(response => {
          this.builds = response.data;
        })
        .catch(error => {
          console.log("-----error-------");
          console.log(error);
        });
    }
  }
};
</script>