var fs = require('fs');
var path = require('path');

var config = fs.readFileSync(process.env.PWD + '/config.xml', { encoding: 'utf8' });
var projectName = config.match(/id="([^"]+)"/g).pop().split(".").pop().split('"').join('');
var propertiesPath = process.env.PWD + '/platforms/android/br.com.ferreiraz.fullCamera/' + projectName + '-fullcamera/project.properties';
var propertiesFile = fs.readFileSync(propertiesPath, { encoding: 'utf8' });

if(propertiesFile) {
	console.log('FerreiraZ >>> Fixing properties file.');
	propertiesFile = propertiesFile.replace(/{{projectName}}/g, projectName);
	fs.writeFileSync(propertiesPath, propertiesFile, { encoding: 'utf8' });	
} else {
	console.log('------ FerreiraZ');
	console.log('Error fixing properties file.');
	console.log('process.env.PWD may be undefined. Showing process.env:');
	console.log(process.env);
	console.log('------');
	process.exit();
}