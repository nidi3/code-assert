let svg = d3.select('svg'),
    width = +svg.attr('width'),
    height = +svg.attr('height');

let color = d3.scaleOrdinal(d3.schemeCategory20);

let simulation = d3.forceSimulation()
    .force('link', d3.forceLink().id(d => d.id))
    .force('charge', d3.forceManyBody(-10))
    .force('center', d3.forceCenter(width / 2, height / 2))
    .force('collide', d3.forceCollide(10));

function startsWith(s, p) {
    return s.substring(0, p.length) === p;
}

function packFilter(pack) {
    // return pack.substring(0, 4) !== 'java' && pack.substring(0, 9) !== 'org.slf4j';
    return startsWith(pack, 'org.springframework');
}

function packOf(name) {
    return name.substring(0, name.lastIndexOf('.'));
}

// d3.json('/model?jarfile=/Users/nidi/.m2/repository/guru/nidi/code-assert/0.8.2/code-assert-0.8.2.jar', (error, model) => {
// d3.json('/model?jarfile=/Users/nidi/.m2/repository/guru/nidi/raml/raml-tester/0.8.11/raml-tester-0.8.11.jar', (error, model) => {
// d3.json('/model?jarfile=/Users/nidi/.m2/repository/guru/nidi/graphviz-java/0.2.2/graphviz-java-0.2.2.jar', (error, model) => {
// d3.json('/model?jarfile=/Users/nidi/.m2/repository/org/springframework/spring-core/3.1.3.RELEASE/spring-core-3.1.3.RELEASE.jar', (error, model) => {
d3.json('/model?jarfile=/Users/nidi/.m2/repository/org/springframework/spring-expression/4.3.10.RELEASE/spring-expression-4.3.10.RELEASE.jar', (error, model) => {
    if (error) throw error;

    let packages = {};
    let i = 0;
    model.packages.forEach(p => {
        if (packFilter(p.name)) {
            packages[p.name] = {color: color(i++)};
        }
    });
    let links = [];
    let classes = [];
    model.classes.forEach(c => {
        if (packFilter(c.package)) {
            classes.push({id: c.name, package: c.package, size: c.size});
            for (let use in c.useClasses) {
                if (packFilter(use)) {
                    links.push({source: c.name, target: use, value: c.useClasses[use]});
                }
            }
        }
    });
    // for (let i = 0; i < model.classes.length; i++) {
    //     for (let j = i + 1; j < model.classes.length; j++) {
    //         if (model.classes[i].package===model.classes[j].package){
    //             links.push({source:model.classes})
    //         }
    //     }
    // }

    let node = svg.append('g')
        .attr('class', 'nodes')
        .selectAll('circle')
        .data(classes)
        .enter().append('circle')
        .attr('r', d => 5 + Math.sqrt(d.size) / 10)
        .attr('fill', d => packages[d.package].color)
        .call(d3.drag()
            .on('start', dragstarted)
            .on('drag', dragged)
            .on('end', dragended));

    node.append('title').text(d => d.id);

    let link = svg.append('g')
        .attr('class', 'links')
        .selectAll('line')
        .data(links)
        .enter().append('line')
        .attr('stroke', d =>
            packOf(d.source) === packOf(d.target) ? packages[packOf(d.source)].color : '#888')
        .attr('stroke-width', d => Math.sqrt(d.value));

    simulation
        .nodes(classes)
        .on('tick', ticked);

    simulation.force('link')
        .links(links);

    function ticked() {
        link
            .attr('x1', d => d.source.x)
            .attr('y1', d => d.source.y)
            .attr('x2', d => d.target.x)
            .attr('y2', d => d.target.y);

        node
            .attr('cx', d => d.x)
            .attr('cy', d => d.y);
    }

    function dragstarted(d) {
        if (!d3.event.active) simulation.alphaTarget(0.3).restart();
        d.fx = d.x;
        d.fy = d.y;
    }

    function dragged(d) {
        d.fx = d3.event.x;
        d.fy = d3.event.y;
    }

    function dragended(d) {
        if (!d3.event.active) simulation.alphaTarget(0);
        d.fx = null;
        d.fy = null;
    }
});