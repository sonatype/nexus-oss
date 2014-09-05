/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2014 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2014-09-02 11:12:40 (ef1fa70924f51a26dacbe29644ca3f31501a5fce)
*/
// @tag extras,core
// @require perf/Monitor.js
// @define Ext.Supports

/**
 * @class Ext.is
 * 
 * Determines information about the current platform the application is running on.
 * 
 * @singleton
 */
Ext.is = {
    init : function(navigator) {
        var platforms = this.platforms,
            ln = platforms.length,
            i, platform;

        navigator = navigator || window.navigator;

        for (i = 0; i < ln; i++) {
            platform = platforms[i];
            this[platform.identity] = platform.regex.test(navigator[platform.property]);
        }

        /**
         * @property Desktop True if the browser is running on a desktop machine
         * @type {Boolean}
         */
        this.Desktop = this.Mac || this.Windows || (this.Linux && !this.Android);
        /**
         * @property Tablet True if the browser is running on a tablet (iPad)
         */
        this.Tablet = this.iPad;
        /**
         * @property Phone True if the browser is running on a phone.
         * @type {Boolean}
         */
        this.Phone = !this.Desktop && !this.Tablet;
        /**
         * @property iOS True if the browser is running on iOS
         * @type {Boolean}
         */
        this.iOS = this.iPhone || this.iPad || this.iPod;
        
        /**
         * @property Standalone Detects when application has been saved to homescreen.
         * @type {Boolean}
         */
        this.Standalone = !!window.navigator.standalone;
    },
    
    /**
     * @property iPhone True when the browser is running on a iPhone
     * @type {Boolean}
     */
    platforms: [{
        property: 'platform',
        regex: /iPhone/i,
        identity: 'iPhone'
    },
    
    /**
     * @property iPod True when the browser is running on a iPod
     * @type {Boolean}
     */
    {
        property: 'platform',
        regex: /iPod/i,
        identity: 'iPod'
    },
    
    /**
     * @property iPad True when the browser is running on a iPad
     * @type {Boolean}
     */
    {
        property: 'userAgent',
        regex: /iPad/i,
        identity: 'iPad'
    },
    
    /**
     * @property Blackberry True when the browser is running on a Blackberry
     * @type {Boolean}
     */
    {
        property: 'userAgent',
        regex: /Blackberry/i,
        identity: 'Blackberry'
    },
    
    /**
     * @property Android True when the browser is running on an Android device
     * @type {Boolean}
     */
    {
        property: 'userAgent',
        regex: /Android/i,
        identity: 'Android'
    },
    
    /**
     * @property Mac True when the browser is running on a Mac
     * @type {Boolean}
     */
    {
        property: 'platform',
        regex: /Mac/i,
        identity: 'Mac'
    },
    
    /**
     * @property Windows True when the browser is running on Windows
     * @type {Boolean}
     */
    {
        property: 'platform',
        regex: /Win/i,
        identity: 'Windows'
    },
    
    /**
     * @property Linux True when the browser is running on Linux
     * @type {Boolean}
     */
    {
        property: 'platform',
        regex: /Linux/i,
        identity: 'Linux'
    }]
};

Ext.is.init();

/**
 * @class Ext.supports
 *
 * Determines information about features are supported in the current environment
 * 
 * @singleton
 */
