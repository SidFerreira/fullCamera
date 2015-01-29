
var argscheck = require('cordova/argscheck'),
    exec = require('cordova/exec');

var fullCameraExport = {};

var globalOptions = {
    shouldSaveOnGallery     : true,
    imageBox                : 720,
    imageCompression        : 100,

    maxVideoDuration        : 30,
    maxPhotoCount           : 5,
    minPhotoCount           : 5,

    stringOk                : "OK",
    stringCancel            : "Cancel",
    stringMaxPhotos         : "You are limited to X photos.",
    stringDeletePhoto       : "Are you sure that you want to delete this photo?",
    stringDeleteAllPhotos   : "This will cause all photos to be removed. Are you sure?",
    stringDeleteVideo       : "Are you sure that you want to delete this video?",
    stringDeleteAllVideos   : "This will cause your video to be removed. Are you sure?",
    stringProcessingVideos  : "Processing video",
    stringAppFolder         : "fullcam"
};
fullCameraExport.getOptions = function() {
    return globalOptions;
}
fullCameraExport.setOptions = function(options) {
    for (var key = options.length - 1; key >= 0; key--) {
        var value = options[key];
        globalOptions[key] = value;
    };
}

fullCameraExport.get = function(onSuccess, onError, options) {
    console.log("fullCameraExport.get");

    options = options || {};

    var _options = globalOptions;

    for (var key = options.length - 1; key >= 0; key--) {
        var value = options[key];
        _options[key] = value;
    };

    cordova.exec(
        typeof onSuccess === 'function' ? onSuccess : function () {},
        typeof onError === 'function' ? onError : function () {},
        "FullCamera",
        "get",
        [
            _options['shouldSaveOnGallery'],
            _options['imageBox'],
            _options['imageCompression'],
            _options['maxVideoDuration'],
            _options['maxPhotoCount'],
            _options['minPhotoCount'],
            _options['stringOk'],
            _options['stringCancel'],
            _options['stringMaxPhotos'],
            _options['stringDeletePhoto'],
            _options['stringDeleteAllPhotos'],
            _options['stringDeleteVideo'],
            _options['stringDeleteAllVideos'],
            _options['stringProcessingVideos'],
            _options['stringAppFolder']
        ]
    );
};

/*fullCameraExport.cleanup = function(successCallback, errorCallback) {
    exec(successCallback, errorCallback, "Camera", "cleanup", []);
};*/

module.exports = fullCameraExport;
