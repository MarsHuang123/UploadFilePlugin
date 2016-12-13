/*global cordova, module*/

module.exports = {
uploadFinish: function(caseID)
    {
        console.log(caseID);
    },
    resume: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Upload", "resume", [name]);
    }

};
