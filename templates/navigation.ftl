[#import "/libs/header.ftl" as header]

<html>
	<head>
		<title>${title}</title>
		
		[@header.bootstrap /]

	</head>
	<body>
		${content}

<div>
<h4>nav from current</h4>
[#assign nodes = navigationFunction(".", 0)]
<ul>
[#list nodes as node]
    <li>${node.name} | ${node.path}</li>
[/#list]
</ul>
</div>

<div>
<h4>nav from root</h4>
[#assign nodes = navigationFunction("/", 0)]
<ul>
[#list nodes as node]
    <li>${node.name} | ${node.path}</li>
[/#list]
</ul>
</div>

	</body>
</html>
