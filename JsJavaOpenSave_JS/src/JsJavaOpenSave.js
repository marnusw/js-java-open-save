/*
 JsJavaOpenSave: Client Side File Management
 JavaScript + Flash Library
 
 Version: 0.1
 
 Copyright (c) 2014 Marnus Weststrate (marnusw@mwconsult.co.za)
 
 */

(function() {
    var JJOS = window.JsJavaOpenSave = {
        options: {
            appletId: 'JsJavaOpenSave_Applet',
            code: 'JsJavaOpenSave/JsJavaOpenSave.class',
            jar: '/media/JsJavaOpenSave.jar'
        },
        uid: 0,
        queue: {},
        
        setOptions: function(options) {
            for (var k in options) {
                JJOS.options[k] = options[k];
            }
        },
        saveComplete: function(queue) {
            var obj = JsJavaOpenSave.queue[queue];
            if (obj)
                obj.complete();
            return true;
        },
        saveCancel: function(queue) {
            var obj = JsJavaOpenSave.queue[queue];
            if (obj)
                obj.cancel();
            return true;
        },
        saveError: function(queue) {
            var obj = JsJavaOpenSave.queue[queue];
            if (obj)
                obj.error();
            return true;
        },
        addToQueue: function(container) {
            JJOS.queue[id] = container;
        },
        saveAs: function(fileName, data) {
//            var param, applet = document.createElement('applet'),
//                id = 'JsJavaOpenSave';
//        
//            applet.id = id;
//            applet.archive = JJOS.options.jar;
//            applet.code = JJOS.options.code;
//            applet.width = '200';
//            applet.height = '200';
//            
//            param = document.createElement('param');
//            param.name = "id";
//            param.value = id;
//            applet.appendChild(param);
//            
//            console.log("adding save applet " + id);
//            document.body.appendChild(applet);
        },
        download: function(fileName, url) {
            var param, applet = document.createElement('applet'),
                id = JJOS.getUID(applet);
            applet.archive = JJOS.options.jar;
            applet.code = JJOS.options.code;
            applet.width = '200';
            applet.height = '200';
            
            param = document.createElement('param');
            param.name = "fileName";
            param.value = fileName;
            applet.appendChild(param);
            
            param = document.createElement('param');
            param.name = "url";
            param.value = url;
            applet.appendChild(param);
            
            param = document.createElement('param');
            param.name = "id";
            param.value = id;
            applet.appendChild(param);
            
            console.log("adding applet " + id);
            document.body.appendChild(applet);
            JJOS.addToQueue(id, applet);
        },
        removeApplet: function(id) {
            var el = document.getElementById(id);
            el.parentNode.removeChild(el);
            console.log('Removed applet ' + id);
        },
        log: function(text) {
            console.log(text);
        },
        // Concept adapted from: http://tinyurl.com/yzsyfto
        // SWF object runs off of ID's, so this is the good way to get a unique ID
        getUID: function(el){
          if(el.id === "") el.id = 'JsJavaOpenSave_' + JJOS.uid++;
          return el.id;
        }
    };

    /**
     * Save the contents of a buffer to a file on the file system using JsJavaOpenSave.swf.
     * 
     * @param {String} fileName The full path to load file content from.
     * @param {String} data The data that will be written to the file.
     * @param {String} dataType Whether the data is an unencoded "string" (default) or "base64" encoded.
     * @param {Function} success Callback after a successful save: function(fileName, savedData) {}
     * @param {Function} error Callback when an error occurred: function(errorMsg, fileName, savedData) {}
     * @param {Function} cancel Callback when the user cancelled the operation: function(fileName, savedData) {}
     * @returns {Number}
     */
    JsJavaOpenSave.save = function(fileName, data, dataType, success, error, cancel) {
        
        return 1; // promise!
    };
    
    /**
     * Load the contents of a file from the file system using JsJavaOpenSave.swf.
     * 
     * @param {String} fileName The full path to load file content from.
     * @param {String} dataType Whether content should be returned as a "string" (default) or "base64" encoded.
     * @param {Function} success Callback after a successful load: function(readData, fileName) {}
     * @param {Function} error Callback when an error occurred: function(errorMsg, fileName) {}
     * @param {Function} cancel Callback when the user cancelled the operation: function(fileName) {}
     * @returns {Number}
     */
    JsJavaOpenSave.load = function(fileName, dataType, success, error, cancel) {
        
        return 1; // promise!
    };
    
    JsJavaOpenSave.Container = function(el, options) {
        var base = this;

        base.el = el;
        base.enabled = true;
        base.dataCallback = null;
        base.filenameCallback = null;
        base.data = null;
        base.filename = null;

        var init = function() {
            base.options = options;

            if (!base.options.append)
                base.el.innerHTML = "";

            base.flashContainer = document.createElement('span');
            base.el.appendChild(base.flashContainer);

            base.queue_name = JsJavaOpenSave.getUID(base.flashContainer);

            if (typeof (base.options.filename) === "function")
                base.filenameCallback = base.options.filename;
            else if (base.options.filename)
                base.filename = base.options.filename;

            if (typeof (base.options.data) === "function")
                base.dataCallback = base.options.data;
            else if (base.options.data)
                base.data = base.options.data;


            var flashVars = {
                queue_name: base.queue_name,
                width: base.options.width,
                height: base.options.height
            };

            var params = {
                allowScriptAccess: 'always'
            };

            var attributes = {
                id: base.flashContainer.id,
                name: base.flashContainer.id
            };

            if (base.options.enabled === false)
                base.enabled = false;

            if (base.options.transparent === true)
                params.wmode = "transparent";

            if (base.options.downloadImage)
                flashVars.downloadImage = base.options.downloadImage;

            swfobject.embedSWF(base.options.swf, base.flashContainer.id, base.options.width, base.options.height, "10", null, flashVars, params, attributes);

            JsJavaOpenSave.addToQueue(base);
        };

        base.enable = function() {
            var swf = document.getElementById(base.flashContainer.id);
            swf.setEnabled(true);
            base.enabled = true;
        };

        base.disable = function() {
            var swf = document.getElementById(base.flashContainer.id);
            swf.setEnabled(false);
            base.enabled = false;
        };

        base.getData = function() {
            if (!base.enabled)
                return "";
            if (base.dataCallback)
                return base.dataCallback();
            else if (base.data)
                return base.data;
            else
                return "";
        };

        base.getFilename = function() {
            if (base.filenameCallback)
                return base.filenameCallback();
            else if (base.filename)
                return base.filename;
            else
                return "";
        };

        base.getDataType = function() {
            if (base.options.dataType)
                return base.options.dataType;
            return "string";
        };

        base.complete = function() {
            if (typeof (base.options.onComplete) === "function")
                base.options.onComplete();
        };

        base.cancel = function() {
            if (typeof (base.options.onCancel) === "function")
                base.options.onCancel();
        };

        base.error = function() {
            if (typeof (base.options.onError) === "function")
                base.options.onError();
        };

        init();
    };

    JsJavaOpenSave.defaultOptions = {
        swf: 'media/downloadify.swf',
        downloadImage: 'images/download.png',
        width: 100,
        height: 30,
        transparent: true,
        append: false,
        dataType: "string"
    };
})();

// Support for jQuery
if (jQuery !== undefined) {
    (function($) {
        $.fn.downloadify = function(options) {
            return this.each(function() {
                options = $.extend({}, JsJavaOpenSave.defaultOptions, options);
                var dl = JsJavaOpenSave.create(this, options);
                $(this).data('JsJavaOpenSave', dl);
            });
        };
    })(jQuery);
}
