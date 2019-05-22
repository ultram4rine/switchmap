$.get("/getmap", function(data){
    map = new Map(Object.entries(data))
    var container = document.getElementById('viz');
    var nodes = new vis.DataSet();
    var edges = new vis.DataSet();
    map.forEach(function(value, key) {
        nodes.add([{id: key, label: key}])
    });
    map.forEach(function(value, key) {
        if (value != null) {
            value.forEach(function(val) {
                edges.add([{from: key, to: val}])
            })
        }
    });
    var data = {
        nodes: nodes,
        edges: edges
    };
    var options = {
        physics: {enabled: false},
        nodes: {physics: false},
        edges: {arrows: {middle: {enabled: true}}, color: {color: '#181616'}}
    };
    options.nodes = {
        color: 'rgb(255, 140, 0)',
        font: {color: '#181616', face: 'sans-serif'},
        shape: 'box'
    }
    var network = new vis.Network(container, data, options);
})