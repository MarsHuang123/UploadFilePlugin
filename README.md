# Cordova upload file Plugin

Plugin that upload file to server.

Using

Create a new Cordova Project

$ cordova create upload com.example.UploadFile Upload
Install the plugin

$ cd upload

$ cordova plugin add https://github.com/MarsHuang123/UploadFilePlugin.git

Edit www/js/index.js and add the following code inside onDeviceReady

    var success = function(message) {
        alert(message);
        }

        var failure = function() {
            alert("Error calling upload Plugin");
        }

        upload.resume("S090R", success, failure);

Install iOS platform

cordova platform add ios

Run the code

cordova run 