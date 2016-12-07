/*global cordova, module*/

module.exports = {
    resume: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Upload", "resume", [name]);
    }
};
