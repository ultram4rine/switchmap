import "@testing-library/jest-dom";
import VueCompositionAPI from "@vue/composition-api";

import { routes } from "@/router";
import BuildCard from "@/components/cards/BuildCard.vue";
import { BuildResponse } from "@/interfaces/build";

import { renderWithVuetify } from "../../../common";

describe("BuildCard", () => {
  it("makes sure the build card is rendered", () => {
    const build = {
      name: "Build 1",
      shortName: "b1",
      floorsNumber: 2,
      switchesNumber: 2,
    } as BuildResponse;

    const { getByText } = renderWithVuetify(
      BuildCard,
      {
        routes,
        props: { build },
      },
      (vue: any) => vue.use(VueCompositionAPI)
    );

    expect(getByText(build.name)).toBeInTheDocument();
    expect(
      getByText(
        `${build.floorsNumber.toString()} floors, ${build.switchesNumber.toString()} switches`
      )
    ).toBeInTheDocument();
  });
});
