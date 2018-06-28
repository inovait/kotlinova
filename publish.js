async function publish(pluginConfig, params) {	
	let fs = require('fs');
	
	fs.writeFile("next_changelog.md", params.nextRelease.notes, function(err) {})
	fs.writeFile("next_version.txt", params.nextRelease.version, function(err) {})
}

module.exports = {publish};
