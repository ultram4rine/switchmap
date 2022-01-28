<template>
  <div id="switches">
    <v-card>
      <v-toolbar dark flat>
        <v-toolbar-title>Switches</v-toolbar-title>
        <v-divider class="mx-4" inset vertical></v-divider>
        <v-tooltip bottom>
          <template v-slot:activator="{ on, attrs }">
            <v-btn
              text
              icon
              @click="handleSyncSwitches"
              color="orange accent-2"
              v-bind="attrs"
              v-on="on"
            >
              <v-icon>{{ mdiSync }}</v-icon>
            </v-btn>
          </template>
          <span>Sync switches</span>
        </v-tooltip>
        <v-spacer></v-spacer>
        <v-text-field
          v-model="search"
          :append-icon="this.mdiMagnify"
          label="Search"
          color="orange accent-2"
          single-line
          hide-details
        ></v-text-field>
      </v-toolbar>
      <v-data-table
        :headers="headers"
        :items="switches"
        :items-per-page="10"
        :search="search"
        sort-by="name"
        multi-sort
        class="elevation-1"
      >
        <template v-slot:[`item.actions`]="{ item }">
          <v-tooltip bottom>
            <template v-slot:activator="{ on, attrs }">
              <v-btn
                icon
                small
                @click="openSwitchForm('Edit', item)"
                v-bind="attrs"
                v-on="on"
              >
                <v-icon small>
                  {{ mdiPencil }}
                </v-icon>
              </v-btn>
            </template>
            <span>Edit</span>
          </v-tooltip>
          <v-tooltip bottom>
            <template v-slot:activator="{ on, attrs }">
              <v-btn
                icon
                small
                @click="handleDelete(item)"
                v-bind="attrs"
                v-on="on"
              >
                <v-icon small>
                  {{ mdiDelete }}
                </v-icon>
              </v-btn>
            </template>
            <span>Delete</span>
          </v-tooltip>
          <v-tooltip bottom>
            <template v-slot:activator="{ on, attrs }">
              <v-btn
                icon
                small
                v-if="item.floorNumber"
                :to="{
                  name: 'floor',
                  params: {
                    shortName: item.buildShortName,
                    floor: item.floorNumber,
                  },
                }"
                v-bind="attrs"
                v-on="on"
              >
                <v-icon small>{{ mdiEye }}</v-icon>
              </v-btn>
            </template>
            <span>View</span>
          </v-tooltip>
        </template>
      </v-data-table>
    </v-card>

    <v-row no-gutters>
      <v-card :style="{ visibility: 'hidden' }" class="ma-1">
        <v-btn rounded large></v-btn>
      </v-card>
    </v-row>

    <v-btn
      rounded
      large
      fixed
      bottom
      right
      color="orange accent-4"
      class="white--text"
      @click="openSwitchForm('Add')"
    >
      Add switch
    </v-btn>

    <switch-form
      :form="switchForm"
      :action="switchFormAction"
      :needLocationFields="true"
      :swit="sw"
      @submit="handleSubmitSwitch"
      @close="closeSwitchForm"
    />

    <delete-confirmation
      :confirmation="deleteConfirmation"
      :name="deleteItemName"
      @confirm="confirmDelete"
      @cancel="deleteCancel"
    />

    <snackbar-notification
      :snackbar="snackbar"
      :type="snackbarType"
      :text="snackbarText"
      @close="closeSnackbar"
    />

    <v-dialog :value="showDiff" persistent scrollable>
      <v-card dark>
        <v-toolbar>
          <v-toolbar-title>Sync diff</v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon @click="closeDiff">
            <v-icon>{{ mdiClose }}</v-icon>
          </v-btn>
        </v-toolbar>
        <v-card-text>
          <div class="code">{{ diff }}</div>
        </v-card-text>
      </v-card>
    </v-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, Ref, ref } from "@vue/composition-api";
import {
  mdiSync,
  mdiMagnify,
  mdiPencil,
  mdiDelete,
  mdiEye,
  mdiClose,
} from "@mdi/js";

import SwitchForm from "@/components/forms/SwitchForm.vue";
import DeleteConfirmation from "@/components/DeleteConfirmation.vue";
import SnackbarNotification from "@/components/SnackbarNotification.vue";

import { SwitchRequest, SwitchResponse } from "@/interfaces/switch";
import { syncSwitches, getSwitches, deleteSwitch } from "@/api/switches";

import {
  useSwitchForm,
  useDeleteConfirmation,
  useSnackbar,
} from "@/composables";

