!function (t, e) {
    "object" == typeof exports && "object" == typeof module ? module.exports = e() : "function" == typeof define && define.amd ? define([], e) : "object" == typeof exports ? exports.LarkSSOSDKWebQRCode = e() : t.LarkSSOSDKWebQRCode = e()
}(this, (function () {
    return function (t) {
        var e = {};

        function o(n) {
            if (e[n]) return e[n].exports;
            var r = e[n] = {i: n, l: !1, exports: {}};
            return t[n].call(r.exports, r, r.exports, o), r.l = !0, r.exports
        }

        return o.m = t, o.c = e, o.d = function (t, e, n) {
            o.o(t, e) || Object.defineProperty(t, e, {enumerable: !0, get: n})
        }, o.r = function (t) {
            "undefined" != typeof Symbol && Symbol.toStringTag && Object.defineProperty(t, Symbol.toStringTag, {value: "Module"}), Object.defineProperty(t, "__esModule", {value: !0})
        }, o.t = function (t, e) {
            if (1 & e && (t = o(t)), 8 & e) return t;
            if (4 & e && "object" == typeof t && t && t.__esModule) return t;
            var n = Object.create(null);
            if (o.r(n), Object.defineProperty(n, "default", {
                enumerable: !0,
                value: t
            }), 2 & e && "string" != typeof t) for (var r in t) o.d(n, r, function (e) {
                return t[e]
            }.bind(null, r));
            return n
        }, o.n = function (t) {
            var e = t
            return o.d(e, "a", e), e
        }, o.o = function (t, e) {
            return Object.prototype.hasOwnProperty.call(t, e)
        }, o.p = "", o(o.s = 0)
    }([function (t, e) {
        window.QRLogin = function (t) {
            var e = t.id, o = t.url, n = t.width, r = t.height, i = t.style, u = /^https:\/\/([\w\-]+\.)+\w+/,
                c = /^https:\/\/([\w\-]+\.)?(feishu(-boe|-pre)?\.cn|larksuite(-boe|-pre)?\.com)/, f = u.test(o),
                s = c.test(o);
            if (!f) throw new Error('The param "url" is not valid.');
            var a = o.match(u)[0];
            return function () {
                var t, u = "";
                u = /suite\/passport/.test(o) ? a + "/suite/passport/sso/qr?goto=" + encodeURIComponent(o) : a + "/accounts/auth_login/qr?goto=" + encodeURIComponent(o);
                var c = document.createElement("iframe");
                c.setAttribute("width", n), c.setAttribute("height", r), c.setAttribute("style", i), c.setAttribute("src", u), null === (t = document.getElementById(e)) || void 0 === t || t.appendChild(c)
            }(), {
                matchOrigin: function (t) {
                    return s ? c.test(t) : u.test(t)
                }
            }
        }
    }])
}));