module.exports = {
    uploadFinish: function(caseID, succesful)
    {
               console.log('here');
               console.log(succesful);
       console.log(caseID);
    },
    resume: function (name, files, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Upload", "resume", [name, files]);
               },
    getFilesStatus:function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Upload", "getFilesStatus");
               },
               stop:function (successCallback, errorCallback) {
               cordova.exec(successCallback, errorCallback, "Upload", "stop");
               }
    
};