<script lang="ts">
  import LayoutGrid, { Cell } from "@smui/layout-grid";
  import FormField from "@smui/form-field";
  import Checkbox from "@smui/checkbox";
  import Textfield from "@smui/textfield";
  import Autocomplete from "@smui-extra/autocomplete";
  import Select, { Option } from "@smui/select";

  import FormWrap from "../FormWrap.svelte";

  export let form = false;

  let communities = ["aaaaaa"];

  let retrieveFromNetData = true;
  let name = "";
  let retrieveIPFromDNS = true;
  let ip = "";
  let mac = "";
  let retrieveUpLinkFromSeens = true;
  let upSwitchName = "";
  let upLink = "";
  let retrieveTechDataFromSNMP = true;
  let revision = "";
  let serial = "";
  let snmpCommunity = communities[0];
  let needLocationFields = false;
  let buildShortName = "";
  let floorNumber: number | undefined;
</script>

<FormWrap bind:form title="Add switch" action="add">
  <LayoutGrid>
    <Cell span={12}>
      <FormField>
        <Checkbox
          bind:checked={retrieveFromNetData}
          name="retrieveFromNetData"
        />
        <span slot="label">Retrieve switch data from netdata</span>
      </FormField>
    </Cell>
    <Cell span={12}>
      <Textfield
        bind:value={name}
        name="name"
        label="Name"
        type="text"
        style="width: 100%;"
        helperLine$style="width: 100%;"
      />
    </Cell>
    {#if !retrieveFromNetData}
      <Cell span={6}>
        <FormField>
          <Checkbox bind:checked={retrieveIPFromDNS} name="retrieveIPFromDNS" />
          <span slot="label">Get IP from DNS</span>
        </FormField>
      </Cell>
      {#if !retrieveIPFromDNS}
        <Cell span={6}>
          <Textfield
            bind:value={ip}
            name="ip"
            label="IP"
            type="text"
            style="width: 100%;"
            helperLine$style="width: 100%;"
          />
        </Cell>
      {/if}
      <Cell span={12}>
        <Textfield
          bind:value={mac}
          name="mac"
          label="MAC"
          type="text"
          style="width: 100%;"
          helperLine$style="width: 100%;"
        />
      </Cell>
    {/if}
    <Cell span={12}>
      <FormField>
        <Checkbox
          bind:checked={retrieveUpLinkFromSeens}
          name="retrieveUpLinkFromSeens"
        />
        <span slot="label">Retrieve uplink from seens</span>
      </FormField>
    </Cell>
    {#if !retrieveUpLinkFromSeens}
      <Cell span={6}>
        <Autocomplete
          combobox
          options={[]}
          bind:value={upSwitchName}
          name="upSwitchName"
          label="Up switch"
          type="text"
          style="width: 100%;"
        />
      </Cell>
      <Cell span={6}>
        <Textfield
          bind:value={upLink}
          name="upLink"
          label="Up link"
          type="text"
          style="width: 100%;"
          helperLine$style="width: 100%;"
        />
      </Cell>
    {/if}
    <Cell span={12}>
      <FormField>
        <Checkbox
          bind:checked={retrieveTechDataFromSNMP}
          name="retrieveTechDataFromSNMP"
        />
        <span slot="label">Retrieve tech data from SNMP</span>
      </FormField>
    </Cell>
    {#if !retrieveTechDataFromSNMP}
      <Cell span={6}>
        <Textfield
          bind:value={revision}
          name="revision"
          label="Revision"
          type="text"
          style="width: 100%;"
          helperLine$style="width: 100%;"
        />
      </Cell>
      <Cell span={6}>
        <Textfield
          bind:value={serial}
          name="serial"
          label="Serial"
          type="text"
          style="width: 100%;"
          helperLine$style="width: 100%;"
        />
      </Cell>
    {:else}
      <Cell span={12}>
        <Select bind:value={snmpCommunity} label="SNMP community">
          {#each communities as community}
            <Option value={community}>{community}</Option>
          {/each}
        </Select>
      </Cell>
    {/if}
    {#if needLocationFields}
      <Cell span={6}>
        <Autocomplete
          combobox
          options={[]}
          bind:value={buildShortName}
          name="buildShortName"
          label="Build"
          type="text"
          style="width: 100%;"
        />
      </Cell>
      <Cell span={6}>
        <Select bind:value={floorNumber} label="Floor">
          <Option value={communities[0]} />
          {#each communities as community}
            <Option value={community}>{community}</Option>
          {/each}
        </Select>
      </Cell>
    {/if}
  </LayoutGrid>
</FormWrap>
