[#import "/libs/header.ftl" as header]
[#import "/libs/navigation.ftl" as nav]
<html>

<head>
	<title>
		${title}
	</title>
	[@header.bootstrap /]
</head>

<body>
	<div class="menu">
		[@nav.main /]
	</div>
	${content}
	<div>
		[#assign x = "something"]
		<h3>
			${x}
		</h3>
	</div>
	<p>
		[@upper]
		hallo leute
		[/@upper]
	</p>
	<div>
		<h2>Absätze</h2>
		[#if sections['card']?? ]
			${sections['card'].content()}
		[/#if]
	</div>
</body>

</html>