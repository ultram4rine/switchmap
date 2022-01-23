import { ref, Ref } from "@vue/composition-api";

import { wsURL } from "@/api";

export const useWebSocket = (
  shortName: string,
  floor: number
): {
  wsState: Ref<"loading" | "opened" | "closed" | "errored">;
  sendSock: WebSocket;
  receiveSock: WebSocket;
} => {
  const wsState: Ref<"loading" | "opened" | "closed" | "errored"> =
    ref("loading");

  const sendSock = new WebSocket(wsURL(shortName, floor) + "/send");
  sendSock.onopen = () => {
    wsState.value = "opened";
    console.log("opened send");
  };
  sendSock.onerror = () => {
    wsState.value = "errored";
    console.log("errored send");
  };
  sendSock.onclose = () => {
    wsState.value = "closed";
    console.log("closing send");
  };

  const receiveSock = new WebSocket(wsURL(shortName, floor) + "/receive");
  receiveSock.onopen = () => {
    wsState.value = "opened";
    console.log("opened receive");
  };
  receiveSock.onerror = () => {
    wsState.value = "errored";
    console.log("errored receive");
  };
  receiveSock.onclose = () => {
    wsState.value = "closed";
    console.log("closing receive");
  };

  return {
    wsState,
    sendSock,
    receiveSock,
  };
};
