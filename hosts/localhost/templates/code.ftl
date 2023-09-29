[#import "/libs/header.ftl" as header]
[#import "/libs/navigation.ftl" as nav]
<html>
	<head>
		<title>${title}</title>
		<script src="/assets/prism/prism.js"></script>
		<link rel="stylesheet" href="/assets/prism/prism.css" />

		[@header.bootstrap /]

	</head>
	<body>
<div class="menu">
[@nav.main /]
</div>
<div class="container">
		${content}
</div>
	</body>
</html>
