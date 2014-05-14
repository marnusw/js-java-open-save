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
    var queue = {},
        uid = 0;

    var JJOS = window.JsJavaOpenSave = {
        params: {
            code:   'JsJavaOpenSave/JsJavaOpenSave.class',
            jar:    '/lib/js-java-open-save/JsJavaOpenSave.jar',
            height: '1',
            width:  '1'
        },
        setParams: function(params) {
            for (var k in params) {
                JJOS.params[k] = params[k];
            }
        },

        /**
         * Open a file dialogue to select a folder from the local file system. If the file
         * dialogue is closed without selecting a folder an empty string is returned.
         * 
         * @param {String} initialPath An initial folder to start the file dialogue in.
         * @returns {String} The absolute path of the chosen folder.
         */
        chooseFolder: function(initialPath) {
            var data = {initialPath : initialPath || ''},
                id = 'JJOS_chooseFolder_applet';
            addApplet(id, {});
            document.getElementById(id).chooseFolder(data);
            removeApplet(id);
            return data.chosenFolder;
        },
        /**
         * Load the contents of a file from the file system using a JsJavaOpenSave applet.
         *
         * @param {String} fileName The full path to load file content from.
         * @param {Object} callbacks Optional onComplete(data), onProgress(done, total), onCancel(), onError(msg).
         * @return {String} A unique identifier for this operation.
         */
        load: function(fileName, callbacks) {
            if (fileName) {
                return new JJOS.Applet({
                    fileName : fileName
                }, callbacks).id;
            }
        },
        /**
         * Save the contents of a buffer to a file on the file system using a JsJavaOpenSave applet.
         *
         * @param {String} fileName The full path of the file to save to.
         * @param {String} data The data that will be written to the file.
         * @param {Object} callbacks Optional onComplete(data), onProgress(done, total), onCancel(), onError(msg).
         * @return {String} A unique identifier for this operation.
         */
        save: function(fileName, data, callbacks) {
            if (fileName && data) {
                return new JJOS.Applet({
                    fileName : fileName,
                    data : data
                }, callbacks).id;
            }
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
                var id = new JJOS.Applet({}, callbacks).id;
                document.getElementById(id).download({
                    fileName : fileName,
                    url : url
                });
            }
        },
        /**
         * Cancel an operation by providing the previously returned id of that operation.
         *
         * @param {String} id
         */
        cancel: function(id) {
            JJOS.onCancel(id);
        },

        onComplete: function(id, data) {
            if (queue[id]) {
                queue[id].complete(data);
            }
        },
        onProgress: function(id, bps, done, total) {
            if (queue[id]) {
                queue[id].progress(bps, done, total);
            }
        },
        onCancel: function(id) {
            if (queue[id]) {
                queue[id].cancel();
            }
        },
        onError: function(id, msg) {
            if (queue[id]) {
                queue[id].error(msg);
            }
        }
    };

    JJOS.Applet = function(params, callbacks) {
        var base = this;

        var init = function() {
            base.callbacks = callbacks;
            base.id = 'JsJavaOpenSave_' + (uid++);
            addApplet(base.id, params);
            queue[base.id] = base;
        };

        base.complete = function(data) {
            typeof(base.callbacks.onComplete) === "function" && base.callbacks.onComplete(data);
            removeApplet(base.id);
        };
        base.progress = function(bps, done, total) {
            typeof(base.callbacks.onProgress) === "function" && base.callbacks.onProgress(bps, done, total);
        };
        base.cancel = function() {
            typeof(base.callbacks.onCancel) === "function" && base.callbacks.onCancel();
            removeApplet(base.id);
        };
        base.error = function(msg) {
            typeof(base.callbacks.onError) === "function" && base.callbacks.onError(msg);
            removeApplet(base.id);
        };

        init();
    };

    function getApplet() {
        var id = 'JJOS_applet',
            applet = document.getElementById(id);
        if (!applet) {
            addApplet(id, {});
        }
        return applet ? applet : document.getElementById(id);
    }

    function addApplet(id, params, code) {
        var key, applet = document.createElement('applet');
        applet.id = id;
        applet.code = code || 'JsJavaOpenSave/JsJavaOpenSave.class';
        applet.archive = JJOS.params.jar;
        applet.width = JJOS.params.width;
        applet.height = JJOS.params.height;
        applet.mayscript = 'true';

        for (key in params) {
            applet.appendChild(makeParam(key, params[key]));
        }

        document.body.appendChild(applet);
    }

    function makeParam(key, value) {
        var param = document.createElement('param');
        param.name = key;
        param.value = value;
        return param;
    }

    function removeApplet(id) {
        queue[id] && (delete queue[id]);
        var el = document.getElementById(id);
        el && el.parentNode.removeChild(el);
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

// Support for jQuery
if (window.jQuery !== undefined) {
    (function($) {
        var lastId;
        $.jjosFileAPI = {
            setJarPath: setJarPath,
            getOpId: function() {
                return lastId;
            },
            download: function(fileName, url) {
                var deferred = $.Deferred();
                lastId = JsJavaOpenSave.download(fileName, url, getCallbacks(deferred));
                return deferred.promise();
            },
            save: function(fileName, data) {
                var deferred = $.Deferred();
                lastId = JsJavaOpenSave.save(fileName, data, getCallbacks(deferred));
                return deferred.promise();
            },
            load: function(fileName) {
                var deferred = $.Deferred();
                lastId = JsJavaOpenSave.load(fileName, getCallbacks(deferred));
                return deferred.promise();
            },
            cancel: function(id) {
                JsJavaOpenSave.cancel(id);
            }
        };
    })(jQuery);
}

// Support for AngularJS
if (window.angular !== undefined) {
    angular.module('jsJavaOpenSave', [])
    
    .factory('fileDialogue', function() {
        return {
            chooseFolder: JsJavaOpenSave.chooseFolder
        };
    })
    
    .factory('jjosFileAPI', ['$q', function($q) {
        var lastId;
        return {
            setJarPath: setJarPath,
            getOpId: function() {
                return lastId;
            },
            download: function(fileName, url) {
                var deferred = $q.defer();
                lastId = JsJavaOpenSave.download(fileName, url, getCallbacks(deferred));
                return deferred.promise;
            },
            save: function(fileName, data) {
                var deferred = $q.defer();
                lastId = JsJavaOpenSave.save(fileName, data, getCallbacks(deferred));
                return deferred.promise;
            },
            load: function(fileName) {
                var deferred = $q.defer();
                lastId = JsJavaOpenSave.load(fileName, getCallbacks(deferred));
                return deferred.promise;
            },
            cancel: function(id) {
                JsJavaOpenSave.cancel(id);
            }
        };
    }]);
}
    
})();