type TableSwitch = SwitchResponse & {
  location: string;
  uplink: string;
};

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true },
  },

  components: {
    SwitchForm,
    DeleteConfirmation,
    SnackbarNotification,
  },

  setup() {
    const switches: Ref<TableSwitch[]> = ref([]);

    const {
      form: switchForm,
      formAction: switchFormAction,
      sw,
      openForm: openSwitchForm,
      submitForm: submitSwitchForm,
      closeForm: closeSwitchForm,
    } = useSwitchForm();

    const {
      deleteConfirmation,
      deleteItemName,
      confirm: deleteConfirm,
      cancel: deleteCancel,
    } = useDeleteConfirmation();

    const {
      snackbar,
      snackbarType,
      text: snackbarText,
      open: openSnackbar,
      close: closeSnackbar,
    } = useSnackbar();

    const search = ref("");
    const headers = ref([
      {
        text: "Name",
        align: "start",
        value: "name",
      },
      { text: "MAC", value: "mac" },
      { text: "IP", value: "ip" },
      { text: "Serial", value: "serial" },
      { text: "Location", value: "location" },
      { text: "Uplink", value: "uplink" },
      { text: "Actions", value: "actions", sortable: false },
    ]);

    const showDiff = ref(false);
    const diff = ref("");

    return {
      switches,

      // Switch form.
      switchForm,
      switchFormAction,
      sw,
      openSwitchForm,
      submitSwitchForm,
      closeSwitchForm,

      // Delete confirmation.
      deleteConfirmation,
      deleteItemName,
      deleteConfirm,
      deleteCancel,

      // Snackbar.
      snackbar,
      snackbarType,
      snackbarText,
      openSnackbar,
      closeSnackbar,

      search,
      headers,

      showDiff,
      diff,

      mdiSync,
      mdiMagnify,
      mdiPencil,
      mdiDelete,
      mdiEye,
      mdiClose,
    };
  },

  methods: {
    async displaySwitches() {
      const sws = await getSwitches();
      this.switches = sws.map((sw) => {
        sw.mac = sw.mac.toUpperCase();
        let tableSwitch = sw as TableSwitch;
        tableSwitch.location =
          (sw.buildShortName ? `${sw.buildShortName}` : "") +
          (sw.floorNumber === null ? "" : `f${sw.floorNumber}`);
        tableSwitch.uplink =
          sw.upSwitchName && sw.upLink
            ? `${sw.upSwitchName} (${sw.upLink})`
            : "";
        return tableSwitch;
      });
    },

    handleDelete(sw: TableSwitch) {
      this.deleteItemName = sw.name;
      this.deleteConfirmation = true;
    },

    async confirmDelete() {
      await this.deleteConfirm(
        () => deleteSwitch(this.deleteItemName),
        () => {
          this.openSnackbar("success", `${this.deleteItemName} deleted`);
          this.displaySwitches();
        },
        () =>
          this.openSnackbar("error", `Failed to delete ${this.deleteItemName}`)
      );
    },

    async handleSubmitSwitch(swit: SwitchRequest, action: "Add" | "Edit") {
      try {
        const sr = await this.submitSwitchForm(swit, action);
        this.displaySwitches();

        let typ: "success" | "info" | "warning" | "error" = "success";
        let text = "";

        if (sr.seen && sr.snmp) {
          typ = "success";
          text = `${sr.sw.name} succesfully ${action.toLowerCase()}ed`;
        } else if (!sr.seen && !sr.snmp) {
          typ = "warning";
          text = `Failed to get uplink and technical data of ${sr.sw.name}`;
        } else if (!sr.seen) {
          typ = "warning";
          text = `Failed to get uplink of ${sr.sw.name}`;
        } else if (!sr.snmp) {
          typ = "warning";
          text = `Failed to get technical data of ${sr.sw.name}`;
        }

        this.openSnackbar(typ, text);
      } catch (err: unknown) {
        this.openSnackbar("error", `Failed to ${action.toLowerCase()} switch`);
      }
    },

    async handleSyncSwitches() {
      try {
        const diff = await syncSwitches();
        this.showDiff = true;
        this.diff = diff;
      } catch (err: unknown) {
        this.openSnackbar("error", `Failed to sync switches`);
      }
    },

    closeDiff() {
      this.showDiff = false;
    },
  },

  created() {
    this.displaySwitches();
  },
});
</script>

<style>
.code {
  white-space: pre;

  font-family: "Roboto Mono", monospace;
}
</style>
