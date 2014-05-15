/*
    JsJavaOpenSave: File system access in JavaScript via a Java applet
    JavaScript + Java Library

    Version: 0.1

    Copyright (C) 2014 Marnus Weststrate (marnusw@gmail.com)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

(function() {
    var appletId = 'JsJavaOpenSave_Applet',
        downloads = {},
        statusTimer;

    var JJOS = window.JsJavaOpenSave = {
        params: {
            statusUpdateInterval : 125,
            code:   'JsJavaOpenSave/JsJavaOpenSave.class',
            jar:    '/lib/js-java-open-save/JsJavaOpenSave.jar',
            height: '200',
            width:  '200'
        },
        setParams: function(params) {
            for (var k in params) {
                JJOS.params[k] = params[k];
            }
        },
        
        /**
         * Can be used to download the applet from the server on page load so it will be
         * ready when the first action using it is initiated. The operation is asynchronous.
         */
        preLoadApplet: function() {
            setTimeout(getApplet, 1);
        },
        /**
         * Open a file dialogue to select a folder from the local file system. If the file
         * dialogue is closed without selecting a folder an empty string is returned.
         * 
         * @param {String} initialPath An initial folder to start the file dialogue in.
         * @returns {String} The absolute path of the chosen folder.
         */
        chooseFolder: function(initialPath) {
            var params = {initialPath : initialPath || ''};
            getApplet().chooseFolder(params);
            return params.chosenFolder;
        },
        /**
         * Download a file from a provided URL to a specified file on disk using a JsJavaOpenSave applet.
         *
         * @param {String} fileName The full path to save the downloaded file to.
         * @param {String} url The URL to download to the specified file name.
         * @param {Object} callbacks Optional onComplete(data), onProgress(done, total), onCancel(), onError(msg).
         * @return {String} A unique identifier for this operation.
         */
        download: function(fileName, url, callbacks) {
            if (fileName && url) {
                var applet = getApplet(),
                    params = {
                        fileName : fileName,
                        url : url
                    };
                applet.newDownload(params);
                downloads[params.id] = new Operation(callbacks);
                applet.startDownload(params);
                checkStatus();
                return params.id;
            }
        },
        /**
         * Cancel a download by providing the previously returned id of that operation.
         *
         * @param {String} id
         */
        cancelDownload: function(id) {
            getApplet().cancelDownload({id:id});
            downloads[id] && downloads[id].cancel();
        },
        /**
         * Save the contents of a buffer to a file on the file system using a JsJavaOpenSave applet.
         *
         * @param {String} fileName The full path of the file to save to.
         * @param {String} data The data that will be written to the file.
         * @param {Object} callbacks Optional onComplete(), onError(msg).
         * @return {String} A unique identifier for this operation.
         */
        save: function(fileName, data, callbacks) {
            if (fileName && data) {
                var operation = new Operation(callbacks),
                    applet = getApplet(),
                    params = {
                        fileName : fileName,
                        data : data
                    };
                applet.saveToFile(params);
                if (params.error) {
                    operation.error(params.error);
                } else {
                    operation.complete();
                }
            }
        },
        /**
         * Load the contents of a file from the file system using a JsJavaOpenSave applet.
         *
         * @param {String} fileName The full path to load file content from.
         * @param {Object} callbacks Optional onComplete(data), onError(msg).
         * @return {String} A unique identifier for this operation.
         */
        load: function(fileName, callbacks) {
            if (fileName) {
                var operation = new Operation(callbacks),
                    applet = getApplet(),
                    params = {fileName : fileName};
                applet.loadFromFile(params);
                if (params.data) {
                    operation.complete(params.data);
                } else {
                    operation.error(params.error);
                }
            }
        }
    };

    /**
     * 
     */
    function checkStatus() {
        clearInterval(statusTimer);
        
        var id, applet = getApplet(),
            checkAgain = false,
            remove = {};
        for (id in downloads) {
            if (updateProgress(id, applet)) {
                remove[id] = true;
            } else {
                checkAgain = true;
            }
        }
        
        for (id in remove) {
            delete downloads[id];
        }
        
        if (checkAgain) {
            statusTimer = setInterval(checkStatus, JJOS.params.statusUpdateInterval);
        }
    }
    
    /**
     * 
     * @param {Number} id The id of download to update.
     * @param {Element} applet A reference to the download applet.
     * @returns {Boolean} Whether the download should be removed from the list.
     */
    function updateProgress(id, applet) {
        var params = {id : id};
        applet.getDownloadStatus(params);
        if (params.isDone) {
            downloads[id].complete();
        } else if (params.isCancelled) {
            downloads[id].cancel();
        } else if (params.error) {
            downloads[id].error(params.error);
        } else {
            downloads[id].progress(parseFloat(params.bps), parseInt(params.progress), parseInt(params.total));
            return false;
        }
        return true;
    }
    
    function Operation(callbacks) {
        var base = this;

        base.callbacks = callbacks || {};

        base.complete = function(data) {
            typeof(base.callbacks.onComplete) === "function" && base.callbacks.onComplete(data);
        };
        base.progress = function(bps, progress, total) {
            typeof(base.callbacks.onProgress) === "function" && base.callbacks.onProgress(bps, progress, total);
        };
        base.cancel = function() {
            typeof(base.callbacks.onCancel) === "function" && base.callbacks.onCancel();
        };
        base.error = function(msg) {
            typeof(base.callbacks.onError) === "function" && base.callbacks.onError(msg);
        };
    };
    
    function getApplet() {
        var applet = document.getElementById(appletId);
        return applet ? applet : addApplet(appletId);
    }
    
    function addApplet(id, params, code) {
        var applet = document.createElement('applet');
        applet.id = id;
        applet.mayscript = 'true';
        applet.code = code || JJOS.params.code;
        applet.archive = JJOS.params.jar;
        applet.width = JJOS.params.width;
        applet.height = JJOS.params.height;
        applet.style = 'z-index:-10000';

        for (var key in params) {
            applet.appendChild(makeParam(key, params[key]));
        }

        document.body.appendChild(applet);
        return document.getElementById(id);
    }
    
    function makeParam(key, value) {
        var param = document.createElement('param');
        param.name = key;
        param.value = value;
        return param;
    }

