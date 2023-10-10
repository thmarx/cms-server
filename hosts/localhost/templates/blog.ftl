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
			<h3>Beiträge</h3>
			[#assign page = nodeList.from("/blog").index(false).sort("date").reverse(true).page(1).list()]
			[#list page.items as item]
			<div>
				<h4>${item.name}</h4>
				<a href="${item.path}">zum Beitrag</a>
			</div>
			[/#list]
			<div>
				[@pagination.pagination page=page path="/blog" /]
			</div>
		</div>

	</body>
</html>
