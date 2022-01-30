<script lang="ts">
  import LayoutGrid, { Cell as LayoutCell } from "@smui/layout-grid";
  import DataTable, {
    Head,
    Body,
    Row,
    Cell,
    Pagination,
    SortValue,
  } from "@smui/data-table";
  import Select, { Option } from "@smui/select";
  import IconButton from "@smui/icon-button";
  import { Label } from "@smui/common";

  import type { SwitchResponse } from "@/interfaces/switch";

  let rowsPerPage = 10;
  let currentPage = 0;

  $: start = currentPage * rowsPerPage;
  $: end = Math.min(start + rowsPerPage, switches.length);
  $: slice = switches.slice(start, end);
  $: lastPage = Math.max(Math.ceil(switches.length / rowsPerPage) - 1, 0);

  $: if (currentPage > lastPage) {
    currentPage = lastPage;
  }

  let sort: keyof SwitchResponse = "name";
  let sortDirection: Lowercase<keyof typeof SortValue> = "ascending";
  const handleSort = () => {
    switches.sort((a: SwitchResponse, b) => {
      const [aVal, bVal] = [a[sort], b[sort]][
        sortDirection === "ascending" ? "slice" : "reverse"
      ]();
      if (typeof aVal === "string" && typeof bVal === "string") {
        return aVal.localeCompare(bVal);
      }
      return Number(aVal) - Number(bVal);
    });
    switches = switches;
  };

  let switches: Array<SwitchResponse> = [
    {
      name: "switch1",
      ip: "192.168.1.1",
      mac: "001122334455",
      revision: "Model A",
      serial: "ABCDEFGH",
      buildShortName: "b1",
      floorNumber: 1,
      positionTop: 0.0,
      positionLeft: 1.1,
      upSwitchName: "switch2",
      upLink: "1",
    },
    {
      name: "switch2",
      ip: "192.168.1.2",
      mac: "aabbccddeeff",
      revision: "Model B",
      serial: "01234567",
      buildShortName: "b1",
      floorNumber: 2,
      positionTop: 0.0,
      positionLeft: 1.1,
      upSwitchName: "switch1",
      upLink: "1",
    },
  ];
</script>

<LayoutGrid>
  <LayoutCell span={12}>
    <DataTable
      stickyHeader
      sortable
      bind:sort
      bind:sortDirection
      on:SMUIDataTable:sorted={handleSort}
      table$aria-label="Switches list"
      style="width: 100%;"
    >
      <Head>
        <Row>
          <Cell columnId="name" style="width: 100%;">
            <Label>Name</Label>
            <IconButton class="material-icons">arrow_upward</IconButton>
          </Cell>
          <Cell columnId="mac">
            <Label>MAC</Label>
            <IconButton class="material-icons">arrow_upward</IconButton>
          </Cell>
          <Cell columnId="ip">
            <Label>IP</Label>
            <IconButton class="material-icons">arrow_upward</IconButton>
          </Cell>
          <Cell columnId="serial">
            <Label>Serial</Label>
            <IconButton class="material-icons">arrow_upward</IconButton>
          </Cell>
          <Cell columnId="buildShortName">
            <Label>Location</Label>
            <IconButton class="material-icons">arrow_upward</IconButton>
          </Cell>
          <Cell columnId="upLink">
            <Label>Uplink</Label>
            <IconButton class="material-icons">arrow_upward</IconButton>
          </Cell>
          <Cell>Actions</Cell>
        </Row>
      </Head>
      <Body>
        {#each slice as sw (sw.mac)}
          <Row>
            <Cell>{sw.name}</Cell>
            <Cell>{sw.mac}</Cell>
            <Cell>{sw.ip}</Cell>
            <Cell>{sw.serial}</Cell>
            <Cell>{sw.buildShortName}</Cell>
            <Cell>{sw.upSwitchName}</Cell>
            <Cell>actions buttons</Cell>
          </Row>
        {/each}
      </Body>

      <Pagination slot="paginate">
        <svelte:fragment slot="rowsPerPage">
          <Label>Rows Per Page</Label>
          <Select variant="outlined" bind:value={rowsPerPage} noLabel>
            <Option value={10}>10</Option>
            <Option value={25}>25</Option>
            <Option value={100}>100</Option>
          </Select>
        </svelte:fragment>
        <svelte:fragment slot="total">
          {start + 1}-{end} of {switches.length}
        </svelte:fragment>

        <IconButton
          class="material-icons"
          action="first-page"
          title="First page"
          on:click={() => (currentPage = 0)}
          disabled={currentPage === 0}>first_page</IconButton
        >
        <IconButton
          class="material-icons"
          action="prev-page"
          title="Prev page"
          on:click={() => currentPage--}
          disabled={currentPage === 0}>chevron_left</IconButton
        >
        <IconButton
          class="material-icons"
          action="next-page"
          title="Next page"
          on:click={() => currentPage++}
          disabled={currentPage === lastPage}>chevron_right</IconButton
        >
        <IconButton
          class="material-icons"
          action="last-page"
          title="Last page"
          on:click={() => (currentPage = lastPage)}
          disabled={currentPage === lastPage}>last_page</IconButton
        >
      </Pagination>
    </DataTable>
  </LayoutCell>
</LayoutGrid>
