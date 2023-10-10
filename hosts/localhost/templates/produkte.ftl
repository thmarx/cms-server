[#import "/libs/header.ftl" as header]
[#import "/libs/navigation.ftl" as nav]
[#import "/libs/pagination.ftl" as pagination]
<html>
	<head>
		<title>${title}</title>
		[@header.bootstrap /]
	</head>
	<body>
<div class="menu">
[@nav.main /]
</div>
		${content}

<div>
<h3>mit index sortiert nach name</h3>
[#assign page = nodeList.from("/products").page(1).list()]
[#list page.items as item]
<div>
	<h4>${item.name}</h4>
	<a href="${item.path}">zum Produkt</a>
</div>
[/#list]
<div>
[@pagination.pagination page=page path="/produkte" /]
</div>
</div>

<div>
<h3>mit index sortiert nach name reverse</h3>
[#assign page = nodeList.from("/products").page(1).reverse(true).list()]
[#list page.items as item]
<div>
	<h4>${item.name}</h4>
	<a href="${item.path}">zum Produkt</a>
</div>
[/#list]
<div>
[@pagination.pagination page=page path="/produkte" /]
</div>
</div>

<div>
<h3>ohne index</h3>

[#assign pageIndex = renderContext.getQueryParameterAsInt("page", 1)]
[#assign page = nodeList.from("/products").index(false).page(pageIndex).size(1).list()]
[#list page.items as item]
<div>
	<h4>${item.name}</h4>
	<a href="${item.path}">zum Produkt</a>
</div>
[/#list]
<div>
[@pagination.pagination page=page path="/produkte" /]
</div>
</div>

	</body>
</html>
