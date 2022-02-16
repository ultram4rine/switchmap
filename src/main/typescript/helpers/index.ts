/* eslint-disable no-control-regex */
import { colors } from "~/ansi-colors/xterm";

export const authHeader = (): string => {
  const token = localStorage.getItem("token");
  return token ? token : "";
};

export const macNormalization = (mac: string): string => {
  if (mac.length === 17) {
    return mac.toLowerCase().replace(/:|-/g, "");
  } else {
    return mac.toLowerCase();
  }
};

export const colorizeDiff = (diff: string): string => {
  const regexReplace = /(?<=\x1b\[)(.*?)(?=m)/g;
  const regexMatch = /\x1b\[.*?m/g;

  return diff.replace(regexMatch, (substr) => {
    const maybeCode = substr.match(regexReplace);

    if (maybeCode && !isNaN(+maybeCode[0])) {
      const code = parseInt(maybeCode[0]);
      if (code === 0) {
        return "</span>";
      } else {
        const color = colors[code % 10];
        return `<span style='color: #${color};'>`;
      }
    } else {
      return substr;
    }
  });
};
