    var exec = require('cordova/exec');
    //argscheck = require('cordova/argscheck'),
    var fullCameraExport = {};

    var Sources = {};
        Sources.GalleryPhoto = 'GalleryPhoto';
        Sources.GalleryVideo = 'GalleryVideo';
        Sources.GalleryAll = [Sources.GalleryPhoto, Sources.GalleryVideo].join();
        Sources.CameraPhoto  = 'CameraPhoto';
        Sources.CameraVideo  = 'CameraVideo';
        Sources.CameraAll = [Sources.CameraPhoto, Sources.CameraVideo].join();
        Sources.ALL = [Sources.GalleryAll, Sources.CameraAll].join();
    fullCameraExport.Sources = Sources;

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
        stringAppFolder         : "fullcam",

        allowedSources          : fullCameraExport.Sources.ALL
    };
    fullCameraExport.getOptions = function() {
        return globalOptions;
    };
        
    fullCameraExport.setOptions = function(options) {
        for (var key = options.length - 1; key >= 0; key--) {
            var value = options[key];
            globalOptions[key] = value;
        }
    };

    fullCameraExport.get = function(onSuccess, onError, options) {
        options = options || {};

        var _options = globalOptions;

        fullCameraExport.setOptions = function(options) {
            for (var key = options.length - 1; key >= 0; key--) {
                var value = options[key];
                globalOptions[key] = value;
            }
        };

        exec(
            typeof onSuccess === 'function' ? onSuccess : function () {},
            typeof onError === 'function' ? onError : function () {},
            "FullCamera",
            "get",
            [
                _options.shouldSaveOnGallery,
                _options.imageBox,
                _options.imageCompression,
                _options.maxVideoDuration,
                _options.maxPhotoCount,
                _options.minPhotoCount,
                _options.stringOk,
                _options.stringCancel,
                _options.stringMaxPhotos,
                _options.stringDeletePhoto,
                _options.stringDeleteAllPhotos,
                _options.stringDeleteVideo,
                _options.stringDeleteAllVideos,
                _options.stringProcessingVideos,
                _options.stringAppFolder,
                _options.allowedSources
            ]
        );
    };

    /*fullCameraExport.cleanup = function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "Camera", "cleanup", []);
    };*/
    /*
    angular.module('fullCamera', []).factory("$fullCamera", ['$q', function ($q) {
        var _get = function(options) {
            options = options || {};
            success = success || function() { console.log('Success'); };
            fail    = fail    || function() { console.log('Fail'); };

            navigator.fullCamera.get(success, fail, options);
            var q = $q.defer();

            if (!navigator.fullCamera) {
              q.resolve(null);
              return q.promise;
            }

            navigator.fullCamera.get(function (resultData) {
              q.resolve(resultData);
            }, function (err) {
              q.reject(err);
            }, options);

            return q.promise;
        };
        return {
            get: _get
        };
    }]);
    */

    module.exports = fullCameraExport;