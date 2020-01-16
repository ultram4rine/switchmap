$.get("/getmap", resp => {
  let map = new Map(Object.entries(resp));

  let container = document.getElementById("viz");
  let nodes = new vis.DataSet();
  let edges = new vis.DataSet();

  map.forEach((value, key) => {
    nodes.add([{ id: key, label: key }]);
    if (value !== null) {
      value.forEach(val => {
        edges.add([{ from: key, to: val }]);
      });
    }
  });

  let data = {
    nodes: nodes,
    edges: edges
  };
  let options = {
    physics: { enabled: false },
    nodes: { physics: false },
    edges: {
      arrows: { middle: { enabled: true } },
      color: { color: "#181616" }
    }
  };

  options.nodes = {
    color: "rgb(255, 140, 0)",
    font: { color: "#181616", face: "sans-serif" },
    shape: "box"
  };

  let network = new vis.Network(container, data, options);
});
