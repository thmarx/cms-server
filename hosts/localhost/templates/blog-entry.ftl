[#import "/libs/header.ftl" as header]
[#import "/libs/navigation.ftl" as nav]
<html>
	<head>
		<title>${title}</title>
		[@header.bootstrap /]
	</head>
	<body>
<div class="menu">
[@nav.main /]
</div>
<div>
		${content}
<div>

	</body>
</html>