// Support for various JS libraries
function getCallbacks(deferred) {
    return {
        onComplete : function(data)             { deferred.resolve(data); },
        onProgress : function(bps, done, total) { deferred.notify({total:total, done:done, bps:bps}); },
        onCancel   : function()                 { deferred.reject('Cancelled'); },
        onError    : function(msg)              { deferred.reject(msg); }
    };
}

function setJarPath(jar) {
    JsJavaOpenSave.setParams({
        jar : jar
    });
}

function setUpdateInterval(interval) {
    JsJavaOpenSave.setParams({
        statusUpdateInterval : interval
    });
}

// Support for jQuery
if (window.jQuery !== undefined) {
    (function($) {
        var lastId;
        $.jjosFileAPI = {
            preLoadApplet: JsJavaOpenSave.preLoadApplet,
            setUpdateInterval: setUpdateInterval,
            setJarPath: setJarPath,
            getDownloadId: function() {
                return lastId;
            },
            download: function(fileName, url) {
                var deferred = $.Deferred();
                lastId = JsJavaOpenSave.download(fileName, url, getCallbacks(deferred));
                return deferred.promise();
            },
            save: function(fileName, data) {
                var deferred = $.Deferred();
                JsJavaOpenSave.save(fileName, data, getCallbacks(deferred));
                return deferred.promise();
            },
            load: function(fileName) {
                var deferred = $.Deferred();
                JsJavaOpenSave.load(fileName, getCallbacks(deferred));
                return deferred.promise();
            },
            cancelDownload: function(id) {
                JsJavaOpenSave.cancelDownload(id);
            }
        };
    })(jQuery);
}

// Support for AngularJS
if (window.angular !== undefined) {
    angular.module('jsJavaOpenSave', [])
    
    .factory('fileDialogue', function() {
        return {
            preLoadApplet: JsJavaOpenSave.preLoadApplet,
            chooseFolder: JsJavaOpenSave.chooseFolder
        };
    })
    
    .factory('jjosFileAPI', ['$q', function($q) {
        var lastId;
        return {
            preLoadApplet: JsJavaOpenSave.preLoadApplet,
            setUpdateInterval: setUpdateInterval,
            setJarPath: setJarPath,
            getDownloadId: function() {
                return lastId;
            },
            download: function(fileName, url) {
                var deferred = $q.defer();
                lastId = JsJavaOpenSave.download(fileName, url, getCallbacks(deferred));
                return deferred.promise;
            },
            save: function(fileName, data) {
                var deferred = $q.defer();
                JsJavaOpenSave.save(fileName, data, getCallbacks(deferred));
                return deferred.promise;
            },
            load: function(fileName) {
                var deferred = $q.defer();
                JsJavaOpenSave.load(fileName, getCallbacks(deferred));
                return deferred.promise;
            },
            cancelDownload: function(id) {
                JsJavaOpenSave.cancelDownload(id);
            }
        };
    }]);
}
    
})();
