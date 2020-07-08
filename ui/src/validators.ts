const isMAC = () => {
  const regexp = /^[a-fA-F0-9:]{17}|[a-fA-F0-9]{12}$/g;
  return (input: string) =>
    regexp.test(input) ? null : "Please enter a valid MAC address";
};

const isIP = () => {
  const regexp = /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$/gm;
  return (input: string) =>
    regexp.test(input) ? null : "Please enter a valid IP address";
};

export { isMAC, isIP };
