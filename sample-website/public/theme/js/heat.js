$(document).ready(function() {
	$.getJSON('//api.wunderground.com/api/d8ed4bf9d8f1b19b/conditions/q/GA/Brunswick.json', function(data) {
		$('#output').html(
			data['current_observation']['heat_index_string'] + "<br />" +
			data['current_observation']['wind_string']
		);
	});
})
