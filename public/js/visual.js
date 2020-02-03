import "/public/node_modules/jquery/dist/jquery.min.js";
import "/public/node_modules/vis-network/dist/vis-network.min.js";

$.ajax({
  url: "/getmap",
  success: resp => {
    let container = document.getElementById("viz");
    let nodes = new vis.DataSet();
    let edges = new vis.DataSet();

    resp.forEach(val => {
      nodes.add([{ id: val.Name, label: val.Name }]);
      edges.add([{ from: val.Upswitch, to: val.Name, label: val.Port }]);
    });

    let data = {
      nodes: nodes,
      edges: edges
    };
    let options = {
      physics: { enabled: false },
      nodes: { physics: false },
      edges: {
        arrows: { to: { enabled: true } },
        color: { color: "#181616" }
      }
    };

    options.nodes = {
      color: "rgb(255, 140, 0)",
      font: { color: "#181616", face: "sans-serif" },
      shape: "box"
    };

    let network = new vis.Network(container, data, options);
  },
  error: jqXHR => {
    alert(jqXHR.responseText);
  }
});
