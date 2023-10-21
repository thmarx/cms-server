import { group, check } from "k6";
import http from "k6/http";

export let options = {
	thresholds: {
		'http_req_duration{kind:html}': ["avg<=20"],
		'http_req_duration{kind:css}': ["avg<=10"],
		'http_req_duration{kind:img}': ["avg<=100"],
		'http_reqs': ["rate>100"],
	},
    vus: 10,
    duration: '60s',
};

export default function() {
	group("front page", function() {
		check(http.get("http://localhost:1010/", {
			tags: {'kind': 'html' },
		}), {
			"status is 200": (res) => res.status === 200,
		});
	});
	group("stylesheet", function() {
		check(http.get("http://localhost:1010/assets/bootstrap/css/bootstrap.min.css", {
			tags: {'kind': 'css' },
		}), {
			"status is 200": (res) => res.status === 200,
		});
	});
	group("image", function() {
		check(http.get("http://localhost:1010/assets/images/test.jpg", {
			tags: {'kind': 'img' },
		}), {
			"status is 200": (res) => res.status === 200,
		});
	});
}