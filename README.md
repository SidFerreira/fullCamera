# br.com.ferreiraz.fullCamera

## navigator.fullCamera.get

  navigator.fullCamera.get( onSuccess, onError, cameraOptions );
    
## ngCordova usage

Add to your ng-cordova.js:

    angular.module('fullCamera', []).factory("$fullCamera", ['$q', function ($q) {
      return {
          get: function(options) {
            options = options || {};
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
          }
      };
    }]);
Add `fullCamera` inside `ngCordova.plugins`:

    angular.module('ngCordova', [
      'ngCordova.plugins'
    ]);

    angular.module('ngCordova.plugins', [
      'ngCordova.plugins.camera',
      'fullCamera'
    ]);

## Known Issues:

- Using `cordova run android` or `ionic run android` fails for unknown reasons. To make it work, you will need to go to platform/android and run `ant debug`. It will run and then fail blaming `Cordova-lib`. After that you can go back to your project and use the run command.
