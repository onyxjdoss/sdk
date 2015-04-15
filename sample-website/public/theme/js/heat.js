$(document).ready(function() {
	$.getJSON('http://api.wunderground.com/api/d8ed4bf9d8f1b19b/conditions/q/GA/Brunswick.json', function(data) {
		$('#output').html(data['current_observations']['heat_index_string']);
	});
})
