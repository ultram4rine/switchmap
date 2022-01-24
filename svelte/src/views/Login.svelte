<script lang="ts">
  import LayoutGrid, { Cell } from "@smui/layout-grid";
  import Card, { Content, Actions, ActionButtons } from "@smui/card";
  import FormField from "@smui/form-field";
  import Textfield from "@smui/textfield";
  import Icon from "@smui/textfield/icon";
  import HelperText from "@smui/textfield/helper-text";
  import Checkbox from "@smui/checkbox";
  import Button, { Label } from "@smui/button";

  import { createForm } from "felte";
  import { validator } from "@felte/validator-zod";

  import { loginSchema } from "../validations/loginSchema";

  import type { User } from "../interfaces/user";

  const loginData = {
    username: "",
    password: "",
    rememberMe: true,
  } as User;

  let showPass = false;

  const { form, errors } = createForm({
    onSubmit: (values) => {
      console.log(values);
    },
    extend: [validator],
    validateSchema: loginSchema,
  });
</script>

<main
  style="display: flex; justify-content: center; align-items: center; height: 100%;"
>
  <Card padded variant="outlined" style="width: 80%; max-width: 550px;">
    <form use:form>
      <Content>
        <LayoutGrid>
          <Cell span={12} style="justify-self: center;">
            <h2 class="mdc-typography--headline5" style="margin: 0;">
              Log into SwitchMap
            </h2>
          </Cell>
          <Cell span={12}>
            <Textfield
              bind:value={loginData.username}
              type="text"
              name="loginData.username"
              label="Name"
              variant="outlined"
              withLeadingIcon
              style="width: 100%;"
              helperLine$style="width: 100%;"
            >
              <Icon class="material-icons" slot="leadingIcon">person</Icon>
              <HelperText validationMsg slot="helper">
                {$errors.username}
              </HelperText>
            </Textfield>
          </Cell>
          <Cell span={12}>
            <Textfield
              bind:value={loginData.password}
              type={showPass ? "text" : "password"}
              name="loginData.password"
              label="Password"
              variant="outlined"
              withLeadingIcon
              withTrailingIcon
              style="width: 100%;"
              helperLine$style="width: 100%;"
            >
              <Icon class="material-icons" slot="leadingIcon">key</Icon>
              <svelte:fragment slot="trailingIcon">
                {#if showPass}
                  <Icon
                    class="material-icons"
                    role="button"
                    on:click={() => (showPass = !showPass)}
                  >
                    visibility
                  </Icon>
                {:else}
                  <Icon
                    class="material-icons"
                    role="button"
                    on:click={() => (showPass = !showPass)}
                  >
                    visibility_off
                  </Icon>
                {/if}
              </svelte:fragment>
              <HelperText validationMsg slot="helper">
                {$errors.password}
              </HelperText>
            </Textfield>
          </Cell>
          <Cell span={12}>
            <FormField>
              <Checkbox
                bind:value={loginData.rememberMe}
                name="loginData.rememberMe"
                color="primary"
              />
              <span slot="label">Remember me</span>
            </FormField>
          </Cell>
          <Cell span={12}>
            <Button type="submit" variant="raised" style="width: 100%;">
              <Label>Sign in</Label>
            </Button>
          </Cell>
        </LayoutGrid>
      </Content>
    </form>
  </Card>
</main>