(function(){

    // this is a local copy of certain logic from (Abstract)Element.getStyle
    // to break a dependancy between the supports mechanism and Element
    // use this instead of element references to check for styling info
    var getStyle = function(element, styleName){
        var view = element.ownerDocument.defaultView,
            style = (view ? view.getComputedStyle(element, null) : element.currentStyle) || element.style;
        return style[styleName];
    },
    supportsVectors = {
        'IE6-quirks':  [0,1,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,1,1,0,0,0,0,1,0,0,1,0,0,1,0,1,0,0,0,0,0],
        'IE6-strict':  [0,1,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,1,1,0,0,0,1,1,0,1,1,0,0,1,0,1,0,0,0,0,0],
        'IE7-quirks':  [0,1,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,1,1,0,0,0,0,1,0,0,1,0,0,1,0,1,0,0,0,0,0],
        'IE7-strict':  [0,1,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,1,1,0,0,0,1,1,1,0,1,0,0,1,0,1,0,0,0,0,0],
        'IE8-quirks':  [0,1,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,1,1,0,0,0,0,1,0,0,1,0,0,1,0,1,0,0,0,0,0],
        'IE8-strict':  [0,1,0,0,1,0,0,0,1,0,0,0,1,0,0,0,0,1,1,0,0,0,1,1,1,1,1,0,0,1,0,1,0,0,1,0,0],
        'IE9-quirks':  [0,1,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,1,1,1,0,0,0,1,0,0,1,0,0,1,0,1,0,0,0,0,0],
        'IE9-strict':  [0,1,0,0,1,1,1,1,1,1,1,0,1,0,0,1,1,1,1,1,0,1,1,1,1,1,1,1,0,1,0,0,0,0,1,0,0],
        'IE10-quirks': [1,1,0,0,1,1,1,1,0,1,1,1,1,0,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,0,0,0,0,1,0,0],
        'IE10-strict': [1,1,0,0,1,1,1,1,0,1,1,1,1,0,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,0,0,0,0,1,0,0]
    },
    doc = document,
    div = doc.createElement('div');

function getBrowserKey() {
    var browser = Ext.isIE6 ? 'IE6' : Ext.isIE7 ? 'IE7' : Ext.isIE8 ? 'IE8' :
        Ext.isIE9 ? 'IE9': Ext.isIE10 ? 'IE10' : '';

    return browser ? browser + (Ext.isStrict ? '-strict' : '-quirks') : '';
}

Ext.supports = {
    /**
     * Runs feature detection routines and sets the various flags. This is called when
     * the scripts loads (very early) and again at {@link Ext#onReady}. Some detections
     * are flagged as `early` and run immediately. Others that require the document body
     * will not run until ready.
     *
     * Each test is run only once, so calling this method from an onReady function is safe
     * and ensures that all flags have been set.
     * @markdown
     * @private
     */
    init : function() {
        var me = this,
            toRun = me.toRun || me.tests,
            n = toRun.length,
            notRun = [],
            browserKey = getBrowserKey(),
            test, vector, value,
            docReady = Ext.isReady;

        if (docReady) {
            div.innerHTML = [
                '<div style="height:30px;width:50px;">',
                    '<div style="height:20px;width:20px;"></div>',
                '</div>',
                '<div style="width: 200px; height: 200px; position: relative; padding: 5px;">',
                    '<div style="position: absolute; top: 0; left: 0; width: 100%; height: 100%;"></div>',
                '</div>',
                '<div style="position: absolute; left: 10%; top: 10%;"></div>',
                '<div style="float:left; background-color:transparent;"></div>'
            ].join('');

            doc.body.appendChild(div);
        }

        vector = supportsVectors[browserKey];
        while (n--) {
            test = toRun[n];
            value = vector && vector[n];
            if (value !== undefined) {
                me[test.identity] = value;
            } else if (docReady || test.early) {
                me[test.identity] = test.fn.call(me, doc, div);
            } else {
                notRun.push(test);
            }
        }

        if (docReady) {
            doc.body.removeChild(div);
        }

        me.toRun = notRun;
    },

    //<debug>
    /**
     * Generates a support vector for the current browser/mode.  The result can be
     * added to supportsVectors to eliminate feature detection at startup time.
     * @private
     */
    generateVector: function() {
        var tests = this.tests,
            vector = [],
            i = 0,
            ln = tests.length,
            test;

        for (; i < ln; i++) {
            test = tests[i];
            vector.push(this[test.identity] ? 1 : 0);
        }
        return vector;
    },
    //</debug>

    /**
     * @property PointerEvents True if document environment supports the CSS3 pointer-events style.
     * @type {Boolean}
     */
    PointerEvents: 'pointerEvents' in document.documentElement.style,

    // IE10/Win8 throws "Access Denied" accessing window.localStorage, so this test
    // needs to have a try/catch
    /**
     * @property LocalStorage True if localStorage is supported
     */
    LocalStorage: (function() {
        try {
            return 'localStorage' in window && window['localStorage'] !== null;
        } catch (e) {
            return false;
        }
    })(),

    /**
     * @property CSS3BoxShadow True if document environment supports the CSS3 box-shadow style.
     * @type {Boolean}
     */
    CSS3BoxShadow: 'boxShadow' in document.documentElement.style || 'WebkitBoxShadow' in document.documentElement.style || 'MozBoxShadow' in document.documentElement.style,

    /**
     * @property ClassList True if document environment supports the HTML5 classList API.
     * @type {Boolean}
     */
    ClassList: !!document.documentElement.classList,

    /**
     * @property OrientationChange True if the device supports orientation change
     * @type {Boolean}
     */
    OrientationChange: ((typeof window.orientation != 'undefined') && ('onorientationchange' in window)),

    /**
     * @property DeviceMotion True if the device supports device motion (acceleration and rotation rate)
     * @type {Boolean}
     */
    DeviceMotion: ('ondevicemotion' in window),

    /**
     * @property Touch True if the device supports touch
     * @type {Boolean}
     */
    // is.Desktop is needed due to the bug in Chrome 5.0.375, Safari 3.1.2
    // and Safari 4.0 (they all have 'ontouchstart' in the window object).
    Touch: ('ontouchstart' in window) && (!Ext.is.Desktop),

    /**
     * @property TimeoutActualLateness True if the browser passes the "actualLateness" parameter to
     * setTimeout. See: https://developer.mozilla.org/en/DOM/window.setTimeout
     * @type {Boolean}
     */
    TimeoutActualLateness: (function(){
        setTimeout(function(){
            Ext.supports.TimeoutActualLateness = arguments.length !== 0;
        }, 0);
    }()),

    tests: [
        /**
         * @property Transitions True if the device supports CSS3 Transitions
         * @type {Boolean}
         */
        {
            // support Vector 0
            identity: 'Transitions',
            fn: function(doc, div) {
                var prefix = [
                        'webkit',
                        'Moz',
                        'o',
                        'ms',
                        'khtml'
                    ],
                    TE = 'TransitionEnd',
                    transitionEndName = [
                        prefix[0] + TE,
                        'transitionend', //Moz bucks the prefixing convention
                        prefix[2] + TE,
                        prefix[3] + TE,
                        prefix[4] + TE
                    ],
                    ln = prefix.length,
                    i = 0,
                    out = false;

                for (; i < ln; i++) {
                    if (getStyle(div, prefix[i] + "TransitionProperty")) {
                        Ext.supports.CSS3Prefix = prefix[i];
                        Ext.supports.CSS3TransitionEnd = transitionEndName[i];
                        out = true;
                        break;
                    }
                }
                return out;
            }
        },

        /**
         * @property RightMargin True if the device supports right margin.
         * See https://bugs.webkit.org/show_bug.cgi?id=13343 for why this is needed.
         * @type {Boolean}
         */
        {
            // support Vector 1
            identity: 'RightMargin',
            fn: function(doc, div) {
                var view = doc.defaultView;
                return !(view && view.getComputedStyle(div.firstChild.firstChild, null).marginRight != '0px');
            }
        },

        /**
         * @property DisplayChangeInputSelectionBug True if INPUT elements lose their
         * selection when their display style is changed. Essentially, if a text input
         * has focus and its display style is changed, the I-beam disappears.
         *
         * This bug is encountered due to the work around in place for the {@link #RightMargin}
         * bug. This has been observed in Safari 4.0.4 and older, and appears to be fixed
         * in Safari 5. It's not clear if Safari 4.1 has the bug, but it has the same WebKit
         * version number as Safari 5 (according to http://unixpapa.com/js/gecko.html).
         */
        {
            // support Vector 2
            identity: 'DisplayChangeInputSelectionBug',
            early: true,
            fn: function() {
                var webKitVersion = Ext.webKitVersion;
                // WebKit but older than Safari 5 or Chrome 6:
                return 0 < webKitVersion && webKitVersion < 533;
            }
        },

        /**
         * @property DisplayChangeTextAreaSelectionBug True if TEXTAREA elements lose their
         * selection when their display style is changed. Essentially, if a text area has
         * focus and its display style is changed, the I-beam disappears.
         *
         * This bug is encountered due to the work around in place for the {@link #RightMargin}
         * bug. This has been observed in Chrome 10 and Safari 5 and older, and appears to
         * be fixed in Chrome 11.
         */
        {
            // support Vector 3
            identity: 'DisplayChangeTextAreaSelectionBug',
            early: true,
            fn: function() {
                var webKitVersion = Ext.webKitVersion;

                /*
                Has bug w/textarea:

                (Chrome) Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; en-US)
                            AppleWebKit/534.16 (KHTML, like Gecko) Chrome/10.0.648.127
                            Safari/534.16
                (Safari) Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; en-us)
                            AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5
                            Safari/533.21.1

                No bug:

                (Chrome) Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_7)
                            AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.57
                            Safari/534.24
                */
                return 0 < webKitVersion && webKitVersion < 534.24;
            }
        },

        /**
         * @property TransparentColor True if the device supports transparent color
         * @type {Boolean}
         */
        {
            // support Vector 4
            identity: 'TransparentColor',
            fn: function(doc, div, view) {
                view = doc.defaultView;
                return !(view && view.getComputedStyle(div.lastChild, null).backgroundColor != 'transparent');
            }
        },

        /**
         * @property ComputedStyle True if the browser supports document.defaultView.getComputedStyle()
         * @type {Boolean}
         */
        {
            // support Vector 5
            identity: 'ComputedStyle',
            fn: function(doc, div, view) {
                view = doc.defaultView;
                return view && view.getComputedStyle;
            }
        },

        /**
         * @property Svg True if the device supports SVG
         * @type {Boolean}
         */
        {
            // support Vector 6
            identity: 'Svg',
            fn: function(doc) {
                return !!doc.createElementNS && !!doc.createElementNS( "http:/" + "/www.w3.org/2000/svg", "svg").createSVGRect;
            }
        },

        /**
         * @property Canvas True if the device supports Canvas
         * @type {Boolean}
         */
        {
            // support Vector 7
            identity: 'Canvas',
            fn: function(doc) {
                return !!doc.createElement('canvas').getContext;
            },
            early: true
        },

        /**
         * @property Vml True if the device supports VML
         * @type {Boolean}
         */
        {
            // support Vector 8
            identity: 'Vml',
            fn: function(doc) {
                var d = doc.createElement("div");
                d.innerHTML = "<!--[if vml]><br/><br/><![endif]-->";
                return (d.childNodes.length == 2);
            },
            early: true
        },

        /**
         * @property Float True if the device supports CSS float
         * @type {Boolean}
         */
        {
            // support Vector 9
            identity: 'Float',
            fn: function(doc) {
                return 'cssFloat' in doc.documentElement.style;
            },
            early: true
        },

        /**
         * @property AudioTag True if the device supports the HTML5 audio tag
         * @type {Boolean}
         */
        {
            // support Vector 10
            identity: 'AudioTag',
            fn: function(doc) {
                return !!doc.createElement('audio').canPlayType;
            },
            early: true
        },

        /**
         * @property History True if the device supports HTML5 history
         * @type {Boolean}
         */
        {
            // support Vector 11
            identity: 'History',
            fn: function() {
                var history = window.history;
                return !!(history && history.pushState);
            },
            early: true
        },

        /**
         * @property Hashchange True if the user agent supports the hashchange event
         * @type {Boolean}
         */
        {
            // support Vector 12
            identity: 'Hashchange',
            fn: function() {
                // Note that IE8 in IE7 compatibility mode reports true for 'onhashchange' in window, so also test documentMode
                var docMode = document.documentMode;
                return 'onhashchange' in window && (docMode === undefined || docMode > 7);
            },
            early: true
        },

        /**
         * @property CSS3DTransform True if the device supports CSS3DTransform
         * @type {Boolean}
         */
        {
            // support Vector 13
            identity: 'CSS3DTransform',
            fn: function() {
                return (typeof WebKitCSSMatrix != 'undefined' && new WebKitCSSMatrix().hasOwnProperty('m41'));
            },
            early: true
        },

		/**
         * @property CSS3LinearGradient True if the device supports CSS3 linear gradients
         * @type {Boolean}
         */
        {
            // support Vector 14
            identity: 'CSS3LinearGradient',
            fn: function(doc, div) {
                var property = 'background-image:',
                    webkit   = '-webkit-gradient(linear, left top, right bottom, from(black), to(white))',
                    w3c      = 'linear-gradient(left top, black, white)',
                    moz      = '-moz-' + w3c,
                    ms       = '-ms-' + w3c,
                    opera    = '-o-' + w3c,
                    options  = [property + webkit, property + w3c, property + moz, property + ms, property + opera];

                div.style.cssText = options.join(';');

                return (("" + div.style.backgroundImage).indexOf('gradient') !== -1) && !Ext.isIE9;
            },
            early: true
        },

        /**
         * @property CSS3BorderRadius True if the device supports CSS3 border radius
         * @type {Boolean}
         */
        {
            // support Vector 15
            identity: 'CSS3BorderRadius',
            fn: function(doc) {
                var domPrefixes = ['borderRadius', 'BorderRadius', 'MozBorderRadius', 'WebkitBorderRadius', 'OBorderRadius', 'KhtmlBorderRadius'],
                    i;
                for (i = 0; i < domPrefixes.length; i++) {
                    if (domPrefixes[i] in doc.documentElement.style) {
                        return true;
                    }
                }
                return false;
            },
            early: true
        },

        /**
         * @property GeoLocation True if the device supports GeoLocation
         * @type {Boolean}
         */
        {
            // support Vector 16
            identity: 'GeoLocation',
            fn: function() {
                // Use the in check for geolocation, see https://github.com/Modernizr/Modernizr/issues/513
                return (typeof navigator != 'undefined' && 'geolocation' in navigator) || (typeof google != 'undefined' && typeof google.gears != 'undefined');
            },
            early: true
        },
        /**
         * @property MouseEnterLeave True if the browser supports mouseenter and mouseleave events
         * @type {Boolean}
         */
        {
            // support Vector 17
            identity: 'MouseEnterLeave',
            fn: function(doc){
                return ('onmouseenter' in doc.documentElement && 'onmouseleave' in doc.documentElement);
            },
            early: true
        },
        /**
         * @property MouseWheel True if the browser supports the mousewheel event
         * @type {Boolean}
         */
        {
            // support Vector 18
            identity: 'MouseWheel',
            fn: function(doc) {
                return ('onmousewheel' in doc.documentElement);
            },
            early: true
        },
        /**
         * @property Opacity True if the browser supports normal css opacity
         * @type {Boolean}
         */
        {
            // support Vector 19
            identity: 'Opacity',
            fn: function(doc, div){
                // Not a strict equal comparison in case opacity can be converted to a number.
                if (Ext.isIE6 || Ext.isIE7 || Ext.isIE8) {
                    return false;
                }
                div.style.cssText = 'opacity:0.73';
                return div.style.opacity == '0.73';
            },
            early: true
        },
        /**
         * @property Placeholder True if the browser supports the HTML5 placeholder attribute on inputs
         * @type {Boolean}
         */
        {
            // support Vector 20
            identity: 'Placeholder',
            fn: function(doc) {
                return 'placeholder' in doc.createElement('input');
            },
            early: true
        },

        /**
         * @property Direct2DBug True if when asking for an element's dimension via offsetWidth or offsetHeight,
         * getBoundingClientRect, etc. the browser returns the subpixel width rounded to the nearest pixel.
         * @type {Boolean}
         */
        {
            // support Vector 21
            identity: 'Direct2DBug',
            fn: function(doc) {
                return Ext.isString(doc.documentElement.style.msTransformOrigin) && Ext.isIE10m;
            },
            early: true
        },
        /**
         * @property BoundingClientRect True if the browser supports the getBoundingClientRect method on elements
         * @type {Boolean}
         */
        {
            // support Vector 22.
            // Note that though it is present, it does not work properly in IE quirks
            identity: 'BoundingClientRect',
            fn: function(doc) {
                return !Ext.isIEQuirks && 'getBoundingClientRect' in doc.documentElement;
            },
            early: true
        },
        /**
         * @property RotatedBoundingClientRect True if the BoundingClientRect is
         * rotated when the element is rotated using a CSS transform.
         * @type {Boolean}
         */
        {
            // support Vector 23
            identity: 'RotatedBoundingClientRect',
            fn: function(doc) {
                var body = doc.body,
                    supports = false,
                    el = doc.createElement('div'),
                    style = el.style;

                if (el.getBoundingClientRect) {
                    style.WebkitTransform = style.MozTransform =
                        style.OTransform = style.transform = 'rotate(90deg)';
                    style.width = '100px';
                    style.height = '30px';
                    body.appendChild(el);

                    supports = el.getBoundingClientRect().height !== 100;
                    body.removeChild(el);
                }
               
                return supports;
            }
        },
        {
            // support Vector 24
            identity: 'IncludePaddingInWidthCalculation',
            fn: function(doc, div){
                return div.childNodes[1].firstChild.offsetWidth == 210;
            }
        },
        {
            // support Vector 25
            identity: 'IncludePaddingInHeightCalculation',
            fn: function(doc, div){
                return div.childNodes[1].firstChild.offsetHeight == 210;
            }
        },

        /**
         * @property ArraySort True if the Array sort native method isn't bugged.
         * @type {Boolean}
         */
        {
            // support Vector 26
            identity: 'ArraySort',
            fn: function() {
                var a = [1,2,3,4,5].sort(function(){ return 0; });
                return a[0] === 1 && a[1] === 2 && a[2] === 3 && a[3] === 4 && a[4] === 5;
            },
            early: true
        },
        /**
         * @property Range True if browser support document.createRange native method.
         * @type {Boolean}
         */
        {
            // support Vector 27
            identity: 'Range',
            fn: function(doc) {
                return !!doc.createRange;
            },
            early: true
        },
        /**
         * @property CreateContextualFragment True if browser support CreateContextualFragment range native methods.
         * @type {Boolean}
         */
        {
            // support Vector 28
            identity: 'CreateContextualFragment',
            fn: function(doc) {
                var range = doc.createRange ? doc.createRange() : false;

                return range && !!range.createContextualFragment;
            },
            early: true
        },

        /**
         * @property WindowOnError True if browser supports window.onerror.
         * @type {Boolean}
         */
        {
            // support Vector 29
            identity: 'WindowOnError',
            fn: function () {
                // sadly, we cannot feature detect this...
                return Ext.isIE || Ext.isGecko || Ext.webKitVersion >= 534.16; // Chrome 10+
            },
            early: true
        },

        /**
         * @property TextAreaMaxLength True if the browser supports maxlength on textareas.
         * @type {Boolean}
         */
        {
            // support Vector 30
            identity: 'TextAreaMaxLength',
            fn: function(doc) {
                var el = doc.createElement('textarea');
                return ('maxlength' in el);
            },
            early: true
        },
        /**
         * @property GetPositionPercentage True if the browser will return the left/top/right/bottom
         * position as a percentage when explicitly set as a percentage value.
         * @type {Boolean}
         */
        // Related bug: https://bugzilla.mozilla.org/show_bug.cgi?id=707691#c7
        {
            // support Vector 31
            identity: 'GetPositionPercentage',
            fn: function(doc, div){
               return getStyle(div.childNodes[2], 'left') == '10%';
            }
        },
        /**
         * @property {Boolean} PercentageHeightOverflowBug
         * In some browsers (IE quirks, IE6, IE7, IE9, chrome, safari and opera at the time
         * of this writing) a percentage-height element ignores the horizontal scrollbar
         * of its parent element.  This method returns true if the browser is affected
         * by this bug.
         *
         * @private
         */
        {
            // support Vector 32
            identity: 'PercentageHeightOverflowBug',
            fn: function(doc) {
                var hasBug = false,
                    style, el;

                if (Ext.getScrollbarSize().height) {
                    // must have space-consuming scrollbars for bug to be possible
                    el = doc.createElement('div');
                    style = el.style;
                    style.height = '50px';
                    style.width = '50px';
                    style.overflow = 'auto';
                    style.position = 'absolute';
                    
                    el.innerHTML = [
                        '<div style="display:table;height:100%;">',
                            // The element that causes the horizontal overflow must be 
                            // a child of the element with the 100% height, otherwise
                            // horizontal overflow is not triggered in webkit quirks mode
                            '<div style="width:51px;"></div>',
                        '</div>'
                    ].join('');
                    doc.body.appendChild(el);
                    if (el.firstChild.offsetHeight === 50) {
                        hasBug = true;
                    }
                    doc.body.removeChild(el);
                }
                
                return hasBug;
            }
        },
        /**
         * @property {Boolean} xOriginBug
         * In Chrome 24.0, an RTL element which has vertical overflow positions its right X origin incorrectly.
         * It skips a non-existent scrollbar which has been moved to the left edge due to the RTL setting.
         *
         * http://code.google.com/p/chromium/issues/detail?id=174656
         *
         * This method returns true if the browser is affected by this bug.
         *
         * @private
         */
        {
            // support Vector 33
            identity: 'xOriginBug',
            fn: function(doc, div) {
               div.innerHTML = '<div id="b1" style="height:100px;width:100px;direction:rtl;position:relative;overflow:scroll">' +
                    '<div id="b2" style="position:relative;width:100%;height:20px;"></div>' +
                    '<div id="b3" style="position:absolute;width:20px;height:20px;top:0px;right:0px"></div>' +
                '</div>';

                var outerBox = document.getElementById('b1').getBoundingClientRect(),
                    b2 = document.getElementById('b2').getBoundingClientRect(),
                    b3 = document.getElementById('b3').getBoundingClientRect();

                return (b2.left !== outerBox.left && b3.right !== outerBox.right);
            }
        },

        /**
         * @property {Boolean} ScrollWidthInlinePaddingBug
         * In some browsers the right padding of an overflowing element is not accounted
         * for in its scrollWidth.  The result can vary depending on whether or not
         * The element contains block-level children.  This method tests the effect
         * of padding on scrollWidth when there are no block-level children inside the
         * overflowing element.
         * 
         * This method returns true if the browser is affected by this bug.
         */
        {
            // support Vector 34
            identity: 'ScrollWidthInlinePaddingBug',
            fn: function(doc) {
                var hasBug = false,
                    style, el;

                el = doc.createElement('div');
                style = el.style;
                style.height = '50px';
                style.width = '50px';
                style.padding = '10px';
                style.overflow = 'hidden';
                style.position = 'absolute';
                
                el.innerHTML =
                    '<span style="display:inline-block;zoom:1;height:60px;width:60px;"></span>';
                doc.body.appendChild(el);
                if (el.scrollWidth === 70) {
                    hasBug = true;
                }
                doc.body.removeChild(el);
                
                return hasBug;
            }
        },

        /**
         * @property {Boolean} rtlVertScrollbarOnRight
         * Safari, in RTL mode keeps the scrollbar at the right side.
         * This means that when two elements must keep their left/right positions synched, if one has no vert
         * scrollbar, it must have some extra padding.
         * See https://sencha.jira.com/browse/EXTJSIV-11245
         *
         * @private
         */
        {
            identity: 'rtlVertScrollbarOnRight',
            fn: function(doc, div) {
               div.innerHTML = '<div style="height:100px;width:100px;direction:rtl;overflow:scroll">' +
                    '<div style="width:20px;height:200px;"></div>' +
                '</div>';

                var outerBox = div.firstChild,
                    innerBox = outerBox.firstChild;

                return (innerBox.offsetLeft + innerBox.offsetWidth !== outerBox.offsetLeft + outerBox.offsetWidth);
            }
        },

        /**
         * @property {Boolean} rtlVertScrollbarOverflowBug
         * In Chrome, in RTL mode, horizontal overflow only into the vertical scrollbar does NOT trigger horizontal scrollability.
         * See https://code.google.com/p/chromium/issues/detail?id=179332
         * We need to detect this for when a grid header needs to have exactly the same horizontal scrolling range as its table view.
         * See {@link Ext.grid.ColumnLayout#beginLayout}
         * TODO: Remove this when all supported Chrome versions are fixed.
         *
         * @private
         */
        {
            identity: 'rtlVertScrollbarOverflowBug',
            fn: function(doc, div) {
               div.innerHTML = '<div style="height:100px;width:100px;direction:rtl;overflow:auto">' +
                    '<div style="width:95px;height:200px;"></div>' +
                '</div>';

                // If the bug is present, the 95 pixel wide inner div, encroaches into the
                // vertical scrollbar, but does NOT trigger horizontal overflow, so the clientHeight remains
                // equal to the offset height.
                var outerBox = div.firstChild;
                return outerBox.clientHeight === outerBox.offsetHeight;
            }
        }
    ]
};
}());

Ext.supports.init(); // run the "early" detections now
