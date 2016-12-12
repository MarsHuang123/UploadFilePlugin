/*global cordova, module*/

module.exports = {
    resume: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Upload", "resume", [name]);
    },
    isRunning: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Upload", "isRunning", [name]);
    },
    dataPath: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Upload", "dataPath", [name]);
   }
};

