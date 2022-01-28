import { macNormalization, colorizeDiff } from "@/helpers";

describe("MAC address normalization", () => {
  test("'00:11:22:33:44:55' become '001122334455'", () => {
    expect(macNormalization("00:11:22:33:44:55")).toBe("001122334455");
  });

  test("'AA-BB-CC-DD-EE-FF' become 'aabbccddeeff'", () => {
    expect(macNormalization("AA-BB-CC-DD-EE-FF")).toBe("aabbccddeeff");
  });

  test("'123456789ABC' become '123456789abc'", () => {
    expect(macNormalization("123456789ABC")).toBe("123456789abc");
  });
});

describe("Colorize diff", () => {
  test("green and red", () => {
    expect(
      colorizeDiff("Serial: 00\x1b[31m11\x1b[0m22 -> 00\x1b[32m22\x1b[0m22")
    ).toBe(
      "Serial: 00<span style='color: #cd0000;'>11</span>22 -> 00<span style='color: #00cd00;'>22</span>22"
    );
  });
});
